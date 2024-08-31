package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
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
      buf.appendIndent(indent++)
        .append("run label")
        .append(id)
        .append("@{")
        .appendLineSeparator();
    }

    buf.appendIndent(indent).append(first.toJava());

    boolean showIfHidden = DecompilerContext.getOption(IFernflowerPreferences.SHOW_HIDDEN_STATEMENTS);

    if (isPhantom()) {
      if (!showIfHidden) {
        return buf;
      } else {
        buf.appendIndent(indent)
          .append("/*")
          .appendLineSeparator();
      }
    }

    buf.append(getHeadexprent().toJava(indent))
      .append(" {")
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
          buf.append(", ");
        }

        if (value instanceof ConstExprent && !VarType.VARTYPE_NULL.equals(value.getExprType())) {
          ConstExprent constValue = (ConstExprent) value.copy();
          constValue.setConstType(switchType);
        }

        if (value instanceof FieldExprent field && field.isStatic()) { // enum
          ImportCollector importCollector = DecompilerContext.getImportCollector();
          buf.appendClass(importCollector.getShortName(field.getClassname()), false, field.getClassname())
            .append('.')
            .appendField(field.getName(), false, field.getClassname(), field.getName(), field.getDescriptor());
        } else {
          buf.append(value.toJava(indent));
        }
      }

      TextBuffer body = KExprProcessor.jmpWrapper(stat, indent + 2, true);

      if (anyNonDefault) {
        buf.append(" -> ");
        if (body.countLines() > 1) {
          buf.append("{")
            .appendLineSeparator()
            .append(body)
            .appendIndent(indent + 1)
            .append("}")
            .appendLineSeparator();
        } else {
          int indentSize = ((String) DecompilerContext.getProperty(IFernflowerPreferences.INDENT_STRING)).length();
          body.setStart(indentSize * (indent + 2));
          buf.append(body);
        }
      }

      if (edges.contains(getDefaultEdge())) {
        buf.appendIndent(indent + 1)
          .append("else -> ");

        if (body.countLines() > 1) {
          buf.append("{")
            .appendLineSeparator()
            .append(body)
            .appendIndent(indent + 1)
            .append("}")
            .appendLineSeparator();
        } else {
          int indentSize = ((String) DecompilerContext.getProperty(IFernflowerPreferences.INDENT_STRING)).length();
          body.setStart(indentSize * (indent + 2));
          buf.append(body);
        }
      }
    }

    buf.appendIndent(indent)
      .append("}")
      .appendLineSeparator();

    if (isLabeled()) {
      buf.appendIndent(--indent)
        .append("}")
        .appendLineSeparator();
    }

    if (isPhantom()) {
      buf.appendIndent(indent)
        .append("*/")
        .appendLineSeparator();
    }

    return buf;
  }
}
