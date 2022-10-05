package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.exps.PatternExprent.RecordPatternExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.PatternExprent.TypePatternExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.util.Pair;

import java.util.List;

public final class RecordPatternNestProcessor {
  
  public static boolean nestRecordPatterns(RootStatement root){
    boolean res = nestRecordPatternsRec(root, root);
    
    if (res) {
      SequenceHelper.condenseSequences(root);
    }
    
    return res;
  }
  
  private static boolean nestRecordPatternsRec(Statement statement, RootStatement root) {
    boolean res = false;
    for (Statement stat : statement.getStats()) {
      if (nestRecordPatternsRec(stat, root)) {
        res = true;
      }
    }
    
    if (statement instanceof IfStatement) {
      res |= handleIf((IfStatement) statement, root);
    }
    
    return res;
  }
  
  private static boolean handleIf(IfStatement st, RootStatement root){
    Exprent condition = st.getHeadexprent().getCondition();
    List<Exprent> exprs = condition.getAllExprents(true, true);
    for(Exprent expr : exprs){
      if (expr instanceof FunctionExprent) {
        FunctionExprent func = (FunctionExprent)expr;
        if (func.getFuncType() == FunctionType.BOOLEAN_AND) {
          var ops = func.getLstOperands();
          if (ops.get(0) instanceof FunctionExprent
            && ops.get(1) instanceof FunctionExprent) {
            FunctionExprent left = unwrapNe((FunctionExprent)ops.get(0));
            Pair<FunctionExprent, FunctionExprent> rightP = unwrapChain((FunctionExprent)ops.get(1));
            var right = unwrapNe(rightP.a);
            if (left.getFuncType() == FunctionType.INSTANCEOF
              && left.getLstOperands().get(1) instanceof RecordPatternExprent
              && right.getFuncType() == FunctionType.INSTANCEOF
              && right.getLstOperands().get(1) instanceof PatternExprent) {
              RecordPatternExprent rec = (RecordPatternExprent)left.getLstOperands().get(1);
              Exprent common = right.getLstOperands().get(0);
              if (common instanceof VarExprent) {
                var rightPattern = (PatternExprent)right.getLstOperands().get(1);
                if (rightPattern instanceof RecordPatternExprent) {
                  ((RecordPatternExprent)rightPattern).setName((VarExprent)common);
                }
                for (PatternExprent component : rec.getNestedPatterns()) {
                  if (component instanceof TypePatternExprent) {
                    if (common.equals(((TypePatternExprent)component).getVar())) {
                      rec.replacePattern(component, rightPattern);
                      if (rightP.b.getFuncType() == FunctionType.BOOLEAN_AND)
                        func.replaceExprentRecursive(rightP.b, rightP.b.getLstOperands().get(1));
                      else {
                        func.replaceExprentRecursive(rightP.b, new ConstExprent(1, true, null));
                      }
                      // modification in the loop is fine if we return immediately
                      return true;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }
  
  private static FunctionExprent unwrapNe(FunctionExprent func) {
    if (func.getFuncType() == FunctionType.NE && func.getLstOperands().get(0) instanceof FunctionExprent)
      func = (FunctionExprent)func.getLstOperands().get(0);
    return func;
  }
  
  private static Pair<FunctionExprent, FunctionExprent> unwrapChain(FunctionExprent func) {
    var last = func;
    while (func.getFuncType() == FunctionType.BOOLEAN_AND && func.getLstOperands().get(0) instanceof FunctionExprent){
      last = func;
      func = (FunctionExprent)func.getLstOperands().get(0);
    }
    return Pair.of(func, last);
  }
}