package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchAllStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.util.StartEndPair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TryHelper {
  public static boolean enhanceTryStats(RootStatement root, StructClass cl) {
    boolean ret = makeTryWithResourceRec(cl, root);

    if (ret) {
      if (cl.isVersion(CodeConstants.BYTECODE_JAVA_11)) {
        SequenceHelper.condenseSequences(root);

        if (mergeTrys(root)) {
          SequenceHelper.condenseSequences(root);
        }
      } else {
        SequenceHelper.condenseSequences(root);

        if (collapseTryRec(root)) {
          SequenceHelper.condenseSequences(root);
        }
      }
    }

    return ret;
  }

  private static boolean makeTryWithResourceRec(StructClass cl, Statement stat) {
    if (cl.isVersion(CodeConstants.BYTECODE_JAVA_11)) {
      if (stat.type == Statement.TYPE_TRYCATCH) {
        if (TryWithResourcesProcessor.makeTryWithResourceJ11((CatchStatement) stat)) {
          return true;
        }
      }
    } else {
      if (stat.type == Statement.TYPE_CATCHALL && ((CatchAllStatement) stat).isFinally()) {
        if (TryWithResourcesProcessor.makeTryWithResource((CatchAllStatement) stat)) {
          return true;
        }
      }
    }

    for (Statement st : new ArrayList<>(stat.getStats())) {
      if (makeTryWithResourceRec(cl, st)) {
        return true;
      }
    }

    return false;
  }

  // J11+
  // Merge all try statements recursively
  private static boolean mergeTrys(Statement root) {
    boolean ret = false;

    if (root.type == Statement.TYPE_TRYCATCH) {
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
    if (stat.getStats().size() == 0) {
      return false;
    }

    // Get the statement inside of the current try
    Statement inner = stat.getStats().get(0);

    // Check if the inner statement is a try statement
    if (inner.type == Statement.TYPE_TRYCATCH) {
      // Filter on try with resources statements
      if (((CatchStatement)inner).getTryType() == CatchStatement.RESOURCES) {
        // One try inside of the catch

        // Only merge trys that have an inner statement size of 1, a single block
        // TODO: how does this handle nested nullable try stats?
        if (inner.getStats().size() == 1) {
          // Set the outer try to be resources, and initialize
          stat.setTryType(CatchStatement.RESOURCES);
          stat.getResources().addAll(((CatchStatement)inner).getResources());
          stat.getVarDefinitions().addAll(inner.getVarDefinitions());

          // Get inner block of inner try stat
          Statement innerBlock = inner.getStats().get(0);

          // Replace the inner try statement with the block inside
          stat.replaceStatement(inner, innerBlock);

          // replaceStatement ends up doubling the amount of edges on the inner block, we need to remove the ones we've already seen
          // TODO: this is an internal bug. The edges need to be manually modified otherwise it creates labels

          Set<StartEndPair> pairs = new HashSet<>();
          for (StatEdge edge : innerBlock.getAllSuccessorEdges()) {
            StartEndPair pair = new StartEndPair(edge.getSource().id, edge.getDestination().id);

            // Remove successors that we've already seen
            if (pairs.contains(pair)) {
              innerBlock.removeSuccessor(edge);
            } else {
              pairs.add(pair);
            }
          }

          return true;
        }
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

  private static boolean collapseTry(CatchStatement catchStat) {
    Statement parent = catchStat;
    if (parent.getFirst() != null && parent.getFirst().type == Statement.TYPE_SEQUENCE) {
      parent = parent.getFirst();
    }
    if (parent != null && parent.getFirst() != null && parent.getFirst().type == Statement.TYPE_TRYCATCH) {
      CatchStatement toRemove = (CatchStatement)parent.getFirst();

      if (toRemove.getTryType() == CatchStatement.RESOURCES) {
        catchStat.setTryType(CatchStatement.RESOURCES);
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
}
