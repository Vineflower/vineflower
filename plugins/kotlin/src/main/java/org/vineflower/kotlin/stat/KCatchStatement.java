package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.util.TextBuffer;

public class KCatchStatement extends CatchStatement {
  public KCatchStatement(CatchStatement statement) {
    super();
    getExctStrings().addAll(statement.getExctStrings());
    getVars().addAll(statement.getVars());
    getResources().addAll(statement.getResources());

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
    buf.append(ExprProcessor.listToJava(varDefinitions, indent));

    if (isLabeled()) {
      buf.appendIndent(indent);
      appendLabel(buf, -1);
      buf.appendPunctuation("@").appendWhitespace(" ");
    }

    buf.appendIndent(indent++).appendKeyword("try").appendWhitespace(" ").appendPunctuation("{").appendLineSeparator();

    for (Exprent resource : getResources()) {
      buf.appendIndent(indent)
        .append(resource.toJava(indent))
        .appendPunctuation(".")
        .appendMethod("use", false, "kotlin/jdk7/AutoCloseableKt", "use", "(Lkotlin/jvm/functions/Function1;)Ljava/lang/Object;")
        .appendWhitespace(" ")
        .appendPunctuation("{")
        .appendLineSeparator();

      indent++;
    }

    buf.append(ExprProcessor.jmpWrapper(first, indent, false));

    buf.appendIndent(--indent).appendPunctuation("}");
    for (Exprent ignored : getResources()) {
      buf.appendLineSeparator();
      buf.appendIndent(--indent).appendPunctuation("}");
    }

    for (int i = 1; i < stats.size(); i++) {
      Statement stat = stats.get(i);

      BasicBlock block = stat.getBasichead().getBlock();
      if (!block.getSeq().isEmpty() && block.getInstruction(0).opcode == CodeConstants.opc_astore) {
        Integer offset = block.getOldOffset(0);
        if (offset > -1) buf.addBytecodeMapping(offset);
      }

      buf.appendWhitespace(" ").appendKeyword("catch").appendWhitespace(" ").appendPunctuation("(");
      buf.append(getVars().get(i - 1).toJava(indent));
      buf.appendPunctuation(")").appendWhitespace(" ").appendPunctuation("{").appendLineSeparator();
      buf.append(stat.toJava(indent + 1));
      buf.appendIndent(indent).appendPunctuation("}");
    }

    buf.appendLineSeparator();

    return buf;
  }
}
