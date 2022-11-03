package org.jetbrains.java.decompiler.ir;

import org.jetbrains.java.decompiler.modules.decompiler.LabelHelper;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;

public class IdentifyLabelsTest extends IrTestBase {
  @Override
  protected void registerAll() {
    register("TestLoopBreak");
  }

  @Override
  protected String transformName() {
    return "IdentifyLabels";
  }

  @Override
  protected void runTransform(RootStatement root) {
    LabelHelper.identifyLabels(root);
  }
}
