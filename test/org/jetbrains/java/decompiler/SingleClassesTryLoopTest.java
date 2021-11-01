package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.JAVA_8;

public class SingleClassesTryLoopTest extends SingleClassesTestBase {
  @Override
  protected String[] getDecompilerOptions() {
    return new String[] {
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.INLINE_SIMPLE_LAMBDAS, "0",
      IFernflowerPreferences.EXPERIMENTAL_TRY_LOOP_FIX, "1"
    };
  }

  @Override
  protected void registerAll() {
    register(JAVA_8, "TestTryLoop");
    register(JAVA_8, "TestTryLoopRecompile");
    register(JAVA_8, "TestTryLoopSimpleFinally");
    // TODO: Still doesn't properly decompile, loop needs to be in the try block
    register(JAVA_8, "TestTryLoopReturnFinally");
  }
}
