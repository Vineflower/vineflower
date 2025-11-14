package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.StructClass;

import java.util.ArrayList;
import java.util.List;

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

  // J11+
  // Merge all try statements recursively
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
              if (succ.getDestination() == innerEdge.getDestination() && succ.getType() == innerEdge.getType()) {
                found = true;
                break;
              }
            }

            if (found) {
              innerBlock.removeSuccessor(succ);
            }
          }

          // Replace the inner try statement with the block inside
          stat.replaceStatement(inner, innerBlock);

          return true;
        }
      }
    }

    return false;
  }
}
