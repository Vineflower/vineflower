// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.DirectNode;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.FlattenStatementsHelper;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PPandMMHelper {

  private boolean exprentReplaced;
  private VarProcessor varProc;
  private DirectGraph dgraph;

  public PPandMMHelper(VarProcessor varProc) {
    this.varProc = varProc;
  }

  public boolean findPPandMM(RootStatement root) {

    FlattenStatementsHelper flatthelper = new FlattenStatementsHelper();
    this.dgraph = flatthelper.buildDirectGraph(root);

    LinkedList<DirectNode> stack = new LinkedList<>();
    stack.add(this.dgraph.first);

    HashSet<DirectNode> setVisited = new HashSet<>();

    boolean res = false;

    while (!stack.isEmpty()) {

      DirectNode node = stack.removeFirst();

      if (setVisited.contains(node)) {
        continue;
      }
      setVisited.add(node);

      res |= processExprentList(node.exprents);

      stack.addAll(node.succs);
    }

    return res;
  }

  private boolean processExprentList(List<Exprent> lst) {

    boolean result = false;

    for (int i = 0; i < lst.size(); i++) {
      Exprent exprent = lst.get(i);
      exprentReplaced = false;

      Exprent retexpr = processExprentRecursive(exprent);
      if (retexpr != null) {
        lst.set(i, retexpr);

        result = true;
        i--; // process the same exprent again
      }

      result |= exprentReplaced;
    }

    return result;
  }

  private Exprent processExprentRecursive(Exprent exprent) {

    boolean replaced = true;
    while (replaced) {
      replaced = false;

      for (Exprent expr : exprent.getAllExprents()) {
        Exprent retexpr = processExprentRecursive(expr);
        if (retexpr != null) {
          exprent.replaceExprent(expr, retexpr);
          replaced = true;
          exprentReplaced = true;
          break;
        }
      }
    }

    if (exprent.type == Exprent.EXPRENT_ASSIGNMENT) {
      AssignmentExprent as = (AssignmentExprent)exprent;

      if (as.getRight().type == Exprent.EXPRENT_FUNCTION) {
        FunctionExprent func = (FunctionExprent)as.getRight();

        VarType midlayer = null;
        if (func.getFuncType() >= FunctionExprent.FUNCTION_I2L &&
            func.getFuncType() <= FunctionExprent.FUNCTION_I2S) {
          midlayer = func.getSimpleCastType();
          if (func.getLstOperands().get(0).type == Exprent.EXPRENT_FUNCTION) {
            func = (FunctionExprent)func.getLstOperands().get(0);
          }
          else {
            return null;
          }
        }

        if (func.getFuncType() == FunctionExprent.FUNCTION_ADD ||
            func.getFuncType() == FunctionExprent.FUNCTION_SUB) {
          Exprent econd = func.getLstOperands().get(0);
          Exprent econst = func.getLstOperands().get(1);

          if (econst.type != Exprent.EXPRENT_CONST && econd.type == Exprent.EXPRENT_CONST &&
              func.getFuncType() == FunctionExprent.FUNCTION_ADD) {
            econd = econst;
            econst = func.getLstOperands().get(0);
          }

          if (econst.type == Exprent.EXPRENT_CONST && ((ConstExprent)econst).hasValueOne()) {
            Exprent left = as.getLeft();

            VarType condtype = left.getExprType();
            if (exprsEqual(left, econd) && (midlayer == null || midlayer.equals(condtype))) {
              FunctionExprent ret = new FunctionExprent(
                func.getFuncType() == FunctionExprent.FUNCTION_ADD ? FunctionExprent.FUNCTION_PPI : FunctionExprent.FUNCTION_MMI,
                econd, func.bytecode);
              ret.setImplicitType(condtype);

              exprentReplaced = true;

              if (!left.equals(econd)) {
                updateVersions(this.dgraph, new VarVersionPair((VarExprent)left), new VarVersionPair((VarExprent)econd));
              }

              return ret;
            }
          }
        }
      }
    }

    return null;
  }

  private boolean exprsEqual(Exprent e1, Exprent e2) {
    if (e1 == e2) return true;
    if (e1 == null || e2 == null) return false;
    if (e1.type == VarExprent.EXPRENT_VAR) {
      return varsEqual(e1, e2);
    }
    return e1.equals(e2);
  }

  private boolean varsEqual(Exprent e1, Exprent e2) {
    if (!(e1 instanceof VarExprent)) return false;
    if (!(e2 instanceof VarExprent)) return false;

    VarExprent v1 = (VarExprent)e1;
    VarExprent v2 = (VarExprent)e2;
    return varProc.getVarOriginalIndex(v1.getIndex()) == varProc.getVarOriginalIndex(v2.getIndex());
    // TODO: Verify the types are in the same 'family' {byte->short->int}
    //        && InterpreterUtil.equalObjects(v1.getVarType(), v2.getVarType());
  }


  private void updateVersions(DirectGraph graph, final VarVersionPair oldVVP, final VarVersionPair newVVP) {
    graph.iterateExprents(new DirectGraph.ExprentIterator() {
      @Override
      public int processExprent(Exprent exprent) {
        List<Exprent> lst = exprent.getAllExprents(true);
        lst.add(exprent);

        for (Exprent expr : lst) {
          if (expr.type == Exprent.EXPRENT_VAR) {
            VarExprent var = (VarExprent)expr;
            if (var.getIndex() == oldVVP.var && var.getVersion() == oldVVP.version) {
              var.setIndex(newVVP.var);
              var.setVersion(newVVP.version);
            }
          }
        }

        return 0;
      }
    });
  }

  //
  // ++a
  // (a > 0) {
  //   ...
  // }
  //
  // becomes
  //
  // if (++a > 0) {
  //   ...
  // }
  //
  // Semantically the same, but cleaner and allows for loop inlining
  public static boolean inlinePPIandMMIIf(RootStatement stat) {
    boolean res = inlinePPIandMMIIfRec(stat);

    if (res) {
      SequenceHelper.condenseSequences(stat);
    }

    return res;
  }

  private static boolean inlinePPIandMMIIfRec(Statement stat) {
    boolean res = false;
    for (Statement st : stat.getStats()) {
      res |= inlinePPIandMMIIfRec(st);
    }

    if (stat.getExprents() != null && !stat.getExprents().isEmpty()) {
      IfStatement destination = findIfSuccessor(stat);

      if (destination != null) {
        // Last exprent
        Exprent expr = stat.getExprents().get(stat.getExprents().size() - 1);
        if (expr.type == Exprent.EXPRENT_FUNCTION) {
          FunctionExprent func = (FunctionExprent)expr;

          if (func.getFuncType() == FunctionExprent.FUNCTION_PPI || func.getFuncType() == FunctionExprent.FUNCTION_MMI) {
            Exprent inner = func.getLstOperands().get(0);

            if (inner.type == Exprent.EXPRENT_VAR) {
              Exprent ifExpr = destination.getHeadexprent().getCondition();

              if (ifExpr.type == Exprent.EXPRENT_FUNCTION) {
                FunctionExprent ifFunc = (FunctionExprent)ifExpr;

                while (ifFunc.getFuncType() == FunctionExprent.FUNCTION_BOOL_NOT) {
                  Exprent innerFunc = ifFunc.getLstOperands().get(0);

                  if (innerFunc.type == Exprent.EXPRENT_FUNCTION) {
                    ifFunc = (FunctionExprent)innerFunc;
                  } else {
                    break;
                  }
                }

                // Search for usages of variable
                boolean found = false;
                VarExprent old = null;
                for (Exprent ex : ifFunc.getAllExprents()) {
                  if (ex.type == Exprent.EXPRENT_VAR) {
                    VarExprent var = (VarExprent)ex;
                    if (var.getIndex() == ((VarExprent)inner).getIndex()) {
                      // Found variable to replace

                      // Fail if we've already seen this variable!
                      if (found) {
                        return false;
                      }

                      // Store the var we want to replace
                      old = var;
                      found = true;
                    }
                  }
                }

                if (found) {
                  // Replace variable with ppi/mmi
                  ifFunc.replaceExprent(old, expr);

                  // Remove old expr
                  stat.getExprents().remove(expr);
                  res = true;
                }
              }
            }
          }
        }
      }
    }

    return res;
  }

  private static IfStatement findIfSuccessor(Statement stat) {
    if (stat.getParent().type == Statement.TYPE_IF) {
      if (stat.getParent().getFirst() == stat) {
        return (IfStatement) stat.getParent();
      }
    }

    return null;
  }
}
