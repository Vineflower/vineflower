package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.vineflower.kotlin.stat.*;

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
      if (st instanceof SequenceStatement seqStat) {
        st.replaceWith(new KSequenceStatement(seqStat));
        res = true;
      } else if (st instanceof DoStatement doStat) {
        st.replaceWith(new KDoStatement(doStat));
        res = true;
      } else if (st instanceof SwitchStatement switchStat) {
        st.replaceWith(new KSwitchStatement(switchStat));
        res = true;
      } else if (st instanceof IfStatement ifStat) {
        st.replaceWith(new KIfStatement(ifStat));
        res = true;
      } else if (st instanceof CatchStatement catchStat) {
        st.replaceWith(new KCatchStatement(catchStat));
        res = true;
      }
    }

    return res;
  }
}
