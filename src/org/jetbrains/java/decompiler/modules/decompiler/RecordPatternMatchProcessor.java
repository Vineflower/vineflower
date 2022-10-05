package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.PatternExprent.RecordPatternExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.PatternExprent.TypePatternExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RecordPatternMatchProcessor {
  
  public static boolean matchRecordPattern(RootStatement root){
    boolean res = matchRecordPatternRec(root, root);
  
    if (res) {
      SequenceHelper.condenseSequences(root);
    }
  
    return res;
  }
  
  private static boolean matchRecordPatternRec(Statement statement, RootStatement root) {
    boolean res = false;
    for (Statement stat : statement.getStats()) {
      if (matchRecordPatternRec(stat, root)) {
        res = true;
      }
    }
    
    if (statement instanceof IfStatement) {
      res |= handleIf((IfStatement) statement, root);
    }
    
    // TODO!! guarded record pattern switches
    if (statement instanceof SwitchStatement) {
      if (((SwitchStatement)statement).isPattern()) {
        res |= handleSwitch((SwitchStatement)statement, root);
      }
    }
    
    return res;
  }
  
  private static boolean handleIf(IfStatement st, RootStatement root){
    /*
      follows the pattern:
      
      if(o instanceof SomeRecord){
        {
          C varN = $proxy$x((SomeRecord)o);
          T x = patternExtractor(varN);
        } for all record components
        ...
      }
      
      where patternExtractor(...) is actually either a no-op (var patterns) and earlier steps collapse it,
      a no-op `Integer.valueOf` or equivalent for primitives,
      an instanceof pattern for any other nested pattern
     */
    Exprent condition = st.getHeadexprent().getCondition();
    if(condition instanceof FunctionExprent){
      FunctionExprent func = (FunctionExprent) condition;
      if (func.getFuncType() == FunctionExprent.FunctionType.NE
        && func.getLstOperands().get(0) instanceof FunctionExprent) {
        func = (FunctionExprent) func.getLstOperands().get(0);
      }
      if (func.getLstOperands().size() == 2 && func.getFuncType() == FunctionExprent.FunctionType.INSTANCEOF) {
        Exprent source = func.getLstOperands().get(0);
        Exprent target = func.getLstOperands().get(1);
  
        var pattern = matchRecordPattern(st.getIfstat(), source, target.getExprType());
        if(pattern != null){
          st.getHeadexprent().setCondition(new FunctionExprent(FunctionExprent.FunctionType.INSTANCEOF, List.of(source, pattern), null));
          return true;
        }
      }
    }
    return false;
  }
  
  private static boolean handleSwitch(SwitchStatement st, RootStatement root){
    // already a pattern matching switch, we're just possibly enhancing it
    List<List<Exprent>> values = st.getCaseValues();
    for(int i = 0; i < values.size(); i++){
      List<Exprent> value = values.get(i);
      if(value.size() == 1 && value.get(0) instanceof TypePatternExprent){
        TypePatternExprent patternCase = (TypePatternExprent)value.get(0);
        var caseVar = patternCase.getVar();
        if(isRecordClass(caseVar.getExprType())){
          var pattern = matchRecordPattern(st.getCaseStatements().get(i), caseVar, caseVar.getExprType());
          if (pattern != null) {
            value.set(0, pattern);
            return true;
          }
        }
      }
    }
    return false;
  }
  
  private static RecordPatternExprent matchRecordPattern(Statement head, Exprent source, VarType targetType){
    if (isRecordClass(targetType)) {
      List<VarExprent> proxyVars = new ArrayList<>();
      List<PatternExprent> components = new ArrayList<>();
      // keep looking for possible components
      Statement current = head.getBasichead();
      // follow control flow through pattern ifs
      while (true) {
        var proc = processStat(current, components, proxyVars, source);
        current = proc.a;
        if (!proc.b)
          break;
      }
      checkLastProxyVar(components, proxyVars);
      return new RecordPatternExprent(targetType, components, null);
    }
    return null;
  }
  
  private static Pair<Statement, Boolean> processStat(Statement current, List<PatternExprent> collected, List<VarExprent> proxyVars, Exprent recordExpr) {
    if (current instanceof BasicBlockStatement) {
      List<Exprent> toRemove = new ArrayList<>();
      for (Exprent exprent : current.getExprents()) {
        if (exprent instanceof AssignmentExprent) {
          var assignment = (AssignmentExprent)exprent;
          if (isProxyAssignment(assignment, recordExpr)) {
            checkLastProxyVar(collected, proxyVars);
            proxyVars.add((VarExprent)assignment.getLeft());
            toRemove.add(assignment);
          } else {
            var primType = isPrimitiveValueOfAssignment(assignment, proxyVars);
            if (primType != null) {
              collected.add(new TypePatternExprent(primType, (VarExprent)assignment.getLeft()));
              toRemove.add(assignment);
            }
          }
        }
      }
      current.getExprents().removeAll(toRemove);
    } else {
      return Pair.of(current.getBasichead(), true);
    }
    
    var edges = current.getSuccessorEdges(StatEdge.TYPE_REGULAR);
    if (edges.size() == 1)
      return Pair.of(edges.get(0).getDestination(), true);
    return Pair.of(current, false);
  }
  
  private static void checkLastProxyVar(List<PatternExprent> collected, List<VarExprent> proxyVars){
    // if we didn't add the last proxy var through another pattern, add it here
    if (proxyVars.size() > 0 && collected.size() < proxyVars.size()) {
      var lastProxy = proxyVars.get(proxyVars.size() - 1);
      collected.add(new TypePatternExprent(lastProxy.getVarType(), lastProxy));
    }
  }
  
  private static boolean isRecordClass(VarType type) {
    StructClass target = DecompilerContext.getStructContext().getClass(type.value);
    return target != null && target.getRecordComponents() != null;
  }
  
  private static boolean isProxyAssignment(AssignmentExprent assignment, Exprent param) {
    // TODO: better heuristics?
    // - check method contents?
    Exprent right = assignment.getRight();
    if (right instanceof InvocationExprent) {
      var inv = (InvocationExprent)right;
      if (inv.getLstParameters().size() == 1) {
        var first = inv.getLstParameters().get(0);
        if(isSameOrCast(first, param))
          return inv.getName().startsWith("$proxy$");
      }
    }
    return false;
  }
  
  private static VarType isPrimitiveValueOfAssignment(AssignmentExprent assignment, List<VarExprent> proxyVars) {
    for(Map.Entry<VarType, VarType> entry : VarType.UNBOXING_TYPES.entrySet()){
      VarType prim = entry.getValue();
      VarType box = entry.getKey();
      
      if (assignment.getLeft().getExprType().equals(prim)) {
        if (assignment.getRight() instanceof InvocationExprent) {
          InvocationExprent invoke = (InvocationExprent)assignment.getRight();
          if (invoke.isUnboxingCall()) {
            Exprent inner = invoke.getInstance();
            if (inner instanceof InvocationExprent) {
              InvocationExprent innerInvoke = (InvocationExprent)inner;
              if (innerInvoke.getName().equals("valueOf") && innerInvoke.getClassname().equals(box.value))
                return prim;
            }
          }
        }
      }
    }
    return null;
  }
  
  private static boolean isSameOrCast(Exprent expr, Exprent original) {
    if (expr.equals(original))
      return true;
    if (expr instanceof FunctionExprent && ((FunctionExprent)expr).getFuncType() == FunctionExprent.FunctionType.CAST)
      return ((FunctionExprent)expr).getLstOperands().get(0).equals(original);
    return false;
  }
}