package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

public class SingleClassesEntireClasspathTest extends SingleClassesTestBase {
  @Override
  protected String[] getDecompilerOptions() {
    return new String[] {
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "1",
      IFernflowerPreferences.INLINE_SIMPLE_LAMBDAS, "0"
    };
  }

  @Override
  protected void registerAll() {
    // TODO: reevaluate behavior, especially with casting
    register("TestGenerics");
    register("TestClassTypes");
    register("TestClassCast");
    // TODO: intValue() call where there shouldn't be
    register("TestBoxingConstructor");
    register("TestLocalsSignature");
    register("TestShadowing", "Shadow", "ext/Shadow", "TestShadowingSuperClass");
    register("TestPrimitives");
    register("TestKotlinConstructorKt");
    register("TestVarArgCalls");
    register("TestUnionType");
    register("TestNamedSuspendFun2Kt");
    register("TestTryWithResources");
    register("TestNestedLoops");
    register("TestAnonymousClass");
    // TODO: Object[] becomes <unknown>
    register("TestObjectArrays");
    register("TestAnonymousParams");
    register("TestThrowException");
    register("TestClassSimpleBytecodeMapping");
    register("TestAnonymousSignature");
  }
}
