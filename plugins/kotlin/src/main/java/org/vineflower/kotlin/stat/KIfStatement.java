package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.TextUtil;
import org.jetbrains.java.decompiler.util.token.TokenType;
import org.vineflower.kotlin.util.KExprProcessor;

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
    
    MethodWrapper method = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
    
    if (isLabeled()) {
      buf.appendIndent(indent);
      appendLabel(buf, -1);
      buf.appendPunctuation("@").appendLineSeparator();
    }

    Exprent condition = getHeadexprent();
    buf.appendIndent(indent).append(condition.toJava(indent)).appendWhitespace(" ").appendPunctuation("{").appendLineSeparator();

    if (getIfstat() == null) {
      if (getIfEdge().explicit) {
        buf.appendIndent(indent + 1);
        if (getIfEdge().getType() == StatEdge.TYPE_BREAK) {
          if (getIfEdge().closure instanceof KSequenceStatement) {
            buf.appendKeyword("return");
          } else {
            buf.appendKeyword("break");
          }
        } else {
          buf.appendKeyword("continue");
        }

        if (getIfEdge().labeled) {
          buf.appendPunctuation("@").appendLabel("label" + getIfEdge().closure.id, false, method.classStruct.qualifiedName, method.methodStruct.getName(), method.desc(), getIfEdge().closure.id);
        } else if (getIfEdge().closure instanceof KSequenceStatement) {
          buf.appendPunctuation("@").append("run", TokenType.METHOD);
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
        buf.appendIndent(indent).appendPunctuation("}").appendWhitespace(" ").appendKeyword("else").appendWhitespace(" ");

        TextBuffer content = KExprProcessor.jmpWrapper(getElsestat(), indent, false);
        content.setStart(TextUtil.getIndentString(indent).length());
        buf.append(content);
      } else {
        TextBuffer content = KExprProcessor.jmpWrapper(getElsestat(), indent + 1, false);

        if (content.length() > 0) {
          buf.appendIndent(indent).appendPunctuation("}").appendWhitespace(" ").appendKeyword("else").appendWhitespace(" ").appendPunctuation("{").appendLineSeparator();
          buf.append(content);
        }
      }
    }

    if (!elseIf) {
      buf.appendIndent(indent).appendPunctuation("}").appendLineSeparator();
    }

    return buf;
  }
}
