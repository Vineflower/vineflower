package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Processes try catch statements to turns them into try-with-resources statements wherever possible.
 * Including the entire classpath is generally needed for this to work as it needs to know which classes implement AutoCloseable.
 *
 * @author ForgeFlower devs, SuperCoder79
 */
public final class TryWithResourcesProcessor {
  // Make try with resources with the old style bytecode (J8)
  public static boolean makeTryWithResource(CatchAllStatement finallyStat) {
    Statement handler = finallyStat.getHandler();

    // The finally block has a specific statement structure we can check for
    if (handler.getStats().size() != 2) {
      return false;
    }

    Statement toCheck = finallyStat.getHandler().getFirst();
    if (toCheck.type != Statement.TYPE_IF || ((IfStatement)toCheck).getIfstat() == null || ((IfStatement)toCheck).getIfstat().type != Statement.TYPE_IF) {
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

          child.setTryType(CatchStatement.RESOURCES);
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

  // Make try with resources with the new style bytecode (J11+)
  // It doesn't use finally blocks, and is just a try catch
  public static boolean makeTryWithResourceJ11(CatchStatement tryStatement) {
    // Doesn't have a catch block, probably already processed
    if (tryStatement.getStats().size() < 2) {
      return false;
    }

    Statement inner = tryStatement.getStats().get(1); // Get catch block

    VarExprent closeable = null;

    boolean nullable = false;

    if (inner.type == Statement.TYPE_SEQUENCE) {
      // Replace dummy inner with real inner
      inner = inner.getStats().get(0);

      // If the catch statement contains a simple try catch, then it's a nonnull resource
      if (inner.type == Statement.TYPE_TRYCATCH) {
        Statement inTry = inner.getStats().get(0);

        // Catch block contains a basic block inside which has the closeable invocation
        if (inTry.type == Statement.TYPE_BASICBLOCK) {
          Exprent first = inTry.getExprents().get(0);

          if (isCloseable(first)) {
            closeable = (VarExprent) ((InvocationExprent)first).getInstance();
          }
        }
      }

      // Nullable resource, contains null checks
      if (inner.type == Statement.TYPE_IF) {
        Exprent ifCase = ((IfStatement)inner).getHeadexprent().getCondition();

        if (ifCase.type == Exprent.EXPRENT_FUNCTION) {
          // Will look like "if (!(!(var != null)))"
          FunctionExprent func = unwrapNegations((FunctionExprent) ifCase);

          Exprent check = func.getLstOperands().get(0);

          // If it's not a var, end processing early
          if (check.type != Exprent.EXPRENT_VAR) {
            return false;
          }

          // Make sure it's checking against null
          if (func.getLstOperands().get(1).getExprType().equals(VarType.VARTYPE_NULL)) {
            // Ensured that the if stat is a null check

            inner = ((IfStatement)inner).getIfstat();

            // Process try catch inside of if statement
            if (inner.type == Statement.TYPE_TRYCATCH) {
              Statement inTry = inner.getStats().get(0);

              if (inTry.type == Statement.TYPE_BASICBLOCK) {
                Exprent first = inTry.getExprents().get(0);

                // Check for closable invocation
                if (isCloseable(first)) {
                  closeable = (VarExprent) ((InvocationExprent)first).getInstance();
                  nullable = true;

                  // Double check that the variables in the null check and the closeable match
                  if (!closeable.getVarVersionPair().equals(((VarExprent)check).getVarVersionPair())) {
                    closeable = null;
                  }
                }
              }
            }
          }
        }
      }
    }

    // Didn't find an autocloseable, return early
    if (closeable == null) {
      return false;
    }

    Set<Statement> destinations = findExitpoints(tryStatement);

    Statement check = tryStatement;
    List<StatEdge> preds = new ArrayList<>();
    while (check != null && preds.isEmpty()) {
      preds = check.getPredecessorEdges(StatEdge.TYPE_REGULAR);
      check = check.getParent();
    }

    if (preds.isEmpty()) {
      return false;
    }

    StatEdge edge = preds.get(0);
    if (edge.getSource().type == Statement.TYPE_BASICBLOCK) {
      AssignmentExprent assignment = findResourceDef(closeable, edge.getSource());

      if (assignment == null) {
        return false;
      }

      for (Statement destination : destinations) {
        if (!isValid(destination, closeable, nullable)) {
          return false;
        }
      }

      for (Statement destination : destinations) {
        removeClose(destination, nullable);
      }

      edge.getSource().getExprents().remove(assignment);

      // Set the try statement type
      tryStatement.setTryType(CatchStatement.RESOURCES);

      // Add resource assignment to try
      tryStatement.getResources().add(0, assignment);

      // Destroy catch block
      tryStatement.getStats().remove(1);

      return true;
    }

    return false;
  }

  private static boolean isValid(Statement stat, VarExprent closeable, boolean nullable) {
    if (nullable) {
      // Check for if statement that contains a null check and a close()
      if (stat.type == Statement.TYPE_IF) {
        IfStatement ifStat = (IfStatement) stat;
        Exprent condition = ifStat.getHeadexprent().getCondition();

        if (condition.type == Exprent.EXPRENT_FUNCTION) {
          // This can sometimes be double inverted negative conditions too, handle that case
          FunctionExprent func = unwrapNegations((FunctionExprent) condition);

          // Ensure the exprent is the one we want to remove
          if (func.getFuncType() == FunctionExprent.FUNCTION_NE && func.getLstOperands().get(0).type == Exprent.EXPRENT_VAR && func.getLstOperands().get(1).getExprType().equals(VarType.VARTYPE_NULL)) {
            if (func.getLstOperands().get(0).type == Exprent.EXPRENT_VAR && ((VarExprent) func.getLstOperands().get(0)).getVarVersionPair().equals(closeable.getVarVersionPair())) {
              return true;
            }
          }
        }
      }
    } else {
      if (stat.type == Statement.TYPE_BASICBLOCK) {
        if (stat.getExprents() != null && !stat.getExprents().isEmpty()) {
          Exprent exprent = stat.getExprents().get(0);

          if (exprent.type == Exprent.EXPRENT_INVOCATION) {
            Exprent inst = ((InvocationExprent) exprent).getInstance();

            // Ensure the var exprent we want to remove is the right one
            if (inst.type == Exprent.EXPRENT_VAR && inst.equals(closeable) && isCloseable(exprent)) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  private static void removeClose(Statement statement, boolean nullable) {
    if (nullable) {
      // Breaking out of parent, remove label
      // TODO: The underlying problem is that empty labeled basic blocks remove their label but the edge is marked as labeled and explicit.
      // label1: {
      //   ...
      //   break label1; // identifyLabels() removes this entirely but keeps the edge labeled
      // }
      //
      List<StatEdge> edges = statement.getAllSuccessorEdges();
      if (!edges.isEmpty() && edges.get(0).closure == statement.getParent()) {
        SequenceHelper.destroyAndFlattenStatement(statement);
      } else {
        for (StatEdge edge : statement.getFirst().getAllSuccessorEdges()) {
          edge.getDestination().removePredecessor(edge);
        }

        for (StatEdge edge : ((IfStatement)statement).getIfstat().getAllSuccessorEdges()) {
          edge.getDestination().removePredecessor(edge);

          if (edge.closure != null) {
            edge.closure.getLabelEdges().remove(edge);
          }
        }

        // Keep the label as it's not the parent
        statement.destroy();
      }
    } else {
      statement.getExprents().remove(0);
    }
  }

  private static Set<Statement> findExitpoints(Statement stat) {
    Set<StatEdge> edges = new LinkedHashSet<>();
    findEdgesLeaving(stat.getFirst(), stat, edges);

    return edges.stream().map(StatEdge::getDestination).collect(Collectors.toSet());
  }

  // TODO: move to better place
  public static void findEdgesLeaving(Statement curr, Statement check, Set<StatEdge> edges) {
    for (StatEdge edge : curr.getAllSuccessorEdges()) {
      if (!check.containsStatement(edge.getDestination()) && edge.getDestination().type != Statement.TYPE_DUMMYEXIT) {
        edges.add(edge);
      }
    }

    for (Statement stat : curr.getStats()) {
      findEdgesLeaving(stat, check, edges);
    }
  }

  private static FunctionExprent unwrapNegations(FunctionExprent func) {
    while (func.getFuncType() == FunctionExprent.FUNCTION_BOOL_NOT) {
      Exprent expr = func.getLstOperands().get(0);

      if (expr.type == Exprent.EXPRENT_FUNCTION) {
        func = (FunctionExprent) expr;
      } else {
        break;
      }
    }

    return func;
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

                for (StatEdge edge : temp.getAllSuccessorEdges()) {
                  edge.getSource().removeSuccessor(edge);
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
