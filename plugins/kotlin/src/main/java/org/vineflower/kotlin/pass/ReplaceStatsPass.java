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
      if (st instanceof SequenceStatement) {
        st.replaceWith(new KSequenceStatement((SequenceStatement) st));
        res = true;
      } else if (st instanceof DoStatement) {
        st.replaceWith(new KDoStatement((DoStatement) st));
        res = true;
      } else if (st instanceof SwitchStatement) {
        st.replaceWith(new KSwitchStatement((SwitchStatement) st));
        res = true;
      } else if (st instanceof IfStatement) {
        st.replaceWith(new KIfStatement((IfStatement) st));
        res = true;
      }
    }

    return res;
  }
}
