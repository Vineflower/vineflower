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
    register(KOTLIN, "TestVars");
    // TODO: handle lambda's
    register(KOTLIN, "TestNonInlineLambda");
    // TODO: Generics containing nullability (like List<String?>) lose the nullability
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
    // TODO: top-level constructs are wrongly put into a class and turned into instance methods
    register(KOTLIN, "TestTopLevelKt");
    // TODO: lists, sets, etc. should be considered to be their Kotlin type, not their Java type
    register(KOTLIN, "TestConvertedK2JOps");
    // TODO: Does not decompile to data class
    register(KOTLIN, "TestDataClass");
    // TODO: Type projections are not Kotlin equivalents
    register(KOTLIN, "TestGenerics");
    // TODO: Nothing, function types
    register(KOTLIN, "TestKotlinTypes");
    // TODO: Does not decompile to object literal
    // FIXME: @JvmStatic function fails to decompile at all
    register(KOTLIN, "TestObject");
    // TODO: sealed becomes "open abstract"
    register(KOTLIN, "TestSealedHierarchy");
    // TODO: Annotation classes decompile entirely incorrectly
    register(KOTLIN, "TestAnnotations");
    register(KOTLIN, "TestBitwiseFunctions");
    register(KOTLIN, "TestCompileTimeErrors");
    register(KOTLIN, "TestPoorNames");
  }
}
