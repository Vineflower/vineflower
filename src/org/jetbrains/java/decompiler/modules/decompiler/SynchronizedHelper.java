package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SynchronizedStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericType;

public final class SynchronizedHelper {
  public static boolean cleanSynchronizedVar(Statement stat) {
    boolean res = false;
    for (Statement st : stat.getStats()) {
      res |= cleanSynchronizedVar(st);
    }

    if (stat.type == Statement.TYPE_SYNCRONIZED) {
      SynchronizedStatement sync = (SynchronizedStatement)stat;

      if (sync.getHeadexprentList().get(0).type == Exprent.EXPRENT_MONITOR) {
        MonitorExprent mon = (MonitorExprent)sync.getHeadexprentList().get(0);

        for (Exprent e : sync.getFirst().getExprents()) {
          if (e.type == Exprent.EXPRENT_ASSIGNMENT) {
            AssignmentExprent ass = (AssignmentExprent)e;

            if (ass.getLeft().type == Exprent.EXPRENT_VAR) {
              VarExprent var = (VarExprent)ass.getLeft();

              if (ass.getRight().equals(mon.getValue()) && !var.isVarReferenced(stat.getParent())) {
                sync.getFirst().getExprents().remove(e);
                res = true;
                break;
              }
            }
          }
        }
      }
    }

    return res;
  }

  public static boolean insertSink(RootStatement root, VarProcessor varProcessor, Statement stat) {
    boolean res = false;
    for (Statement st : stat.getStats()) {
      res |= insertSink(root, varProcessor, st);
    }

    if (stat.type == Statement.TYPE_SYNCRONIZED) {
      MonitorExprent mon = (MonitorExprent) ((SynchronizedStatement)stat).getHeadexprent();
      Exprent value = mon.getValue();

      if (value.type == Exprent.EXPRENT_CONST && ((ConstExprent)value).getConstType() != VarType.VARTYPE_STRING && !(((ConstExprent)value).getConstType() instanceof GenericType)) {
        // Somehow created a const monitor, add assignment of object to ensure that it functions
        int var = DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.VAR_COUNTER);

        VarExprent varEx = new VarExprent(var, VarType.VARTYPE_OBJECT, varProcessor);
        // Doesn't track var type without this, ends up as <unknown>!
        varProcessor.setVarType(varEx.getVarVersionPair(), VarType.VARTYPE_OBJECT);

        AssignmentExprent assign = new AssignmentExprent(varEx, value, null);
        mon.replaceExprent(value, assign);
        root.addComment("$QF: Added assignment to ensure synchronized validity");
      } else if (value.type == Exprent.EXPRENT_INVOCATION) {
        // Force boxing for monitor
        InvocationExprent inv = (InvocationExprent)value;

        if (inv.isBoxingCall()) {
          inv.markUsingBoxingResult();
        }
      }
    }

    return res;
  }

  public static void markLiveMonitors(RootStatement root) {
    markLiveMonitors(root, root);
  }

  private static void markLiveMonitors(RootStatement root, Statement stat) {
    for (Statement st : stat.getStats()) {
      markLiveMonitors(root, st);
    }

    if (stat.type == Statement.TYPE_BASICBLOCK) {
      for (Exprent ex : stat.getExprents()) {
        if (ex.type == Exprent.EXPRENT_MONITOR) {
          root.addComment("$QF: Could not create synchronized statement, marking monitor enters and exits");
          root.addErrorComment = true;
        }
      }
    }
  }
}
