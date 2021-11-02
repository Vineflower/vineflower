package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.*;

public class SingleClassesJavaRuntimeTest extends SingleClassesTestBase {
  @Override
  protected String[] getDecompilerOptions() {
    return new String[] {
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_JAVA_RUNTIME, "1",
      IFernflowerPreferences.INLINE_SIMPLE_LAMBDAS, "0"
    };
  }

  @Override
  protected void registerAll() {
    // TODO: reevaluate behavior, especially with casting
    register(JAVA_8, "TestGenerics");
    register(JAVA_8, "TestClassTypes");
    register(JAVA_8, "TestClassCast");
    // TODO: intValue() call where there shouldn't be
    register(JAVA_8, "TestBoxingConstructor");
    register(JAVA_8, "TestLocalsSignature");
    register(JAVA_8, "TestShadowing", "Shadow", "ext/Shadow", "TestShadowingSuperClass");
    register(JAVA_8, "TestPrimitives");
    register(JAVA_8, "TestVarArgCalls");
    register(JAVA_8, "TestUnionType");
    register(JAVA_8, "TestTryWithResources");
    register(JAVA_8, "TestNestedLoops");
    register(JAVA_8, "TestAnonymousClass");
    // TODO: Object[] becomes <unknown>
    register(JAVA_8, "TestObjectArrays");
    register(JAVA_8, "TestAnonymousParams");
    register(JAVA_8, "TestThrowException");
    register(JAVA_8, "TestClassSimpleBytecodeMapping");
    register(JAVA_8, "TestAnonymousSignature");
    register(JAVA_16, "TestTryWithResourcesJ16");
    register(JAVA_16, "TestTryWithResourcesCatchJ16");
    register(JAVA_16, "TestTryWithResourcesMultiJ16");
    register(JAVA_16, "TestTryWithResourcesFinallyJ16");
    register(JAVA_16, "TestTryWithResourcesCatchFinallyJ16");
    // TODO: returning in finally causes broken control flow and undecompileable methods
    register(JAVA_16, "TestTryWithResourcesReturnJ16");
    register(JAVA_16, "TestTryWithResourcesNestedJ16");
    // TODO: extra casts, var type reverted to object
    register(JAVA_16, "TestTryWithResourcesNullJ16");
    // TODO: doesn't make try with resources block
    register(JAVA_16, "TestTryWithResourcesOuterJ16");
    // TODO: fails to decompile
    register(JAVA_16, "TestTryWithResourcesFake");
  }
}
