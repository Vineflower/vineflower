package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.TypeFamily;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMethodDescriptor;
import org.jetbrains.java.decompiler.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IntersectionCastProcessor {

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
    } else if (exp instanceof AssignmentExprent assignment) {
      if (handleAssignment(assignment, root)) {
        return true;
      }
    }
    boolean result = false;
    for (Exprent sub : exp.getAllExprents()) {
      result |= makeIntersectionCasts(sub, root);
    }
    return result;
  }

  private static boolean handleInvocation(InvocationExprent exp, RootStatement root) {
    List<Exprent> lstParameters = exp.getLstParameters();
    boolean result = false;
    for (int i = 0; i < lstParameters.size(); i++) {
      Exprent parameter = lstParameters.get(i);
      if (parameter instanceof FunctionExprent cast && isValidCast(cast)) {
        Pair<List<Exprent>, Exprent> casts = getCasts(cast);
        List<Exprent> types = casts.a;
        Exprent inner = casts.b;
        List<VarType> bounds = getBounds(exp, i).stream()
            .filter(bound -> !types
                .stream()
                .anyMatch(constant -> DecompilerContext.getStructContext().instanceOf(constant.getExprType().value, bound.value)))
            .toList();

        if (!bounds.isEmpty() && bounds.stream().allMatch(bound -> DecompilerContext.getStructContext().instanceOf(inner.getExprType().value, bound.value))) {
          types.add(new ConstExprent(inner.getExprType(), null, null));
        }
        result |= replaceCasts(cast, types, inner);
      }
    }
    return result;
  }

  private static boolean handleAssignment(AssignmentExprent exp, RootStatement root) {
    if (exp.getLeft() instanceof VarExprent varExp) {
      Exprent assigned = exp.getRight();
      if (assigned instanceof FunctionExprent cast && isValidCast(cast)) {
        Pair<List<Exprent>, Exprent> casts = getCasts(cast);
        List<Exprent> types = casts.a;
        Exprent inner = casts.b;
        List<VariablePosition> references = findReferences(varExp, root);
        Set<VarType> bounds = new HashSet<>();
        for (VariablePosition position : references) {
          bounds.addAll(switch (position.position) {
            case METHOD_PARAMETER -> getBounds((InvocationExprent) position.exp, position.index);
          });
        }
        bounds = bounds.stream()
            .filter(bound -> !types
                .stream()
                .anyMatch(constant -> DecompilerContext.getStructContext().instanceOf(constant.getExprType().value, bound.value)))
            .collect(Collectors.toSet());

        if (!bounds.isEmpty() && bounds.stream().allMatch(bound -> DecompilerContext.getStructContext().instanceOf(inner.getExprType().value, bound.value))) {
          types.add(new ConstExprent(inner.getExprType(), null, null));
        }
        if (replaceCasts(cast, types, inner)) {
          varExp.setIntersectionType(true);
          return true;
        }
      }
    }
    return false;
  }

  private static List<VarType> getBounds(InvocationExprent exp, int parameter) {
    StructMethod method = exp.getDesc();
    GenericMethodDescriptor gmd = method != null ? method.getSignature() : null;
    int start = gmd != null && DecompilerContext.getStructContext().getClass(method.getClassQualifiedName()).hasModifier(CodeConstants.ACC_ENUM) && method.getName().equals(CodeConstants.INIT_NAME) ? 2 : 0;
    if (gmd != null) {
      int index = parameter - start;
      VarType type = gmd.parameterTypes.get(index);
      if (type.type == CodeType.GENVAR) {
        int typeParameterIndex = gmd.typeParameters.indexOf(type.value);
        if (typeParameterIndex != -1) {
          return gmd.typeParameterBounds.get(typeParameterIndex);
        }
      }
    }
    return List.of();
  }

  private static List<VariablePosition> findReferences(VarExprent varExp, RootStatement root) {
    List<VariablePosition> list = new ArrayList<>();
    findReferencesRec(varExp, root, root, list);
    return list;
  }

  private static void findReferencesRec(VarExprent varExp, Statement stat, RootStatement root, List<VariablePosition> list) {
    if (stat.getExprents() != null) {
      for (Exprent e : stat.getExprents()) {
        findReferences(varExp, e, root, list);
      }
    } else {
      for (Object o : stat.getSequentialObjects()) {
        if (o instanceof Statement s) {
          findReferencesRec(varExp, s, root, list);
        } else if (o instanceof Exprent e) {
          findReferences(varExp, e, root, list);
        }
      }
    }
  }

  private static void findReferences(VarExprent varExp, Exprent exp, RootStatement root, List<VariablePosition> list) {
    // TODO: other places that variables can be referenced
    if (exp instanceof InvocationExprent inv) {
      findReferences(varExp, inv, list);
    }
    for (Exprent sub : exp.getAllExprents()) {
      findReferences(varExp, sub, root, list);
    }
  }

  private static void findReferences(VarExprent varExp, InvocationExprent inv, List<VariablePosition> list) {
    List<Exprent> lstParameters = inv.getLstParameters();
    for (int i = 0; i < lstParameters.size(); i++) {
      Exprent parameter = lstParameters.get(i);
      if (parameter instanceof VarExprent otherVar && varExp.getVarVersionPair().equals(otherVar.getVarVersionPair())) {
        list.add(new VariablePosition(VariablePositionEnum.METHOD_PARAMETER, inv, i));
      }
    }
  }

  private static Pair<List<Exprent>, Exprent> getCasts(Exprent exp) {
    List<Exprent> types = new ArrayList<>();
    Exprent inner = exp;
    while (inner instanceof FunctionExprent cast && isValidCast(cast)) {
      types.add(cast.getLstOperands().get(1));
      inner = cast.getLstOperands().get(0);
    }
    return Pair.of(types, inner);
  }

  private static boolean isValidCast(FunctionExprent cast) {
    if (cast.getFuncType() == FunctionType.CAST && cast.getLstOperands().size() == 2) {
      VarType type = cast.getLstOperands().get(1).getExprType();
      return type.typeFamily == TypeFamily.OBJECT && type.arrayDim == 0;
    }
    return false;
  }

  private static boolean replaceCasts(FunctionExprent cast, List<Exprent> types, Exprent inner) {
    if (types.size() > 1) {
      Exprent nonInterface = null;
      for (Exprent type : types) {
        StructClass clazz = DecompilerContext.getStructContext().getClass(type.getExprType().value);
        if (clazz != null && !clazz.hasModifier(CodeConstants.ACC_INTERFACE)) {
          if (nonInterface == null) {
            nonInterface = type;
          } else {
            return false;
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
      return true;
    }
    return false;
  }

  private static record VariablePosition(VariablePositionEnum position, Exprent exp, int index) {

  }

  private static enum VariablePositionEnum {
    METHOD_PARAMETER;
  }
}
