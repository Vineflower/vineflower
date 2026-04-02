package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FieldExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.util.KExprProcessor;

import java.util.List;

public class KSwitchStatement extends SwitchStatement {
  public KSwitchStatement(SwitchStatement statement) {
    super();
    
    getCaseStatements().addAll(statement.getCaseStatements());
    getCaseEdges().addAll(statement.getCaseEdges());
    getCaseValues().addAll(statement.getCaseValues());
    getCaseGuards().addAll(statement.getCaseGuards());
    //TODO: check if scoped case statements are needed?
    getHeadexprentList().remove(0); // remove added null
    getHeadexprentList().addAll(statement.getHeadexprentList());

    setFirst(statement.getFirst());
    setPhantom(statement.isPhantom()); // Only type of statement that can be phantom
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

    // Set up default edge
    initSimpleCopy();
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();
    buf.append(ExprProcessor.listToJava(varDefinitions, indent));

    if (isLabeled()) {
      MethodWrapper method = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_METHOD_WRAPPER);
      buf.appendIndent(indent++)
        .appendMethod("run", false, "kotlin/StandardKt", "run", "(Lkotlin/jvm/functions/Function0;)Ljava/lang/Object;")
        .appendWhitespace(" ")
        .appendLabel("label" + id, true, method.classStruct.qualifiedName, method.methodStruct.getName(), method.desc(), id)
        .appendPunctuation("@{")
        .appendLineSeparator();
    }

    TextBuffer firstBuf = first.toJava();
    buf.appendIndent(indent).append(firstBuf);

    boolean showIfHidden = DecompilerContext.getOption(IFernflowerPreferences.SHOW_HIDDEN_STATEMENTS);

    if (isPhantom()) {
      if (!showIfHidden) {
        return buf;
      } else {
        buf.appendIndent(indent)
          .appendComment("/*")
          .appendLineSeparator();
      }
    }

    if (firstBuf.length() > 0) {
      buf.appendIndent(indent);
    }

    buf.append(getHeadexprent().toJava(indent))
      .appendWhitespace(" ").appendPunctuation("{")
      .appendLineSeparator();

    VarType switchType = getHeadexprent().getExprType();

    for (int i = 0; i < getCaseStatements().size(); i++) {
      Statement stat = getCaseStatements().get(i);
      List<StatEdge> edges = getCaseEdges().get(i);
      List<Exprent> values = getCaseValues().get(i);
//      Exprent guard = getCaseGuards().size() > i ? getCaseGuards().get(i) : null;

      boolean anyNonDefault = false;
      for (int j = 0; j < edges.size(); j++) {
        if (edges.get(j) == getDefaultEdge()) {
          continue; // Default / "else" edges must show up alone
        }

        anyNonDefault = true;

        Exprent value = values.get(j);

        if (j == 0) {
          buf.appendIndent(indent + 1);
        } else {
          buf.appendPunctuation(",").appendWhitespace(" ");
        }

        if (value instanceof ConstExprent && !VarType.VARTYPE_NULL.equals(value.getExprType())) {
          ConstExprent constValue = (ConstExprent) value.copy();
          constValue.setConstType(switchType);
        }

        if (value instanceof FieldExprent field && field.isStatic()) { // enum
          ImportCollector importCollector = DecompilerContext.getImportCollector();
          buf.appendClass(importCollector.getShortName(field.getClassname()), false, field.getClassname())
            .appendPunctuation('.')
            .appendField(field.getName(), false, field.getClassname(), field.getName(), field.getDescriptor());
        } else {
          buf.append(value.toJava(indent));
        }
      }

      if (anyNonDefault) {
        buf.appendWhitespace(" ").appendOperator("->").appendWhitespace(" ");
        writeCase(indent + 1, buf, stat);
      }
    }

    if (getDefaultEdge() != null) {
      buf.appendIndent(indent + 1)
        .appendKeyword("else").appendWhitespace(" ").appendOperator("->").appendWhitespace(" ");

      writeCase(indent + 1, buf, getDefaultEdge().getDestination());
    }

    buf.appendIndent(indent)
      .appendPunctuation("}")
      .appendLineSeparator();

    if (isLabeled()) {
      buf.appendIndent(--indent)
        .appendPunctuation("}")
        .appendLineSeparator();
    }

    if (isPhantom()) {
      buf.appendIndent(indent)
        .appendComment("*/")
        .appendLineSeparator();
    }

    return buf;
  }

  private void writeCase(int indent, TextBuffer buf, Statement stat) {
    TextBuffer body = KExprProcessor.jmpWrapper(stat, indent + 1, true);
    if (body.countLines() > 1) {
      buf.appendPunctuation("{")
        .appendLineSeparator()
        .append(body)
        .appendIndent(indent)
        .appendPunctuation("}")
        .appendLineSeparator();
    } else if (!body.containsOnlyWhitespaces()) {
      int indentSize = ((String) DecompilerContext.getProperty(IFernflowerPreferences.INDENT_STRING)).length();
      body.setStart(indentSize * (indent + 1));
      buf.append(body);
    } else {
      buf.appendPunctuation("{}").appendLineSeparator();
    }
  }
}
