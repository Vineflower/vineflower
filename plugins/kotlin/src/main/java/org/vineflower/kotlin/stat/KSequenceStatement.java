package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.util.TextBuffer;

public class KSequenceStatement extends KStatement<SequenceStatement> {
  public KSequenceStatement(SequenceStatement statement) {
    super(statement);
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();
    boolean labeled = statement.isLabeled();

    buf.append(ExprProcessor.listToJava(statement.getVarDefinitions(), indent));

    if (labeled) {
      buf.appendIndent(indent++)
        .append("run label")
        .append(statement.id)
        .append("@{")
        .appendLineSeparator();
    }

    boolean notEmpty = false;
    for (int i = 0; i < statement.getStats().size(); i++) {
      Statement st = statement.getStats().get(i);
      TextBuffer str = ExprProcessor.jmpWrapper(st, indent, false);

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
