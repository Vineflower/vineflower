package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.TypeFamily;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericType;

import java.util.ArrayList;
import java.util.List;

public class IntersectionCastProcessor {

  private static boolean result = false;

  public static boolean makeIntersectionCasts(RootStatement root) {
    return makeIntersectionCastsRec(root, root);
  }

  private static boolean makeIntersectionCastsRec(Statement stat, RootStatement root) {
    boolean result = false;
    if (stat.getExprents() != null) {
      for (Exprent e : stat.getExprents()) {
        result |= makeIntersectionCasts(e, root);
      }
    } else {
      for (Object o : stat.getSequentialObjects()) {
        if (o instanceof Statement s) {
          result |= makeIntersectionCastsRec(s, root);
        } else if (o instanceof Exprent e) {
          result |= makeIntersectionCasts(e, root);
        }
      }
    }
    return result;
  }

  private static boolean makeIntersectionCasts(Exprent exp, RootStatement root) {
    if (exp instanceof InvocationExprent inv) {
      if (root.mt.getClassQualifiedName().contains("CastIntersection") && root.mt.getName().contains("test2")) {
        System.out.println();
      }
      if (handleInvocation(inv, root)) {
        return true;
      }
    }
    return false;
  }

  private static boolean handleInvocation(InvocationExprent exp, RootStatement root) {
    List<Exprent> lstParameters = exp.getLstParameters();
    StructMethod method = exp.getDesc();
    GenericMethodDescriptor gmd = method != null ? method.getSignature() : null;
    int start = gmd != null && DecompilerContext.getStructContext().getClass(method.getClassQualifiedName()).hasModifier(CodeConstants.ACC_ENUM) && method.getName().equals(CodeConstants.INIT_NAME) ? 2 : 0;
    parameters:
    for (int i = 0; i < lstParameters.size(); i++) {
      Exprent parameter = lstParameters.get(i);
      if (parameter instanceof FunctionExprent cast && cast.getFuncType() == FunctionType.CAST && cast.getLstOperands().size() == 2) {
        List<Exprent> types = new ArrayList<>();
        Exprent inner = parameter;
        while (inner instanceof FunctionExprent testCast && testCast.getFuncType() == FunctionType.CAST && cast.getLstOperands().size() == 2 && cast.getLstOperands().get(1).getExprType().typeFamily == TypeFamily.OBJECT) {
          types.add(testCast.getLstOperands().get(1));
          inner = testCast.getLstOperands().get(0);
        }
        Exprent finalInner = inner;
        if (gmd != null) {
          int index = i - start;
          VarType type = gmd.parameterTypes.get(index);
          if (type.type == CodeType.GENVAR) {
            int typeParameterIndex = gmd.typeParameters.indexOf(type.value);
            List<VarType> bounds = gmd.typeParameterBounds.get(typeParameterIndex).stream()
                .filter(bound -> !types
                    .stream()
                    .anyMatch(constant -> DecompilerContext.getStructContext().instanceOf(constant.getExprType().value, bound.value)))
                .toList();

            if (!bounds.isEmpty() && bounds.stream().allMatch(bound -> DecompilerContext.getStructContext().instanceOf(finalInner.getExprType().value, bound.value))) {
              types.add(new ConstExprent(inner.getExprType(), null, null));
            }
            System.out.println();
          }
        }
//        exp.getDesc().getSignature().parameterTypes
        if (types.size() > 1) {
          Exprent nonInterface = null;
          for (Exprent type : types) {
            StructClass clazz = DecompilerContext.getStructContext().getClass(type.getExprType().value);
            if (clazz != null && !clazz.hasModifier(CodeConstants.ACC_INTERFACE)) {
              if (nonInterface == null) {
                nonInterface = type;
              } else {
                continue parameters;
              }
            }
          }
          if (nonInterface != null) {
            // The class is required to be first
            types.remove(types.indexOf(nonInterface));
            types.add(0, nonInterface);
          }
          cast.getLstOperands().clear();
          cast.getLstOperands().add(inner);
          cast.getLstOperands().addAll(types);
        }
      }
    }
    return false;
  }
}
