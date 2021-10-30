package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.MonitorExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SynchronizedStatement;

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
}
