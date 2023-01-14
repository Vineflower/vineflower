package org.jetbrains.java.decompiler.modules.decompiler.sforms;

import org.jetbrains.java.decompiler.api.SFormsCreator;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.flow.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionNode;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.util.DotExporter;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory;
import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory.FastSparseSet;
import org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect;

import java.util.*;

import static org.jetbrains.java.decompiler.modules.decompiler.sforms.VarMapHolder.mergeMaps;

public abstract class SFormsConstructor implements SFormsCreator {

  @Deprecated(forRemoval = true)
  private final boolean trackFieldVars;
  @Deprecated(forRemoval = true)
  private final boolean trackDirectAssignments;


  // node id, var, version
  private final HashMap<String, SFormsFastMapDirect> inVarVersions = new HashMap<>();

  // node id, var, version (direct branch)
  private final HashMap<String, SFormsFastMapDirect> outVarVersions = new HashMap<>();

  // node id, var, version (negative branch)
  private final HashMap<String, SFormsFastMapDirect> outNegVarVersions = new HashMap<>();

  // node id, var, version
  private final HashMap<String, SFormsFastMapDirect> extraVarVersions = new HashMap<>();

  // node id, var, version
  private final HashMap<String, SFormsFastMapDirect> catchableVersions = new HashMap<>();

  // var, version
  private final HashMap<Integer, Integer> lastversion = new HashMap<>();

  // set factory
  FastSparseSetFactory<Integer> factory;


  private SFormsFastMapDirect currentCatchableMap = null;


  protected RootStatement root;
  private StructMethod mt;
  DirectGraph dgraph;

  public SFormsConstructor(
    boolean trackFieldVars,
    boolean trackDirectAssignments) {
    this.trackFieldVars = trackFieldVars;
    this.trackDirectAssignments = trackDirectAssignments;
  }

  public void splitVariables(RootStatement root, StructMethod mt) {
    this.root = root;
    this.mt = mt;

    FlattenStatementsHelper flatthelper = new FlattenStatementsHelper();
    DirectGraph dgraph = flatthelper.buildDirectGraph(root);
    this.dgraph = dgraph;
    ValidationHelper.validateDGraph(dgraph, root);
    ValidationHelper.validateAllVarVersionsAreNull(dgraph, root);

    // FIXME: this overrides the previous iteration
    DotExporter.toDotFile(dgraph, mt, "ssaSplitVariables");

    List<Integer> setInit = new ArrayList<>();
    for (int i = 0; i < 64; i++) {
      setInit.add(i);
    }
    this.factory = new FastSparseSetFactory<>(setInit);

    this.extraVarVersions.put(dgraph.first.id, this.createFirstMap());

    this.setCatchMaps(root, dgraph, flatthelper);

    int iteration = 1;
    Set<String> updated = new HashSet<>();
    do {
      // System.out.println("~~~~~~~~~~~~~ \r\n"+root.toJava());
      this.ssaStatements(dgraph, updated, false, mt, iteration++);
      // System.out.println("~~~~~~~~~~~~~ \r\n"+root.toJava());
    }
    while (!updated.isEmpty());
  }

  void ssaStatements(DirectGraph dgraph, Set<String> updated, boolean calcLiveVars, StructMethod mt, int iteration) {

    DotExporter.toDotFile(dgraph, mt, "ssaStatements_" + iteration, this.outVarVersions);

    for (DirectNode node : dgraph.nodes) {

      updated.remove(node.id);
      this.mergeInVarMaps(node, dgraph);

      SFormsFastMapDirect varmap = this.inVarVersions.get(node.id);
      VarMapHolder varmaps = VarMapHolder.ofNormal(varmap);
      this.currentCatchableMap = null;

      if (node.hasSuccessors(DirectEdgeType.EXCEPTION)) {
        this.currentCatchableMap = varmap.getCopy();
        this.currentCatchableMap.removeAllStacks(); // stack gets cleared when throwing
        this.currentCatchableMap.removeAllFields(); // fields gets invalidated when throwing
        this.catchableVersions.put(node.id, this.currentCatchableMap);
      }

      // Foreach init node: mark as assignment!
      if (node.type == DirectNodeType.FOREACH_VARDEF && node.exprents.get(0) instanceof VarExprent) {
        this.updateVarExprent((VarExprent) node.exprents.get(0), node.statement, varmaps.getNormal(), calcLiveVars);
      } else if (node.exprents != null) {
        for (Exprent expr : node.exprents) {
          varmaps.toNormal(); // make sure we are in normal form
          this.processExprent(expr, varmaps, node.statement, calcLiveVars);
        }
      }

      if (this.hasUpdated(node, varmaps)) {
        this.outVarVersions.put(node.id, varmaps.getIfTrue());
        if (dgraph.mapNegIfBranch.containsKey(node.id)) {
          this.outNegVarVersions.put(node.id, varmaps.getIfFalse());
        }

        // Don't update the node if it wasn't discovered normally, as that can lead to infinite recursion due to bad ordering!
        if (!dgraph.extraNodes.contains(node)) {
          for (DirectEdge nd : node.getSuccessors(DirectEdgeType.REGULAR)) {
            updated.add(nd.getDestination().id);
          }

          for (DirectEdge nd : node.getSuccessors(DirectEdgeType.EXCEPTION)) {
            updated.add(nd.getDestination().id);
          }
        }
      }
    }
  }

  // processes exprents, much like section 16.1. of the java language specifications
  // (Definite Assignment and Expressions).
  private void processExprent(Exprent expr, VarMapHolder varMaps, Statement stat, boolean calcLiveVars) {

    if (expr == null) {
      return;
    }

    varMaps.removeAllFields();

    // The var map data can't depend yet on the result of this expression.
    varMaps.assertIsNormal();

    switch (expr.type) {
      case IF: {
        // EXPRENT_IF is a wrapper for the head exprent of an if statement.
        // Therefore, the map needs to stay split, unlike with most other exprents.
        IfExprent ifexpr = (IfExprent) expr;
        this.processExprent(ifexpr.getCondition(), varMaps, stat, calcLiveVars);
        return;
      }
      case ASSIGNMENT: {
        // Assigning a local overrides all the readable versions of that node.

        AssignmentExprent assexpr = (AssignmentExprent) expr;

        Exprent dest = assexpr.getLeft();
        switch (dest.type) {
          case VAR: {
            final VarExprent destVar = (VarExprent) dest;

            if (assexpr.getCondType() != null) {
              this.processExprent(destVar, varMaps, stat, calcLiveVars);
              this.processExprent(assexpr.getRight(), varMaps, stat, calcLiveVars);

              // make sure we are in normal form (eg `x &= ...`)
              SFormsFastMapDirect varMap = varMaps.toNormal();

              varMap.setCurrentVar(this.getOrCreatePhantom(destVar.getVarVersionPair()));
            } else {
              this.processExprent(assexpr.getRight(), varMaps, stat, calcLiveVars);
              this.updateVarExprent(destVar, stat, varMaps.toNormal(), calcLiveVars);

              if (this.trackDirectAssignments) {
                switch (assexpr.getRight().type) {
                  case VAR: {
                    VarVersionPair rightpaar = ((VarExprent) assexpr.getRight()).getVarVersionPair();
                    this.markDirectAssignment(destVar.getVarVersionPair(), rightpaar);
                    break;
                  }
                  case FIELD: {
                    int index = this.getFieldIndex((FieldExprent) assexpr.getRight());
                    VarVersionPair rightpaar = new VarVersionPair(index, 0);
                    this.markDirectAssignment(destVar.getVarVersionPair(), rightpaar);
                    break;
                  }
                }
              }
            }

            return;
          }
          case FIELD: {
            this.processExprent(assexpr.getLeft(), varMaps, stat, calcLiveVars);
            varMaps.assertIsNormal(); // the left side of an assignment can't be a boolean expression
            this.processExprent(assexpr.getRight(), varMaps, stat, calcLiveVars);
            varMaps.toNormal();
            varMaps.getNormal().removeAllFields();
            // assignment to a field resets all fields. (could be more precise, but this is easier)
            return;
          }
          default: {
            this.processExprent(assexpr.getLeft(), varMaps, stat, calcLiveVars);
            varMaps.assertIsNormal(); // the left side of an assignment can't be a boolean expression
            this.processExprent(assexpr.getRight(), varMaps, stat, calcLiveVars);
            varMaps.toNormal();
            return;
          }
        }

      }
      case FUNCTION: {
        FunctionExprent func = (FunctionExprent) expr;
        switch (func.getFuncType()) {
          case TERNARY: {
            // `a ? b : c`
            // Java language spec: 16.1.5.
            this.processExprent(func.getLstOperands().get(0), varMaps, stat, calcLiveVars);

            VarMapHolder bVarMaps = VarMapHolder.ofNormal(varMaps.getIfTrue());
            this.processExprent(func.getLstOperands().get(1), bVarMaps, stat, calcLiveVars);

            // reuse the varMaps for the false branch.
            varMaps.setNormal(varMaps.getIfFalse());
            this.processExprent(func.getLstOperands().get(2), varMaps, stat, calcLiveVars);

            if (bVarMaps.isNormal() && varMaps.isNormal()) {
              varMaps.mergeNormal(bVarMaps.getNormal());
            } else if (!varMaps.isNormal()) {
              // b and c are boolean expression and at least c had an assignment.
              varMaps.mergeIfTrue(bVarMaps.getIfTrue());
              varMaps.mergeIfFalse(bVarMaps.getIfFalse());
            } else {
              // b and c are boolean expression and at b had an assignment.
              // avoid cloning the c varmap.
              bVarMaps.mergeIfTrue(varMaps.getNormal());
              bVarMaps.mergeIfFalse(varMaps.getNormal());

              varMaps.set(bVarMaps); // move over the maps.
            }

            return;
          }
          case BOOLEAN_AND: {
            // `a && b`
            // Java language spec: 16.1.2.
            this.processExprent(func.getLstOperands().get(0), varMaps, stat, calcLiveVars);

            varMaps.makeFullyMutable();
            SFormsFastMapDirect ifFalse = varMaps.getIfFalse();
            varMaps.setNormal(varMaps.getIfTrue());

            this.processExprent(func.getLstOperands().get(1), varMaps, stat, calcLiveVars);
            varMaps.mergeIfFalse(ifFalse);
            return;
          }
          case BOOLEAN_OR: {
            // `a || b`
            // Java language spec: 16.1.3.
            this.processExprent(func.getLstOperands().get(0), varMaps, stat, calcLiveVars);

            varMaps.makeFullyMutable();
            SFormsFastMapDirect ifTrue = varMaps.getIfTrue();
            varMaps.setNormal(varMaps.getIfFalse());

            this.processExprent(func.getLstOperands().get(1), varMaps, stat, calcLiveVars);
            varMaps.mergeIfTrue(ifTrue);
            return;
          }
          case BOOL_NOT: {
            // `!a`
            // Java language spec: 16.1.4.
            this.processExprent(func.getLstOperands().get(0), varMaps, stat, calcLiveVars);
            varMaps.swap();

            return;
          }
          case INSTANCEOF: {
            // `a instanceof B`
            // pattern matching instanceof creates a new variable when true.
            this.processExprent(func.getLstOperands().get(0), varMaps, stat, calcLiveVars);
            varMaps.toNormal();

            if (func.getLstOperands().size() == 3) {
              // pattern matching
              // `a instanceof B b`
              // pattern matching variables are explained in different parts of the spec,
              // but it comes down to the same ideas.
              varMaps.makeFullyMutable();

              VarExprent var = (VarExprent) func.getLstOperands().get(2);

              this.updateVarExprent(var, stat, varMaps.getIfTrue(), calcLiveVars);
            }

            return;
          }
          case IMM:
          case MMI:
          case IPP:
          case PPI: {
            // process the var/field/array access
            // Note that ++ and -- are both reads and writes.
            this.processExprent(func.getLstOperands().get(0), varMaps, stat, calcLiveVars);

            switch (func.getLstOperands().get(0).type) {
              case VAR: {
                VarExprent varExprent = (VarExprent) func.getLstOperands().get(0);

                VarVersionPair phantomPair = this.getOrCreatePhantom(varExprent.getVarVersionPair());

                // Can't have ++ or -- on a boolean expression.
                varMaps.getNormal().setCurrentVar(phantomPair);
                break;
              }
              case FIELD: {
                // assignment to a field resets all fields.
                // Can't have ++ or -- on a boolean expression.
                varMaps.getNormal().removeAllFields();
                break;
              }
            }
            return;
          }
        }
        break;
      }
      case FIELD: {
        FieldExprent field = (FieldExprent) expr;
        this.processExprent(field.getInstance(), varMaps, stat, calcLiveVars);
        this.fieldRead(field, varMaps.getNormal());
        return;
      }
      case VAR: {
        // a read of a variable.
        VarExprent varExprent = (VarExprent) expr;
        this.varRead(varMaps, stat, calcLiveVars, varExprent);
      }
    }

    for (Exprent ex : expr.getAllExprents()) {
      this.processExprent(ex, varMaps, stat, calcLiveVars);
      varMaps.toNormal();
    }

    if (this.trackFieldVars && makesFieldsDirty(expr)) {
      varMaps.getNormal().removeAllFields();
    }
  }

  abstract public VarVersionPair getOrCreatePhantom(VarVersionPair var);

  private void varRead(VarMapHolder varMaps, Statement stat, boolean calcLiveVars, VarExprent varExprent) {
    final SFormsFastMapDirect varmap = varMaps.getNormal();

    FastSparseSet<Integer> versions = varmap.get(varExprent);

    int cardinality = versions != null ? versions.getCardinality() : 0;
    switch (cardinality) {
      case 0: { // size == 0 (var has no discovered assignments yet)
        // TODO: shouldn't every path from the start of the method to a variable usage have an assignment?
        //   seems to trigger with enhanced switches
        this.updateVarExprent(varExprent, stat, varmap, calcLiveVars);
        ValidationHelper.assertTrue(false, "Variable read before assignment: " + varExprent);
        break;
      }
      case 1: { // size == 1 (var has only one discovered assignment)
        this.varReadSingleVersion(stat, calcLiveVars, varExprent, varmap, versions.iterator().next());
        break;
      }
      case 2:  // size > 1 (var has more than one assignment)
        this.varReadMultipleVersions(stat, calcLiveVars, varExprent, varmap, versions);
        break;
    }
  }

  abstract void varReadSingleVersion(
    Statement stat,
    boolean calcLiveVars,
    VarExprent varExprent,
    SFormsFastMapDirect varmap,
    int lastVersion);

  abstract void varReadMultipleVersions(
    Statement stat,
    boolean calcLiveVars,
    VarExprent varExprent,
    SFormsFastMapDirect varMap,
    FastSparseSet<Integer> versions);

  abstract void markDirectAssignment(VarVersionPair varVersionPair, VarVersionPair rightPair);


  private static boolean makesFieldsDirty(Exprent expr) {
    switch (expr.type) {
      case INVOCATION:
        return true;
      case NEW:
        if (((NewExprent) expr).getNewType().type == CodeConstants.TYPE_OBJECT) {
          return true;
        }
        break;
    }
    return false;
  }

  abstract void initVersion(VarExprent varExprent, Statement stat);

  // Declaration of a variable
  private void updateVarExprent(VarExprent varassign, Statement stat, SFormsFastMapDirect varmap, boolean calcLiveVars) {
    int varIndex = varassign.getIndex();

    this.initVersion(varassign, stat);

    this.onAssignment(varassign.getVarVersionPair(), varmap, calcLiveVars);

    this.setCurrentVar(varmap, varIndex, varassign.getVersion());

    // update catchables map for normal vars only
    if (this.currentCatchableMap != null && varIndex < VarExprent.STACK_BASE && varIndex >= 0) {

      if (this.currentCatchableMap.containsKey(varIndex)) {
        this.currentCatchableMap.get(varIndex).add(varassign.getVersion());
      } else {
        FastSparseSet<Integer> set = this.factory.createEmptySet();
        set.add(varassign.getVersion());
        varmap.put(varIndex, set);
      }
    }
  }

  // TODO: make calcLiveVars a field in SSAU
  protected void onAssignment(VarVersionPair varVersionPair, SFormsFastMapDirect varMap, boolean calcLiveVars) {

  }

  private void mergeInVarMaps(DirectNode node, DirectGraph dgraph) {

    SFormsFastMapDirect mapNew = new SFormsFastMapDirect(this.factory);

    for (DirectEdge pred : node.getPredecessors(DirectEdgeType.REGULAR)) {
      SFormsFastMapDirect mapOut = this.getFilteredOutMap(node, pred.getSource(), dgraph);
      if (mapNew.isEmpty()) {
        mapNew = mapOut.getCopy();
      } else {
        mergeMaps(mapNew, mapOut);
      }
    }

    for (DirectEdge pred : node.getPredecessors(DirectEdgeType.EXCEPTION)) {
      // TODO: interact with finally?
      SFormsFastMapDirect mapOut = this.catchableVersions.get(pred.getSource().id);
      if (mapOut != null) {
        if (mapNew.isEmpty()) {
          mapNew = mapOut.getCopy();
        } else {
          mergeMaps(mapNew, mapOut);
        }
      }
    }

    if (this.extraVarVersions.containsKey(node.id)) {
      SFormsFastMapDirect mapExtra = this.extraVarVersions.get(node.id);
      if (mapNew.isEmpty()) {
        mapNew = mapExtra.getCopy();
      } else {
        mergeMaps(mapNew, mapExtra);
      }
    }

    this.inVarVersions.put(node.id, mapNew);
  }

  private SFormsFastMapDirect getFilteredOutMap(DirectNode node, DirectNode pred, DirectGraph dgraph) {

    SFormsFastMapDirect mapNew = new SFormsFastMapDirect(this.factory);

    if (node.id.equals(dgraph.mapNegIfBranch.get(pred.id))) {
      if (this.outNegVarVersions.containsKey(pred.id)) {
        mapNew = this.outNegVarVersions.get(pred.id).getCopy();
      }
    } else if (this.outVarVersions.containsKey(pred.id)) {
      mapNew = this.outVarVersions.get(pred.id).getCopy();
    }

    // handle finally
    if (node.tryFinally != pred.tryFinally) {
      if (node.tryFinally != null &&
        node.tryFinally.type == DirectNodeType.FINALLY &&
        node.tryFinally.tryFinally == pred.tryFinally) {
        // we are entering a try, nothing to do here
      } else if (pred.type == DirectNodeType.FINALLY) {
        // we are entering the finally block
      } else {
        DirectNode finallyNode = pred.tryFinally;
        while (finallyNode != node.tryFinally) {
          ValidationHelper.notNull(finallyNode);
          if (finallyNode.type == DirectNodeType.FINALLY) {

            getAndApplyDiff(this.inVarVersions.get(finallyNode.statement.id + "_FINALLY"), this.outVarVersions.get(finallyNode.id), mapNew);

          }
          finallyNode = finallyNode.tryFinally;
        }
      }
    }

    return mapNew;
  }

  private static void getAndApplyDiff(SFormsFastMapDirect input, SFormsFastMapDirect output, SFormsFastMapDirect target) {
    if (input == null || output == null) {
      return;
    }

    for (Map.Entry<Integer, FastSparseSet<Integer>> entry : input.entryList()) {
      Integer key = entry.getKey();

      if (key >= VarExprent.STACK_BASE) {
        continue;
      }

      if (entry.getValue().isEmpty()) {
        continue;
      }

      Integer first = entry.getValue().iterator().next();
      if (output.containsKey(key)) {
        if (output.get(key).contains(first)) {
          // the input is still readable
          FastSparseSet<Integer> check = output.get(key).getCopy();
          check.complement(entry.getValue());
          if (check.isEmpty()) {
            // no writes happened, do nothing
          } else {
            // some writes happened, append the additional writes
            target.get(key).union(check);
          }
        } else {
          // the input is not readable anymore, only set the writes
          target.put(key, entry.getValue().getCopy());
        }
      }
    }

    for (Map.Entry<Integer, FastSparseSet<Integer>> entry : output.entryList()) {
      Integer key = entry.getKey();

      if (key >= VarExprent.STACK_BASE) {
        continue;
      }

      if (entry.getValue().isEmpty()) {
        continue;
      }

      if (input.containsKey(key) && !input.get(key).isEmpty()) {
        continue; // already handled
      }

      // set the writes in the output
      target.put(key, entry.getValue().getCopy());
    }
  }

  public static Statement getFirstProtectedRange(Statement stat) {
    while (true) {
      Statement parent = stat.getParent();

      if (parent == null) {
        break;
      }

      if (parent instanceof CatchAllStatement || parent instanceof CatchStatement) {
        if (parent.getFirst() == stat) {
          return parent;
        }
      } else if (parent instanceof SynchronizedStatement) {
        if (((SynchronizedStatement) parent).getBody() == stat) {
          return parent;
        }
      }

      stat = parent;
    }

    return null;
  }

  // TODO: these could instead be VarExprents / PatternExprents in the catch dnode
  private void setCatchMaps(Statement stat, DirectGraph dgraph, FlattenStatementsHelper flatthelper) {

    SFormsFastMapDirect map;

    switch (stat.type) {
      case CATCH_ALL:
      case TRY_CATCH:

        List<VarExprent> lstVars;
        if (stat instanceof CatchAllStatement) {
          lstVars = ((CatchAllStatement) stat).getVars();
        } else {
          lstVars = ((CatchStatement) stat).getVars();
        }

        for (int i = 1; i < stat.getStats().size(); i++) {
          int varindex = lstVars.get(i - 1).getIndex();
          map = new SFormsFastMapDirect(this.factory);

          this.initParameter(varindex, map, true);

          this.extraVarVersions.put(flatthelper.getDirectNode(stat.getStats().get(i)).id, map);

        }
    }

    for (Statement st : stat.getStats()) {
      this.setCatchMaps(st, dgraph, flatthelper);
    }
  }

  private SFormsFastMapDirect createFirstMap() {
    boolean hasThis = !this.mt.hasModifier(CodeConstants.ACC_STATIC);

    MethodDescriptor md = MethodDescriptor.parseDescriptor(this.mt.getDescriptor());

    int paramCount = md.params.length + (hasThis ? 1 : 0);

    SFormsFastMapDirect varMap = new SFormsFastMapDirect(this.factory);
    for (int varIndex = 0, i = 0; i < paramCount; i++) {
      this.initParameter(varIndex, varMap, false);

      if (hasThis) {
        if (i == 0) {
          varIndex++;
        } else {
          varIndex += md.params[i - 1].stackSize;
        }
      } else {
        varIndex += md.params[i].stackSize;
      }
    }

    return varMap;
  }

  abstract public void initParameter(int varIndex, SFormsFastMapDirect varMap, boolean isCatchVar);

  public static void makeReadEdge(VarVersionNode phiNode, VarVersionNode tempNode) {
    tempNode.successors.add(phiNode);
    phiNode.predecessors.add(tempNode);
  }


  static boolean mapsEqual(SFormsFastMapDirect map1, SFormsFastMapDirect map2) {

    if (map1 == null) {
      return map2 == null;
    } else if (map2 == null) {
      return false;
    }

    if (map1.size() != map2.size()) {
      return false;
    }

    for (Map.Entry<Integer, FastSparseSet<Integer>> ent2 : map2.entryList()) {
      if (!InterpreterUtil.equalObjects(map1.get(ent2.getKey()), ent2.getValue())) {
        return false;
      }
    }

    return true;
  }

  void fieldRead(FieldExprent field, SFormsFastMapDirect varmap) {
    // a read of a field variable.
    if (this.trackFieldVars) {
      int index = this.getFieldIndex(field);

      varmap.setCurrentVar(index, 1);
    }
  }

  @Deprecated
  void setCurrentVar(SFormsFastMapDirect varmap, int var, int vers) {
    FastSparseSet<Integer> set = this.factory.createEmptySet();
    set.add(vers);
    varmap.put(var, set);
  }

  boolean hasUpdated(DirectNode node, VarMapHolder varmaps) {
    return !mapsEqual(varmaps.getIfTrue(), this.outVarVersions.get(node.id))
      || (this.outNegVarVersions.containsKey(node.id) && !mapsEqual(varmaps.getIfFalse(), this.outNegVarVersions.get(node.id)));
  }

  protected abstract Integer getFieldIndex(FieldExprent field);

  protected int getNextFreeVersion(int var, Statement stat) {
    return this.lastversion.compute(var, (k, v) -> v == null ? 1 : v + 1);
  }
}
