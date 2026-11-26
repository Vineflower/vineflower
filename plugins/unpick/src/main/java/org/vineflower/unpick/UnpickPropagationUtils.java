package org.vineflower.unpick;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchHeadExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.YieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.MutableObject;
import org.jetbrains.java.decompiler.util.StatementIterator;

import java.util.ArrayList;
import java.util.List;

public final class UnpickPropagationUtils {
  private UnpickPropagationUtils() {
  }

  static boolean propagateGroups(ClassesProcessor.ClassNode classNode, GroupTracker groupTracker) {
    MutableObject<Boolean> res = MutableObject.of(false);
    UnpickUtils.iterateAllMethods(classNode, (node, method, classStack) -> {
      if (propagateGroups(method, classStack, groupTracker)) {
        res.set(true);
      }
    });
    return res.get();
  }

  private static boolean propagateGroups(MethodWrapper method, List<ClassesProcessor.ClassNode> classStack, GroupTracker groupTracker) {
    MutableObject<Boolean> changed = MutableObject.of(false);
    String methodKey = InterpreterUtil.makeUniqueKey(method.classStruct.qualifiedName, method.methodStruct.getName(), method.methodStruct.getDescriptor());

    StatementIterator.iterate(method.root, exprent -> {
      if (exprent instanceof VarExprent varExprent) {
        String varContainingMethod = null;
        VarVersionPair var = varExprent.getVarVersionPair();
        MethodWrapper md = method;
        int classStackIndex = classStack.size() - 1;
        while (true) {
          var varSource = md.varproc.getVarSource(var);
          if (varSource == null) {
            varContainingMethod = InterpreterUtil.makeUniqueKey(md.classStruct.qualifiedName, md.methodStruct.getName(), md.methodStruct.getDescriptor());
            break;
          }

          classStackIndex--;
          if (classStackIndex < 0) {
            break;
          }

          var = varSource.b;
          String[] nameAndDesc = varSource.a.split(" ", 2);
          md = classStack.get(classStackIndex).getWrapper().getMethodWrapper(nameAndDesc[0], nameAndDesc[1]);
        }

        if (varContainingMethod != null) {
          String groupName = groupTracker.getVarGroup(varContainingMethod, var);
          if (groupName == null) {
            groupName = groupTracker.getExprentGroup(methodKey, varExprent);
          }
          if (groupName != null) {
            tryAssignGroup(changed, groupTracker, methodKey, varExprent, groupName);
            if (groupTracker.trySetVarGroup(varContainingMethod, var, groupName)) {
              changed.set(true);
            }
          }
        }
      } else if (exprent instanceof FunctionExprent functionExprent) {
        switch (functionExprent.getFuncType()) {
          case ADD, SUB, MUL, AND, OR, XOR, STR_CONCAT -> {
            String groupName = firstGroupOf(groupTracker, methodKey, functionExprent, functionExprent.getLstOperands().get(0), functionExprent.getLstOperands().get(1));
            if (groupName != null) {
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent, groupName);
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent.getLstOperands().get(0), groupName);
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent.getLstOperands().get(1), groupName);
            }
          }
          case DIV, REM, NEG, BIT_NOT, PPI, IPP, MMI, IMM -> {
            String groupName = firstGroupOf(groupTracker, methodKey, functionExprent, functionExprent.getLstOperands().get(0));
            if (groupName != null) {
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent, groupName);
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent.getLstOperands().get(0), groupName);
            }
          }
          case EQ, NE, GT, LT, GE, LE -> {
            String groupName = firstGroupOf(groupTracker, methodKey, functionExprent.getLstOperands().get(0), functionExprent.getLstOperands().get(1));
            if (groupName != null) {
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent.getLstOperands().get(0), groupName);
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent.getLstOperands().get(1), groupName);
            }
          }
          case TERNARY -> {
            String groupName = firstGroupOf(groupTracker, methodKey, functionExprent, functionExprent.getLstOperands().get(1), functionExprent.getLstOperands().get(2));
            if (groupName != null) {
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent, groupName);
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent.getLstOperands().get(1), groupName);
              tryAssignGroup(changed, groupTracker, methodKey, functionExprent.getLstOperands().get(2), groupName);
            }
          }
        }
      } else if (exprent instanceof AssignmentExprent assignmentExprent) {
        boolean propagate;
        boolean propagateToRight;
        if (assignmentExprent.getCondType() == null) {
          propagate = true;
          propagateToRight = true;
        } else {
          switch (assignmentExprent.getCondType()) {
            case ADD, SUB, MUL, AND, OR, XOR, STR_CONCAT -> {
              propagate = true;
              propagateToRight = true;
            }
            case DIV, REM, SHR, USHR, SHL -> {
              propagate = true;
              propagateToRight = false;
            }
            default -> {
              propagate = false;
              propagateToRight = false;
            }
          }
        }

        if (propagate) {
          String groupName;
          if (propagateToRight) {
            groupName = firstGroupOf(groupTracker, methodKey, assignmentExprent, assignmentExprent.getLeft(), assignmentExprent.getRight());
          } else {
            groupName = firstGroupOf(groupTracker, methodKey, assignmentExprent, assignmentExprent.getLeft());
          }
          if (groupName != null) {
            tryAssignGroup(changed, groupTracker, methodKey, assignmentExprent, groupName);
            tryAssignGroup(changed, groupTracker, methodKey, assignmentExprent.getLeft(), groupName);
            if (propagateToRight) {
              tryAssignGroup(changed, groupTracker, methodKey, assignmentExprent.getRight(), groupName);
            }
          }
        }
      } else if (exprent instanceof SwitchHeadExprent switchHeadExprent) {
        List<Exprent> subjectAndCases = new ArrayList<>();
        subjectAndCases.add(switchHeadExprent.getValue());
        for (List<Exprent> caze : switchHeadExprent.getCaseValues()) {
          subjectAndCases.addAll(caze);
        }
        String groupName = firstGroupOf(groupTracker, methodKey, subjectAndCases.toArray(Exprent[]::new));
        if (groupName != null) {
          for (Exprent expr : subjectAndCases) {
            tryAssignGroup(changed, groupTracker, methodKey, expr, groupName);
          }
        }
      } else if (exprent instanceof SwitchExprent switchExprent) {
        List<Exprent> switchAndYields = new ArrayList<>();
        collectYieldExprents(switchExprent.getBacking(), switchAndYields);
        String groupName = firstGroupOf(groupTracker, methodKey, switchAndYields.toArray(Exprent[]::new));
        if (groupName != null) {
          for (Exprent expr : switchAndYields) {
            tryAssignGroup(changed, groupTracker, methodKey, expr, groupName);
          }
        }
      }
      return 0;
    });

    return changed.get();
  }

  private static void collectYieldExprents(Statement stat, List<? super YieldExprent> yields) {
    if (stat.getExprents() == null) {
      for (Statement childStat : stat.getStats()) {
        if (!(childStat instanceof SwitchStatement)) {
          collectYieldExprents(childStat, yields);
        }
      }

      for (Exprent exprent : stat.getStatExprents()) {
        for (Exprent expr : exprent.getAllExprents(true, true)) {
          if (expr instanceof YieldExprent yield) {
            yields.add(yield);
          }
        }
      }
    } else {
      for (Exprent exprent : stat.getExprents()) {
        for (Exprent expr : exprent.getAllExprents(true, true)) {
          if (expr instanceof YieldExprent yield) {
            yields.add(yield);
          }
        }
      }
    }
  }

  @Nullable
  private static String firstGroupOf(GroupTracker groupTracker, String methodKey, Exprent... exprents) {
    for (Exprent exprent : exprents) {
      String groupName = groupTracker.getExprentGroup(methodKey, exprent);
      if (groupName != null) {
        return groupName;
      }
    }

    return null;
  }

  private static void tryAssignGroup(MutableObject<Boolean> changed, GroupTracker groupTracker, String methodKey, Exprent exprent, String groupName) {
    if (groupTracker.trySetExprentGroup(methodKey, exprent, groupName)) {
      changed.set(true);
    }
  }
}
