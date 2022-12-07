package org.quiltmc.quiltflower.scala;

import org.jetbrains.java.decompiler.SingleClassesTestBase;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.SCALA;

public class ScalaTests extends SingleClassesTestBase{
  
  protected void registerAll(){
    registerSet("Entire Classpath", this::registerScalaTests,
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
  
  private void registerScalaTests(){
    register(SCALA, "TestCaseClasses", "Option1", "Option1$", "Option2", "Option2$", "Option3", "Option3$", "EnumLike", "EnumLike$");
    register(SCALA, "TestObject", "TestObject$");
    register(SCALA, "TestCompanionObject", "TestCompanionObject$");
  }
}
