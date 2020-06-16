// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionEdge;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionNode;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsGraph;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.FastSparseSetFactory.FastSparseSet;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.SFormsFastMapDirect;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class StackVarsProcessor {
  public void simplifyStackVars(RootStatement root, StructMethod mt, StructClass cl) {
    Set<Integer> setReorderedIfs = new HashSet<>();
    SSAUConstructorSparseEx ssau = null;

    while (true) {
      boolean found = false;
      boolean first = ssau == null;

      SSAConstructorSparseEx ssa = new SSAConstructorSparseEx();
      ssa.splitVariables(root, mt);

      SimplifyExprentsHelper sehelper = new SimplifyExprentsHelper(first);
      while (sehelper.simplifyStackVarsStatement(root, setReorderedIfs, ssa, cl)) {
        found = true;
      }

      setVersionsToNull(root);

      SequenceHelper.condenseSequences(root);

      ssau = new SSAUConstructorSparseEx();
      ssau.splitVariables(root, mt);

      if (first) {
        setEffectivelyFinalVars(root, ssau, new HashMap<>());
      }

      if (iterateStatements(root, ssau)) {
        found = true;
      }

      setVersionsToNull(root);

      if (!found) {
        break;
      }
    }

    // remove unused assignments
    ssau = new SSAUConstructorSparseEx();
    ssau.splitVariables(root, mt);

    iterateStatements(root, ssau);

    setVersionsToNull(root);
  }

  private static void setVersionsToNull(Statement stat) {
    if (stat.getExprents() == null) {
      for (Object obj : stat.getSequentialObjects()) {
        if (obj instanceof Statement) {
          setVersionsToNull((Statement)obj);
        }
        else if (obj instanceof Exprent) {
          setExprentVersionsToNull((Exprent)obj);
        }
      }
    }
    else {
      for (Exprent exprent : stat.getExprents()) {
        setExprentVersionsToNull(exprent);
      }
    }
  }

  private static void setExprentVersionsToNull(Exprent exprent) {
    List<Exprent> lst = exprent.getAllExprents(true);
    lst.add(exprent);

    for (Exprent expr : lst) {
      if (expr.type == Exprent.EXPRENT_VAR) {
        ((VarExprent)expr).setVersion(0);
      }
    }
  }

  private boolean iterateStatements(RootStatement root, SSAUConstructorSparseEx ssa) {
    FlattenStatementsHelper flatthelper = new FlattenStatementsHelper();
    DirectGraph dgraph = flatthelper.buildDirectGraph(root);

    boolean res = false;

    Set<DirectNode> setVisited = new HashSet<>();
    LinkedList<DirectNode> stack = new LinkedList<>();
    LinkedList<Map<VarVersionPair, Exprent>> stackMaps = new LinkedList<>();

    stack.add(dgraph.first);
    stackMaps.add(new HashMap<>());

    while (!stack.isEmpty()) {
      DirectNode nd = stack.removeFirst();
      Map<VarVersionPair, Exprent> mapVarValues = stackMaps.removeFirst();

      if (setVisited.contains(nd)) {
        continue;
      }
      setVisited.add(nd);

      List<List<Exprent>> lstLists = new ArrayList<>();

      if (!nd.exprents.isEmpty()) {
        lstLists.add(nd.exprents);
      }

      if (nd.succs.size() == 1) {
        DirectNode ndsucc = nd.succs.get(0);
        if (ndsucc.type == DirectNode.NODE_TAIL && !ndsucc.exprents.isEmpty()) {
          lstLists.add(nd.succs.get(0).exprents);
          nd = ndsucc;
        }
      }

      for (int i = 0; i < lstLists.size(); i++) {
        List<Exprent> lst = lstLists.get(i);

        int index = 0;
        while (index < lst.size()) {
          Exprent next = null;
          if (index == lst.size() - 1) {
            if (i < lstLists.size() - 1) {
              next = lstLists.get(i + 1).get(0);
            }
          }
          else {
            next = lst.get(index + 1);
          }

          int[] ret = iterateExprent(lst, index, next, mapVarValues, ssa);
          if (ret[0] >= 0) {
            index = ret[0];
          }
          else {
            index++;
          }
          res |= (ret[1] == 1);
        }
      }

      for (DirectNode ndx : nd.succs) {
        stack.add(ndx);
        stackMaps.add(new HashMap<>(mapVarValues));
      }

      // make sure the 3 special exprent lists in a loop (init, condition, increment) are not empty
      // change loop type if necessary
      if (nd.exprents.isEmpty() &&
          (nd.type == DirectNode.NODE_INIT || nd.type == DirectNode.NODE_CONDITION || nd.type == DirectNode.NODE_INCREMENT)) {
        nd.exprents.add(null);

        if (nd.statement.type == Statement.TYPE_DO) {
          DoStatement loop = (DoStatement)nd.statement;

          if (loop.getLooptype() == DoStatement.LOOP_FOR &&
              loop.getInitExprent() == null &&
              loop.getIncExprent() == null) { // "downgrade" loop to 'while'
            loop.setLooptype(DoStatement.LOOP_WHILE);
          }
        }
      }
    }

    return res;
  }

  private static Exprent isReplaceableVar(Exprent exprent, Map<VarVersionPair, Exprent> mapVarValues) {
    Exprent dest = null;
    if (exprent.type == Exprent.EXPRENT_VAR) {
      VarExprent var = (VarExprent)exprent;
      dest = mapVarValues.get(new VarVersionPair(var));
    }
    return dest;
  }

  private static void replaceSingleVar(Exprent parent, VarExprent var, Exprent dest, SSAUConstructorSparseEx ssau) {
    parent.replaceExprent(var, dest);

    // live sets
    SFormsFastMapDirect livemap = ssau.getLiveVarVersionsMap(new VarVersionPair(var));
    Set<VarVersionPair> setVars = getAllVersions(dest);

    for (VarVersionPair varpaar : setVars) {
      VarVersionNode node = ssau.getSsuversions().nodes.getWithKey(varpaar);

      for (Iterator<Entry<Integer, FastSparseSet<Integer>>> itent = node.live.entryList().iterator(); itent.hasNext(); ) {
        Entry<Integer, FastSparseSet<Integer>> ent = itent.next();

        Integer key = ent.getKey();

        if (!livemap.containsKey(key)) {
          itent.remove();
        }
        else {
          FastSparseSet<Integer> set = ent.getValue();

          set.complement(livemap.get(key));
          if (set.isEmpty()) {
            itent.remove();
          }
        }
      }
    }
  }

  private int[] iterateExprent(List<Exprent> lstExprents,
                               int index,
                               Exprent next,
                               Map<VarVersionPair, Exprent> mapVarValues,
                               SSAUConstructorSparseEx ssau) {
    Exprent exprent = lstExprents.get(index);

    int changed = 0;

    for (Exprent expr : exprent.getAllExprents()) {
      while (true) {
        Object[] arr = iterateChildExprent(expr, exprent, next, mapVarValues, ssau);
        Exprent retexpr = (Exprent)arr[0];
        changed |= (Boolean)arr[1] ? 1 : 0;

        boolean isReplaceable = (Boolean)arr[2];
        if (retexpr != null) {
          if (isReplaceable) {
            replaceSingleVar(exprent, (VarExprent)expr, retexpr, ssau);
            expr = retexpr;
          }
          else {
            exprent.replaceExprent(expr, retexpr);
          }
          changed = 1;
        }

        if (!isReplaceable) {
          break;
        }
      }
    }

    // no var on the highest level, so no replacing

    VarExprent left = null;
    Exprent right = null;

    if (exprent.type == Exprent.EXPRENT_ASSIGNMENT) {
      AssignmentExprent as = (AssignmentExprent)exprent;
      if (as.getLeft().type == Exprent.EXPRENT_VAR) {
        left = (VarExprent)as.getLeft();
        right = as.getRight();
      }
    }

    if (left == null || left.isEffectivelyFinal()) {
      return new int[]{-1, changed};
    }

    VarVersionPair leftpaar = new VarVersionPair(left);

    List<VarVersionNode> usedVers = new ArrayList<>();
    boolean notdom = getUsedVersions(ssau, leftpaar, usedVers);

    if (!notdom && usedVers.isEmpty()) {
      if (left.isStack() && (right.type == Exprent.EXPRENT_INVOCATION ||
                             right.type == Exprent.EXPRENT_ASSIGNMENT || right.type == Exprent.EXPRENT_NEW)) {
        if (right.type == Exprent.EXPRENT_NEW) {
          // new Object(); permitted
          NewExprent nexpr = (NewExprent)right;
          if (nexpr.isAnonymous() || nexpr.getNewType().arrayDim > 0
              || nexpr.getNewType().type != CodeConstants.TYPE_OBJECT) {
            return new int[]{-1, changed};
          }
        }

        lstExprents.set(index, right);
        return new int[]{index + 1, 1};
      }
      else if (right.type == Exprent.EXPRENT_VAR) {
        lstExprents.remove(index);
        return new int[]{index, 1};
      }
      else {
        return new int[]{-1, changed};
      }
    }

    int useflags = right.getExprentUse();

    // stack variables only
    if (!left.isStack() &&
        (right.type != Exprent.EXPRENT_VAR || ((VarExprent)right).isStack())) { // special case catch(... ex)
      return new int[]{-1, changed};
    }

    if ((useflags & Exprent.MULTIPLE_USES) == 0 && (notdom || usedVers.size() > 1)) {
      return new int[]{-1, changed};
    }

    Map<Integer, Set<VarVersionPair>> mapVars = getAllVarVersions(leftpaar, right, ssau);

    boolean isSelfReference = mapVars.containsKey(leftpaar.var);
    if (isSelfReference && notdom) {
      return new int[]{-1, changed};
    }

    Set<VarVersionPair> setNextVars = next == null ? null : getAllVersions(next);

    // FIXME: fix the entire method!
    if (right.type != Exprent.EXPRENT_CONST &&
        right.type != Exprent.EXPRENT_VAR &&
        setNextVars != null &&
        mapVars.containsKey(leftpaar.var)) {
      for (VarVersionNode usedvar : usedVers) {
        if (!setNextVars.contains(new VarVersionPair(usedvar.var, usedvar.version))) {
          return new int[]{-1, changed};
        }
      }
    }

    mapVars.remove(leftpaar.var);

    boolean vernotreplaced = false;
    boolean verreplaced = false;

    Set<VarVersionPair> setTempUsedVers = new HashSet<>();

    for (VarVersionNode usedvar : usedVers) {
      VarVersionPair usedver = new VarVersionPair(usedvar.var, usedvar.version);
      if (isVersionToBeReplaced(usedver, mapVars, ssau, leftpaar) &&
          (right.type == Exprent.EXPRENT_CONST || right.type == Exprent.EXPRENT_VAR || right.type == Exprent.EXPRENT_FIELD
           || setNextVars == null || setNextVars.contains(usedver))) {

        setTempUsedVers.add(usedver);
        verreplaced = true;
      }
      else {
        vernotreplaced = true;
      }
    }

    if (isSelfReference && vernotreplaced) {
      return new int[]{-1, changed};
    }
    else {
      for (VarVersionPair usedver : setTempUsedVers) {
        Exprent copy = right.copy();
        if (right.type == Exprent.EXPRENT_FIELD && ssau.getMapFieldVars().containsKey(right.id)) {
          ssau.getMapFieldVars().put(copy.id, ssau.getMapFieldVars().get(right.id));
        }

        mapVarValues.put(usedver, copy);
      }
    }

    if (!notdom && !vernotreplaced) {
      // remove assignment
      lstExprents.remove(index);
      return new int[]{index, 1};
    }
    else if (verreplaced) {
      return new int[]{index + 1, changed};
    }
    else {
      return new int[]{-1, changed};
    }
  }

  private static Set<VarVersionPair> getAllVersions(Exprent exprent) {
    Set<VarVersionPair> res = new HashSet<>();

    List<Exprent> listTemp = new ArrayList<>(exprent.getAllExprents(true));
    listTemp.add(exprent);

    for (Exprent expr : listTemp) {
      if (expr.type == Exprent.EXPRENT_VAR) {
        VarExprent var = (VarExprent)expr;
        res.add(new VarVersionPair(var));
      }
    }

    return res;
  }

  private static Object[] iterateChildExprent(Exprent exprent,
                                              Exprent parent,
                                              Exprent next,
                                              Map<VarVersionPair, Exprent> mapVarValues,
                                              SSAUConstructorSparseEx ssau) {
    boolean changed = false;

    for (Exprent expr : exprent.getAllExprents()) {
      while (true) {
        Object[] arr = iterateChildExprent(expr, parent, next, mapVarValues, ssau);
        Exprent retexpr = (Exprent)arr[0];
        changed |= (Boolean)arr[1];

        boolean isReplaceable = (Boolean)arr[2];
        if (retexpr != null) {
          if (isReplaceable) {
            replaceSingleVar(exprent, (VarExprent)expr, retexpr, ssau);
            expr = retexpr;
          }
          else {
            exprent.replaceExprent(expr, retexpr);
          }
          changed = true;
        }

        if (!isReplaceable) {
          break;
        }
      }
    }

    Exprent dest = isReplaceableVar(exprent, mapVarValues);
    if (dest != null) {
      return new Object[]{dest, true, true};
    }


    VarExprent left = null;
    Exprent right = null;

    if (exprent.type == Exprent.EXPRENT_ASSIGNMENT) {
      AssignmentExprent as = (AssignmentExprent)exprent;
      if (as.getLeft().type == Exprent.EXPRENT_VAR) {
        left = (VarExprent)as.getLeft();
        right = as.getRight();
      }
    }

    if (left == null || left.isEffectivelyFinal()) {
      return new Object[]{null, changed, false};
    }

    boolean isHeadSynchronized = false;
    if (next == null && parent.type == Exprent.EXPRENT_MONITOR) {
      MonitorExprent monexpr = (MonitorExprent)parent;
      if (monexpr.getMonType() == MonitorExprent.MONITOR_ENTER && exprent.equals(monexpr.getValue())) {
        isHeadSynchronized = true;
      }
    }

    // stack variable or synchronized head exprent
    if (!left.isStack() && !isHeadSynchronized) {
      return new Object[]{null, changed, false};
    }

    VarVersionPair leftpaar = new VarVersionPair(left);

    List<VarVersionNode> usedVers = new ArrayList<>();
    boolean notdom = getUsedVersions(ssau, leftpaar, usedVers);

    if (!notdom && usedVers.isEmpty()) {
      return new Object[]{right, changed, false};
    }

    // stack variables only
    if (!left.isStack()) {
      return new Object[]{null, changed, false};
    }

    int useflags = right.getExprentUse();

    if ((useflags & Exprent.BOTH_FLAGS) != Exprent.BOTH_FLAGS) {
      return new Object[]{null, changed, false};
    }

    Map<Integer, Set<VarVersionPair>> mapVars = getAllVarVersions(leftpaar, right, ssau);
    if (mapVars.containsKey(leftpaar.var) && notdom) {
      return new Object[]{null, changed, false};
    }

    mapVars.remove(leftpaar.var);

    Set<VarVersionPair> setAllowedVars = getAllVersions(parent);
    if (next != null) {
      setAllowedVars.addAll(getAllVersions(next));
    }

    boolean vernotreplaced = false;

    Set<VarVersionPair> setTempUsedVers = new HashSet<>();

    for (VarVersionNode usedvar : usedVers) {
      VarVersionPair usedver = new VarVersionPair(usedvar.var, usedvar.version);
      if (isVersionToBeReplaced(usedver, mapVars, ssau, leftpaar) &&
          (right.type == Exprent.EXPRENT_VAR || setAllowedVars.contains(usedver))) {

        setTempUsedVers.add(usedver);
      }
      else {
        vernotreplaced = true;
      }
    }

    if (!notdom && !vernotreplaced) {
      for (VarVersionPair usedver : setTempUsedVers) {
        Exprent copy = right.copy();
        if (right.type == Exprent.EXPRENT_FIELD && ssau.getMapFieldVars().containsKey(right.id)) {
          ssau.getMapFieldVars().put(copy.id, ssau.getMapFieldVars().get(right.id));
        }

        mapVarValues.put(usedver, copy);
      }

      // remove assignment
      return new Object[]{right, changed, false};
    }

    return new Object[]{null, changed, false};
  }

  private static boolean getUsedVersions(SSAUConstructorSparseEx ssa, VarVersionPair var, List<? super VarVersionNode> res) {
    VarVersionsGraph ssuversions = ssa.getSsuversions();
    VarVersionNode varnode = ssuversions.nodes.getWithKey(var);

    Set<VarVersionNode> setVisited = new HashSet<>();
    Set<VarVersionNode> setNotDoms = new HashSet<>();

    LinkedList<VarVersionNode> stack = new LinkedList<>();
    stack.add(varnode);

    while (!stack.isEmpty()) {
      VarVersionNode nd = stack.remove(0);
      setVisited.add(nd);

      if (nd != varnode && (nd.flags & VarVersionNode.FLAG_PHANTOM_FINEXIT) == 0) {
        res.add(nd);
      }

      for (VarVersionEdge edge : nd.succs) {
        VarVersionNode succ = edge.dest;

        if (!setVisited.contains(edge.dest)) {

          boolean isDominated = true;
          for (VarVersionEdge prededge : succ.preds) {
            if (!setVisited.contains(prededge.source)) {
              isDominated = false;
              break;
            }
          }

          if (isDominated) {
            stack.add(succ);
          }
          else {
            setNotDoms.add(succ);
          }
        }
      }
    }

    setNotDoms.removeAll(setVisited);

    return !setNotDoms.isEmpty();
  }

  private static boolean isVersionToBeReplaced(VarVersionPair usedvar,
                                               Map<Integer, Set<VarVersionPair>> mapVars,
                                               SSAUConstructorSparseEx ssau,
                                               VarVersionPair leftpaar) {
    VarVersionsGraph ssuversions = ssau.getSsuversions();

    SFormsFastMapDirect mapLiveVars = ssau.getLiveVarVersionsMap(usedvar);
    if (mapLiveVars == null) {
      // dummy version, predecessor of a phi node
      return false;
    }

    // compare protected ranges
    if (!InterpreterUtil.equalObjects(ssau.getMapVersionFirstRange().get(leftpaar),
                                      ssau.getMapVersionFirstRange().get(usedvar))) {
      return false;
    }

    for (Entry<Integer, Set<VarVersionPair>> ent : mapVars.entrySet()) {
      FastSparseSet<Integer> liveverset = mapLiveVars.get(ent.getKey());
      if (liveverset == null || liveverset.isEmpty()) {
        return false;
      }

      Set<VarVersionNode> domset = new HashSet<>();
      for (VarVersionPair verpaar : ent.getValue()) {
        domset.add(ssuversions.nodes.getWithKey(verpaar));
      }

      boolean isdom = true;

      for (Integer livever : liveverset) {
        VarVersionNode node = ssuversions.nodes.getWithKey(new VarVersionPair(ent.getKey().intValue(), livever.intValue()));

        if (!ssuversions.isDominatorSet(node, domset)) {
          isdom = false;
          break;
        }
      }

      if (!isdom) {
        return false;
      }
    }

    return true;
  }

  private static Map<Integer, Set<VarVersionPair>> getAllVarVersions(VarVersionPair leftvar,
                                                                     Exprent exprent,
                                                                     SSAUConstructorSparseEx ssau) {
    Map<Integer, Set<VarVersionPair>> map = new HashMap<>();
    SFormsFastMapDirect mapLiveVars = ssau.getLiveVarVersionsMap(leftvar);

    List<Exprent> lst = exprent.getAllExprents(true);
    lst.add(exprent);

    for (Exprent expr : lst) {
      if (expr.type == Exprent.EXPRENT_VAR) {
        int varindex = ((VarExprent)expr).getIndex();
        if (leftvar.var != varindex) {
          if (mapLiveVars.containsKey(varindex)) {
            Set<VarVersionPair> verset = new HashSet<>();
            for (Integer vers : mapLiveVars.get(varindex)) {
              verset.add(new VarVersionPair(varindex, vers.intValue()));
            }
            map.put(varindex, verset);
          }
          else {
            throw new RuntimeException("inkonsistent live map!");
          }
        }
        else {
          map.put(varindex, null);
        }
      }
      else if (expr.type == Exprent.EXPRENT_FIELD) {
        if (ssau.getMapFieldVars().containsKey(expr.id)) {
          int varindex = ssau.getMapFieldVars().get(expr.id);
          if (mapLiveVars.containsKey(varindex)) {
            Set<VarVersionPair> verset = new HashSet<>();
            for (Integer vers : mapLiveVars.get(varindex)) {
              verset.add(new VarVersionPair(varindex, vers.intValue()));
            }
            map.put(varindex, verset);
          }
        }
      }
    }

    return map;
  }

  private static void setEffectivelyFinalVars(Statement stat, SSAUConstructorSparseEx ssau, Map<VarVersionPair, VarExprent> varLookupMap) {
    if (stat.getExprents() != null && !stat.getExprents().isEmpty()) {
      for (int i = 0; i < stat.getExprents().size(); ++i) {
        setEffectivelyFinalVars(stat.getExprents().get(i), ssau, i, stat.getExprents(), varLookupMap);
      }
    }

    for (Statement st : stat.getStats()) {
      setEffectivelyFinalVars(st, ssau, varLookupMap);
    }
  }

  private static void setEffectivelyFinalVars(Exprent exprent, SSAUConstructorSparseEx ssau, int index, List<Exprent> list, Map<VarVersionPair, VarExprent> varLookupMap) {
    if (exprent.type == Exprent.EXPRENT_ASSIGNMENT) {
      AssignmentExprent assign = (AssignmentExprent)exprent;
      if (assign.getLeft().type == Exprent.EXPRENT_VAR) {
        VarExprent var = (VarExprent)assign.getLeft();
        varLookupMap.put(var.getVarVersionPair(), var);
      }
    }
    else if (exprent.type == Exprent.EXPRENT_NEW) {
      NewExprent newExpr = (NewExprent)exprent;
      if (newExpr.isAnonymous()) {
        ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(newExpr.getNewType().value);

        if (node != null) {
          if (!newExpr.isLambda()) {
            for (StructMethod mt : node.classStruct.getMethods()) {
              if (mt.getName().equals(CodeConstants.INIT_NAME)) {
                List<VarType> paramTypes = Arrays.asList(MethodDescriptor.parseDescriptor(mt.getDescriptor()).params);

                for (int i = Math.max(0, index - paramTypes.size()); i < index; ++i) {
                  Exprent temp = list.get(i);
                  if (temp.type == Exprent.EXPRENT_ASSIGNMENT) {
                    Exprent left = ((AssignmentExprent)temp).getLeft();
                    if (left.type == Exprent.EXPRENT_VAR) {
                      VarExprent leftVar = (VarExprent)left;
                      if (leftVar.getLVT() != null && paramTypes.contains(leftVar.getLVT().getVarType())) {
                        leftVar.setEffectivelyFinal(true);
                      }
                    }
                  }
                }
                break;
              }
            }
          }
          else if (!newExpr.isMethodReference()) {
            MethodDescriptor mdLambda = MethodDescriptor.parseDescriptor(node.lambdaInformation.method_descriptor);
            MethodDescriptor mdContent = MethodDescriptor.parseDescriptor(node.lambdaInformation.content_method_descriptor);
            int paramOffset = node.lambdaInformation.is_content_method_static ? 0 : 1;
            int varsCount = mdContent.params.length - mdLambda.params.length;

            for (int i = 0; i < varsCount; ++i) {
              Exprent param = newExpr.getConstructor().getLstParameters().get(paramOffset + i);
              if (param.type == Exprent.EXPRENT_VAR) {
                VarExprent paramVar = (VarExprent)param;
                VarVersionPair vvp = paramVar.getVarVersionPair();
                VarVersionNode vvnode = ssau.getSsuversions().nodes.getWithKey(vvp);

                while (true) {
                  VarVersionNode next = null;
                  if (vvnode.var >= VarExprent.STACK_BASE) {
                    vvnode = vvnode.preds.iterator().next().source;
                    VarVersionPair nextVVP = ssau.getVarAssignmentMap().get(new VarVersionPair(vvnode.var, vvnode.version));
                    next = ssau.getSsuversions().nodes.getWithKey(nextVVP);

                    if (nextVVP != null && nextVVP.var < 0) { // TODO check if field is final?
                      vvp = nextVVP;
                      break;
                    }
                  }
                  else {
                    final int j = i;
                    final int varIndex = vvnode.var;
                    final int varVersion = vvnode.version;
                    List<VarVersionNode> roots = getRoots(vvnode);
                    List<VarVersionNode> allRoots = ssau.getSsuversions().nodes.stream()
                                                          .distinct()
                                                          .filter(n -> n.var == varIndex && n.preds.isEmpty())
                                                          .filter(n -> {
                                                            if (n.lvt != null) {
                                                              return mdContent.params[j].equals(new VarType(n.lvt.getDescriptor()));
                                                            }
                                                            return n.version > varVersion;
                                                          })
                                                          .collect(Collectors.toList());

                    if (roots.size() >= allRoots.size()) {
                      if (roots.size() == 1) {
                        vvnode = roots.get(0);
                        vvp = new VarVersionPair(vvnode.var, vvnode.version);
                        VarVersionPair nextVVP = ssau.getVarAssignmentMap().get(vvp);
                        next = ssau.getSsuversions().nodes.getWithKey(nextVVP);
                        if (nextVVP != null && nextVVP.var < 0) {
                          vvp = nextVVP;
                          break;
                        }
                      }
                      else if (roots.size() == 2) {
                        VarVersionNode first = roots.get(0);
                        VarVersionNode second = roots.get(1);

                        // check for an if-else var definition
                        if (first.lvt != null && second.lvt != null && first.lvt.getVersion().equals(second.lvt.getVersion())) {
                          vvp = first.lvt.getVersion();
                          break;
                        }
                      }
                    }
                  }

                  if (next == null) {
                    break;
                  }
                  vvnode = next;
                }

                VarExprent var = varLookupMap.get(vvp);
                if (var != null) {
                  var.setEffectivelyFinal(true);
                }
              }
            }
          }
        }
      }
    }

    for (Exprent ex : exprent.getAllExprents()) {
      setEffectivelyFinalVars(ex, ssau, index, list, varLookupMap);
    }
  }

  private static List<VarVersionNode> getRoots(VarVersionNode vvnode) {
    List<VarVersionNode> ret = new ArrayList<>();
    Set<VarVersionNode> visited = new HashSet<>();
    LinkedList<VarVersionNode> queue = new LinkedList<>();

    queue.add(vvnode);
    visited.add(vvnode);

    while (!queue.isEmpty()) {
      VarVersionNode next = queue.removeFirst();

      if (next.preds.isEmpty()) {
        ret.add(next);
      }
      else {
        next.preds.forEach(vvn -> {
          if (visited.add(vvn.source)) {
            queue.add(vvn.source);
          }
        });
      }
    }

    return ret;
  }
}