package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.code.cfg.ControlFlowGraph;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.modules.decompiler.deobfuscator.ExceptionDeobfuscator;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.util.DotExporter;

public class ObfuscatedExceptionsPass implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    ControlFlowGraph graph = ctx.getGraph();
    StructMethod mt = ctx.getMethod();

    if (ExceptionDeobfuscator.hasObfuscatedExceptions(graph)) {
      DotExporter.toDotFile(graph, mt, "cfgExceptionsPre", true);

      if (!ExceptionDeobfuscator.handleMultipleEntryExceptionRanges(graph)) {
        DecompilerContext.getLogger().writeMessage("Found multiple entry exception ranges which could not be splitted", IFernflowerLogger.Severity.WARN);
        graph.addComment("$VF: Could not handle exception ranges with multiple entries");
        graph.addErrorComment = true;
      }

      DotExporter.toDotFile(graph, mt, "cfgMultipleExceptionEntry", true);
      ExceptionDeobfuscator.insertDummyExceptionHandlerBlocks(graph, mt.getBytecodeVersion());
      DotExporter.toDotFile(graph, mt, "cfgMultipleExceptionDummyHandlers", true);
      return true;
    }

    return false;
  }
}
