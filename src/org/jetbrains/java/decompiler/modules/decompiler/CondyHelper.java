package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.attr.StructBootstrapMethodsAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.consts.LinkConstant;
import org.jetbrains.java.decompiler.struct.consts.PooledConstant;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Handles the java.lang.invoke.ConstantBootstraps bootstraps
public class CondyHelper {

  // TODO: handle other bootstraps (invoke, explicitCast)
  private static final String CONSTANT_BOOTSTRAPS_CLASS = "java/lang/invoke/ConstantBootstraps";
  private static final String CONSTANT_ENUMDESC_CLASS = "java/lang/Enum$EnumDesc";
  private static final String CONSTANT_CLASSDESC_CLASS = "java/lang/constant/ClassDesc";

  // converts a condy exprent into an equivalent "normal java" exprent
  public static Exprent simplifyCondy(InvocationExprent condyExpr) {
    if (condyExpr.getInvocationType() != InvocationExprent.InvocationType.CONSTANT_DYNAMIC) {
      return condyExpr;
    }

    LinkConstant method = condyExpr.getBootstrapMethod();
    Exprent result = simplifyCondy(method, condyExpr.getName(), condyExpr.getExprType(), condyExpr.getBootstrapArguments());
    return result != null ? result : condyExpr;
  }

  public static Exprent simplifyCondy(LinkConstant method, String name, VarType type, List<PooledConstant> constArgs) {
    return switch (method.classname) {
      case CONSTANT_BOOTSTRAPS_CLASS -> simplifyConstantBootstraps(method, name, type, constArgs);
      case CONSTANT_ENUMDESC_CLASS -> simplifyEnumDesc(method, name, type, constArgs);
      case CONSTANT_CLASSDESC_CLASS -> simplifyClassDesc(method, name, type, constArgs);
      default -> null;
    };
  }

  private static Exprent simplifyConstantBootstraps(LinkConstant method, String name, VarType type, List<PooledConstant> constArgs) {
    switch (method.elementname) {
      case "nullConstant": // -> null
        // TODO: include target type?
        return new ConstExprent(VarType.VARTYPE_NULL, null, null).setWasCondy(true);
      case "primitiveClass": // -> int.class
        String desc = name;
        // the name of the constant is the descriptor of the primitive type, check that its valid
        if (desc.length() != 1 || !("ZCBSIJFDV".contains(desc))) {
          break;
        }
        VarType primitiveType = new VarType(desc, false);
        return new ConstExprent(VarType.VARTYPE_CLASS, ExprProcessor.getCastTypeName(primitiveType), null).setWasCondy(true);
      case "enumConstant": // MyEnum.NAME
        String typeName = type.value;
        return new FieldExprent(name, typeName, true, null, FieldDescriptor.parseDescriptor("L" + typeName + ";"), null, false, true);
      case "getStaticFinal": { // MyClass.fieldName
        // name of the constant is the field name
        String fieldType = type.value;
        String ownerClass;
        // if a constant argument is present, that argument must be a class that contains the field
        if (constArgs.size() == 1) {
          PooledConstant ownerName = constArgs.get(0);
          if (ownerName instanceof PrimitiveConstant) {
            ownerClass = ((PrimitiveConstant) ownerName).value.toString();
          } else {
            return null;
          }
        // otherwise, the field is declared in the type of the field
        } else {
          if (type.type != VarType.VARTYPE_OBJECT.type) {
            return null;
          }
          ownerClass = fieldType;
        }
        return new FieldExprent(name, ownerClass, true, null, FieldDescriptor.parseDescriptor(fieldType), null, false, true);
      }
      case "fieldVarHandle":
      case "staticFieldVarHandle": { // --> MethodHandles.lookup().find[Static]VarHandle(...)
        if (!DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_COMPLEX_CONDYS)) {
          return null;
        }
        boolean isStatic = method.elementname.startsWith("static");
        String fieldName = name;
        // first argument is fieldname so should be primitive, second might be condy for primitive classes
        if (constArgs.size() != 2 || !(constArgs.get(0) instanceof PrimitiveConstant)) {
          return null;
        }
        String ownerClass = ((PrimitiveConstant) constArgs.get(0)).getString();
        return constructVarHandle(fieldName, ownerClass, constArgs.get(1), isStatic);
      }
      case "arrayVarHandle": { // --> MethodHandles.arrayElementVarHandle(...)
        if (!DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_COMPLEX_CONDYS)) {
          return null;
        }
        // argument is the array class
        if (constArgs.size() != 1) {
          return null;
        }
        return constructArrayVarHandleExprent(constArgs.get(0));
      }
      case "invoke": {
        // first argument is the method to invoke
        // remaining arguments are used as arguments for the method
        if (constArgs.size() < 1 || !(constArgs.get(0) instanceof LinkConstant other)) {
          return null;
        }
        return simplifyCondy(other, name, type, constArgs.subList(1, constArgs.size()));
      }
    }
    return null;
  }

  private static Exprent simplifyEnumDesc(LinkConstant method, String name, VarType type, List<PooledConstant> constArgs) {
    // First argument is a method call to get the class (which is wrapped with ConstantBootstraps.invoke)
    // Second argument is the name of the enum value
    if (constArgs.size() != 2
        || !(constArgs.get(0) instanceof LinkConstant getClass)
        || !(constArgs.get(1) instanceof PrimitiveConstant valueNameConstant)
        || valueNameConstant.type != CodeConstants.TYPE_OBJECT
        || !(valueNameConstant.value instanceof String valueName)) {
      return null;
    }
    StructBootstrapMethodsAttribute bootstrap = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS).getAttribute(StructGeneralAttribute.ATTRIBUTE_BOOTSTRAP_METHODS);
    LinkConstant getClassMethod = bootstrap.getMethodReference(getClass.index1);
    List<PooledConstant> getClassArgs = bootstrap.getMethodArguments(getClass.index1);
    Exprent enumType = simplifyCondy(getClassMethod, getClass.elementname, new VarType(getClass.descriptor), getClassArgs);
    if (!(enumType instanceof ConstExprent constExp)
        || !constExp.getExprType().equals(VarType.VARTYPE_CLASS)) {
      return null;
    }

    String typeName = constExp.getValue().toString();
    return new FieldExprent(valueName, typeName, true, null, FieldDescriptor.parseDescriptor("L" + typeName + ";"), null, false, true);
  }

  private static Exprent simplifyClassDesc(LinkConstant method, String name, VarType type, List<PooledConstant> constArgs) {
    // Argument is the class name
    if (constArgs.size() != 1
        || !(constArgs.get(0) instanceof PrimitiveConstant classNameConstant)
        || classNameConstant.type != CodeConstants.TYPE_OBJECT
        || !(classNameConstant.value instanceof String className)) {
      return null;
    }
    return new ConstExprent(VarType.VARTYPE_CLASS, className, null);
  }

  private static Exprent constructVarHandle(String fieldName, String fieldOwner, PooledConstant fieldType, boolean isStatic) {
    // makes an invocation exprent for MethodHandles.lookup().find[Static]VarHandle(fieldOwner.class, fieldName, fieldType.class)
    Exprent lookupExprent = constructLookupExprent();
    VarType ownerClassClass = new VarType(fieldOwner, false);
    Exprent ownerClassConst = new ConstExprent(VarType.VARTYPE_CLASS, ExprProcessor.getCastTypeName(ownerClassClass), null);
    Exprent fieldNameConst = new ConstExprent(VarType.VARTYPE_STRING, fieldName, null);
    Exprent fieldTypeConst = toClassExprent(fieldType);
    return constructFindVarHandleExprent(isStatic, lookupExprent, ownerClassConst, fieldNameConst, fieldTypeConst);
  }

  private static Exprent toClassExprent(PooledConstant constant) {
    // if the constant is a primitive constant (assumed a class), makes a class constant
    Exprent constExpr;
    if (constant instanceof PrimitiveConstant) {
      VarType fieldTypeClass = new VarType(((PrimitiveConstant) constant).getString(), false);
      constExpr = new ConstExprent(VarType.VARTYPE_CLASS, ExprProcessor.getCastTypeName(fieldTypeClass), null);
    } else {
      // otherwise we have a condy, expand and simplify it like normal
      // assume it has the correct type
      constExpr = toCondyExprent((LinkConstant) constant);
    }
    return constExpr;
  }

  private static Exprent toCondyExprent(LinkConstant fieldType) {
    Exprent fieldTypeConst;
    // TODO: is this correct in non-trivial cases?
    StructClass cl = (StructClass) DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS);
    // same as in ExprProcessor, use bootstrap attribute from current file to link the constant to bootstrap method
    StructBootstrapMethodsAttribute bootstrap = cl.getAttribute(StructGeneralAttribute.ATTRIBUTE_BOOTSTRAP_METHODS);
    LinkConstant bootstrapMethod = null;
    List<PooledConstant> constArgs = null;
    if (bootstrap != null) {
      bootstrapMethod = bootstrap.getMethodReference(fieldType.index1);
      constArgs = bootstrap.getMethodArguments(fieldType.index1);
    }
    InvocationExprent arg = new InvocationExprent(CodeConstants.opc_ldc, fieldType, bootstrapMethod, constArgs, null, null);
    // simplify nested condys, for e.g. fieldVarHandle(...int.class)
    fieldTypeConst = simplifyCondy(arg);
    if (fieldTypeConst instanceof ConstExprent) {
      ((ConstExprent) fieldTypeConst).setWasCondy(false); // comment is redundant for nested condys
    }
    return fieldTypeConst;
  }

  private static InvocationExprent constructLookupExprent() {
    // creates an exprent for MethodHandles.lookup()
    InvocationExprent exprent = new InvocationExprent();
    exprent.setName("lookup");
    exprent.setClassname("java/lang/invoke/MethodHandles");
    String desc = "()Ljava/lang/invoke/MethodHandles$Lookup;";
    exprent.setStringDescriptor(desc);
    exprent.setDescriptor(MethodDescriptor.parseDescriptor(desc));
    exprent.setFunctype(InvocationExprent.Type.GENERAL);
    exprent.setStatic(true);
    return exprent;
  }

  private static InvocationExprent constructFindVarHandleExprent(boolean isStatic, Exprent lookup, Exprent ownerClass, Exprent fieldName, Exprent fieldClass) {
    // creates an exprent for [lookup()].find[Static]VarHandle(Owner.class, "name", Type.class)
    // the receiver is passed in, should be made in `constructLookupExprent`
    InvocationExprent exprent = new InvocationExprent();
    exprent.setName(isStatic ? "findStaticVarHandle" : "findVarHandle");
    exprent.setClassname("java/lang/invoke/MethodHandles$Lookup");
    String desc = "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;";
    exprent.setStringDescriptor(desc);
    exprent.setDescriptor(MethodDescriptor.parseDescriptor(desc));
    exprent.setFunctype(InvocationExprent.Type.GENERAL);
    exprent.setStatic(false);
    exprent.setInstance(lookup);
    exprent.setLstParameters(Arrays.asList(ownerClass, fieldName, fieldClass));
    return exprent.markWasLazyCondy();
  }

  private static InvocationExprent constructArrayVarHandleExprent(PooledConstant classConst) {
    // creates an exprent for MethodHandles.arrayElementVarHandle(Array[].class)
    InvocationExprent exprent = new InvocationExprent();
    exprent.setName("arrayElementVarHandle");
    exprent.setClassname("java/lang/invoke/MethodHandles");
    String desc = "(Ljava/lang/Class;)Ljava/lang/invoke/VarHandle;";
    exprent.setStringDescriptor(desc);
    exprent.setDescriptor(MethodDescriptor.parseDescriptor(desc));
    exprent.setFunctype(InvocationExprent.Type.GENERAL);
    exprent.setStatic(true);
    Exprent classExpr = toClassExprent(classConst);
    exprent.setLstParameters(Collections.singletonList(classExpr));
    return exprent.markWasLazyCondy();
  }
}
