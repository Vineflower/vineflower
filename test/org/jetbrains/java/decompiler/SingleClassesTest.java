// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

public class SingleClassesTest extends SingleClassesTestBase {
  @Override
  protected String[] getDecompilerOptions() {
    return new String[] {
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.INLINE_SIMPLE_LAMBDAS, "0"
    };
  }

  @Override
  protected void registerAll() {
    register("TestEnhancedForLoops");
    register("TestPrimitiveNarrowing");
    register("TestClassFields");
    register("TestInterfaceFields");
    register("TestClassLambda");
    register("TestClassLoop");
    register("TestClassSwitch");
    register("TestClassVar");
    register("TestClassNestedInitializer");
    register("TestDeprecations");
    register("TestExtendsList");
    register("TestMethodParameters");
    register("TestMethodParametersAttr");
    register("TestCodeConstructs");
    register("TestConstants");
    register("TestEnum");
    register("TestDebugSymbols");
    registerRaw("InvalidMethodSignature");
    register("TestAnonymousClassConstructor");
    register("TestInnerClassConstructor");
    register("v11/TestInnerClassConstructor");
    register("TestTryCatchFinally");
    register("TestAmbiguousCall");
    register("TestAmbiguousCallWithDebugInfo");
    register("TestSynchronizedMapping");
    register("TestAbstractMethods");
    register("TestLocalClass");
    register("TestInnerLocal");
    register("TestInnerSignature");
    register("TestParameterizedTypes");
    register("TestStringConcat");
    register("java9/TestJava9StringConcat");
    register("java9/module-info");
    register("java11/TestJava11StringConcat");
    register("TestMethodReferenceSameName");
    register("TestMethodReferenceLetterClass");
    register("TestConstructorReference");
    register("TestMemberAnnotations");
    register("MoreAnnotations");
    register("TypeAnnotations");
    register("TestStaticNameClash");
    register("TestExtendingSubclass");
    register("TestSyntheticAccess");
    register("TestIllegalVarName");
    register("TestIffSimplification");
    register("TestAsserts");
    register("TestLocalsNames");
    register("TestAnonymousParamNames");
    register("TestAccessReplace");
    register("TestStringLiterals");

    register("TestClassFields");
    register("TestClashName", "SharedName1",
      "SharedName2", "SharedName3", "SharedName4", "NonSharedName",
      "TestClashNameParent", "ext/TestClashNameParent","TestClashNameIface", "ext/TestClashNameIface");
    register("TestSwitchOnEnum");
    register("TestSwitchOnStrings");
    register("TestLambdaParams");
    register("TestInterfaceMethods");
    register("TestConstType");
    register("TestPop2OneDoublePop2");
    register("TestPop2OneLongPop2");
    register("TestPop2TwoIntPop2");
    register("TestPop2TwoIntTwoPop");
    register("TestSuperInner", "TestSuperInnerBase");
    register("TestMissingConstructorCallGood");
    register("TestMissingConstructorCallBad");
    register("TestEmptyBlocks");
    register("TestInvertedFloatComparison");
    register("TestPrivateEmptyConstructor");
    // TODO: the local variable name there is wildly mangled
    register("TestSynchronizedUnprotected");
    register("TestInterfaceSuper");
    register("TestFieldSingleAccess");
    register("package-info");

    register("TestInner2");
    register("TestInUse");

    register("TestGroovyClass");
    register("TestGroovyTrait");
    // TODO: This class fails to decompile
//  register("PrivateClasses");
    register("TestSuspendLambdaKt");
    register("TestGenericArgs");
    register("records/TestRecordEmpty");
    register("records/TestRecordSimple");
    register("records/TestRecordVararg");
    register("records/TestRecordGenericVararg");
    register("records/TestRecordAnno");
    // TODO: The (double) in front of the (int) should be removed
    register("TestMultiCast");
    // TODO: The ternary here needs to be removed
    register("TestNestedLambdas");
    register("TestSwitchAssign");
    register("TestSwitchReturn");
    // TODO: Turned into for loops
    register("TestWhileCondition");
    register("TestLocalScopes");
    register("TestInterfaceSubclass");
    register("TestGenericStatic");
    register("TestAssignmentInLoop");
    register("TestArrays");
    register("TestArrayForeach");
    // TODO: I'm pretty sure this test opened the gates of hell somewhere. We need to figure out what's causing that
    register("TestTernaryCall");

    register("TestAnonymousObject");
    register("TestArrayAssignmentEquals");
    // TODO: Loop becomes infinte loop where it should be assignment in loop
    register("TestArrayCopy");
    register("TestArrayDoWhile");
    // TODO: Creating a new object where the array should be set to null
    register("TestArrayNull1");
    // TODO: Object should be int[], cast where there shouldn't be
    register("TestArrayNull2");
    // TODO: Redefinition of array, extra cast
    register("TestArrayNullAccess");
    register("TestArrayTernary");
    // TODO: Do while loops become standard while loops
    register("TestAssignmentInDoWhile");
    // TODO: Assignment of a = a is removed
    register("TestBooleanAssignment");
    register("TestCastPrimitiveToObject");
    register("TestDoWhileTrue");
    register("TestExtraClass");
    // TODO: Object foreach should be generic
    register("TestGenericMapInput");
    register("TestGenericNull");
    register("TestInlineAssignments");
    // TODO: Cast of (Func) is removed
    register("TestInterfaceLambdaCast");
    // TODO: Local scope is removed, replaced with boolean cast
    register("TestLocalScopeClash");
    register("TestMultiBoolean");
    register("TestNestedFor");
    register("TestNestedLoops2");
    register("TestOverloadedNull");
    register("TestReturnIf");
    // TODO: Shift equals is broken, and bitwise should be x & (x >> 2)
    register("TestShiftAssignmentInCall");
    register("TestSplitColorComponents");
    // TODO: extra casts on assignment
    register("TestStaticBlockNull");
    register("TestStringLiteral");
    register("TestSwitchStringHashcodeCollision");
    // TODO: Assignment of o = new Object() is removed
    register("TestSynchronized");
    // TODO: Assignment of o = null is removed, synchronize on null is invalid
    register("TestSynchronizeNull");
    register("TestWhileIterator");
    register("TestReturnTernaryChar");
    register("TestCompoundAssignment");
    // TODO: Fails to decompile empty infinite loop
    register("TestInfiniteLoop");
    // TODO: many problems, wrong else if placement, wrong promotion of while to for, wrong control flow with infinite loop
    register("TestIfLoop");
    register("TestInheritanceChainCycle");
  }
}
