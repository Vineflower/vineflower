// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.BytecodeMappingTracer;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.ClasspathHelper;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.vars.CheckTypesResult;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.consts.LinkConstant;
import org.jetbrains.java.decompiler.struct.consts.PooledConstant;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericType;
import org.jetbrains.java.decompiler.struct.match.MatchEngine;
import org.jetbrains.java.decompiler.struct.match.MatchNode;
import org.jetbrains.java.decompiler.struct.match.MatchNode.RuleValue;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.ListStack;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.TextUtil;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

public class InvocationExprent extends Exprent {

  public static final int INVOKE_SPECIAL = 1;
  public static final int INVOKE_VIRTUAL = 2;
  public static final int INVOKE_STATIC = 3;
  public static final int INVOKE_INTERFACE = 4;
  public static final int INVOKE_DYNAMIC = 5;

  public static final int TYP_GENERAL = 1;
  public static final int TYP_INIT = 2;
  public static final int TYP_CLINIT = 3;

  private static final BitSet EMPTY_BIT_SET = new BitSet(0);

  private String name;
  private String classname;
  private boolean isStatic;
  private boolean canIgnoreBoxing = true;
  private int functype = TYP_GENERAL;
  private Exprent instance;
  private MethodDescriptor descriptor;
  private String stringDescriptor;
  private String invokeDynamicClassSuffix;
  private int invocationTyp = INVOKE_VIRTUAL;
  private List<Exprent> lstParameters = new ArrayList<>();
  private List<PooledConstant> bootstrapArguments;
  private List<VarType> genericArgs = new ArrayList<>();

  public InvocationExprent() {
    super(EXPRENT_INVOCATION);
  }

  public InvocationExprent(int opcode,
                           LinkConstant cn,
                           List<PooledConstant> bootstrapArguments,
                           ListStack<? extends Exprent> stack,
                           BitSet bytecodeOffsets) {
    this();

    name = cn.elementname;
    classname = cn.classname;
    this.bootstrapArguments = bootstrapArguments;
    switch (opcode) {
      case CodeConstants.opc_invokestatic:
        invocationTyp = INVOKE_STATIC;
        break;
      case CodeConstants.opc_invokespecial:
        invocationTyp = INVOKE_SPECIAL;
        break;
      case CodeConstants.opc_invokevirtual:
        invocationTyp = INVOKE_VIRTUAL;
        break;
      case CodeConstants.opc_invokeinterface:
        invocationTyp = INVOKE_INTERFACE;
        break;
      case CodeConstants.opc_invokedynamic:
        invocationTyp = INVOKE_DYNAMIC;

        classname = "java/lang/Class"; // dummy class name
        invokeDynamicClassSuffix = "##Lambda_" + cn.index1 + "_" + cn.index2;
    }

    if (CodeConstants.INIT_NAME.equals(name)) {
      functype = TYP_INIT;
    }
    else if (CodeConstants.CLINIT_NAME.equals(name)) {
      functype = TYP_CLINIT;
    }

    stringDescriptor = cn.descriptor;
    descriptor = MethodDescriptor.parseDescriptor(cn.descriptor);

    for (VarType ignored : descriptor.params) {
      lstParameters.add(0, stack.pop());
    }

    if (opcode == CodeConstants.opc_invokedynamic) {
      int dynamicInvocationType = -1;
      if (bootstrapArguments != null) {
        if (bootstrapArguments.size() > 1) { // INVOKEDYNAMIC is used not only for lambdas
          PooledConstant link = bootstrapArguments.get(1);
          if (link instanceof LinkConstant) {
            dynamicInvocationType = ((LinkConstant)link).index1;
          }
        }
      }
      if (dynamicInvocationType == CodeConstants.CONSTANT_MethodHandle_REF_invokeStatic) {
        isStatic = true;
      }
      else {
        // FIXME: remove the first parameter completely from the list. It's the object type for a virtual lambda method.
        if (!lstParameters.isEmpty()) {
          instance = lstParameters.get(0);
        }
      }
    }
    else if (opcode == CodeConstants.opc_invokestatic) {
      isStatic = true;
    }
    else {
      instance = stack.pop();
    }

    addBytecodeOffsets(bytecodeOffsets);
  }

  private InvocationExprent(InvocationExprent expr) {
    this();

    name = expr.getName();
    classname = expr.getClassname();
    isStatic = expr.isStatic();
    canIgnoreBoxing = expr.canIgnoreBoxing;
    functype = expr.getFunctype();
    instance = expr.getInstance();
    if (instance != null) {
      instance = instance.copy();
    }
    invocationTyp = expr.getInvocationTyp();
    invokeDynamicClassSuffix = expr.getInvokeDynamicClassSuffix();
    stringDescriptor = expr.getStringDescriptor();
    descriptor = expr.getDescriptor();
    lstParameters = new ArrayList<>(expr.getLstParameters());
    ExprProcessor.copyEntries(lstParameters);

    addBytecodeOffsets(expr.bytecode);
    bootstrapArguments = expr.getBootstrapArguments();
  }

  @Override
  public VarType getExprType() {
    return descriptor.ret;
  }


  @Override
  public VarType getInferredExprType(VarType upperBound) {
    List<StructMethod> matches = getMatchedDescriptors();
    StructMethod desc = null;
    if(matches.size() == 1) {
      desc = matches.get(0);
    }

    genericArgs.clear();

    if (desc != null && desc.getSignature() != null) {
      VarType ret = desc.getSignature().returnType;

      if (instance != null) {
        VarType instType = instance.getInferredExprType(upperBound);

        if (instType.isGeneric()) {
          StructClass cls = DecompilerContext.getStructContext().getClass(instType.value);

          if (cls != null && cls.getSignature() != null) {
            Map<VarType, VarType> map = new HashMap<>();
            GenericType ginstance = (GenericType)instType;

            if (cls.getSignature().fparameters.size() == ginstance.getArguments().size()) {
              for (int x = 0; x < ginstance.getArguments().size(); x++) {
                if (ginstance.getArguments().get(x) != null) { //TODO: Wildcards are null arguments.. look into fixing things?
                  map.put(GenericType.parse("T" + cls.getSignature().fparameters.get(x) + ";"), ginstance.getArguments().get(x));
                }
              }
            }

            if (!map.isEmpty()) {
              ret = ret.remap(map);
            }
          }
        }
      }

      VarType _new = this.gatherGenerics(upperBound, ret, desc.getSignature().typeParameters, genericArgs);
      if (desc.getSignature().returnType != _new) {
        return _new;
      }
    }

    return getExprType();
  }


  @Override
  public CheckTypesResult checkExprTypeBounds() {
    CheckTypesResult result = new CheckTypesResult();

    for (int i = 0; i < lstParameters.size(); i++) {
      Exprent parameter = lstParameters.get(i);

      VarType leftType = descriptor.params[i];

      result.addMinTypeExprent(parameter, VarType.getMinTypeInFamily(leftType.typeFamily));
      result.addMaxTypeExprent(parameter, leftType);
    }

    return result;
  }

  @Override
  public List<Exprent> getAllExprents() {
    List<Exprent> lst = new ArrayList<>();
    if (instance != null) {
      lst.add(instance);
    }
    lst.addAll(lstParameters);
    return lst;
  }


  @Override
  public Exprent copy() {
    return new InvocationExprent(this);
  }

  @Override
  public TextBuffer toJava(int indent, BytecodeMappingTracer tracer) {
    TextBuffer buf = new TextBuffer();

    String super_qualifier = null;
    boolean isInstanceThis = false;

    tracer.addMapping(bytecode);

    if (instance instanceof InvocationExprent) {
      ((InvocationExprent) instance).markUsingBoxingResult();
    }

    if (isStatic) {
      if (isBoxingCall() && canIgnoreBoxing) {
        // process general "boxing" calls, e.g. 'Object[] data = { true }' or 'Byte b = 123'
        // here 'byte' and 'short' values do not need an explicit narrowing type cast
        ExprProcessor.getCastedExprent(lstParameters.get(0), descriptor.params[0], buf, indent, false, false, false, false, tracer);
        return buf;
      }

      ClassNode node = (ClassNode)DecompilerContext.getProperty(DecompilerContext.CURRENT_CLASS_NODE);
      if (node == null || !classname.equals(node.classStruct.qualifiedName)) {
        buf.append(DecompilerContext.getImportCollector().getShortNameInClassContext(ExprProcessor.buildJavaClassName(classname)));
      }
    }
    else {

      if (instance != null && instance.type == Exprent.EXPRENT_VAR) {
        VarExprent instVar = (VarExprent)instance;
        VarVersionPair varPair = new VarVersionPair(instVar);

        VarProcessor varProc = instVar.getProcessor();
        if (varProc == null) {
          MethodWrapper currentMethod = (MethodWrapper)DecompilerContext.getProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
          if (currentMethod != null) {
            varProc = currentMethod.varproc;
          }
        }

        String this_classname = null;
        if (varProc != null) {
          this_classname = varProc.getThisVars().get(varPair);
        }

        if (this_classname != null) {
          isInstanceThis = true;

          if (invocationTyp == INVOKE_SPECIAL) {
            if (!classname.equals(this_classname)) { // TODO: direct comparison to the super class?
              StructClass cl = DecompilerContext.getStructContext().getClass(classname);
              boolean isInterface = cl != null && cl.hasModifier(CodeConstants.ACC_INTERFACE);
              super_qualifier = !isInterface ? this_classname : classname;
            }
          }
        }
      }

      if (functype == TYP_GENERAL) {
        if (super_qualifier != null) {
          TextUtil.writeQualifiedSuper(buf, super_qualifier);
        }
        else if (instance != null) {
          TextBuffer res = instance.toJava(indent, tracer);

          if (isUnboxingCall()) {
            // we don't print the unboxing call - no need to bother with the instance wrapping / casting
            buf.append(res);
            return buf;
          }

          VarType rightType = instance.getExprType();
          VarType leftType = new VarType(CodeConstants.TYPE_OBJECT, 0, classname);

          if (rightType.equals(VarType.VARTYPE_OBJECT) && !leftType.equals(rightType)) {
            buf.append("((").append(ExprProcessor.getCastTypeName(leftType)).append(")");

            if (instance.getPrecedence() >= FunctionExprent.getPrecedence(FunctionExprent.FUNCTION_CAST)) {
              res.enclose("(", ")");
            }
            buf.append(res).append(")");
          }
          else if (instance.getPrecedence() > getPrecedence()) {
            buf.append("(").append(res).append(")");
          }
          else {
            buf.append(res);
          }
        }
      }
    }

    switch (functype) {
      case TYP_GENERAL:
        if (VarExprent.VAR_NAMELESS_ENCLOSURE.equals(buf.toString())) {
          buf = new TextBuffer();
        }

        if (buf.length() > 0) {
          buf.append(".");
          this.appendParameters(buf, genericArgs);
        }

        buf.append(name);
        if (invocationTyp == INVOKE_DYNAMIC) {
          buf.append("<invokedynamic>");
        }
        buf.append("(");
        break;

      case TYP_CLINIT:
        throw new RuntimeException("Explicit invocation of " + CodeConstants.CLINIT_NAME);

      case TYP_INIT:
        if (super_qualifier != null) {
          buf.append("super(");
        }
        else if (isInstanceThis) {
          buf.append("this(");
        }
        else if (instance != null) {
          buf.append(instance.toJava(indent, tracer)).append(".<init>(");
        }
        else {
          throw new RuntimeException("Unrecognized invocation of " + CodeConstants.INIT_NAME);
        }
    }

    List<VarVersionPair> mask = null;
    boolean isEnum = false;
    if (functype == TYP_INIT) {
      ClassNode newNode = DecompilerContext.getClassProcessor().getMapRootClasses().get(classname);
      if (newNode != null) {
        mask = ExprUtil.getSyntheticParametersMask(newNode, stringDescriptor, lstParameters.size());
        isEnum = newNode.classStruct.hasModifier(CodeConstants.ACC_ENUM) && DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ENUM);
      }
    }
    List<StructMethod> matches = getMatchedDescriptors();
    BitSet setAmbiguousParameters = getAmbiguousParameters(matches);
    StructMethod desc = null;
    if(matches.size() == 1) {
      desc = matches.get(0);
    }

    StructClass cl = DecompilerContext.getStructContext().getClass(classname);
    Map<VarType, VarType> genArgs = new HashMap<VarType, VarType>();

    // building generic info from the instance
    VarType inferred = instance == null ? null : instance.getInferredExprType(null);
    if (cl != null && cl.getSignature() != null && instance != null && inferred.isGeneric()) {
      GenericType genType = (GenericType)inferred;
      if (genType.getArguments().size() == cl.getSignature().fparameters.size()) {
        for (int i = 0; i < cl.getSignature().fparameters.size(); i++) {
          VarType from = GenericType.parse("T" + cl.getSignature().fparameters.get(i) + ";");
          VarType to = genType.getArguments().get(i);
          if (from != null && to != null) {
            genArgs.put(from, to);
          }
        }
      }
    }

    // omit 'new Type[] {}' for the last parameter of a vararg method call
    if (lstParameters.size() == descriptor.params.length && isVarArgCall()) {
      Exprent lastParam = lstParameters.get(lstParameters.size() - 1);
      if (lastParam.type == EXPRENT_NEW && lastParam.getExprType().arrayDim >= 1) {
        ((NewExprent) lastParam).setVarArgParam(true);
      }
    }

    boolean firstParameter = true;
    int start = isEnum ? 2 : 0;
    for (int i = start; i < lstParameters.size(); i++) {
      if (mask == null || mask.get(i) == null) {
        TextBuffer buff = new TextBuffer();
        boolean ambiguous = setAmbiguousParameters.get(i);
        /*
        VarType type = descriptor.params[i];

        // using info from the generic signature
        if (desc != null && desc.getSignature() != null && desc.getSignature().params.size() == lstParameters.size()) {
          type = desc.getSignature().params.get(i);
        }

        // applying generic info from the signature
        VarType remappedType = type.remap(genArgs);
        if(type != remappedType) {
          type = remappedType;
        }
        else if (desc != null && desc.getSignature() != null && genericArgs.size() != 0) { // and from the inferred generic arguments
          Map<VarType, VarType> genMap = new HashMap<VarType, VarType>();
          for (int j = 0; j < genericArgs.size(); j++) {
            VarType from = GenericType.parse("T" + desc.getSignature().fparameters.get(j) + ";");
            VarType to = genericArgs.get(j);
            genMap.put(from, to);
          }
        }

        // not passing it along if what we get back is more specific
        VarType exprType = lstParameters.get(i).getInferredExprType(type);
        if (exprType != null && type != null && type.type == CodeConstants.TYPE_GENVAR) {
          //type = exprType;
        }
        */

        // 'byte' and 'short' literals need an explicit narrowing type cast when used as a parameter
        ExprProcessor.getCastedExprent(lstParameters.get(i), descriptor.params[i], buff, indent, true, ambiguous, true, true, tracer);

        // the last "new Object[0]" in the vararg call is not printed
        if (buff.length() > 0) {
          if (!firstParameter) {
            buf.append(", ");
          }
          buf.append(buff);
        }

        firstParameter = false;
      }
    }

    buf.append(')');

    return buf;
  }

  private boolean isVarArgCall() {
    StructClass cl = DecompilerContext.getStructContext().getClass(classname);
    if (cl != null) {
      StructMethod mt = cl.getMethod(InterpreterUtil.makeUniqueKey(name, stringDescriptor));
      if (mt != null) {
        return mt.hasModifier(CodeConstants.ACC_VARARGS);
      }
    }
    else {
      // TODO: tap into IDEA indices to access libraries methods details

      // try to check the class on the classpath
      Method mtd = ClasspathHelper.findMethod(classname, name, descriptor);
      return mtd != null && mtd.isVarArgs();
    }
    return false;
  }

  public boolean isBoxingCall() {
    if (isStatic && "valueOf".equals(name) && lstParameters.size() == 1) {
      int paramType = lstParameters.get(0).getExprType().type;

      // special handling for ambiguous types
      if (lstParameters.get(0).type == Exprent.EXPRENT_CONST) {
        // 'Integer.valueOf(1)' has '1' type detected as TYPE_BYTECHAR
        // 'Integer.valueOf(40_000)' has '40_000' type detected as TYPE_CHAR
        // so we check the type family instead
        if (lstParameters.get(0).getExprType().typeFamily == CodeConstants.TYPE_FAMILY_INTEGER) {
          if (classname.equals("java/lang/Integer")) {
            return true;
          }
        }

        if (paramType == CodeConstants.TYPE_BYTECHAR || paramType == CodeConstants.TYPE_SHORTCHAR) {
          if (classname.equals("java/lang/Character")) {
            return true;
          }
        }
      }

      return classname.equals(getClassNameForPrimitiveType(paramType));
    }

    return false;
  }

  public void markUsingBoxingResult() {
    canIgnoreBoxing = false;
  }

  // TODO: move to CodeConstants ???
  private static String getClassNameForPrimitiveType(int type) {
    switch (type) {
      case CodeConstants.TYPE_BOOLEAN:
        return "java/lang/Boolean";
      case CodeConstants.TYPE_BYTE:
      case CodeConstants.TYPE_BYTECHAR:
        return "java/lang/Byte";
      case CodeConstants.TYPE_CHAR:
        return "java/lang/Character";
      case CodeConstants.TYPE_SHORT:
      case CodeConstants.TYPE_SHORTCHAR:
        return "java/lang/Short";
      case CodeConstants.TYPE_INT:
        return "java/lang/Integer";
      case CodeConstants.TYPE_LONG:
        return "java/lang/Long";
      case CodeConstants.TYPE_FLOAT:
        return "java/lang/Float";
      case CodeConstants.TYPE_DOUBLE:
        return "java/lang/Double";
    }
    return null;
  }

  private static final Map<String, String> UNBOXING_METHODS;

  static {
    UNBOXING_METHODS = new HashMap<>();
    UNBOXING_METHODS.put("booleanValue", "java/lang/Boolean");
    UNBOXING_METHODS.put("byteValue", "java/lang/Byte");
    UNBOXING_METHODS.put("shortValue", "java/lang/Short");
    UNBOXING_METHODS.put("intValue", "java/lang/Integer");
    UNBOXING_METHODS.put("longValue", "java/lang/Long");
    UNBOXING_METHODS.put("floatValue", "java/lang/Float");
    UNBOXING_METHODS.put("doubleValue", "java/lang/Double");
    UNBOXING_METHODS.put("charValue", "java/lang/Character");
  }

  public boolean isUnboxingCall() {
    return !isStatic && lstParameters.size() == 0 && classname.equals(UNBOXING_METHODS.get(name));
  }

  private List<StructMethod> getMatchedDescriptors() {
    List<StructMethod> matches = new ArrayList<>();
    StructClass cl = DecompilerContext.getStructContext().getClass(classname);
    if (cl == null) return matches;

    nextMethod:
    for (StructMethod mt : cl.getMethods()) {
      if (name.equals(mt.getName())) {
        MethodDescriptor md = MethodDescriptor.parseDescriptor(mt.getDescriptor());
        if (md.params.length == descriptor.params.length) {
          for (int i = 0; i < md.params.length; i++) {
            if (md.params[i].typeFamily != descriptor.params[i].typeFamily) {
              continue nextMethod;
            }
          }
          matches.add(mt);
        }
      }
    }

    return matches;
  }

  private BitSet getAmbiguousParameters(List<StructMethod> matches) {
    StructClass cl = DecompilerContext.getStructContext().getClass(classname);
    if (cl == null || matches.size() == 1) {
      return EMPTY_BIT_SET;
    }

    // check if a call is unambiguous
    StructMethod mt = cl.getMethod(InterpreterUtil.makeUniqueKey(name, stringDescriptor));
    if (mt != null) {
      MethodDescriptor md = MethodDescriptor.parseDescriptor(mt.getDescriptor());
      if (md.params.length == lstParameters.size()) {
        boolean exact = true;
        for (int i = 0; i < md.params.length; i++) {
          if (!md.params[i].equals(lstParameters.get(i).getExprType())) {
            exact = false;
            break;
          }
        }
        if (exact) return EMPTY_BIT_SET;
      }
    }

    // mark parameters
    BitSet ambiguous = new BitSet(descriptor.params.length);
    for (int i = 0; i < descriptor.params.length; i++) {
      VarType paramType = descriptor.params[i];
      for (StructMethod mtt : matches) {

        GenericMethodDescriptor gen = mtt.getSignature(); //TODO: Find synthetic flags for params, as Enum generic signatures do no contain the String,int params
        if (gen != null && gen.parameterTypes.size() > i && gen.parameterTypes.get(i).isGeneric()) {
          break;
        }

        MethodDescriptor md = MethodDescriptor.parseDescriptor(mtt.getDescriptor());
        if (!paramType.equals(md.params[i])) {
          ambiguous.set(i);
          break;
        }
      }
    }
    return ambiguous;
  }

  @Override
  public void replaceExprent(Exprent oldExpr, Exprent newExpr) {
    if (oldExpr == instance) {
      instance = newExpr;
    }

    for (int i = 0; i < lstParameters.size(); i++) {
      if (oldExpr == lstParameters.get(i)) {
        lstParameters.set(i, newExpr);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof InvocationExprent)) return false;

    InvocationExprent it = (InvocationExprent)o;
    return InterpreterUtil.equalObjects(name, it.getName()) &&
           InterpreterUtil.equalObjects(classname, it.getClassname()) &&
           isStatic == it.isStatic() &&
           InterpreterUtil.equalObjects(instance, it.getInstance()) &&
           InterpreterUtil.equalObjects(descriptor, it.getDescriptor()) &&
           functype == it.getFunctype() &&
           InterpreterUtil.equalLists(lstParameters, it.getLstParameters());
  }

  public List<Exprent> getLstParameters() {
    return lstParameters;
  }

  public void setLstParameters(List<Exprent> lstParameters) {
    this.lstParameters = lstParameters;
  }

  public MethodDescriptor getDescriptor() {
    return descriptor;
  }

  public void setDescriptor(MethodDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  public String getClassname() {
    return classname;
  }

  public void setClassname(String classname) {
    this.classname = classname;
  }

  public int getFunctype() {
    return functype;
  }

  public void setFunctype(int functype) {
    this.functype = functype;
  }

  public Exprent getInstance() {
    return instance;
  }

  public void setInstance(Exprent instance) {
    this.instance = instance;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStringDescriptor() {
    return stringDescriptor;
  }

  public void setStringDescriptor(String stringDescriptor) {
    this.stringDescriptor = stringDescriptor;
  }

  public int getInvocationTyp() {
    return invocationTyp;
  }

  public String getInvokeDynamicClassSuffix() {
    return invokeDynamicClassSuffix;
  }

  public List<PooledConstant> getBootstrapArguments() {
    return bootstrapArguments;
  }

  @Override
  public void getBytecodeRange(BitSet values) {
    measureBytecode(values, lstParameters);
    measureBytecode(values, instance);
    measureBytecode(values);
  }

  // *****************************************************************************
  // IMatchable implementation
  // *****************************************************************************

  @Override
  public boolean match(MatchNode matchNode, MatchEngine engine) {
    if (!super.match(matchNode, engine)) {
      return false;
    }

    for (Entry<MatchProperties, RuleValue> rule : matchNode.getRules().entrySet()) {
      RuleValue value = rule.getValue();

      MatchProperties key = rule.getKey();
      if (key == MatchProperties.EXPRENT_INVOCATION_PARAMETER) {
        if (value.isVariable() && (value.parameter >= lstParameters.size() ||
                                   !engine.checkAndSetVariableValue(value.value.toString(), lstParameters.get(value.parameter)))) {
          return false;
        }
      }
      else if (key == MatchProperties.EXPRENT_INVOCATION_CLASS) {
        if (!value.value.equals(this.classname)) {
          return false;
        }
      }
      else if (key == MatchProperties.EXPRENT_INVOCATION_SIGNATURE) {
        if (!value.value.equals(this.name + this.stringDescriptor)) {
          return false;
        }
      }
    }

    return true;
  }
}
