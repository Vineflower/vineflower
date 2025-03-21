package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.util.KExprProcessor;

public class KSequenceStatement extends SequenceStatement {
  public KSequenceStatement(SequenceStatement statement) {
    super();

    setFirst(statement.getFirst());
    stats.addAllWithKey(statement.getStats(), statement.getStats().getLstKeys());
    parent = statement.getParent();
    first = statement.getFirst();
    exprents = statement.getExprents();
    labelEdges.addAll(statement.getLabelEdges());
    varDefinitions.addAll(statement.getVarDefinitions());
    post = statement.getPost();
    lastBasicType = statement.getLastBasicType();

    isMonitorEnter = statement.isMonitorEnter();
    containsMonitorExit = statement.containsMonitorExit();
    isLastAthrow = statement.containsMonitorExitOrAthrow() && !containsMonitorExit;

    continueSet = statement.getContinueSet();
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();
    boolean labeled = isLabeled();

    buf.append(ExprProcessor.listToJava(varDefinitions, indent));

    if (labeled) {
      buf.appendIndent(indent++)
        .append("run label")
        .append(id)
        .append("@{")
        .appendLineSeparator();
    }

    boolean notEmpty = false;
    for (int i = 0; i < stats.size(); i++) {
      Statement st = stats.get(i);
      TextBuffer str = KExprProcessor.jmpWrapper(st, indent, false);

      if (i > 0 && !str.containsOnlyWhitespaces() && notEmpty) {
        buf.appendLineSeparator();
      }

      buf.append(str);

      notEmpty = !str.containsOnlyWhitespaces();
    }

    if (labeled) {
      buf.appendIndent(--indent)
        .append("}")
        .appendLineSeparator();
    }

    return buf;
  }
}
