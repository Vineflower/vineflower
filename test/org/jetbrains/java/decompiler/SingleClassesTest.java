// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.junit.Test;

public class SingleClassesTest extends SingleClassesTestBase {
  protected DecompilerTestFixture fixture;
  
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
  @Test public void testEnhancedForLoops() { doTest("pkg/TestEnhancedForLoops"); }
  @Test public void testPrimitiveNarrowing() { doTest("pkg/TestPrimitiveNarrowing"); }
  @Test public void testClassFields() { doTest("pkg/TestClassFields"); }
  @Test public void testInterfaceFields() { doTest("pkg/TestInterfaceFields"); }
  @Test public void testClassLambda() { doTest("pkg/TestClassLambda"); }
  @Test public void testClassLoop() { doTest("pkg/TestClassLoop"); }
  @Test public void testClassSwitch() { doTest("pkg/TestClassSwitch"); }
  @Test public void testClassTypes() { doTest("pkg/TestClassTypes"); }
  @Test public void testClassVar() { doTest("pkg/TestClassVar"); }
  @Test public void testClassNestedInitializer() { doTest("pkg/TestClassNestedInitializer"); }
  @Test public void testClassCast() { doTest("pkg/TestClassCast"); }
  @Test public void testDeprecations() { doTest("pkg/TestDeprecations"); }
  @Test public void testExtendsList() { doTest("pkg/TestExtendsList"); }
  @Test public void testMethodParameters() { doTest("pkg/TestMethodParameters"); }
  @Test public void testMethodParametersAttr() { doTest("pkg/TestMethodParametersAttr"); }
  @Test public void testCodeConstructs() { doTest("pkg/TestCodeConstructs"); }
  @Test public void testConstants() { doTest("pkg/TestConstants"); }
  @Test public void testEnum() { doTest("pkg/TestEnum"); }
  @Test public void testDebugSymbols() { doTest("pkg/TestDebugSymbols"); }
  @Test public void testInvalidMethodSignature() { doTest("InvalidMethodSignature"); }
  @Test public void testAnonymousClassConstructor() { doTest("pkg/TestAnonymousClassConstructor"); }
  @Test public void testInnerClassConstructor() { doTest("pkg/TestInnerClassConstructor"); }
  @Test public void testInnerClassConstructor11() { doTest("v11/TestInnerClassConstructor"); }
  @Test public void testTryCatchFinally() { doTest("pkg/TestTryCatchFinally"); }
  @Test public void testAmbiguousCall() { doTest("pkg/TestAmbiguousCall"); }
  @Test public void testAmbiguousCallWithDebugInfo() { doTest("pkg/TestAmbiguousCallWithDebugInfo"); }
  @Test public void testSimpleBytecodeMapping() { doTest("pkg/TestClassSimpleBytecodeMapping"); }
  @Test public void testSynchronizedMapping() { doTest("pkg/TestSynchronizedMapping"); }
  @Test public void testAbstractMethods() { doTest("pkg/TestAbstractMethods"); }
  @Test public void testLocalClass() { doTest("pkg/TestLocalClass"); }
  @Test public void testAnonymousClass() { doTest("pkg/TestAnonymousClass"); }
  @Test public void testThrowException() { doTest("pkg/TestThrowException"); }
  @Test public void testInnerLocal() { doTest("pkg/TestInnerLocal"); }
  @Test public void testInnerSignature() { doTest("pkg/TestInnerSignature"); }
  @Test public void testAnonymousSignature() { doTest("pkg/TestAnonymousSignature"); }
  @Test public void testLocalsSignature() { doTest("pkg/TestLocalsSignature"); }
  @Test public void testParameterizedTypes() { doTest("pkg/TestParameterizedTypes"); }
  @Test public void testShadowing() { doTest("pkg/TestShadowing", "pkg/Shadow", "ext/Shadow", "pkg/TestShadowingSuperClass"); }
  @Test public void testStringConcat() { doTest("pkg/TestStringConcat"); }
  @Test public void testJava9StringConcat() { doTest("java9/TestJava9StringConcat"); }
  @Test public void testJava9ModuleInfo() { doTest("java9/module-info"); }
  @Test public void testJava11StringConcat() { doTest("java11/TestJava11StringConcat"); }
  @Test public void testMethodReferenceSameName() { doTest("pkg/TestMethodReferenceSameName"); }
  @Test public void testMethodReferenceLetterClass() { doTest("pkg/TestMethodReferenceLetterClass"); }
  @Test public void testConstructorReference() { doTest("pkg/TestConstructorReference"); }
  @Test public void testMemberAnnotations() { doTest("pkg/TestMemberAnnotations"); }
  @Test public void testMoreAnnotations() { doTest("pkg/MoreAnnotations"); }
  @Test public void testTypeAnnotations() { doTest("pkg/TypeAnnotations"); }
  @Test public void testStaticNameClash() { doTest("pkg/TestStaticNameClash"); }
  @Test public void testExtendingSubclass() { doTest("pkg/TestExtendingSubclass"); }
  @Test public void testSyntheticAccess() { doTest("pkg/TestSyntheticAccess"); }
  @Test public void testIllegalVarName() { doTest("pkg/TestIllegalVarName"); }
  @Test public void testIffSimplification() { doTest("pkg/TestIffSimplification"); }
  @Test public void testKotlinConstructor() { doTest("pkg/TestKotlinConstructorKt"); }
  @Test public void testAsserts() { doTest("pkg/TestAsserts"); }
  @Test public void testLocalsNames() { doTest("pkg/TestLocalsNames"); }
  @Test public void testAnonymousParamNames() { doTest("pkg/TestAnonymousParamNames"); }
  @Test public void testAnonymousParams() { doTest("pkg/TestAnonymousParams"); }
  @Test public void testAccessReplace() { doTest("pkg/TestAccessReplace"); }
  @Test public void testStringLiterals() { doTest("pkg/TestStringLiterals"); }
  @Test public void testPrimitives() { doTest("pkg/TestPrimitives"); }
  @Test public void testClashName() { doTest("pkg/TestClashName", "pkg/SharedName1",
          "pkg/SharedName2", "pkg/SharedName3", "pkg/SharedName4", "pkg/NonSharedName",
          "pkg/TestClashNameParent", "ext/TestClashNameParent","pkg/TestClashNameIface", "ext/TestClashNameIface"); }
  @Test public void testSwitchOnEnum() { doTest("pkg/TestSwitchOnEnum");}
  @Test public void testSwitchOnStrings() { doTest("pkg/TestSwitchOnStrings");}
  @Test public void testVarArgCalls() { doTest("pkg/TestVarArgCalls"); }
  @Test public void testLambdaParams() { doTest("pkg/TestLambdaParams"); }
  @Test public void testInterfaceMethods() { doTest("pkg/TestInterfaceMethods"); }
  @Test public void testConstType() { doTest("pkg/TestConstType"); }
  @Test public void testPop2OneDoublePop2() { doTest("pkg/TestPop2OneDoublePop2"); }
  @Test public void testPop2OneLongPop2() { doTest("pkg/TestPop2OneLongPop2"); }
  @Test public void testPop2TwoIntPop2() { doTest("pkg/TestPop2TwoIntPop2"); }
  @Test public void testPop2TwoIntTwoPop() { doTest("pkg/TestPop2TwoIntTwoPop"); }
  @Test public void testSuperInner() { doTest("pkg/TestSuperInner", "pkg/TestSuperInnerBase"); }
  @Test public void testMissingConstructorCallGood() { doTest("pkg/TestMissingConstructorCallGood"); }
  @Test public void testMissingConstructorCallBad() { doTest("pkg/TestMissingConstructorCallBad"); }
  @Test public void testEmptyBlocks() { doTest("pkg/TestEmptyBlocks"); }
  @Test public void testInvertedFloatComparison() { doTest("pkg/TestInvertedFloatComparison"); }
  @Test public void testPrivateEmptyConstructor() { doTest("pkg/TestPrivateEmptyConstructor"); }
  // TODO: the local variable name there is wildly mangled
  @Test public void testSynchronizedUnprotected() { doTest("pkg/TestSynchronizedUnprotected"); }
  @Test public void testInterfaceSuper() { doTest("pkg/TestInterfaceSuper"); }
  @Test public void testFieldSingleAccess() { doTest("pkg/TestFieldSingleAccess"); }
  @Test public void testPackageInfo() { doTest("pkg/package-info"); }

  @Test public void testUnionType() { doTest("pkg/TestUnionType"); }
  @Test public void testInnerClassConstructor2() { doTest("pkg/TestInner2"); }
  @Test public void testInUse() { doTest("pkg/TestInUse"); }

  @Test public void testGroovyClass() { doTest("pkg/TestGroovyClass"); }
  @Test public void testGroovyTrait() { doTest("pkg/TestGroovyTrait"); }
  // TODO: This class fails to decompile
//  @Test public void testPrivateClasses() { doTest("pkg/PrivateClasses"); }
  @Test public void testSuspendLambda() { doTest("pkg/TestSuspendLambdaKt"); }
  @Test public void testNamedSuspendFun2Kt() { doTest("pkg/TestNamedSuspendFun2Kt"); }
  @Test public void testGenericArgs() { doTest("pkg/TestGenericArgs"); }
  @Test public void testRecordEmpty() { doTest("records/TestRecordEmpty"); }
  @Test public void testRecordSimple() { doTest("records/TestRecordSimple"); }
  @Test public void testRecordVararg() { doTest("records/TestRecordVararg"); }
  @Test public void testRecordGenericVararg() { doTest("records/TestRecordGenericVararg"); }
  @Test public void testRecordAnno() { doTest("records/TestRecordAnno"); }
  @Test public void testTryWithResources() { doTest("pkg/TestTryWithResources"); }
  // TODO: The (double) in front of the (int) should be removed
  @Test public void testMultiCast() { doTest("pkg/TestMultiCast"); }
  // TODO: Fails to decompile
  @Test public void testNestedLoops() { doTest("pkg/TestNestedLoops"); }
  // TODO: The ternary here needs to be removed
  @Test public void testNestedLambdas() { doTest("pkg/TestNestedLambdas"); }
  @Test public void testSwitchAssign() { doTest("pkg/TestSwitchAssign"); }
  @Test public void testSwitchReturn() { doTest("pkg/TestSwitchReturn"); }
  // TODO: Turned into for loops
  @Test public void testWhileCondition() { doTest("pkg/TestWhileCondition"); }
  @Test public void testLocalScopes() { doTest("pkg/TestLocalScopes"); }
  @Test public void testInterfaceSubclass() { doTest("pkg/TestInterfaceSubclass"); }
  @Test public void testGenericStatic() { doTest("pkg/TestGenericStatic"); }
  @Test public void testAssignmentInLoop() { doTest("pkg/TestAssignmentInLoop"); }
  @Test public void testArrays() { doTest("pkg/TestArrays"); }
  @Test public void testArrayForeach() { doTest("pkg/TestArrayForeach"); }
  // TODO: I'm pretty sure this test opened the gates of hell somewhere. We need to figure out what's causing that
  @Test public void testTernaryCall() { doTest("pkg/TestTernaryCall"); }

  @Test public void testAnonymousObject() { doTest("pkg/TestAnonymousObject"); }
  @Test public void testArrayAssignmentEquals() { doTest("pkg/TestArrayAssignmentEquals"); }
  // TODO: Loop becomes infinte loop where it should be assignment in loop
  @Test public void testArrayCopy() { doTest("pkg/TestArrayCopy"); }
  @Test public void testArrayDoWhile() { doTest("pkg/TestArrayDoWhile"); }
  // TODO: Creating a new object where the array should be set to null
  @Test public void testArrayNull1() { doTest("pkg/TestArrayNull1"); }
  // TODO: Object should be int[], cast where there shouldn't be
  @Test public void testArrayNull2() { doTest("pkg/TestArrayNull2"); }
  // TODO: Redefinition of array, extra cast
  @Test public void testArrayNullAccess() { doTest("pkg/TestArrayNullAccess"); }
  @Test public void testArrayTernary() { doTest("pkg/TestArrayTernary"); }
  // TODO: Do while loops become standard while loops
  @Test public void testAssignmentInDoWhile() { doTest("pkg/TestAssignmentInDoWhile"); }
  // TODO: Assignment of a = a is removed
  @Test public void testBooleanAssignment() { doTest("pkg/TestBooleanAssignment"); }
  // TODO: intValue() call where there shouldn't be
  @Test public void testBoxingConstructor() { doTest("pkg/TestBoxingConstructor"); }
  @Test public void testCastPrimitiveToObject() { doTest("pkg/TestCastPrimitiveToObject"); }
  @Test public void testDoWhileTrue() { doTest("pkg/TestDoWhileTrue"); }
  @Test public void testExtraClass() { doTest("pkg/TestExtraClass"); }
  // TODO: Object foreach should be generic
  @Test public void testGenericMapInput() { doTest("pkg/TestGenericMapInput"); }
  @Test public void testGenericNull() { doTest("pkg/TestGenericNull"); }
  @Test public void testInlineAssignments() { doTest("pkg/TestInlineAssignments"); }
  // TODO: Cast of (Func) is removed
  @Test public void testInterfaceLambdaCast() { doTest("pkg/TestInterfaceLambdaCast"); }
  // TODO: Local scope is removed, replaced with boolean cast
  @Test public void testLocalScopeClash() { doTest("pkg/TestLocalScopeClash"); }
  @Test public void testMultiBoolean() { doTest("pkg/TestMultiBoolean"); }
  @Test public void testNestedFor() { doTest("pkg/TestNestedFor"); }
  @Test public void testNestedLoops2() { doTest("pkg/TestNestedLoops2"); }
  // TODO: Object[] becomes <unknown>
  @Test public void testObjectArrays() { doTest("pkg/TestObjectArrays"); }
  @Test public void testOverloadedNull() { doTest("pkg/TestOverloadedNull"); }
  @Test public void testReturnIf() { doTest("pkg/TestReturnIf"); }
  // TODO: Shift equals is broken, and bitwise should be x & (x >> 2)
  @Test public void testShiftAssignmentInCall() { doTest("pkg/TestShiftAssignmentInCall"); }
  @Test public void testSplitColorComponents() { doTest("pkg/TestSplitColorComponents"); }
  // TODO: extra casts on assignment
  @Test public void testStaticBlockNull() { doTest("pkg/TestStaticBlockNull"); }
  @Test public void testStringLiteral() { doTest("pkg/TestStringLiteral"); }
  @Test public void testSwitchStringHashcodeCollision() { doTest("pkg/TestSwitchStringHashcodeCollision"); }
  // TODO: Assignment of o = new Object() is removed
  @Test public void testSynchronized() { doTest("pkg/TestSynchronized"); }
  // TODO: Assignment of o = null is removed, synchronize on null is invalid
  @Test public void testSynchronizeNull() { doTest("pkg/TestSynchronizeNull"); }
  @Test public void testWhileIterator() { doTest("pkg/TestWhileIterator"); }

}
