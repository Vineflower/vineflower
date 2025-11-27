package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.List;

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
      buf.appendIndent(indent).append("label").append(id).append("@ ");
    }

    buf.appendIndent(indent++).append("try {").appendLineSeparator();

    for (Exprent resource : getResources()) {
      buf.append(indent++).append(resource.toJava(indent)).append(".use {").appendLineSeparator();
    }

    buf.append(ExprProcessor.jmpWrapper(first, indent, false));

    buf.appendIndent(--indent).append("}");
    for (Exprent ignored : getResources()) {
      buf.appendLineSeparator();
      buf.appendIndent(--indent).append("}");
    }

    for (int i = 1; i < stats.size(); i++) {
      Statement stat = stats.get(i);

      BasicBlock block = stat.getBasichead().getBlock();
      if (!block.getSeq().isEmpty() && block.getInstruction(0).opcode == CodeConstants.opc_astore) {
        Integer offset = block.getOldOffset(0);
        if (offset > -1) buf.addBytecodeMapping(offset);
      }

      buf.append(" catch (");
      buf.append(getVars().get(i - 1).toJava(indent));
      buf.append(") {").appendLineSeparator();
      buf.append(stat.toJava(indent + 1));
      buf.appendIndent(indent).append("}");
    }

    buf.appendLineSeparator();

    return buf;
  }
}
