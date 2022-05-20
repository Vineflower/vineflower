package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.struct.consts.LinkConstant;
import org.jetbrains.java.decompiler.struct.consts.PooledConstant;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.List;

public class CondyHelper {

  // handles the java.lang.invoke.ConstantBootstraps bootstraps:
  // - nullConstant(_, type)
  // - primitiveClass(descriptor, Class.class)
  // - enumConstant(name, enumClass)
  // TODO: handle other bootstraps (invoke, fieldVarHandle, staticFieldVarHandle, arrayVarHandle, explicitCast)
  private static final String CONSTANT_BOOTSTRAPS_CLASS = "java/lang/invoke/ConstantBootstraps";

  public static Exprent simplifyCondy(InvocationExprent condyExpr) {
    if (condyExpr.getInvocationTyp() != InvocationExprent.CONSTANT_DYNAMIC) {
      return condyExpr;
    }

    LinkConstant method = condyExpr.getBootstrapMethod();
    if (!CONSTANT_BOOTSTRAPS_CLASS.equals(method.classname)) {
      return condyExpr;
    }

    switch (method.elementname) {
      case "nullConstant":
        // TODO: include target type?
        return new ConstExprent(VarType.VARTYPE_NULL, null, null).markAsCondy();
      case "primitiveClass":
        String desc = condyExpr.getName();
        if (desc.length() != 1 || !("ZCBSIJFDV".contains(desc))) {
          break;
        }
        VarType type = new VarType(desc, false);
        return new ConstExprent(VarType.VARTYPE_CLASS, ExprProcessor.getCastTypeName(type), null).markAsCondy();
      case "enumConstant":
        String typeName = condyExpr.getExprType().value;
        return new FieldExprent(condyExpr.getName(), typeName, true, null, FieldDescriptor.parseDescriptor("L" + typeName + ";"), null, false, true);
      case "getStaticFinal":
        List<PooledConstant> constArgs = condyExpr.getBootstrapArguments();
        String fieldType = condyExpr.getExprType().value;
        String ownerClass;
        if (constArgs.size() == 1) {
          PooledConstant ownerName = constArgs.get(0);
          if (ownerName instanceof PrimitiveConstant) {
            ownerClass = ((PrimitiveConstant) ownerName).value.toString();
          } else {
            return condyExpr;
          }
        } else {
          if(condyExpr.getExprType().type != VarType.VARTYPE_OBJECT.type) {
            return condyExpr;
          }
          ownerClass = fieldType;
        }
        return new FieldExprent(condyExpr.getName(), ownerClass, true, null, FieldDescriptor.parseDescriptor(fieldType), null, false, true);
    }
    return condyExpr;
  }
}
