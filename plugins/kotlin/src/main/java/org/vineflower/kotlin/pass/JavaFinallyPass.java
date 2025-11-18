package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.code.cfg.ControlFlowGraph;
import org.jetbrains.java.decompiler.modules.decompiler.FinallyProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.DomHelper;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;

public class JavaFinallyPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    RootStatement root = ctx.getRoot();
    StructMethod mt = ctx.getMethod();
    StructClass cl = ctx.getEnclosingClass();
    VarProcessor varProc = ctx.getVarProc();
    ControlFlowGraph graph = ctx.getGraph();
    MethodDescriptor md = ctx.getMethodDescriptor();

    FinallyProcessor fProc = new FinallyProcessor(mt, md, varProc);

    boolean res = false;
    int iteration = 0;
    while (fProc.iterateGraph(cl, mt, root, graph)) {
      RootStatement oldRoot = root;

      root = DomHelper.parseGraph(graph, mt, ++iteration);
      root.addComments(oldRoot);

      res = true;
    }

    return res;
  }
}
