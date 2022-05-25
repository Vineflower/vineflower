package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SwitchPatternMatchProcessor {
  public static boolean processPatternMatching(Statement root) {
    boolean ret = processPatternMatchingRec(root, root);

    if (ret) {
      SequenceHelper.condenseSequences(root);
    }

    return ret;
  }

  private static boolean processPatternMatchingRec(Statement stat, Statement root) {
    boolean ret = false;
    for (Statement st : new ArrayList<>(stat.getStats())) {
      ret |= processPatternMatchingRec(st, root);
    }

    if (stat.type == Statement.TYPE_SWITCH) {
      ret |= processStatement((SwitchStatement) stat, root);
    }

    return ret;
  }

  private static boolean processStatement(SwitchStatement stat, Statement root) {
    if (stat.isPhantom()) {
      return false;
    }

    SwitchHeadExprent head = (SwitchHeadExprent)stat.getHeadexprent();

    boolean switchPatternMatch = isSwitchPatternMatch(head);

    if (!switchPatternMatch) {
      return false;
    }

    // Found switch pattern match, start applying basic transformations
    // replace `SwitchBootstraps.typeSwitch<...>(o, idx)` with `o` if `idx` is not still used
    // if `idx` is still used, we have guarded labels -> skip // TODO for now
    InvocationExprent value = (InvocationExprent) head.getValue();
    List<Exprent> origParams = value.getLstParameters();
    boolean guarded = true;
    List<Pair<Statement, Exprent>> references = new ArrayList<>();
    if (origParams.get(1) instanceof VarExprent) {
      VarExprent var = (VarExprent) origParams.get(1);
      SwitchHelper.findExprents(root, Exprent.class, var::isVarReferenced, false, (st, expr) -> references.add(Pair.of(st, expr)));
      // If we have one reference...
      if (references.size() == 1) {
        // ...and its just assignment...
        Pair<Statement, Exprent> ref = references.get(0);
        if (ref.b instanceof AssignmentExprent) { // NOTE TO SELF: might break test??
          // ...remove the variable
          ref.a.getExprents().remove(ref.b);
          guarded = false;
        }
      }
    }

    Map<List<Exprent>, Exprent> guards = new HashMap<>(0);
    if (guarded) {
      guards = new HashMap<>(references.size());
      // in j17,
      // a guard takes the form of exactly
      // if (!guardCond) { idx = __thisIdx + 1; break; }
      // at the start of that branch
      // remove the initial assignment to 0
      Pair<Statement, Exprent> refA = references.get(0);
      if (refA.b instanceof AssignmentExprent && ((AssignmentExprent) refA.b).getRight() instanceof ConstExprent) {
        ConstExprent constExprent = (ConstExprent) ((AssignmentExprent) refA.b).getRight();
        if (constExprent.getConstType().typeFamily == CodeConstants.TYPE_FAMILY_INTEGER && constExprent.getIntValue() == 0) {
          refA.a.getExprents().remove(refA.b);
          references.remove(0);
        }
      }
      // TODO: more tests
      for (Pair<Statement, Exprent> reference : references) {
        System.out.println(reference);
        if (reference.b instanceof AssignmentExprent) {
          Statement assignStat = reference.a;
          // i assure you, my esteemed reviewer, this is important
          if (assignStat instanceof BasicBlockStatement
              && assignStat.getExprents().size() == 1
              && assignStat.getParent() instanceof IfStatement
              && assignStat.getParent().getParent() instanceof SequenceStatement
              && assignStat.getParent().getParent().getParent() == stat) {
            Statement next = assignStat.getSuccessorEdges(StatEdge.TYPE_CONTINUE).get(0).getDestination();
            if (next == stat.getParent()) {
              IfStatement guardIf = (IfStatement) assignStat.getParent();
              if (guardIf.getHeadexprent().getCondition() instanceof FunctionExprent) {
                FunctionExprent cond = (FunctionExprent) guardIf.getHeadexprent().getCondition();
                Exprent guardExprent = cond.getLstOperands().get(0);
                List<Statement> caseStatements = stat.getCaseStatements();
                for (int i = 0; i < caseStatements.size(); i++) {
                  if (caseStatements.get(i).containsStatement(reference.a)) {
                    guards.put(stat.getCaseValues().get(i), guardExprent);

                    break;
                  }
                }
              }
            }
          }
        }
      }
    }

    // TODO for self:
    //  remove parent loop
    //  invert guard condition
    System.out.println(guards);
    for (int i = 0; i < stat.getCaseStatements().size(); i++) {
      Statement caseStat = stat.getCaseStatements().get(i);
      System.out.println(caseStat);

      if (caseStat.type != Statement.TYPE_BASICBLOCK) {
        continue;
      }

      List<Exprent> allCases = stat.getCaseValues().get(i);
      Exprent caseExpr = allCases.get(0);

      // Default branch
      if (caseExpr == null) {
        continue;
      }

      if (guards.containsKey(allCases)) {
        // TODO: this is bad
        while(stat.getCaseGuards().size() <= i)
          stat.getCaseGuards().add(null);
        stat.getCaseGuards().set(i, guards.get(allCases));
      }
      if (caseExpr.type == Exprent.EXPRENT_CONST) {
        int caseValue = ((ConstExprent)caseExpr).getIntValue();

        if (caseValue == -1) {
          // null
          allCases.set(0, new ConstExprent(VarType.VARTYPE_NULL, null, null));
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
              allCases.set(0, func);
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

      stat.setPhantom(true);
      suc.getExprents().add(0, new SwitchExprent(stat, VarType.VARTYPE_INT, false, true));
    }

    head.setValue(origParams.get(0));

    return false;
  }

  private static boolean isSwitchPatternMatch(SwitchHeadExprent head) {
    Exprent value = head.getValue();

    if (value.type == Exprent.EXPRENT_INVOCATION) {
      InvocationExprent invoc = (InvocationExprent)value;

      if (invoc.getInvocationTyp() == InvocationExprent.INVOKE_DYNAMIC) {
        return invoc.getName().equals("typeSwitch");
      }
    }

    return false;
  }
}
