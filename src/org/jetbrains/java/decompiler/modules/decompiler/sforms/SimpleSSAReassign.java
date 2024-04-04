package org.jetbrains.java.decompiler.modules.decompiler.sforms;

import org.jetbrains.java.decompiler.code.Instruction;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

// Turns all SSA assigned variables with versions > 1 into new variable indices.
// Basically what VarVersionsProcessor does but much simpler.
// TODO: should this also be taking into account phi versions?
public final class SimpleSSAReassign {
  public static Map<Instruction, Integer> reassignSSAForm(SSAConstructorSparseEx ssa, RootStatement root) {
    Set<Integer> vers = new HashSet<>();
    Map<VarVersionPair, Integer> versionLookup = ssa.getSimpleReversePhiLookup();

    // Add all found var indices to the set
    findAllVars(root, var -> {
      int index = var.getIndex();

      // Skip stack vars
      if (index < VarExprent.STACK_BASE) {
        vers.add(index);
      }
    });

    // Make new variables to increment the index for
    int maxVer = DecompilerContext.getCounterContainer().getCounter(CounterContainer.VAR_COUNTER);

    // No variables?
    if (vers.isEmpty()) {
      return new HashMap<>();
    }

    // Needed for lambda
    AtomicInteger counter = new AtomicInteger(maxVer);

    // Find rewrite map
    Map<VarVersionPair, Integer> newVers = new HashMap<>();
    findAllVars(root, var -> {
      VarVersionPair vvp = var.getVarVersionPair();
      int version = versionLookup.getOrDefault(vvp, vvp.version);

      // When encountering an unseen variable, increment and get counter for next index
      if (version > 1 && vers.contains(vvp.var)) {
        VarVersionPair base = vvp;
        if (vvp.version != version) {
          base = new VarVersionPair(vvp.var, version);
        }
        if (!newVers.containsKey(base)) {
          newVers.put(base, counter.incrementAndGet());
        }

        if (base != vvp) {
          newVers.put(vvp, newVers.get(base));
        }
      }
    });

    // Perform rewrite
    Map<Instruction, Integer> rewriteMap = new HashMap<>();
    findAllVars(root, var -> {
      VarVersionPair vvp = var.getVarVersionPair();

      // Rewrite single variable
      if (newVers.containsKey(vvp)) {
        int newIdx = newVers.get(vvp);
        var.setIndex(newIdx);
        var.setVersion(1);

        // If the backing instruction is known (and it should be!), add it to the rewritten map
        // The instruction itself isn't modified to prevent accidental pollution.
        if (var.getBackingInstr() != null) {
          rewriteMap.put(var.getBackingInstr(), newIdx);
        }
      }
    });

    return rewriteMap;
  }

  private static void findAllVars(Statement stat, Consumer<VarExprent> action) {
    if (stat.getExprents() == null) {
      for (Object obj : stat.getSequentialObjects()) {
        if (obj instanceof Statement) {
          findAllVars((Statement)obj, action);
        }
        else if (obj instanceof Exprent) {
          findAllVars((Exprent)obj, action);
        }
      }
    }
    else {
      for (Exprent exprent : stat.getExprents()) {
        findAllVars(exprent, action);
      }
    }
  }

  private static void findAllVars(Exprent exprent, Consumer<VarExprent> action) {
    List<Exprent> lst = exprent.getAllExprents(true, true);

    for (Exprent expr : lst) {
      if (expr instanceof VarExprent) {
        action.accept((VarExprent)expr);
      }
    }
  }
}
