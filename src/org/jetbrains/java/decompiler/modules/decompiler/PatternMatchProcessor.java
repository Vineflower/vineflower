package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.ArrayList;
import java.util.List;

public final class PatternMatchProcessor {
  public static void matchInstanceof(RootStatement root) {
    runMatchInstanceof(root, root);
  }

  private static void runMatchInstanceof(Statement statement, RootStatement root) {
    for (Statement stat : statement.getStats()) {
      runMatchInstanceof(stat, root);
    }

    if (statement instanceof IfStatement) {
      handleIf((IfStatement) statement, root);
    }
  }

  private static void handleIf(IfStatement statement, RootStatement root) {
    Exprent condition = statement.getHeadexprent().getCondition();

    if (condition.type != Exprent.EXPRENT_FUNCTION) {
      return;
    }

    FunctionExprent func = (FunctionExprent) condition;

    List<Exprent> exprents = func.getAllExprents(true);

    for (Exprent exprent : exprents) {
      if (exprent.type == Exprent.EXPRENT_FUNCTION) {
        FunctionExprent iof = (FunctionExprent)exprent;

        if (iof.getFuncType() == FunctionExprent.FUNCTION_INSTANCEOF) {
          Exprent source = iof.getLstOperands().get(0);
          Exprent target = iof.getLstOperands().get(1);

          if (statement.getIfstat() != null && statement.getIfstat().getExprents() != null && statement.getIfstat().getExprents().size() > 0) {
            Exprent first = statement.getIfstat().getExprents().get(0);

            // TODO: case for single assignment. We want to not match in those cases

            if (first.type == Exprent.EXPRENT_ASSIGNMENT) {
              Exprent left = first.getAllExprents().get(0);
              Exprent right = first.getAllExprents().get(1);

              if (right.type == Exprent.EXPRENT_FUNCTION) {
                if (((FunctionExprent)right).getFuncType() == FunctionExprent.FUNCTION_CAST) {
                  Exprent casted = right.getAllExprents().get(0);

                  if (source.equals(casted)) {
                    List<VarType> vts = new ArrayList<>();

                    findAllVarDefsExcluding(vts, root, statement.getIfstat());

                    if (left.type == Exprent.EXPRENT_VAR && target.getExprType().equals(left.getExprType())) {

                      // TODO: incredibly dirty hack!! Find a better way!
                      if (vts.contains(left.getExprType())) {
                        continue;
                      }

                      iof.getLstOperands().add(2, left);
                      statement.getIfstat().getExprents().remove(0);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // TODO: better filter- needs to search for all in root excluding this (scope analysis, don't go deeper into blocks apart from where we already are)
  private static void findAllVarDefsExcluding(List<VarType> vts, Statement root, Statement exclude) {
    List<Exprent> exps = root.getExprents();

    if (exps == null) {
      List<Object> objs = root.getSequentialObjects();
      for (Object obj : objs) {
        if (obj instanceof Statement) {
          if (obj == exclude) {
            continue;
          }

          findAllVarDefsExcluding(vts, (Statement) obj, exclude);
        } else if (obj instanceof Exprent) {
          filterExprentsForTypes(vts, (Exprent) obj);
        }
      }
    } else {
      for (Exprent exp : exps) {
        filterExprentsForTypes(vts, exp);
      }
    }
  }

  private static void filterExprentsForTypes(List<VarType> vts, Exprent exp) {
    if (exp.type == Exprent.EXPRENT_ASSIGNMENT) {
      AssignmentExprent assignment = (AssignmentExprent) exp;

      Exprent left = assignment.getLeft();

      if (left.type == Exprent.EXPRENT_VAR) {
        vts.add(((VarExprent)left).getVarType());
      }
    }
  }
}
