package org.vineflower.kotlin.stat;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.expr.KConstExprent;
import org.vineflower.kotlin.expr.KVarExprent;

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
        buf.append("do {").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
        buf.appendIndent(indent).append("} while (");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        buf.append(statement.getConditionExprent().toJava(indent));
        buf.appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.append(")").appendLineSeparator();
      }
      case WHILE -> {
        buf.append("while (");
        buf.pushNewlineGroup(indent, 1);
        buf.appendPossibleNewline();
        buf.append(statement.getConditionExprent().toJava(indent));
        buf.appendPossibleNewline("", true);
        buf.popNewlineGroup();
        buf.append(") {").appendLineSeparator();
        buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
        buf.appendIndent(indent).append("}").appendLineSeparator();
      }
      case FOR_EACH -> {
        buf.append("for (").append(statement.getInitExprent().toJava(indent));
        statement.getIncExprent().getInferredExprType(null); //TODO: see DoStatement
        buf.append(" in ").append(statement.getIncExprent().toJava(indent)).append(") {").appendLineSeparator();
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
          inc.getFuncType() == FunctionExprent.FunctionType.IPP &&

          statement.getConditionExprent() instanceof FunctionExprent condition &&
          condition.getFuncType() == FunctionExprent.FunctionType.LT &&
          condition.getLstOperands().get(condition.getLstOperands().size() - 1) instanceof ConstExprent conditionConst
        ) {
          // Turn for loop into range
          varExpr = new KVarExprent(varExpr);
          ((KVarExprent) varExpr).setExcludeVarVal(true);

          constExpr.setConstType(varExpr.getExprType());
          conditionConst.setConstType(varExpr.getExprType());
          if (conditionConst.getValue() instanceof Integer i) {
            conditionConst = new ConstExprent(varExpr.getExprType(), i - 1, conditionConst.bytecode);
            conditionConst = new KConstExprent(conditionConst);
          }

          buf.append("for (")
              .append(varExpr.toJava(indent))
              .append(" in ")
              .append(constExpr.toJava())
              .append("..")
              .append(conditionConst.toJava())
              .append(") {")
              .appendLineSeparator();

          buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
          buf.appendIndent(indent).append("}").appendLineSeparator();
        } else {
          //TODO other cases
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

          if (statement.getInitExprent() != null) {
            buf.append(statement.getInitExprent().toJava(indent)).appendLineSeparator().appendIndent(indent);
          }

          buf.append("while (true) {").appendLineSeparator();
          buf.appendIndent(indent + 1);
          buf.append("if (");
          buf.append(statement.getConditionExprent().toJava(indent + 1));
          buf.append(") break").appendLineSeparator();
          buf.append(ExprProcessor.jmpWrapper(statement.getFirst(), indent + 1, false));
          buf.appendLineSeparator();
          buf.appendIndent(indent + 1).append(statement.getIncExprent().toJava(indent + 1)).appendLineSeparator();
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
