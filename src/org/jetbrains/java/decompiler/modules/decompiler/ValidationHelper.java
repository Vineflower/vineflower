package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.util.VBStyleCollection;

import java.util.Deque;
import java.util.LinkedList;

public final class ValidationHelper {
  private static final boolean VALIDATE = System.getProperty("VALIDATE_DECOMPILED_CODE", "false").equals("true");

  public static void validateStatement(RootStatement statement) {
    if (!VALIDATE) {
      return;
    }

    VBStyleCollection<Statement, Integer> statements = new VBStyleCollection<>();

    Deque<Statement> stack = new LinkedList<>();
    stack.push(statement.getDummyExit());
    stack.push(statement);

    while (!stack.isEmpty()) {
      Statement stat = stack.pop();

      statements.putWithKey(stat, stat.id);

      stack.addAll(stat.getStats());
    }

    for (Statement stat : statements) {
      for (StatEdge edge : stat.getAllSuccessorEdges()) {
        if (!statements.contains(edge.getSource())) {
          throw new IllegalStateException("Edge pointing from non-existing statement: [" + stat + "] " + edge);
        }

        if (!statements.contains(edge.getDestination())) {
          throw new IllegalStateException("Edge pointing to non-existing statement: [" + stat + "] " + edge);
        }

        if (edge.closure != null) {
          if (!statements.contains(edge.closure)) {
            throw new IllegalStateException("Edge with non-existing closure: [" + stat + "] " + edge);
          }
        }
      }
    }
  }
}
