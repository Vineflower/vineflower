package org.quiltmc.quiltflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.passes.Pass;
import org.jetbrains.java.decompiler.api.passes.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectNode;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.SSAUConstructorSparseEx;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsGraph;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect;

import java.util.HashMap;
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
              if (right instanceof ConstExprent) {
                if (ssu.nodes.getWithKey(vvp).succs.isEmpty()) {
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
}
