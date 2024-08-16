package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.expr.KConstExprent;
import org.vineflower.kotlin.expr.KVarExprent;
import org.vineflower.kotlin.util.KUtils;

public class KDoStatement extends KStatement<DoStatement> {
  public KDoStatement(DoStatement statement) {
    super(statement);
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
    boolean labeled = statement.isLabeled();

    buf.appendIndent(indent);

    if (labeled) {
      buf.append("label")
        .append(this.id)
        .append("@ ");
    }

    switch (statement.getLooptype()) {
      case INFINITE -> {
        buf.append("while (true) {").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
        buf.appendIndent(indent).append("}").appendLineSeparator();
      }
      case DO_WHILE -> {
        Exprent expr = KUtils.replaceExprent(statement.getConditionExprent());
        if (expr == null) {
          expr = statement.getConditionExprent();
        }

        buf.append("do {").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
        buf.appendIndent(indent).append("} while (");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        buf.append(expr.toJava(indent));
        buf.appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.append(")").appendLineSeparator();
      }
      case WHILE -> {
        Exprent expr = KUtils.replaceExprent(statement.getConditionExprent());
        if (expr == null) {
          expr = statement.getConditionExprent();
        }

        buf.append("while (");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        buf.append(expr.toJava(indent));
        buf.appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.append(") {").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
        buf.appendIndent(indent).append("}").appendLineSeparator();
      }
      case FOR_EACH -> {
        KVarExprent init = new KVarExprent((VarExprent) statement.getInitExprent());
        init.setExcludeVarVal(true);

        Exprent inc = KUtils.replaceExprent(statement.getIncExprent());
        if (inc == null) {
          inc = statement.getIncExprent();
        }

        buf.append("for (").append(init.toJava(indent));
        inc.getInferredExprType(null); //TODO: see DoStatement
        buf.append(" in ").append(inc.toJava(indent)).append(") {").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
        buf.appendIndent(indent).append("}").appendLineSeparator();
      }
      case FOR -> {
        // This is a hard one as Kotlin has no one-to-one equivalent to Java's for loop
        if (
          statement.getInitExprent() instanceof AssignmentExprent init &&
          init.getLeft() instanceof VarExprent varExpr &&
          isIntegerType(varExpr.getExprType()) &&
          init.getRight() instanceof ConstExprent constExpr &&

          statement.getIncExprent() instanceof FunctionExprent inc &&
          inc.getFuncType().isPPMM() &&

          statement.getConditionExprent() instanceof FunctionExprent condition &&
          condition.getFuncType() == FunctionExprent.FunctionType.LT
        ) {
          // Turn for loop into range
          varExpr = new KVarExprent(varExpr);
          ((KVarExprent) varExpr).setExcludeVarVal(true);

          KConstExprent conditionConst;
          if (condition.getLstOperands().get(1) instanceof ConstExprent c) {
            conditionConst = new KConstExprent(c);
          } else {
            conditionConst = new KConstExprent((ConstExprent) condition.getLstOperands().get(0));
          }

          conditionConst.setConstType(varExpr.getExprType());
          if (conditionConst.getValue() instanceof Integer i) {
            int newValue = inc.getFuncType().isPP() ? i - 1 : i + 1;
            conditionConst = new KConstExprent(new ConstExprent(varExpr.getExprType(), newValue, conditionConst.bytecode));
          }

          constExpr.setConstType(varExpr.getExprType());

          if (constExpr.getValue() instanceof Integer i && i == 0) {
            buf.append("repeat(")
              .append(conditionConst.toJava())
              .append(") {");

            if (!"it".equals(varExpr.getName())) {
              buf.append(" ")
                .append(varExpr.toJava(indent))
                .append(" ->");
            }
            buf.appendLineSeparator();
          } else {
            buf.append("for (")
              .append(varExpr.toJava(indent))
              .append(" in ")
              .append(constExpr.toJava())
              .append(inc.getFuncType().isPP() ? ".." : " downTo ")
              .append(conditionConst.toJava())
              .append(") {")
              .appendLineSeparator();
          }

          buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
          buf.appendIndent(indent).append("}").appendLineSeparator();
        } else {
          //TODO other cases
          Exprent init = KUtils.replaceExprent(statement.getInitExprent());
          if (init == null) {
            init = statement.getInitExprent();
          }

          Exprent condition = KUtils.replaceExprent(statement.getConditionExprent());
          if (condition == null) {
            condition = statement.getConditionExprent();
          }

          Exprent inc = KUtils.replaceExprent(statement.getIncExprent());
          if (inc == null) {
            inc = statement.getIncExprent();
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
          buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
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
