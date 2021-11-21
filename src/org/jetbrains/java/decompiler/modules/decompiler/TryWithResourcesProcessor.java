package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    // Prevent processing if we find any weird edges, such as those in PackResourcesAdapterV4
    // TODO: find the root cause of the problem and fix it
    List<StatEdge> regedges = tryStatement.getSuccessorEdges(StatEdge.TYPE_REGULAR);
    if (!regedges.isEmpty()) {
      Statement destination = regedges.get(0).getDestination();
      if (destination.type == Statement.TYPE_IF) {
        List<StatEdge> breaks = destination.getSuccessorEdges(StatEdge.TYPE_BREAK);

        if (!breaks.isEmpty()) {

          if (!tryStatement.getParent().containsStatement(breaks.get(0).closure)) {
            return false;
          }
        }
      }
    }

    // Find basic block that contains the resource assignment
    for (StatEdge edge : tryStatement.getPredecessorEdges(StatEdge.TYPE_REGULAR)) {
      // Find predecessors that lead towards the target try statement
      if (edge.getDestination().equals(tryStatement) && edge.getSource().type == Statement.TYPE_BASICBLOCK) {
        AssignmentExprent assignment = findResourceDef(closeable, edge.getSource());

        // Remove the resource assignment from the basic block and further process
        if (assignment != null) {
          edge.getSource().getExprents().remove(assignment);

          // Set the try statement type
          tryStatement.setTryType(CatchStatement.RESOURCES);

          // Add resource assignment to try
          tryStatement.getResources().add(0, assignment);

          // Destroy catch block
          tryStatement.getStats().remove(1);

          // Remove outer close()
          Statement parent = tryStatement.getParent();

          boolean processedClose = false;
          for (int i = 0; i < parent.getStats().size(); i++) {
            Statement stat = parent.getStats().get(i);

            // Exclude our statement from processing
            if (stat == tryStatement) {
              continue;
            }

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
                    if (func.getLstOperands().get(0).type == Exprent.EXPRENT_VAR && ((VarExprent)func.getLstOperands().get(0)).getVarVersionPair().equals(closeable.getVarVersionPair())) {
                      // TODO: add APIs to do this automatically

                      // First start by removing the contents of the if statement.
                      // This block's connections need to be removed first before we can move onto the statement itself.

                      // Contents of the if statement
                      Statement ifBlock = ifStat.getIfstat();

                      // Disconnect edges to and from the inside of block's contents
                      for (StatEdge suc : ifBlock.getAllSuccessorEdges()) {
                        ifBlock.removeSuccessor(suc);
                      }

                      // Disconnect predecessors
                      for (StatEdge pred : ifBlock.getAllPredecessorEdges()) {
                        // Disconnect successors from pred to the block
                        pred.getSource().removeSuccessor(pred);
                        ifBlock.removePredecessor(pred);
                      }

                      // Remove inner block from the statement
                      ifStat.getStats().removeWithKey(ifBlock.id);

                      // Start processing the actual if statement

                      // Get successor, which will be connected to predecessors in place of the if statement
                      StatEdge successor = ifStat.getAllSuccessorEdges().get(0);

                      for (StatEdge pred : ifStat.getAllPredecessorEdges()) {
                        Statement predStat = pred.getSource();
                        // Disconnect if stat's predecessor from the stat
                        predStat.removeSuccessor(pred);

                        // Connect predecessor of if stat to it's successor, circumventing it

                        // When the predecessor is the try statement, we add normal control flow, as the successor is located next to the try statement. When it is not, it must be inside the try, so we break out of it.
                        // This prevents successor blocks from being inlined, as there are still multiple breaks to the successor and not a singular one that can be inlined [TestTryWithResourcesCatchJ16#test1]
                        StatEdge newEdge = new StatEdge(predStat == tryStatement ? StatEdge.TYPE_REGULAR : StatEdge.TYPE_BREAK, predStat, successor.getDestination());
                        predStat.addSuccessor(newEdge);
                      }

                      // Remove successor from if stat, as we've made the control go from it's predecessors to it's successor
                      ifStat.removeSuccessor(successor);
                      successor.getDestination().removePredecessor(successor); // TODO: is this needed?

                      // Remove if statement containing close() check- finally we're done!
                      parent.getStats().removeWithKey(ifStat.id);

                      processedClose = true;
                    }
                  }
                }
              }
            } else {
              if (stat.getExprents() != null) {
                Iterator<Exprent> itr = stat.getExprents().iterator();

                while (itr.hasNext()) {
                  Exprent exprent = itr.next();

                  // Check and remove the close exprent
                  if (exprent.type == Exprent.EXPRENT_INVOCATION) {
                    Exprent inst = ((InvocationExprent) exprent).getInstance();

                    // Ensure the var exprent we want to remove is the right one
                    if (inst.type == Exprent.EXPRENT_VAR && ((VarExprent)inst).getVarVersionPair().equals(closeable.getVarVersionPair()) && isCloseable(exprent)) {
                      itr.remove(); // Remove tested exprent

                      processedClose = true;
                    }
                  }
                }
              }
            }

            // Processed close, break out of loop to prevent multiple close() exprents from being removed
            if (processedClose) {
              break;
            }
          }

          if (processedClose) {
            return true;
          } else {
            // TODO: not processing the close() but also transforming the try block is invalid- leave a source level comment here
          }
        }
      }
    }

    return false;
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
