package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.StructClass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TryHelper {
  public static boolean enhanceTryStats(RootStatement root, StructClass cl) {
    boolean ret = makeTryWithResourceRec(cl, root);

    if (ret) {
      SequenceHelper.condenseSequences(root);
    }

    if (mergeTrys(root)) {
      SequenceHelper.condenseSequences(root);

      ret = true;
    }

    return ret;
  }

  private static boolean makeTryWithResourceRec(StructClass cl, Statement stat) {
      boolean ret = false;

      if (cl.getVersion().hasNewTryWithResources() && stat instanceof CatchStatement trySt) {
        if (TryWithResourcesProcessor.makeTryWithResourceJ11(trySt)) {
          ret = true;
        }
      } else if (stat instanceof CatchAllStatement trySt && trySt.isFinally()) {
        if (TryWithResourcesProcessor.makeTryWithResource(trySt)) {
          ret = true;
        }
      }

      for (Statement st : new ArrayList<>(stat.getStats())) {
        if (makeTryWithResourceRec(cl, st)) {
          ret = true;
        }
      }

      return ret;
  }

  private static boolean mergeTrys(Statement root) {
    boolean ret = false;

    if (root instanceof CatchStatement) {
      if (mergeTry((CatchStatement) root)) {
        ret = true;
      }
    }

    for (Statement stat : new ArrayList<>(root.getStats())) {
      ret |= mergeTrys(stat);
    }

    return ret;
  }

  // J11+
  // Merges try with resource statements that are nested within each other, as well as try with resources statements nested in a normal try.
  private static boolean mergeTry(CatchStatement stat) {
    if (stat.getStats().isEmpty()) {
      return false;
    }

    // Get the statement inside of the current try
    Statement inner = stat.getStats().get(0);

    // If the inside is a sequence, check the first of the sequence, which can happen for J8 try with resources due to
    // finally return behavior. Don't do this if there are non-exit breaks coming out of the statement.
    Set<StatEdge> edges = new HashSet<>();
    TryWithResourcesProcessor.findEdgesLeaving(stat, inner, edges);
    Statement parent = stat;
    if (inner instanceof SequenceStatement && edges.isEmpty()) {
      parent = inner;
      inner = inner.getStats().get(0);
    }

    // Check if the inner statement is a try statement
    if (inner instanceof CatchStatement) {
      // Filter on try with resources statements
      List<Exprent> resources = ((CatchStatement) inner).getResources();
      if (!resources.isEmpty()) {
        // One try inside of the catch

        // Only merge try statements without catches, otherwise we might produce invalid code
        if (inner.getStats().size() == 1) {
          // Set the outer try to be resources, and initialize
          stat.getResources().addAll(resources);
          stat.getVarDefinitions().addAll(inner.getVarDefinitions());

          // Get inner block of inner try stat
          Statement innerBlock = inner.getStats().get(0);

          // Remove successors as the replaceStatement call will add the appropriate successor
          List<StatEdge> innerEdges = inner.getAllSuccessorEdges();
          for (StatEdge succ : innerBlock.getAllSuccessorEdges()) {
            boolean found = false;
            for (StatEdge innerEdge : innerEdges) {
              if (succ.getDestination() == innerEdge.getDestination()) {
                found = true;
                break;
              }
            }

            if (found) {
              innerBlock.removeSuccessor(succ);
            }
          }

          // Replace the inner try statement with the block inside
          parent.replaceStatement(inner, innerBlock);

          return true;
        }
      }
    }

    return false;
  }

  // After JDK 9+ try-with-resources reconstruction, temp return variables may
  // be left over from the desugaring pattern:
  //   try (...) { ...; var = value; }
  //   return var;
  // This pass inlines them to:
  //   try (...) { ...; return value; }
  public static boolean inlineTwrReturnVars(Statement stat) {
    boolean changed = false;

    for (Statement st : new ArrayList<>(stat.getStats())) {
      if (inlineTwrReturnVars(st)) {
        changed = true;
      }
    }

    if (stat instanceof SequenceStatement) {
      for (int i = 0; i < stat.getStats().size() - 1; i++) {
        Statement curr = stat.getStats().get(i);
        Statement next = stat.getStats().get(i + 1);

        if (curr instanceof CatchStatement catchStat
            && !catchStat.getResources().isEmpty()
            && next instanceof BasicBlockStatement
            && next.getExprents() != null
            && next.getExprents().size() == 1
            && next.getExprents().get(0) instanceof ExitExprent exitExpr
            && exitExpr.getExitType() == ExitExprent.Type.RETURN
            && exitExpr.getValue() instanceof VarExprent returnVar) {

          Statement tryBody = catchStat.getFirst();
          Statement lastInBody = findLastStatement(tryBody);

          if (lastInBody != null
              && lastInBody.getExprents() != null
              && !lastInBody.getExprents().isEmpty()) {
            List<Exprent> bodyExprents = lastInBody.getExprents();
            Exprent lastExpr = bodyExprents.get(bodyExprents.size() - 1);

            if (lastExpr instanceof AssignmentExprent assignment
                && assignment.getCondType() == null
                && assignment.getLeft() instanceof VarExprent assignVar
                && assignVar.equals(returnVar)) {

              ExitExprent newReturn = (ExitExprent) exitExpr.copy();
              newReturn.replaceExprent(newReturn.getValue(), assignment.getRight().copy());
              bodyExprents.set(bodyExprents.size() - 1, newReturn);

              next.getExprents().clear();
              changed = true;
            }
          }
        }
      }
    }

    return changed;
  }

  private static Statement findLastStatement(Statement stat) {
    if (stat instanceof SequenceStatement) {
      Statement last = stat.getStats().get(stat.getStats().size() - 1);
      return findLastStatement(last);
    }

    if (stat instanceof CatchStatement) {
      return findLastStatement(((CatchStatement) stat).getFirst());
    }

    if (stat.getExprents() != null) {
      return stat;
    }

    return null;
  }
}
