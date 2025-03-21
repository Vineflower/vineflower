package org.vineflower.kotlin.util;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.stats.DummyExitStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.stat.KSequenceStatement;

import java.util.List;

public class KExprProcessor {
  public static TextBuffer jmpWrapper(Statement stat, int indent, boolean isSwitch) {
    TextBuffer buf = stat.toJava(indent);

    List<StatEdge> successors = stat.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL);
    if (successors.size() == 1) {
      StatEdge edge = successors.get(0);
      if (edge.getType() != StatEdge.TYPE_REGULAR && edge.explicit && !(edge.getDestination() instanceof DummyExitStatement)) {
        TextBuffer innerBuf = new TextBuffer();
        innerBuf.appendIndent(indent);

        switch (edge.getType()) {
          case StatEdge.TYPE_BREAK -> {
            ExprProcessor.addDeletedGotoInstructionMapping(stat, buf);
            if (!isSwitch || edge.labeled) {
              if (edge.closure instanceof KSequenceStatement) {
                innerBuf.append("return");
              } else {
                innerBuf.append("break");
              }
            }
          }
          case StatEdge.TYPE_CONTINUE -> {
            ExprProcessor.addDeletedGotoInstructionMapping(stat, buf);
            innerBuf.append("continue");
          }
        }

        if (edge.labeled) {
          innerBuf.append("@label").append(edge.closure.id);
        } else if (edge.closure instanceof KSequenceStatement) {
          innerBuf.append("@run");
        }

        if (!innerBuf.containsOnlyWhitespaces()) {
          buf.append(innerBuf).appendLineSeparator();
        } else {
          innerBuf.convertToStringAndAllowDataDiscard();
        }
      }
    }

    return buf;
  }
}
