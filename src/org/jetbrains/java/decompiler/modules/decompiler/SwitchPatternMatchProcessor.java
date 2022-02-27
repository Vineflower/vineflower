package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.BasicBlockStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.ArrayList;
import java.util.List;

public final class SwitchPatternMatchProcessor {
  public static boolean processPatternMatching(Statement root) {
    boolean ret = processPatternMatchingRec(root);

    if (ret) {
      SequenceHelper.condenseSequences(root);
    }

    return ret;
  }

  private static boolean processPatternMatchingRec(Statement stat) {
    boolean ret = false;
    for (Statement st : new ArrayList<>(stat.getStats())) {
      ret |= processPatternMatchingRec(st);
    }

    if (stat.type == Statement.TYPE_SWITCH) {
      ret |= processStatement((SwitchStatement) stat);
    }

    return ret;
  }

  private static boolean processStatement(SwitchStatement stat) {
    if (stat.isPhantom()) {
      return false;
    }

    SwitchHeadExprent head = (SwitchHeadExprent)stat.getHeadexprent();

    boolean switchPatternMatch = isSwitchPatternMatch(head);

    if (!switchPatternMatch) {
      return false;
    }

    // Found switch pattern match, start applying basic transformations
    boolean isLoopParent = stat.getParent().type == Statement.TYPE_DO;

    // TODO: handle this case!
    if (isLoopParent) {
      return false;
    }

    for (int i = 0; i < stat.getCaseStatements().size(); i++) {
      Statement caseStat = stat.getCaseStatements().get(i);

      if (caseStat.type != Statement.TYPE_BASICBLOCK) {
        continue;
      }

      Exprent caseExpr = stat.getCaseValues().get(i).get(0);

      // Default branch
      if (caseExpr == null) {
        continue;
      }

      if (caseExpr.type == Exprent.EXPRENT_CONST) {
        int caseValue = ((ConstExprent)caseExpr).getIntValue();

        if (caseValue == -1) {
          // null
          stat.getCaseValues().get(i).set(0, new ConstExprent(VarType.VARTYPE_NULL, null, null));
        }
      }

      // Make instanceof
      BasicBlockStatement caseStatBlock = (BasicBlockStatement)caseStat;
      if (caseStatBlock.getExprents().size() > 1) {
        Exprent expr = caseStatBlock.getExprents().get(0);
        if (expr.type == Exprent.EXPRENT_ASSIGNMENT) {
          AssignmentExprent assign = (AssignmentExprent)expr;

          if (assign.getLeft().type == Exprent.EXPRENT_VAR) {
            VarExprent var = (VarExprent)assign.getLeft();

            if (assign.getRight().type == Exprent.EXPRENT_FUNCTION && ((FunctionExprent)assign.getRight()).getFuncType() == FunctionExprent.FUNCTION_CAST) {
              FunctionExprent cast = (FunctionExprent)assign.getRight();

              List<Exprent> operands = new ArrayList<>();
              operands.add(cast.getLstOperands().get(0)); // checking var
              operands.add(cast.getLstOperands().get(1)); // type
              operands.add(var); // pattern match var

              FunctionExprent func = new FunctionExprent(FunctionExprent.FUNCTION_INSTANCEOF, operands, null);

              caseStatBlock.getExprents().remove(0);

              // TODO: ssau representation
              stat.getCaseValues().get(i).set(0, func);
            }
          }
        }
      }
    }

    List<StatEdge> sucs = stat.getSuccessorEdges(StatEdge.TYPE_REGULAR);

    if (!sucs.isEmpty()) {

      Statement suc = sucs.get(0).getDestination();
      if (suc.type != Statement.TYPE_BASICBLOCK) { // make basic block if it isn't found
        Statement oldSuc = suc;

        suc = BasicBlockStatement.create();
        SequenceStatement seq = new SequenceStatement(stat, suc);

        seq.setParent(stat.getParent());

        stat.replaceWith(seq);

        seq.setAllParent();

        // Replace successors with the new basic block
        for (Statement st : stat.getCaseStatements()) {
          for (StatEdge edge : st.getAllSuccessorEdges()) {
            if (edge.getDestination() == oldSuc) {
              st.removeSuccessor(edge);

              st.addSuccessor(new StatEdge(edge.getType(), st, suc, seq));
            }
          }
        }

        // Control flow from new basic block to the next one
        suc.addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR, suc, oldSuc, seq));
      }

      head.setValue(((InvocationExprent)head.getValue()).getLstParameters().get(0));

      stat.setPhantom(true);

      suc.getExprents().add(0, new SwitchExprent(stat, VarType.VARTYPE_INT, false, true));
    }

    return false;
  }

  private static boolean isSwitchPatternMatch(SwitchHeadExprent head) {
    Exprent value = head.getValue();

    if (value.type == Exprent.EXPRENT_INVOCATION) {
      InvocationExprent invoc = (InvocationExprent)value;

      if (invoc.getInvocationTyp() == InvocationExprent.INVOKE_DYNAMIC) {
        if (invoc.getName().equals("typeSwitch")) {
          return true;
        }
      }
    }

    return false;
  }
}
