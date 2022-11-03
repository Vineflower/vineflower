package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.StrongConnectivityHelper;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StronglyConnectedComponentsTest {

  // Ensures that strongly connected component calculation remains the same.
  @Test
  public void testSCCs() {
    MinimalQuiltflowerEnvironment.setup();

    // Create a simple 0 <--> 1 relationship
    Statement bb = new BasicBlockStatement(new BasicBlock(0));
    Statement bb2 = new BasicBlockStatement(new BasicBlock(1));

    bb.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, bb, bb2));
    bb2.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, bb2, bb));

    // Lone structure to base off of
    Statement bb3 = new BasicBlockStatement(new BasicBlock(2));

    // All statements we have
    List<Statement> all = new ArrayList<>(Arrays.asList(bb, bb2, bb3));

    // Create a 0 -> 1 -> 2 -> 3 -> 4 -> 0 loop
    // Start index at 3 to account for the others
    List<Statement> stats = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      stats.add(new BasicBlockStatement(new BasicBlock(3 + i)));
    }
    // Add edges
    for (int i = 0; i < stats.size() - 1; i++) {
      stats.get(i).addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, stats.get(i), stats.get(i + 1)));
    }
    // Connect end to front
    stats.get(4).addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, stats.get(4), stats.get(0)));
    // Add this entire structure onto the lone block
    bb3.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, bb3, stats.get(0)));
    all.addAll(stats);

    // Construct a sequence
    Statement seq = new SequenceStatement(all);

    // Generate strong connectivity
    StrongConnectivityHelper scc = new StrongConnectivityHelper(seq);

    // Manually create a strong connectivity graph with our inputs
    List<List<Statement>> sccGraph = new ArrayList<>();
    sccGraph.add(new ArrayList<>(Arrays.asList(bb, bb2)));
    sccGraph.add(new ArrayList<>(Arrays.asList(stats.get(4), stats.get(3), stats.get(2), stats.get(1), stats.get(0))));
    sccGraph.add(new ArrayList<>(Collections.singletonList(bb3)));

    // Ensure that the lists are the same
    Assertions.assertEquals(scc.getComponents(), sccGraph);
  }
}
