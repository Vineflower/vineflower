package org.jetbrains.java.decompiler.modules.decompiler.sforms;

import org.jetbrains.java.decompiler.api.SFormsCreator;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.flow.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionNode;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionsGraph;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.util.*;
import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory;
import org.jetbrains.java.decompiler.util.collections.FastSparseSetFactory.FastSparseSet;
import org.jetbrains.java.decompiler.util.collections.SFormsFastMapDirect;
import org.jetbrains.java.decompiler.util.collections.VBStyleCollection;

import java.util.*;

import static org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent.Type.FIELD;
import static org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent.Type.VAR;
import static org.jetbrains.java.decompiler.modules.decompiler.sforms.VarMapHolder.mergeMaps;

public abstract class SFormsConstructor implements SFormsCreator {

  private final boolean incrementOnUsage;
  private final boolean simplePhi;
  private final boolean trackFieldVars;
  private final boolean trackPhantomPPNodes;
  private final boolean trackSsuVersions;
  private final boolean doLiveVariableAnalysisRound;
  private final boolean trackDirectAssignments;
  @Deprecated
  private final boolean ssau;


  // node id, var, version
  final HashMap<String, SFormsFastMapDirect> inVarVersions = new HashMap<>();

  // node id, var, version (direct branch)
  final HashMap<String, SFormsFastMapDirect> outVarVersions = new HashMap<>();

  // node id, var, version (negative branch)
  final HashMap<String, SFormsFastMapDirect> outNegVarVersions = new HashMap<>();

  // node id, var, version
  final HashMap<String, SFormsFastMapDirect> extraVarVersions = new HashMap<>();

  // node id, var, version
  final HashMap<String, SFormsFastMapDirect> catchableVersions = new HashMap<>();

  // var, version
  final HashMap<Integer, Integer> lastversion = new HashMap<>();

  // set factory
  FastSparseSetFactory<Integer> factory;


  // (var, version), version
  private final HashMap<VarVersionPair, FastSparseSet<Integer>> phi;

  // version, protected ranges (catch, finally)
  private final Map<VarVersionPair, Integer> mapVersionFirstRange;

  // versions memory dependencies
  private final VarVersionsGraph ssuversions;

  // field access vars (exprent id, var id)
  private final Map<Integer, Integer> mapFieldVars;

  // field access counter
  private int fieldvarcounter = -1;

  // track assignments for finding effectively final vars (left var, right var)
  private final HashMap<VarVersionPair, VarVersionPair> varAssignmentMap;

  private SFormsFastMapDirect currentCatchableMap = null;


  private RootStatement root;
  private StructMethod mt;
  private DirectGraph dgraph;

  public SFormsConstructor(
    boolean incrementOnUsage,
    boolean simplePhi,
    boolean trackFieldVars,
    boolean trackPhantomPPNodes,
    boolean trackSsuVersions,
    boolean doLiveVariableAnalysisRound,
    boolean trackDirectAssignments,
    boolean ssau) {
    this.incrementOnUsage = incrementOnUsage;
    this.simplePhi = simplePhi;
    this.trackFieldVars = trackFieldVars;
    this.trackPhantomPPNodes = trackPhantomPPNodes;
    this.trackSsuVersions = trackSsuVersions;
    this.doLiveVariableAnalysisRound = doLiveVariableAnalysisRound;
    this.trackDirectAssignments = trackDirectAssignments;
    this.ssau = ssau;


    this.phi = simplePhi ? new HashMap<>() : null;
    this.mapVersionFirstRange = ssau ? new HashMap<>() : null;
    this.ssuversions = trackSsuVersions ? new VarVersionsGraph() : null;
    this.mapFieldVars = trackFieldVars ? new HashMap<>() : null;
    this.varAssignmentMap = trackDirectAssignments ? new HashMap<>() : null;


    ValidationHelper.assertTrue(
      !this.doLiveVariableAnalysisRound || this.trackSsuVersions,
      "doLiveVariableAnalysisRound -> trackSsuVersions: We need ssu versions to do live variable analysis");
    ValidationHelper.assertTrue(
      !this.incrementOnUsage || this.trackSsuVersions,
      "incrementOnUsage -> trackSsuVersions: We need ssu versions to be able to increment on usage");
    ValidationHelper.assertTrue(
      this.incrementOnUsage || this.simplePhi,
      "!incrementOnUsage -> simplePhi: We need to know if when nodes are already a phi node or not.");
    ValidationHelper.assertTrue(
      !this.trackDirectAssignments || this.trackFieldVars,
      "trackDirectAssignments -> trackFieldVars: We need to know if when nodes are already a phi node or not.");
  }

  public void splitVariables(RootStatement root, StructMethod mt) {
    this.root = root;
    this.mt = mt;

    FlattenStatementsHelper flatthelper = new FlattenStatementsHelper();
    DirectGraph dgraph = flatthelper.buildDirectGraph(root);
    this.dgraph = dgraph;
    ValidationHelper.validateDGraph(dgraph, root);
    ValidationHelper.validateAllVarVersionsAreNull(dgraph, root);

    DotExporter.toDotFile(dgraph, mt, "ssaSplitVariables");

    List<Integer> setInit = new ArrayList<>();
    for (int i = 0; i < 64; i++) {
      setInit.add(i);
    }
    this.factory = new FastSparseSetFactory<>(setInit);

    this.extraVarVersions.put(dgraph.first.id, this.createFirstMap());

    this.setCatchMaps(root, dgraph, flatthelper);

    int iteration = 1;
    HashSet<String> updated = new HashSet<>();
    do {
      // System.out.println("~~~~~~~~~~~~~ \r\n"+root.toJava());
      this.ssaStatements(dgraph, updated, false, mt, iteration++);
      // System.out.println("~~~~~~~~~~~~~ \r\n"+root.toJava());
    }
    while (!updated.isEmpty());

    if (this.doLiveVariableAnalysisRound) {
      this.ssaStatements(dgraph, updated, true, mt, iteration);

      this.ssuversions.initDominators();
    }

    if (this.trackSsuVersions && this.trackDirectAssignments) {
      // Validation testing
      ValidationHelper.validateVarVersionsGraph(this.ssuversions, root, this.varAssignmentMap);
    }
  }

  private void ssaStatements(DirectGraph dgraph, HashSet<String> updated, boolean calcLiveVars, StructMethod mt, int iteration) {

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

              if (this.trackPhantomPPNodes) {
                VarVersionNode varNode = this.ssuversions.nodes.getWithKey(destVar.getVarVersionPair());
                VarVersionNode phantomNode = this.getOrCreatePhantom(varNode);

                varMap.setCurrentVar(phantomNode);
              } else {
                varMap.setCurrentVar(destVar);
              }
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

            if (func.getLstOperands().get(0).type == VAR && this.trackPhantomPPNodes) {
              VarExprent varExprent = (VarExprent) func.getLstOperands().get(0);

              VarVersionNode varNode = this.ssuversions.nodes.getWithKey(varExprent.getVarVersionPair());
              VarVersionNode phantomNode = this.getOrCreatePhantom(varNode);

              varMaps.getNormal().setCurrentVar(phantomNode);
              return;
            }
            // Can't have ++ or -- on a boolean expression.
            SFormsFastMapDirect varmap = varMaps.getNormal();

            if (func.getLstOperands().get(0).type == FIELD) {
              // assignment to a field resets all fields.
              varmap.removeAllFields();
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

  // SSAU ONLY
  private VarVersionNode getOrCreatePhantom(VarVersionNode varNode) {
    ValidationHelper.assertTrue(this.trackPhantomPPNodes, "SSAU only");
    if (varNode.phantomNode == null) {
      VarVersionNode phantomNode = this.createNewReadNode(varNode.var, null, VarVersionNode.State.PHANTOM);
      varNode.phantomNode = phantomNode;
      phantomNode.phantomParentNode = varNode;
      return phantomNode;
    }

    ValidationHelper.assertTrue(
      varNode.phantomNode.state == VarVersionNode.State.PHANTOM,
      "Expected phantom node to be PHANTOM");

    return varNode.phantomNode;
  }

  // SSAU ONLY
  private VarVersionNode createRead(VarVersionNode node, Statement stat) {
    ValidationHelper.assertTrue(this.incrementOnUsage, "SSAU only");
    VarVersionNode read = this.createNewReadNode(node.var, stat);
    makeReadEdge(read, node);
    return read;
  }

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

  private void varReadSingleVersion(Statement stat, boolean calcLiveVars, VarExprent varExprent, SFormsFastMapDirect varmap, int lastVersion) {
    int varIndex = varExprent.getIndex();
    int currentVersion = varExprent.getVersion();
    if (!this.incrementOnUsage) {
      // simply copy the version
      varExprent.setVersion(lastVersion);
      return;
    }

    if (currentVersion == 0) {
      // first time processing this exprent


      // ssu graph
      VarVersionNode previousNode = this.ssuversions.nodes.getWithKey(new VarVersionPair(varIndex, lastVersion));
      VarVersionNode useNode = this.createRead(previousNode, stat);

      // set version
      varExprent.setVersion(useNode.version);
    } else {
      if (calcLiveVars) {
        this.varMapToGraph(new VarVersionPair(varIndex, currentVersion), varmap);
      }
    }
    varmap.setCurrentVar(varExprent); // update the current var to the usage version
  }

  private void varReadMultipleVersions(
    Statement stat,
    boolean calcLiveVars,
    VarExprent varExprent,
    SFormsFastMapDirect varmap,
    FastSparseSet<Integer> versions) {
    int varIndex = varExprent.getIndex();
    int currentVersion = varExprent.getVersion();
    if (!this.incrementOnUsage) {
      VarVersionPair varVersion = new VarVersionPair(varIndex, currentVersion);
      if (currentVersion != 0 && this.phi.containsKey(varVersion)) {
        // keep phi node up to date of all inputs
        this.phi.get(varVersion).union(versions);
      } else {
        // increase version
        int nextVer = this.getNextFreeVersion(varIndex, stat);
        // set version
        varExprent.setVersion(nextVer);

        // create new phi node
        this.phi.put(new VarVersionPair(varIndex, nextVer), versions);
      }

      varmap.setCurrentVar(varExprent); // update varmap to the phi version
    } else {
      if (currentVersion == 0) { // first time processing this exprent
        // split version
        int useVersion = this.getNextFreeVersion(varIndex, stat);
        // set version
        varExprent.setVersion(useVersion);

        // ssu node
        this.ssuversions.createNode(new VarVersionPair(varIndex, useVersion)).state = VarVersionNode.State.PHI;

        currentVersion = useVersion;
      } else {
        if (calcLiveVars) {
          this.varMapToGraph(new VarVersionPair(varIndex, currentVersion), varmap);
        }
      }

      varmap.setCurrentVar(varExprent); // update varmap to the usage version
      this.createOrUpdatePhiNode(new VarVersionPair(varIndex, currentVersion), versions, stat);
    }
  }


  private Integer getFieldIndex(FieldExprent field) {
    if (this.trackFieldVars) {
      if (this.mapFieldVars.containsKey(field.id)) {
        return this.mapFieldVars.get(field.id);
      } else {
        int index = this.fieldvarcounter--;
        this.mapFieldVars.put(field.id, index);

        // ssu graph
        if (this.trackSsuVersions) {
          this.ssuversions.createNode(new VarVersionPair(index, 1));
        }
        return index;
      }
    } else {
      return -1;
    }
  }

  private void markDirectAssignment(VarVersionPair varVersionPair, VarVersionPair rightPair) {
    if (this.trackDirectAssignments) {
      this.varAssignmentMap.put(varVersionPair, rightPair);
    }
  }


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

  private void initVersion(VarExprent varExprent, Statement stat) {
    int varIndex = varExprent.getIndex();

    if (varExprent.getVersion() == 0) {
      // get next version
      int nextVersion = this.getNextFreeVersion(varIndex, stat);

      // set version
      varExprent.setVersion(nextVersion);

      if (this.trackSsuVersions) {
        // ssu graph
        this.ssuversions.createNode(new VarVersionPair(varIndex, nextVersion), varExprent.getLVT()).state = VarVersionNode.State.WRITE;
      }
    }
  }

  // Declaration of a variable
  private void updateVarExprent(VarExprent varassign, Statement stat, SFormsFastMapDirect varmap, boolean calcLiveVars) {
    int varIndex = varassign.getIndex();

    this.initVersion(varassign, stat);

    if (calcLiveVars) {
      this.varMapToGraph(new VarVersionPair(varIndex, varassign.getVersion()), varmap);
    }

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

  private int getNextFreeVersion(int var, Statement stat) {
    final int nextVersion = this.lastversion.compute(var, (k, v) -> v == null ? 1 : v + 1);

    // save the first protected range, containing current statement
    if (this.ssau && stat != null) { // null iff phantom version
      Statement firstRange = getFirstProtectedRange(stat);

      if (firstRange != null) {
        this.mapVersionFirstRange.put(new VarVersionPair(var, nextVersion), firstRange.id);
      }
    }

    return nextVersion;
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
          int version = this.getNextFreeVersion(varindex, stat); // == 1

          map = new SFormsFastMapDirect(this.factory);
          this.setCurrentVar(map, varindex, version);

          this.extraVarVersions.put(flatthelper.getDirectNode(stat.getStats().get(i)).id, map);
          if (this.trackSsuVersions) {
            this.ssuversions.createNode(new VarVersionPair(varindex, version));
          }
        }
    }

    for (Statement st : stat.getStats()) {
      this.setCatchMaps(st, dgraph, flatthelper);
    }
  }

  private SFormsFastMapDirect createFirstMap() {
    boolean thisvar = !this.mt.hasModifier(CodeConstants.ACC_STATIC);

    MethodDescriptor md = MethodDescriptor.parseDescriptor(this.mt.getDescriptor());

    int paramcount = md.params.length + (thisvar ? 1 : 0);

    int varindex = 0;
    SFormsFastMapDirect map = new SFormsFastMapDirect(this.factory);
    for (int i = 0; i < paramcount; i++) {
      int version = this.getNextFreeVersion(varindex, this.root); // == 1

      FastSparseSet<Integer> set = this.factory.createEmptySet();
      set.add(version);
      map.put(varindex, set);

      if (this.trackSsuVersions) {
        this.ssuversions.createNode(new VarVersionPair(varindex, version));
      }

      if (thisvar) {
        if (i == 0) {
          varindex++;
        } else {
          varindex += md.params[i - 1].stackSize;
        }
      } else {
        varindex += md.params[i].stackSize;
      }
    }

    return map;
  }

  public Map<VarVersionPair, FastSparseSet<Integer>> getPhi() {
    return this.phi;
  }


  private void createOrUpdatePhiNode(VarVersionPair phivar, FastSparseSet<Integer> vers, Statement stat) {
    Set<Integer> oldNodes = new HashSet<>();

    // ssu graph
    VarVersionNode phiNode = this.ssuversions.nodes.getWithKey(phivar);
    ValidationHelper.assertTrue(phiNode.phantomParentNode == null, "phi node can't be a phantom node");
    if (phiNode.preds2.isEmpty()) {
      ValidationHelper.assertTrue(
        phiNode.state == VarVersionNode.State.PHI,
        "Phi node has the wrong state?");
    } else if (phiNode.preds2.size() == 1) {
      // not yet a phi node
      ValidationHelper.assertTrue(
        phiNode.state == VarVersionNode.State.READ,
        "Trying to convert a non read node into a phi node");
      phiNode.state = VarVersionNode.State.PHI;
      phiNode.getSinglePredecessor().removeSuccessor(phiNode);
      phiNode.preds2.clear();
    } else {
      ValidationHelper.assertTrue(
        phiNode.state == VarVersionNode.State.PHI,
        "Phi node has the wrong state?");
      for (Iterator<VarVersionNode> iterator = phiNode.preds2.iterator(); iterator.hasNext(); ) {
        VarVersionNode source = iterator.next();
        ValidationHelper.assertTrue(
          source.state == VarVersionNode.State.READ,
          "Phi node is reading from a non READ node");

        // source is the read node, the version in the varmap is the version it read from
        int verssrc = source.getSinglePredecessor().version;
        if (!vers.contains(verssrc)) {
          source.removeSuccessor(phiNode);
          iterator.remove();
          source.state = VarVersionNode.State.DEAD_READ;
        } else {
          oldNodes.add(verssrc);
        }
      }
    }

    for (int ver : vers) {
      if (oldNodes.contains(ver)) {
        continue;
      }

      VarVersionNode preNode = this.ssuversions.nodes.getWithKey(new VarVersionPair(phivar.var, ver));
      VarVersionNode tempNode = this.createRead(preNode, stat);
      makeReadEdge(phiNode, tempNode);
    }
  }

  private static void makeReadEdge(VarVersionNode phiNode, VarVersionNode tempNode) {
    tempNode.succs2.add(phiNode);
    phiNode.preds2.add(tempNode);
  }

  private VarVersionNode createNewReadNode(int var, Statement stat) {
    return this.createNewReadNode(var, stat, VarVersionNode.State.READ);
  }

  private VarVersionNode createNewReadNode(int var, Statement stat, VarVersionNode.State state) {
    int version = this.getNextFreeVersion(var, stat);
    VarVersionNode node = this.ssuversions.createNode(new VarVersionPair(var, version));
    node.state = state;
    return node;
  }

  private void varMapToGraph(VarVersionPair varVersion, SFormsFastMapDirect varMap) {
    ValidationHelper.assertTrue(this.trackSsuVersions, "Can't make an ssu graph without ssu tracked");

    VBStyleCollection<VarVersionNode, VarVersionPair> nodes = this.ssuversions.nodes;

    VarVersionNode node = nodes.getWithKey(varVersion);

    node.live = varMap.getCopy();
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


  public VarVersionsGraph getSsuVersions() {
    return this.ssuversions;
  }

  public SFormsFastMapDirect getLiveVarVersionsMap(VarVersionPair varVersion) {
    ValidationHelper.assertTrue(this.trackSsuVersions, "Can't get ssu versions if we aren't tracking ssu");

    VarVersionNode node = this.ssuversions.nodes.getWithKey(varVersion);
    if (node != null) {
      return node.live == null ? new SFormsFastMapDirect(this.factory) : node.live;
    }

    return null;
  }

  public Map<VarVersionPair, Integer> getMapVersionFirstRange() {
    ValidationHelper.assertTrue(this.ssau, "This is an ssau only operation");
    return this.mapVersionFirstRange;
  }

  public Map<Integer, Integer> getMapFieldVars() {
    ValidationHelper.assertTrue(this.trackFieldVars, "Can't provide field data, if no field data was tracked");
    return this.mapFieldVars;
  }

  public Map<VarVersionPair, VarVersionPair> getVarAssignmentMap() {
    ValidationHelper.assertTrue(this.trackDirectAssignments, "Can't provide direct assignments, if no direct assignments was tracked");
    return this.varAssignmentMap;
  }

  public Map<VarVersionPair, Integer> getSimpleReversePhiLookup() {
    // simple union find
    Map<VarVersionPair, Integer> ret = new HashMap<>();
    for (var entry : this.phi.entrySet()) {
      int index = entry.getKey().var;
      VarVersionPair left = entry.getKey();

      while (ret.containsKey(left)) {
        int version = ret.get(left);
        if (version == left.version) {
          break;
        }
        left = new VarVersionPair(index, version);
      }

      for (int ver : entry.getValue()) {
        VarVersionPair right = new VarVersionPair(index, ver);

        while (ret.containsKey(right)) {
          int version = ret.get(right);
          if (version == right.version) {
            break;
          }
          right = new VarVersionPair(index, version);
        }

        if (left.version != right.version) {
          if (right.version < left.version) {
            ret.put(left, right.version);
            left = right;
          } else {
            ret.put(right, left.version);
          }
        }
      }
    }

    // fully flatten each version
    for (var entry : this.phi.entrySet()) {
      VarVersionPair pair = entry.getKey();

      while (ret.containsKey(pair)) {
        int version = ret.get(pair);
        if (version == pair.version) {
          break;
        }
        pair = new VarVersionPair(pair.var, version);
      }

      ret.put(entry.getKey(), pair.version);
    }

    return ret;
  }

  /**
   * restores the default identity equality for an object
   */
  static private class Id<T> {
    private final T value;

    public Id(T value) {
      this.value = value;
    }

    public T getValue() {
      return this.value;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(this.value);
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Id && ((Id<?>) obj).value == this.value;
    }

    static <T> Id<T> of(T value) {
      return new Id<>(value);
    }
  }
}
