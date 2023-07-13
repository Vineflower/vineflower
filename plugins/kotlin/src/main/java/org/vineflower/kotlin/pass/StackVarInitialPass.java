package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.passes.Pass;
import org.jetbrains.java.decompiler.api.passes.PassContext;
import org.jetbrains.java.decompiler.code.cfg.ControlFlowGraph;
import org.jetbrains.java.decompiler.modules.decompiler.PPandMMHelper;
import org.jetbrains.java.decompiler.modules.decompiler.StackVarsProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;

public class StackVarInitialPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    RootStatement root = ctx.getRoot();
    StructMethod mt = ctx.getMethod();
    StructClass cl = ctx.getEnclosingClass();
    VarProcessor varProc = ctx.getVarProc();

    do {

      StackVarsProcessor.simplifyStackVars(root, mt, cl);

      varProc.setVarVersions(root);
    } while (new PPandMMHelper(varProc).findPPandMM(root));

    return true;
  }
}
