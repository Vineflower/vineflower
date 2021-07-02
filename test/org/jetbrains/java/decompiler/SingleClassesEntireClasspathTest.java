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
    register(8, "TestGenerics");
    register(8, "TestClassTypes");
    register(8, "TestClassCast");
    // TODO: intValue() call where there shouldn't be
    register(8, "TestBoxingConstructor");
    register(8, "TestLocalsSignature");
    register(8, "TestShadowing", "Shadow", "ext/Shadow", "TestShadowingSuperClass");
    register(8, "TestPrimitives");
    register(-1, "TestKotlinConstructorKt");
    register(8, "TestVarArgCalls");
    register(8, "TestUnionType");
    register(-1, "TestNamedSuspendFun2Kt");
    register(8, "TestTryWithResources");
    register(8, "TestNestedLoops");
    register(8, "TestAnonymousClass");
    // TODO: Object[] becomes <unknown>
    register(8, "TestObjectArrays");
    register(8, "TestAnonymousParams");
    register(8, "TestThrowException");
    register(8, "TestClassSimpleBytecodeMapping");
    register(8, "TestAnonymousSignature");
  }
}
