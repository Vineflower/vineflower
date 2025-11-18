package org.vineflower.variablerenaming;

import org.jetbrains.java.decompiler.SingleClassesTestBase;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.CUSTOM;
import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.JAVA_8;

public class VariableRenamingTest extends SingleClassesTestBase {
  @Override
  protected void registerAll() {
    registerSet("JAD Naming", () -> {
        register(JAVA_8, "TestJADNaming");
        // TODO: loop part fails
        registerRaw(CUSTOM, "TestJadLvtCollision"); // created by placing a class in java8 sources and remapping its param using tinyremapper
        register(JAVA_8, "TestJADLocalClasses");
      }, IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      VariableRenamingOptions.VARIABLE_RENAMER, "jad"
    );

    registerSet("Tiny Naming", () -> {
        register(JAVA_8, "TestTinyNaming");
      }, IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      VariableRenamingOptions.VARIABLE_RENAMER, "tiny"
    );
  }
}
