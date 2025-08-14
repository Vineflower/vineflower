package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.TextUtil;
import org.vineflower.kotlin.util.KExprProcessor;
import org.vineflower.kotlin.util.KUtils;

public class KIfStatement extends IfStatement {
  public KIfStatement(IfStatement statement) {
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

    setIfstat(statement.getIfstat());
    setElsestat(statement.getElsestat());
    setIfEdge(statement.getIfEdge());
    setElseEdge(statement.getElseEdge());
    setNegated(statement.isNegated());
    setPatternMatched(statement.isPatternMatched());
    setHasPPMM(statement.hasPPMM());
    getHeadexprentList().set(0, statement.getHeadexprent());
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();
    
    buf.append(ExprProcessor.listToJava(varDefinitions, indent));
    
    buf.append(first.toJava(indent));
    
    if (isLabeled()) {
      buf.appendIndent(indent).append("label").append(id).append("@ ");
    }

    Exprent condition = getHeadexprent();
    buf.appendIndent(indent).append(condition.toJava(indent)).append(" {").appendLineSeparator();

    if (getIfstat() == null) {
      if (getIfEdge().explicit) {
        buf.appendIndent(indent + 1);
        if (getIfEdge().getType() == StatEdge.TYPE_BREAK) {
          if (getIfEdge().closure instanceof KSequenceStatement) {
            buf.append("return");
          } else {
            buf.append("break");
          }
        } else {
          buf.append("continue");
        }

        if (getIfEdge().labeled) {
          buf.append("@label").append(getIfEdge().closure.id);
        } else if (getIfEdge().closure instanceof KSequenceStatement) {
          buf.append("@run");
        }

        buf.appendLineSeparator();
      }
    } else {
      buf.append(KExprProcessor.jmpWrapper(getIfstat(), indent + 1, false));
    }

    boolean elseIf = false;

    if (getElsestat() != null) {
      if (getElsestat() instanceof KIfStatement
          && getElsestat().getVarDefinitions().isEmpty()
          && getElsestat().getFirst().getExprents() != null
          && getElsestat().getFirst().getExprents().isEmpty()
          && !getElsestat().isLabeled()
          && (getElsestat().getSuccessorEdges(STATEDGE_DIRECT_ALL).isEmpty()
              || !getElsestat().getSuccessorEdges(STATEDGE_DIRECT_ALL).get(0).explicit)
      ) {
        elseIf = true;
        buf.appendIndent(indent).append("} else ");

        TextBuffer content = KExprProcessor.jmpWrapper(getElsestat(), indent, false);
        content.setStart(TextUtil.getIndentString(indent).length());
        buf.append(content);
      } else {
        TextBuffer content = KExprProcessor.jmpWrapper(getElsestat(), indent + 1, false);

        if (content.length() > 0) {
          buf.appendIndent(indent).append("} else {").appendLineSeparator();
          buf.append(content);
        }
      }
    }

    if (!elseIf) {
      buf.appendIndent(indent).append("}").appendLineSeparator();
    }

    return buf;
  }
}
