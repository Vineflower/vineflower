package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.InvocationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchAllStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.gen.VarType;

public class TryHelper
{
  public static boolean enhanceTryStats(RootStatement root) {
    boolean ret = makeTryWithResourceRec(root);
    if (ret) {
      SequenceHelper.condenseSequences(root);
      if (collapseTryRec(root)) {
        SequenceHelper.condenseSequences(root);
      }
    }
    return ret;
  }

  private static boolean makeTryWithResourceRec(Statement stat) {
    if (stat.type == Statement.TYPE_CATCHALL && ((CatchAllStatement)stat).isFinally()) {
      if (makeTryWithResource((CatchAllStatement)stat)) {
        return true;
      }
    }

    for (Statement st : stat.getStats()) {
      if (makeTryWithResourceRec(st)) {
        return true;
      }
    }

    return false;
  }

  private static boolean collapseTryRec(Statement stat) {
    if (stat.type == Statement.TYPE_TRYCATCH && collapseTry((CatchStatement)stat)) {
      return true;
    }

    for (Statement st : stat.getStats()) {
      if (collapseTryRec(st)) {
        return true;
      }
    }

    return false;
  }

  private static boolean makeTryWithResource(CatchAllStatement finallyStat) {
    Statement handler = finallyStat.getHandler();

    // The finally block has a specific statement structure we can check for
    if (handler.getStats().size() != 2) {
      return false;
    }

    Statement toCheck = finallyStat.getHandler().getFirst();
    if (toCheck.type != Statement.TYPE_IF || ((IfStatement)toCheck).getIfstat().type != Statement.TYPE_IF) {
      return false;
    }

    toCheck = ((IfStatement)toCheck).getIfstat();

    if (((IfStatement)toCheck).getElsestat() == null) {
      return false;
    }

    Statement elseBlock = ((IfStatement)toCheck).getElsestat();
    VarExprent var = null;

    if (elseBlock.getExprents() != null && elseBlock.getExprents().size() == 1) {
      Exprent exp = elseBlock.getExprents().get(0);

      if (isCloseable(exp)) {
        var = (VarExprent)((InvocationExprent)exp).getInstance();
      }
    }

    if (var != null) {
      AssignmentExprent ass = null;
      BasicBlockStatement initBlock = null;
      for (StatEdge edge : finallyStat.getAllPredecessorEdges()) {
        if (edge.getDestination().equals(finallyStat) && edge.getSource().type == Statement.TYPE_BASICBLOCK) {
          ass = findResourceDef(var, edge.getSource());
          if (ass != null) {
            initBlock = (BasicBlockStatement)edge.getSource();
            break;
          }
        }
      }

      if (ass != null) {
        Statement stat = finallyStat.getParent();
        Statement stat2 = finallyStat.getFirst();

        if (stat2.type == Statement.TYPE_TRYCATCH) {
          CatchStatement child = (CatchStatement)stat2;

          AssignmentExprent resourceDef = (AssignmentExprent)ass.copy();
          if (ass.getRight().getExprType().equals(VarType.VARTYPE_NULL)) {
            if (child.getFirst() != null) {
              fixResourceAssignment(resourceDef, child.getFirst());
            }
          }

          if (resourceDef.getRight().getExprType().equals(VarType.VARTYPE_NULL)) {
            return false;
          }

          child.setTryType(CatchStatement.RESORCES);
          initBlock.getExprents().remove(ass);
          child.getResources().add(0, resourceDef);

          if (!finallyStat.getVarDefinitions().isEmpty()) {
            child.getVarDefinitions().addAll(0, finallyStat.getVarDefinitions());
          }

          stat.replaceStatement(finallyStat, child);
          removeRedundantThrow(initBlock, child);
          return true;
        }
      }
    }

    return false;
  }

  private static boolean collapseTry(CatchStatement catchStat) {
    Statement parent = catchStat;
    if (parent.getFirst() != null && parent.getFirst().type == Statement.TYPE_SEQUENCE) {
      parent = parent.getFirst();
    }
    if (parent != null && parent.getFirst() != null && parent.getFirst().type == Statement.TYPE_TRYCATCH) {
      CatchStatement toRemove = (CatchStatement)parent.getFirst();

      if (toRemove.getTryType() == CatchStatement.RESORCES) {
        catchStat.setTryType(CatchStatement.RESORCES);
        catchStat.getResources().addAll(toRemove.getResources());

        catchStat.getVarDefinitions().addAll(toRemove.getVarDefinitions());
        parent.replaceStatement(toRemove, toRemove.getFirst());

        if (!toRemove.getVars().isEmpty()) {
          for (int i = 0; i < toRemove.getVars().size(); ++i) {
            catchStat.getVars().add(i, toRemove.getVars().get(i));
            catchStat.getExctStrings().add(i, toRemove.getExctStrings().get(i));

            catchStat.getStats().add(i + 1, catchStat.getStats().get(i + 1));
          }
        }
        return true;
      }
    }
    return false;
  }

  private static AssignmentExprent findResourceDef(VarExprent var, Statement prevStatement) {
    for (Exprent exp : prevStatement.getExprents()) {
      if (exp.type == Exprent.EXPRENT_ASSIGNMENT) {
        AssignmentExprent ass = (AssignmentExprent)exp;
        if (ass.getLeft().type == Exprent.EXPRENT_VAR) { // cannot use equals as var's varType may be unknown and not match
          VarExprent left = (VarExprent)ass.getLeft();
          if (left.getVarVersionPair().equals(var.getVarVersionPair())) {
            return ass;
          }
        }
      }
    }

    return null;
  }

  private static boolean isCloseable(Exprent exp) {
    if (exp.type == Exprent.EXPRENT_INVOCATION) {
      InvocationExprent invocExp = (InvocationExprent)exp;
      if (invocExp.getName().equals("close") && invocExp.getStringDescriptor().equals("()V")) {
        if (invocExp.getInstance() != null && invocExp.getInstance().type == Exprent.EXPRENT_VAR) {
          return DecompilerContext.getStructContext().instanceOf(invocExp.getClassname(), "java/lang/AutoCloseable");
        }
      }
    }

    return false;
  }

  private static void fixResourceAssignment(AssignmentExprent ass, Statement statement) {
    if (statement.getExprents() != null) {
      for (Exprent exp : statement.getExprents()) {
        if (exp.type == Exprent.EXPRENT_ASSIGNMENT) {
          AssignmentExprent toRemove = (AssignmentExprent)exp;
          if (ass.getLeft().equals(toRemove.getLeft()) && !toRemove.getRight().getExprType().equals(VarType.VARTYPE_NULL)) {
            ass.setRight(toRemove.getRight());
            statement.getExprents().remove(toRemove);
            break;
          }
        }
      }
    }
  }

  private static boolean removeRedundantThrow(BasicBlockStatement initBlock, CatchStatement catchStat) {
    if (catchStat.getStats().size() > 1) {
      boolean removed = false;
      Statement temp = null;
      int i = 1;
      for (; i < catchStat.getStats().size(); ++i) {
        temp = catchStat.getStats().get(i);

        if (temp.type == Statement.TYPE_BASICBLOCK && temp.getExprents() != null) {
          if (temp.getExprents().size() >= 2 && catchStat.getVars().get(i - 1).getVarType().value.equals("java/lang/Throwable")) {
            if (temp.getExprents().get(temp.getExprents().size() - 1).type == Exprent.EXPRENT_EXIT) {
              ExitExprent exitExprent = (ExitExprent)temp.getExprents().get(temp.getExprents().size() - 1);
              if (exitExprent.getExitType() == ExitExprent.EXIT_THROW && exitExprent.getValue().equals(catchStat.getVars().get(i - 1))) {

                catchStat.getExctStrings().remove(i - 1);
                catchStat.getVars().remove(i - 1);
                catchStat.getStats().remove(i);

                for (StatEdge edge : temp.getAllPredecessorEdges()) {
                  edge.getSource().removeSuccessor(edge);
                }

                for (StatEdge edge : temp.getAllSuccessorEdges()) {
                  edge.getDestination().removePredecessor(edge);
                }

                removed = true;
                break;
              }
            }
          }
        }
      }

      if (removed && temp.getExprents().get(temp.getExprents().size() - 2).type == Exprent.EXPRENT_ASSIGNMENT) {
        AssignmentExprent assignmentExp = (AssignmentExprent)temp.getExprents().get(temp.getExprents().size() - 2);
        if (assignmentExp.getLeft().getExprType().value.equals("java/lang/Throwable")) {
          for (Exprent exprent : initBlock.getExprents()) {
            if (exprent.type == Exprent.EXPRENT_ASSIGNMENT) {
              AssignmentExprent toRemove = (AssignmentExprent)exprent;
              if (toRemove.getLeft().equals(assignmentExp.getLeft())) {
                initBlock.getExprents().remove(toRemove);
                return true;
              }
            }
          }
        }
      }
    }
    return false;
  }
}
