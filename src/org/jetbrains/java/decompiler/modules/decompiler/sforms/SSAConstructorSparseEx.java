// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.sforms;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.sforms.FlattenStatementsHelper.FinallyPathWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchAllStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.CatchStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.util.DotExporter;
import org.jetbrains.java.decompiler.util.FastSparseSetFactory;
import org.jetbrains.java.decompiler.util.FastSparseSetFactory.FastSparseSet;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.SFormsFastMapDirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import static org.jetbrains.java.decompiler.modules.decompiler.sforms.VarMapHolder.mergeMaps;

public class SSAConstructorSparseEx {

  // node id, var, version
  private final HashMap<String, SFormsFastMapDirect> inVarVersions = new HashMap<>();

  // node id, var, version (direct branch)
  private final HashMap<String, SFormsFastMapDirect> outVarVersions = new HashMap<>();

  // node id, var, version (negative branch)
  private final HashMap<String, SFormsFastMapDirect> outNegVarVersions = new HashMap<>();

  // node id, var, version
  private final HashMap<String, SFormsFastMapDirect> extraVarVersions = new HashMap<>();

  // (var, version), version
  private final HashMap<VarVersionPair, FastSparseSet<Integer>> phi = new HashMap<>();

  // var, version
  private final HashMap<Integer, Integer> lastversion = new HashMap<>();

  // set factory
  private FastSparseSetFactory<Integer> factory;

  public void splitVariables(RootStatement root, StructMethod mt) {

    FlattenStatementsHelper flatthelper = new FlattenStatementsHelper();
    DirectGraph dgraph = flatthelper.buildDirectGraph(root);

    DotExporter.toDotFile(dgraph, mt, "ssaSplitVariables");

    List<Integer> setInit = new ArrayList<>();
    for (int i = 0; i < 64; i++) {
      setInit.add(i);
    }
    factory = new FastSparseSetFactory<>(setInit);

    SFormsFastMapDirect firstmap = createFirstMap(mt);
    extraVarVersions.put(dgraph.first.id, firstmap);

    setCatchMaps(root, dgraph, flatthelper);

    int itteration = 1;
    HashSet<String> updated = new HashSet<>();
    do {
      // System.out.println("~~~~~~~~~~~~~ \r\n"+root.toJava());
      ssaStatements(dgraph, updated, mt, itteration++);
      // System.out.println("~~~~~~~~~~~~~ \r\n"+root.toJava());
    }
    while (!updated.isEmpty());
  }

  private void ssaStatements(DirectGraph dgraph, HashSet<String> updated, StructMethod mt, int itteration) {

    DotExporter.toDotFile(dgraph, mt, "ssaStatements_" + itteration, outVarVersions);

    for (DirectNode node : dgraph.nodes) {

      updated.remove(node.id);
      mergeInVarMaps(node, dgraph);

      SFormsFastMapDirect varmap = inVarVersions.get(node.id);
      VarMapHolder varmaps = VarMapHolder.ofNormal(varmap);

      if (node.exprents != null) {
        for (Exprent expr : node.exprents) {
          varmaps.toNormal(); // make sure we are in normal form
          this.processExprent(node, expr, varmaps);
        }
      }

      boolean this_updated = !mapsEqual(varmaps.getIfTrue(), outVarVersions.get(node.id))
                             || (outNegVarVersions.containsKey(node.id) && !mapsEqual(varmaps.getIfFalse(), outNegVarVersions.get(node.id)));

      if (this_updated) {
        outVarVersions.put(node.id, varmaps.getIfTrue());
        if (dgraph.mapNegIfBranch.containsKey(node.id)) {
          outNegVarVersions.put(node.id, varmaps.getIfFalse());
        }

        // Don't update the node if it wasn't discovered normally, as that can lead to infinite recursion due to bad ordering!
        if (!dgraph.extraNodes.contains(node)) {
          for (DirectNode nd : node.succs) {
            updated.add(nd.id);
          }
        }
      }
    }
  }

  private void processExprent(DirectNode node, Exprent expr, VarMapHolder varMaps) {

    if (expr == null) {
      return;
    }

    // The var map data can't depend yet on the result of this expression.
    varMaps.assertIsNormal();

    switch (expr.type) {
      case Exprent.EXPRENT_IF: {
        // EXPRENT_IF is a wrapper for the head exprent of an if statement.
        // Therefore, the map needs to stay split, unlike with most other exprents.
        IfExprent ifexpr = (IfExprent) expr;
        this.processExprent(node, ifexpr.getCondition(), varMaps);
        return;
      }
      case Exprent.EXPRENT_ASSIGNMENT: {
        // Assigning a local overrides all the readable versions of that node.
        AssignmentExprent assexpr = (AssignmentExprent) expr;
        if (assexpr.getCondType() == AssignmentExprent.CONDITION_NONE) {
          Exprent dest = assexpr.getLeft();
          if (dest.type == Exprent.EXPRENT_VAR) {
            this.processExprent(node, assexpr.getRight(), varMaps);
            this.updateVarExprent((VarExprent) dest, varMaps.getNormal());
            return;
          }
        }
        break;
      }
      case Exprent.EXPRENT_FUNCTION: {
        FunctionExprent func = (FunctionExprent) expr;
        switch (func.getFuncType()) {
          case FunctionExprent.FUNCTION_IIF: {
            // `a ? b : c`
            // Java language spec: 16.1.5.
            this.processExprent(node, func.getLstOperands().get(0), varMaps);

            VarMapHolder bVarMaps = VarMapHolder.ofNormal(varMaps.getIfTrue());
            this.processExprent(node, func.getLstOperands().get(1), bVarMaps);

            // reuse the varMaps for the false branch.
            varMaps.setNormal(varMaps.getIfFalse());
            this.processExprent(node, func.getLstOperands().get(2), varMaps);

            if (bVarMaps.isNormal() && varMaps.isNormal()) {
              varMaps.mergeNormal(bVarMaps.getNormal());
            } else if (!varMaps.isNormal()){
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
          case FunctionExprent.FUNCTION_CADD: {
            // `a && b`
            // Java language spec: 16.1.2.
            this.processExprent(node, func.getLstOperands().get(0), varMaps);

            varMaps.makeFullyMutable();
            SFormsFastMapDirect ifFalse = varMaps.getIfFalse();
            varMaps.setNormal(varMaps.getIfTrue());

            this.processExprent(node, func.getLstOperands().get(1), varMaps);
            varMaps.mergeIfFalse(ifFalse);
            return;
          }
          case FunctionExprent.FUNCTION_COR: {
            // `a || b`
            // Java language spec: 16.1.3.
            this.processExprent(node, func.getLstOperands().get(0), varMaps);

            varMaps.makeFullyMutable();
            SFormsFastMapDirect ifTrue = varMaps.getIfTrue();
            varMaps.setNormal(varMaps.getIfFalse());

            this.processExprent(node, func.getLstOperands().get(1), varMaps);
            varMaps.mergeIfTrue(ifTrue);
            return;
          }
          case FunctionExprent.FUNCTION_BOOL_NOT: {
            // `!a`
            // Java language spec: 16.1.4.
            this.processExprent(node, func.getLstOperands().get(0), varMaps);
            varMaps.swap();

            return;
          }
          case FunctionExprent.FUNCTION_INSTANCEOF: {
            // `a instanceof B`
            // pattern matching instanceof creates a new variable when true.
            this.processExprent(node, func.getLstOperands().get(0), varMaps);
            varMaps.toNormal();

            if (func.getLstOperands().size() == 3) {
              // pattern matching
              // `a instanceof B b`
              // pattern matching variables are explained in different parts of the spec,
              // but it comes down to the same ideas.
              varMaps.makeFullyMutable();

              this.updateVarExprent(
                (VarExprent) func.getLstOperands().get(2),
                varMaps.getIfTrue());
            }
            return;
          }
        }
        break;
      }
      case Exprent.EXPRENT_VAR: {
        // a read of a variable.
        VarExprent vardest = (VarExprent) expr;
        final SFormsFastMapDirect varmap = varMaps.getNormal();

        int varindex = vardest.getIndex();
        FastSparseSet<Integer> vers = varmap.get(varindex);

        int cardinality = vers != null ? vers.getCardinality() : 0;
        switch (cardinality) {
          case 0:  // size == 0 (var has no discovered assignments yet)
            this.updateVarExprent(vardest, varmap);
            break;
          case 1:  // size == 1 (var has only one discovered assignment)
            // set version
            int it = vers.iterator().next();
            vardest.setVersion(it);
            break;
          case 2:  // size > 1 (var has more than one assignment)
            int current_vers = vardest.getVersion();

            VarVersionPair varVersion = new VarVersionPair(varindex, current_vers);
            if (current_vers != 0 && this.phi.containsKey(varVersion)) {
              this.setCurrentVar(varmap, varindex, current_vers);
              // keep phi node up to date of all inputs
              this.phi.get(varVersion).union(vers);
            } else {
              // increase version
              int nextver = this.getNextFreeVersion(varindex);
              // set version
              vardest.setVersion(nextver);

              this.setCurrentVar(varmap, varindex, nextver);
              // create new phi node
              this.phi.put(new VarVersionPair(varindex, nextver), vers);
            }
            break;
        }
        return;
      }
    }

    // Foreach init node- mark as assignment!
    if (node.type == DirectNode.NodeType.FOREACH_VARDEF && node.exprents.get(0).type == Exprent.EXPRENT_VAR) {
      this.updateVarExprent(
        (VarExprent) node.exprents.get(0),
        varMaps.getNormal());
      return;
    }

    for (Exprent ex : expr.getAllExprents()) {
      this.processExprent(node, ex, varMaps);
      varMaps.toNormal();
    }
  }

  private void updateVarExprent(VarExprent varassign, SFormsFastMapDirect varmap) {
    int varindex = varassign.getIndex();

    if (varassign.getVersion() == 0) {
      // get next version
      int nextver = this.getNextFreeVersion(varindex);

      // set version
      varassign.setVersion(nextver);

      this.setCurrentVar(varmap, varindex, nextver);
    } else {
      this.setCurrentVar(varmap, varindex, varassign.getVersion());
    }
  }

  private int getNextFreeVersion(int var) {
    Integer nextver = lastversion.get(var);
    if (nextver == null) {
      nextver = 1;
    } else {
      nextver++;
    }
    lastversion.put(var, nextver);
    return nextver;
  }

  private void mergeInVarMaps(DirectNode node, DirectGraph dgraph) {

    SFormsFastMapDirect mapNew = new SFormsFastMapDirect();

    for (DirectNode pred : node.preds) {
      SFormsFastMapDirect mapOut = getFilteredOutMap(node.id, pred.id, dgraph, node.id);
      if (mapNew.isEmpty()) {
        mapNew = mapOut.getCopy();
      } else {
        mergeMaps(mapNew, mapOut);
      }
    }

    if (extraVarVersions.containsKey(node.id)) {
      SFormsFastMapDirect mapExtra = extraVarVersions.get(node.id);
      if (mapNew.isEmpty()) {
        mapNew = mapExtra.getCopy();
      } else {
        mergeMaps(mapNew, mapExtra);
      }
    }

    inVarVersions.put(node.id, mapNew);
  }

  private SFormsFastMapDirect getFilteredOutMap(String nodeid, String predid, DirectGraph dgraph, String destid) {

    SFormsFastMapDirect mapNew = new SFormsFastMapDirect();

    if (nodeid.equals(dgraph.mapNegIfBranch.get(predid))) {
      if (outNegVarVersions.containsKey(predid)) {
        mapNew = outNegVarVersions.get(predid).getCopy();
      }
    } else if (outVarVersions.containsKey(predid)) {
      mapNew = outVarVersions.get(predid).getCopy();
    }

    boolean isFinallyExit = dgraph.mapShortRangeFinallyPaths.containsKey(predid);

    if (isFinallyExit && !mapNew.isEmpty()) {

      SFormsFastMapDirect mapNewTemp = mapNew.getCopy();

      SFormsFastMapDirect mapTrueSource = new SFormsFastMapDirect();

      String exceptionDest = dgraph.mapFinallyMonitorExceptionPathExits.get(predid);
      boolean isExceptionMonitorExit = (exceptionDest != null && !nodeid.equals(exceptionDest));

      HashSet<String> setLongPathWrapper = new HashSet<>();
      for (FinallyPathWrapper finwraplong : dgraph.mapLongRangeFinallyPaths.get(predid)) {
        setLongPathWrapper.add(finwraplong.destination + "##" + finwraplong.source);
      }

      for (FinallyPathWrapper finwrap : dgraph.mapShortRangeFinallyPaths.get(predid)) {
        SFormsFastMapDirect map;

        boolean recFinally = dgraph.mapShortRangeFinallyPaths.containsKey(finwrap.source);

        if (recFinally) {
          // recursion
          map = getFilteredOutMap(finwrap.entry, finwrap.source, dgraph, destid);
        } else {
          if (finwrap.entry.equals(dgraph.mapNegIfBranch.get(finwrap.source))) {
            map = outNegVarVersions.get(finwrap.source);
          } else {
            map = outVarVersions.get(finwrap.source);
          }
        }

        // false path?
        boolean isFalsePath;

        if (recFinally) {
          isFalsePath = !finwrap.destination.equals(nodeid);
        } else {
          isFalsePath = !setLongPathWrapper.contains(destid + "##" + finwrap.source);
        }

        if (isFalsePath) {
          mapNewTemp.complement(map);
        } else {
          if (mapTrueSource.isEmpty()) {
            if (map != null) {
              mapTrueSource = map.getCopy();
            }
          } else {
            mergeMaps(mapTrueSource, map);
          }
        }
      }

      if (isExceptionMonitorExit) {

        mapNew = mapTrueSource;
      } else {

        mapNewTemp.union(mapTrueSource);

        SFormsFastMapDirect oldInMap = inVarVersions.get(nodeid);
        if (oldInMap != null) {
          mapNewTemp.union(oldInMap);
        }

        mapNew.intersection(mapNewTemp);
      }
    }

    return mapNew;
  }

  private static boolean mapsEqual(SFormsFastMapDirect map1, SFormsFastMapDirect map2) {

    if (map1 == null) {
      return map2 == null;
    } else if (map2 == null) {
      return false;
    }

    if (map1.size() != map2.size()) {
      return false;
    }

    for (Entry<Integer, FastSparseSet<Integer>> ent2 : map2.entryList()) {
      if (!InterpreterUtil.equalObjects(map1.get(ent2.getKey()), ent2.getValue())) {
        return false;
      }
    }

    return true;
  }

  private void setCurrentVar(SFormsFastMapDirect varmap, int var, int vers) {
    FastSparseSet<Integer> set = factory.spawnEmptySet();
    set.add(vers);
    varmap.put(var, set);
  }

  private void setCatchMaps(Statement stat, DirectGraph dgraph, FlattenStatementsHelper flatthelper) {

    SFormsFastMapDirect map;

    switch (stat.type) {
      case Statement.TYPE_CATCHALL:
      case Statement.TYPE_TRYCATCH:

        List<VarExprent> lstVars;
        if (stat.type == Statement.TYPE_CATCHALL) {
          lstVars = ((CatchAllStatement) stat).getVars();
        } else {
          lstVars = ((CatchStatement) stat).getVars();
        }

        for (int i = 1; i < stat.getStats().size(); i++) {
          int varindex = lstVars.get(i - 1).getIndex();
          int version = getNextFreeVersion(varindex); // == 1

          map = new SFormsFastMapDirect();
          setCurrentVar(map, varindex, version);

          extraVarVersions.put(dgraph.nodes.getWithKey(flatthelper.getMapDestinationNodes().get(stat.getStats().get(i).id)[0]).id, map);
        }
    }

    for (Statement st : stat.getStats()) {
      setCatchMaps(st, dgraph, flatthelper);
    }
  }

  private SFormsFastMapDirect createFirstMap(StructMethod mt) {
    boolean thisvar = !mt.hasModifier(CodeConstants.ACC_STATIC);

    MethodDescriptor md = MethodDescriptor.parseDescriptor(mt.getDescriptor());

    int paramcount = md.params.length + (thisvar ? 1 : 0);

    int varindex = 0;
    SFormsFastMapDirect map = new SFormsFastMapDirect();
    for (int i = 0; i < paramcount; i++) {
      int version = getNextFreeVersion(varindex); // == 1

      FastSparseSet<Integer> set = factory.spawnEmptySet();
      set.add(version);
      map.put(varindex, set);

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

  public HashMap<VarVersionPair, FastSparseSet<Integer>> getPhi() {
    return phi;
  }
}
