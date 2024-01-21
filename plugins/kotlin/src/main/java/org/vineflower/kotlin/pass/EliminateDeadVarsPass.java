package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectNode;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.SSAUConstructorSparseEx;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsGraph;
import org.jetbrains.java.decompiler.struct.StructMethod;

import java.util.List;

public class EliminateDeadVarsPass implements Pass {

  @Override
  public boolean run(PassContext ctx) {
    return eliminate(ctx.getRoot(), ctx.getMethod());
  }

  private static boolean eliminate(RootStatement root, StructMethod mt) {
    SSAUConstructorSparseEx ssau = new SSAUConstructorSparseEx();
    ssau.splitVariables(root, mt);

    // TODO: preference for this, as it can be a destructive action
    DirectGraph digraph = ssau.getDirectGraph();
    VarVersionsGraph ssu = ssau.getSsuVersions();

    boolean changedAny = false;
    boolean changed;
    do {
      changed = false;

      for (DirectNode nd : digraph.nodes) {
        List<Exprent> exprents = nd.exprents;

        for (int i = 0; i < exprents.size(); i++) {
          Exprent ex = exprents.get(i);

          if (ex instanceof AssignmentExprent) {
            AssignmentExprent aex = (AssignmentExprent) ex;
            Exprent left = aex.getLeft();
            Exprent right = aex.getRight();

            if (left instanceof VarExprent) {
              VarExprent var = (VarExprent) left;

              VarVersionPair vvp = var.getVarVersionPair();
              if (isPureToReplace(right)) {
                if (!ssu.nodes.getWithKey(vvp).hasAnySuccessors()) {
                  exprents.remove(i);
                  changed = true;
                  changedAny = true;

//                  System.out.println(mt + ": Removed " + var + " = " + right);
                  i--;
                }
              }
            }
          }
        }
      }
    } while (changed);

    StackVarsProcessor.setVersionsToNull(root);

    return changedAny;
  }

  private static boolean isPureToReplace(Exprent expr) {
    if (expr instanceof ConstExprent) {
      return true;
    }

    if (expr instanceof FieldExprent) {
      FieldExprent field = (FieldExprent) expr;

      return field.isStatic() && field.getClassname().equals("kotlin/Unit");
    }

    return false;
  }
}
