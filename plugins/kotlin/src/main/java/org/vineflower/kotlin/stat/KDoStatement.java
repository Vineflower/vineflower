package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.expr.KConstExprent;
import org.vineflower.kotlin.expr.KVarExprent;
import org.vineflower.kotlin.util.KExprProcessor;
import org.vineflower.kotlin.util.KUtils;

public class KDoStatement extends DoStatement {
  public KDoStatement(DoStatement statement) {
    super();
    setLooptype(statement.getLooptype());
    setInitExprent(statement.getInitExprent());
    setConditionExprent(statement.getConditionExprent());
    setIncExprent(statement.getIncExprent());

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

  private static boolean isIntegerType(VarType type) {
    return VarType.VARTYPE_INT.equals(type) ||
      VarType.VARTYPE_BYTE.equals(type) ||
      VarType.VARTYPE_SHORT.equals(type) ||
      VarType.VARTYPE_CHAR.equals(type) ||
      VarType.VARTYPE_LONG.equals(type);
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();
    boolean labeled = isLabeled();

    if (getLooptype() != Type.FOR) {
      buf.append(ExprProcessor.listToJava(varDefinitions, indent));

      buf.appendIndent(indent);

      if (labeled) {
        buf.append("label")
          .append(this.id)
          .append("@ ");
      }
    }

    switch (getLooptype()) {
      case INFINITE -> {
        buf.append("while (true) {").appendLineSeparator();
        buf.append(KExprProcessor.jmpWrapper(first, indent + 1, false));
        buf.appendIndent(indent).append("}").appendLineSeparator();
      }
      case DO_WHILE -> {
        Exprent expr = KUtils.replaceExprent(getConditionExprent());
        if (expr == null) {
          expr = getConditionExprent();
        }

        buf.append("do {").appendLineSeparator();
        buf.append(KExprProcessor.jmpWrapper(first, indent + 1, false));
        buf.appendIndent(indent).append("} while (");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        buf.append(expr.toJava(indent));
        buf.appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.append(")").appendLineSeparator();
      }
      case WHILE -> {
        Exprent expr = KUtils.replaceExprent(getConditionExprent());
        if (expr == null) {
          expr = getConditionExprent();
        }

        buf.append("while (");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        buf.append(expr.toJava(indent));
        buf.appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.append(") {").appendLineSeparator();
        buf.append(KExprProcessor.jmpWrapper(first, indent + 1, false));
        buf.appendIndent(indent).append("}").appendLineSeparator();
      }
      case FOR_EACH -> {
        KVarExprent init = new KVarExprent((VarExprent) getInitExprent());
        init.setDeclarationType(KVarExprent.DeclarationType.FOR_LOOP_VARIABLE);

        Exprent inc = KUtils.replaceExprent(getIncExprent());
        if (inc == null) {
          inc = getIncExprent();
        }

        buf.append("for (").append(init.toJava(indent));
        inc.getInferredExprType(null); //TODO: see DoStatement
        buf.append(" in ").append(inc.toJava(indent)).append(") {").appendLineSeparator();
        buf.append(KExprProcessor.jmpWrapper(first, indent + 1, false));
        buf.appendIndent(indent).append("}").appendLineSeparator();
      }
      case FOR -> {
        buf.appendIndent(indent);

        // This is a hard one as Kotlin has no one-to-one equivalent to Java's for loop
        resugar: if (
          getInitExprent() instanceof AssignmentExprent init &&
          init.getLeft() instanceof VarExprent varExpr &&
          isIntegerType(varExpr.getExprType()) &&
//          init.getRight() instanceof ConstExprent constExpr &&

          getIncExprent() instanceof FunctionExprent inc &&
          inc.getFuncType().isPPMM() &&

          getConditionExprent() instanceof FunctionExprent condition &&
          condition.getFuncType() == FunctionExprent.FunctionType.LT
        ) {
          // Turn for loop into range
          varExpr = new KVarExprent(varExpr);
          ((KVarExprent) varExpr).setDeclarationType(KVarExprent.DeclarationType.FOR_LOOP_VARIABLE);

          Exprent conditionExpr;
          if (condition.getLstOperands().get(1) instanceof ConstExprent c) {
            conditionExpr = new KConstExprent(c);
          } else if (condition.getLstOperands().get(0) instanceof ConstExprent c) {
            conditionExpr = new KConstExprent(c);
          } else {
            Exprent left = condition.getLstOperands().get(0);
            Exprent right = condition.getLstOperands().get(1);
            if (left instanceof VarExprent var && var.equals(varExpr) && right instanceof VarExprent compareVar) {
              conditionExpr = new KVarExprent(compareVar);
            } else if (right instanceof VarExprent var && var.equals(varExpr) && left instanceof VarExprent compareVar) {
              conditionExpr = new KVarExprent(compareVar);
            } else {
              break resugar;
            }
          }

          if (conditionExpr instanceof ConstExprent conditionConst) {
            conditionConst.setConstType(varExpr.getExprType());
            if (conditionConst.getValue() instanceof Integer i) {
              int newValue = inc.getFuncType().isPP() ? i - 1 : i + 1;
              conditionExpr = new KConstExprent(new ConstExprent(varExpr.getExprType(), newValue, conditionConst.bytecode));
            }
          }

//          constExpr.setConstType(varExpr.getExprType());
          if (init.getRight() instanceof ConstExprent constExpr) {
            constExpr.setConstType(varExpr.getExprType());
          }

          if (
            inc.getFuncType().isPP() &&
            init.getRight() instanceof ConstExprent constExpr &&
            constExpr.getValue() instanceof Integer i
            && i == 0
          ) {
            buf.append("repeat(")
              .append(conditionExpr.toJava())
              .append(") ");

            if (labeled) {
              buf.append("label")
                .append(id)
                .append("@");
            }

            buf.append("{");

            if (!"it".equals(varExpr.getName())) {
              buf.append(" ")
                .append(varExpr.toJava(indent))
                .append(" ->");
            }
            buf.appendLineSeparator();
          } else {
            if (labeled) {
              buf.append("label")
                .append(id)
                .append("@ ");
            }

            buf.append("for (")
              .append(varExpr.toJava(indent))
              .append(" in ")
              .append(init.getRight().toJava())
              .append(inc.getFuncType().isPP() ? ".." : " downTo ")
              .append(conditionExpr.toJava())
              .append(") {")
              .appendLineSeparator();
          }

          buf.append(KExprProcessor.jmpWrapper(first, indent + 1, false));
          buf.appendIndent(indent).append("}").appendLineSeparator();
        } else {
          //TODO other cases
          Exprent init = KUtils.replaceExprent(getInitExprent());
          if (init == null) {
            init = getInitExprent();
          }

          Exprent condition = KUtils.replaceExprent(getConditionExprent());
          if (condition == null) {
            condition = getConditionExprent();
          }

          Exprent inc = KUtils.replaceExprent(getIncExprent());
          if (inc == null) {
            inc = getIncExprent();
          }

          if (labeled) {
            buf.setLength(0); // Clear buffer, label needs to be treated differently
            buf.appendIndent(indent);
          }

          buf.append("// $VF: Unable to resugar Kotlin loop from Java for loop")
            .appendLineSeparator()
            .appendIndent(indent);

          if (labeled) {
            buf.append("run label")
              .append(this.id)
              .append("@{")
              .appendLineSeparator()
              .appendIndent(++indent);
          }

          if (init != null) {
            buf.append(init.toJava(indent)).appendLineSeparator().appendIndent(indent);
          }

          buf.append("while (true) {").appendLineSeparator();
          buf.appendIndent(indent + 1);
          buf.append("if (");
          buf.append(condition.toJava(indent + 1));
          buf.append(") break").appendLineSeparator();
          buf.append(KExprProcessor.jmpWrapper(first, indent + 1, false));
          buf.appendLineSeparator();
          buf.appendIndent(indent + 1).append(inc.toJava(indent + 1)).appendLineSeparator();
          buf.appendIndent(indent).append("}").appendLineSeparator();

          if (labeled) {
            buf.appendIndent(--indent)
              .append("}")
              .appendLineSeparator();
          }
        }
      }
    }

    return buf;
  }
}
