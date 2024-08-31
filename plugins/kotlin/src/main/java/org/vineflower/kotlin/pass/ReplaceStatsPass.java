package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement;
import org.vineflower.kotlin.stat.KDoStatement;
import org.vineflower.kotlin.stat.KSequenceStatement;
import org.vineflower.kotlin.stat.KSwitchStatement;

public class ReplaceStatsPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    return replace(ctx.getRoot());
  }
  
  private static boolean replace(Statement stat) {
    boolean res = false;

    for (int i = 0; i < stat.getStats().size(); i++) {
      Statement st = stat.getStats().get(i);
      res |= replace(st);
      if (st instanceof SequenceStatement) {
        stat.getStats().set(i, new KSequenceStatement((SequenceStatement) st));
        res = true;
      } else if (st instanceof DoStatement) {
        stat.getStats().set(i, new KDoStatement((DoStatement) st));
        res = true;
      } else if (st instanceof SwitchStatement) {
        stat.getStats().set(i, new KSwitchStatement((SwitchStatement) st));
        res = true;
      }
    }

    return res;
  }
}
