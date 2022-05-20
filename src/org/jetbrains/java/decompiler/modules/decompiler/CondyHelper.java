package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.struct.consts.LinkConstant;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;

public class CondyHelper {

  // handles the java.lang.invoke.ConstantBootstraps bootstraps:
  // - nullConstant(_, type)
  // - primitiveClass(descriptor, Class.class)
  // - enumConstant(name, enumClass)
  // TODO: handle other bootstraps (getStaticFinal, invoke, fieldVarHandle, staticFieldVarHandle, arrayVarHandle, explicitCast)
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
    }
    return condyExpr;
  }
}
