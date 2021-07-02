package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

public class SingleClassesLiteralTest extends SingleClassesTestBase {
  @Override
  protected String[] getDecompilerOptions() {
    return new String[] {
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.LITERALS_AS_IS, "0"
    };
  }

  @Override
  protected void registerAll() {
    register(8, "TestFloatPrecision");
    register(8, "TestNotFloatPrecision");
    register(8, "TestConstantUninlining");
  }
}
