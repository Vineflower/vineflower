package org.vineflower.unpick;

import org.jetbrains.java.decompiler.SingleClassesTestBase;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

public class UnpickTest extends SingleClassesTestBase {
  @Override
  protected void registerAll() {
    registerSet("Unpick", UnpickTest::registerUnpick,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      UnpickOptions.UNPICK_FILES, System.getProperty("vineflower.unpick.files")
    );
  }

  private static void registerUnpick() {

  }
}
