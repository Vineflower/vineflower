package org.quiltmc.quiltflower.kotlin;

import org.jetbrains.java.decompiler.api.Plugin;
import org.jetbrains.java.decompiler.api.language.LanguageSpec;
import org.jetbrains.java.decompiler.api.passes.LoopingPassBuilder;
import org.jetbrains.java.decompiler.api.passes.MainPassBuilder;
import org.jetbrains.java.decompiler.api.passes.Pass;
import org.jetbrains.java.decompiler.api.passes.WrappedPass;
import org.jetbrains.java.decompiler.modules.decompiler.*;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.DomHelper;
import org.quiltmc.quiltflower.kotlin.pass.JavaFinallyPass;
import org.quiltmc.quiltflower.kotlin.pass.ReplaceVarExprentsPass;

public class KotlinPlugin implements Plugin {

  @Override
  public String id() {
    return "Kotlin";
  }

  @Override
  public LanguageSpec getLanguageSpec() {
    return new LanguageSpec("kotlin", new KotlinChooser(), new DomHelper(), new KotlinWriter(), makePass());
  }

  private static Pass makePass() {
    return new MainPassBuilder()
      .addPass("Finally", new JavaFinallyPass())
      .addPass("RemoveSynchronized", ctx -> DomHelper.removeSynchronizedHandler(ctx.getRoot()))
      .addPass("CondenseSequences", WrappedPass.of(ctx -> SequenceHelper.condenseSequences(ctx.getRoot())))
      .addPass("ClearStatements", WrappedPass.of(ctx -> ClearStructHelper.clearStatements(ctx.getRoot())))
      .addPass("ProcessExpr", WrappedPass.of(
        ctx -> new ExprProcessor(ctx.getMethodDescriptor(), ctx.getVarProc()).processStatement(ctx.getRoot(), ctx.getEnclosingClass()))
      )
      .addPass("CondenseSequences_1", WrappedPass.of(ctx -> SequenceHelper.condenseSequences(ctx.getRoot())))
      .addPass("InlineIfPPMM", ctx -> PPandMMHelper.inlinePPIandMMIIf(ctx.getRoot()))
      .addPass("MainLoop",
        new LoopingPassBuilder("Main")
          .addFallthroughPass("ResetEdges", WrappedPass.of(ctx -> LabelHelper.cleanUpEdges(ctx.getRoot())))
          .addFallthroughPass("MergeLoop",
            new LoopingPassBuilder("Merge")
              .addLoopingPass("EliminateLoops", ctx -> EliminateLoopsHelper.eliminateLoops(ctx.getRoot(), ctx.getEnclosingClass()))
              .addFallthroughPass("EnhanceLoops", WrappedPass.of(ctx -> MergeHelper.enhanceLoops(ctx.getRoot())))
              .addLoopingPass("ExtractLoops", ctx -> LoopExtractHelper.extractLoops(ctx.getRoot()))
              .addLoopingPass("MergeAllIfs", ctx -> IfHelper.mergeAllIfs(ctx.getRoot()))
              .build()
            )
          .addFallthroughPass("SimplifyStack", WrappedPass.of(ctx -> StackVarsProcessor.simplifyStackVars(ctx.getRoot(), ctx.getMethod(), ctx.getEnclosingClass())))
          .addFallthroughPass("VarVersions", WrappedPass.of(ctx -> ctx.getVarProc().setVarVersions(ctx.getRoot())))
          .addFallthroughPass("IdentifyLabels", WrappedPass.of(ctx -> LabelHelper.identifyLabels(ctx.getRoot())))
          .addLoopingPass("InlineSingleBlocks", ctx -> InlineSingleBlockHelper.inlineSingleBlocks(ctx.getRoot()))
          .addLoopingPass("MakeDoWhile", ctx -> MergeHelper.makeDoWhileLoops(ctx.getRoot()))
          .addLoopingPass("CondenseDo", ctx -> MergeHelper.condenseInfiniteLoopsWithReturn(ctx.getRoot()))
          .addLoopingPass("CondenseExits", ctx -> ExitHelper.condenseExits(ctx.getRoot()))
          .build()
        )
      .addPass("AdjustReturnType", ctx -> ExitHelper.adjustReturnType(ctx.getRoot(), ctx.getMethodDescriptor()))
      .addPass("RedundantReturns", ctx -> ExitHelper.removeRedundantReturns(ctx.getRoot()))
      .addPass("IdentifySecondary", ctx -> SecondaryFunctionsHelper.identifySecondaryFunctions(ctx.getRoot(), ctx.getVarProc()))
      .addPass("SetVarDefinitions", WrappedPass.of(ctx -> ctx.getVarProc().setVarDefinitions(ctx.getRoot())))
      .addPass("ReplaceVars", new ReplaceVarExprentsPass())
      .addPass("ReplaceContinue", ctx -> LabelHelper.replaceContinueWithBreak(ctx.getRoot()))

      .build();
  }
}
