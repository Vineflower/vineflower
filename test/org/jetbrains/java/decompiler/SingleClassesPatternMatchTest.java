package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.*;

public class SingleClassesPatternMatchTest extends SingleClassesTestBase {
  @Override
  protected String[] getDecompilerOptions() {
    return new String[] {
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.INLINE_SIMPLE_LAMBDAS, "0",
      IFernflowerPreferences.PATTERN_MATCHING, "1"
    };
  }

  @Override
  protected void registerAll() {
    register(JAVA_16, "TestPatternMatching");
    // TODO: rename this
    register(JAVA_16, "TestPatternMatchingFake");
    register(JAVA_17, "TestPatternMatchingInteger");
    // TODO: testNoInit is wrong
    register(JAVA_16, "TestPatternMatchingMerge");
    // TODO: local variables aren't merged properly, bring out of nodebug when they are
    register(JAVA_16_NODEBUG, "TestPatternMatchingAssign");
    register(JAVA_16, "TestPatternMatchingLocalCapture");
  }
}
