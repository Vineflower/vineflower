package org.vineflower.unpick;

import org.jetbrains.java.decompiler.SingleClassesTestBase;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.*;

public class UnpickTest extends SingleClassesTestBase {
  @Override
  protected void registerAll() {
    registerSet("Unpick", this::registerUnpick,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.INCLUDE_JAVA_RUNTIME, "1",
      UnpickOptions.UNPICK_FILES, System.getProperty("vineflower.unpick.files")
    );
  }

  private void registerUnpick() {
    register(JAVA_8, "TestJdk");
    register(JAVA_8, "TestFloatPrecision");
    register(JAVA_8, "TestNotFloatPrecision");
    register(JAVA_8, "TestConstantUninlining");
    register(JAVA_8, "TestPiDivision");
    register(JAVA_17, "TestJdk17");
  }
}
