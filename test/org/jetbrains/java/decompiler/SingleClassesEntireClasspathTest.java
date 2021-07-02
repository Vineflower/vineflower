package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.junit.Test;

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

  // TODO: reevaluate behavior, especially with casting
  @Test public void testGenerics() { doTest("pkg/TestGenerics"); }
  @Test public void testClassTypes() { doTest("pkg/TestClassTypes"); }
  @Test public void testClassCast() { doTest("pkg/TestClassCast"); }
  // TODO: intValue() call where there shouldn't be
  @Test
  public void testBoxingConstructor() { doTest("pkg/TestBoxingConstructor"); }
  @Test public void testLocalsSignature() { doTest("pkg/TestLocalsSignature"); }
  @Test public void testShadowing() { doTest("pkg/TestShadowing", "pkg/Shadow", "ext/Shadow", "pkg/TestShadowingSuperClass"); }
  @Test public void testPrimitives() { doTest("pkg/TestPrimitives"); }
  @Test public void testKotlinConstructor() { doTest("pkg/TestKotlinConstructorKt"); }
  @Test public void testVarArgCalls() { doTest("pkg/TestVarArgCalls"); }
  @Test public void testUnionType() { doTest("pkg/TestUnionType"); }
  @Test public void testNamedSuspendFun2Kt() { doTest("pkg/TestNamedSuspendFun2Kt"); }
  @Test public void testTryWithResources() { doTest("pkg/TestTryWithResources"); }
  @Test public void testNestedLoops() { doTest("pkg/TestNestedLoops"); }
  @Test public void testAnonymousClass() { doTest("pkg/TestAnonymousClass"); }
  // TODO: Object[] becomes <unknown>
  @Test public void testObjectArrays() { doTest("pkg/TestObjectArrays"); }
  @Test public void testAnonymousParams() { doTest("pkg/TestAnonymousParams"); }
  @Test public void testThrowException() { doTest("pkg/TestThrowException"); }
  @Test public void testSimpleBytecodeMapping() { doTest("pkg/TestClassSimpleBytecodeMapping"); }
  @Test public void testAnonymousSignature() { doTest("pkg/TestAnonymousSignature"); }
  @Test public void testTryWithResourcesJ16() { doTest("java16/TestTryWithResourcesJ16"); }
  @Test public void testTryWithResourcesCatchJ16() { doTest("java16/TestTryWithResourcesCatchJ16"); }
  @Test public void testTryWithResourcesMultiJ16() { doTest("java16/TestTryWithResourcesMultiJ16"); }
  @Test public void testTryWithResourcesFinallyJ16() { doTest("java16/TestTryWithResourcesFinallyJ16"); }
  @Test public void testTryWithResourcesCatchFinallyJ16() { doTest("java16/TestTryWithResourcesCatchFinallyJ16"); }
  // TODO: returning in finally causes broken control flow and undecompileable methods
  @Test public void testTryWithResourcesReturnJ16() { doTest("java16/TestTryWithResourcesReturnJ16"); }
  @Test public void testTryWithResourcesNestedJ16() { doTest("java16/TestTryWithResourcesNestedJ16"); }
  // TODO: extra casts, var type reverted to object
  @Test public void testTryWithResourcesNullJ16() { doTest("java16/TestTryWithResourcesNullJ16"); }
}
