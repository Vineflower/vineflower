package org.quiltmc.quiltflower.kotlin;

import org.jetbrains.java.decompiler.SingleClassesTestBase;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.KOTLIN;

public class KotlinTests extends SingleClassesTestBase {
  
  protected void registerAll() {
    registerSet("Entire Classpath", this::registerKotlinTests,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.REMOVE_SYNTHETIC, "0",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1"
    );
  }

  private void registerKotlinTests() {
    register(KOTLIN, "TestKt");
    register(KOTLIN, "TestParams");
    register(KOTLIN, "TestInfixFun");
    register(KOTLIN, "TestFunVarargs");
    register(KOTLIN, "TestTailrecFunctions");
    register(KOTLIN, "TestTryCatchExpressions");
    register(KOTLIN, "TestTryFinallyExpressions");
    register(KOTLIN, "TestVars");
    // TODO: handle lambda's
    register(KOTLIN, "TestNonInlineLambda");
    register(KOTLIN, "TestNullable");
    register(KOTLIN, "TestExtensionFun");
    register(KOTLIN, "TestClassDec");
    register(KOTLIN, "TestAnyType");
    // TODO: gets condensed into ternaries
    register(KOTLIN, "TestWhen");
    register(KOTLIN, "TestForRange");
    register(KOTLIN, "TestIfRange");
    register(KOTLIN, "TestComparison");
    register(KOTLIN, "TestNullableOperator");
  }
}