package org.vineflower.ideanotnull;

import org.jetbrains.java.decompiler.SingleClassesTestBase;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.CUSTOM;

public class InnClassesTest extends SingleClassesTestBase {
  
  protected void registerAll() {
    registerSet("With Idea Not Null", this::registerInn,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.TERNARY_CONDITIONS, "1",
      IFernflowerPreferences.FORCE_JSR_INLINE, "1"
    );
  }
  
  private void registerInn() {
    registerRaw(CUSTOM, "TestIdeaNotNull");
  }
}
