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
    register(8, "TestEnhancedForLoops");
    register(8, "TestPrimitiveNarrowing");
    register(8, "TestClassFields");
    register(8, "TestInterfaceFields");
    register(8, "TestClassLambda");
    register(8, "TestClassLoop");
    register(8, "TestClassSwitch");
    register(8, "TestClassVar");
    register(8, "TestClassNestedInitializer");
    register(8, "TestDeprecations");
    register(8, "TestExtendsList");
    register(8, "TestMethodParameters");
    register(8, "TestMethodParametersAttr");
    register(8, "TestCodeConstructs");
    register(8, "TestConstants");
    register(8, "TestEnum");
    register(8, "TestDebugSymbols");
    registerRaw(-1, "InvalidMethodSignature");
    register(8, "TestAnonymousClassConstructor");
    register(8, "TestInnerClassConstructor");
    register(-1, "v11/TestInnerClassConstructor");
    register(8, "TestTryCatchFinally");
    register(8, "TestAmbiguousCall");
    register(8, "TestSynchronizedMapping");
    register(8, "TestAbstractMethods");
    register(8, "TestLocalClass");
    register(8, "TestInnerLocal");
    register(8, "TestInnerSignature");
    register(8, "TestParameterizedTypes");
    register(8, "TestStringConcat");
    register(9, "TestJava9StringConcat");
    registerRaw(9, "module-info");
    register(11, "TestJava11StringConcat");
    register(8, "TestMethodReferenceSameName");
    register(8, "TestMethodReferenceLetterClass");
    register(8, "TestConstructorReference");
    register(8, "TestMemberAnnotations");
    register(8, "MoreAnnotations");
    register(8, "TypeAnnotations");
    register(8, "TestStaticNameClash");
    register(8, "TestExtendingSubclass");
    register(8, "TestSyntheticAccess");
    register(-1, "TestIllegalVarName");
    register(8, "TestIffSimplification");
    register(8, "TestAsserts");
    register(8, "TestLocalsNames");
    register(8, "TestAnonymousParamNames");
    register(8, "TestAccessReplace");
    register(8, "TestStringLiterals");

    register(8, "TestClassFields");
    register(8, "TestClashName", "SharedName1",
      "SharedName2", "SharedName3", "SharedName4", "NonSharedName",
      "TestClashNameParent", "ext/TestClashNameParent","TestClashNameIface", "ext/TestClashNameIface");
    register(8, "TestSwitchOnEnum");
    register(8, "TestSwitchOnStrings");
    register(8, "TestLambdaParams");
    register(8, "TestInterfaceMethods");
    register(8, "TestConstType");
    register(-1, "TestPop2OneDoublePop2");
    register(-1, "TestPop2OneLongPop2");
    register(-1, "TestPop2TwoIntPop2");
    register(-1, "TestPop2TwoIntTwoPop");
    register(8, "TestSuperInner", "TestSuperInnerBase");
    register(-1, "TestMissingConstructorCallGood");
    register(-1, "TestMissingConstructorCallBad");
    register(8, "TestEmptyBlocks");
    register(8, "TestInvertedFloatComparison");
    register(8, "TestPrivateEmptyConstructor");
    // TODO: the local variable name there is wildly mangled
    register(-1, "TestSynchronizedUnprotected");
    register(8, "TestInterfaceSuper");
    register(-1, "TestFieldSingleAccess");
    register(8, "package-info");

    register(8, "TestInner2");
    register(8, "TestInUse");

    register(-1, "TestGroovyClass");
    register(-1, "TestGroovyTrait");
    // TODO: This class fails to decompile
//  register("PrivateClasses");
    register(-1, "TestSuspendLambdaKt");
    register(8, "TestGenericArgs");
    register(-1, "records/TestRecordEmpty");
    register(-1, "records/TestRecordSimple");
    register(-1, "records/TestRecordVararg");
    register(-1, "records/TestRecordGenericVararg");
    register(-1, "records/TestRecordAnno");
    // TODO: The (double) in front of the (int) should be removed
    register(8, "TestMultiCast");
    // TODO: The ternary here needs to be removed
    register(8, "TestNestedLambdas");
    register(8, "TestSwitchAssign");
    register(8, "TestSwitchReturn");
    // TODO: Turned into for loops
    register(8, "TestWhileCondition");
    register(8, "TestLocalScopes");
    register(8, "TestInterfaceSubclass");
    register(8, "TestGenericStatic");
    register(8, "TestAssignmentInLoop");
    register(8, "TestArrays");
    register(8, "TestArrayForeach");
    // TODO: I'm pretty sure this test opened the gates of hell somewhere. We need to figure out what's causing that
    register(8, "TestTernaryCall");

    register(8, "TestAnonymousObject");
    register(8, "TestArrayAssignmentEquals");
    // TODO: Loop becomes infinte loop where it should be assignment in loop
    register(8, "TestArrayCopy");
    register(8, "TestArrayDoWhile");
    // TODO: Creating a new object where the array should be set to null
    register(8, "TestArrayNull1");
    // TODO: Object should be int[], cast where there shouldn't be
    register(8, "TestArrayNull2");
    // TODO: Redefinition of array, extra cast
    register(8, "TestArrayNullAccess");
    register(8, "TestArrayTernary");
    // TODO: Do while loops become standard while loops
    register(8, "TestAssignmentInDoWhile");
    // TODO: Assignment of a = a is removed
    register(8, "TestBooleanAssignment");
    register(8, "TestCastPrimitiveToObject");
    register(8, "TestDoWhileTrue");
    register(8, "TestExtraClass");
    // TODO: Object foreach should be generic
    register(8, "TestGenericMapInput");
    register(8, "TestGenericNull");
    register(8, "TestInlineAssignments");
    // TODO: Cast of (Func) is removed
    register(8, "TestInterfaceLambdaCast");
    // TODO: Local scope is removed, replaced with boolean cast
    register(8, "TestLocalScopeClash");
    register(8, "TestMultiBoolean");
    register(8, "TestNestedFor");
    register(8, "TestNestedLoops2");
    register(8, "TestOverloadedNull");
    register(8, "TestReturnIf");
    // TODO: Shift equals is broken, and bitwise should be x & (x >> 2)
    register(8, "TestShiftAssignmentInCall");
    register(8, "TestSplitColorComponents");
    // TODO: extra casts on assignment
    register(8, "TestStaticBlockNull");
    register(8, "TestStringLiteral");
    register(8, "TestSwitchStringHashcodeCollision");
    // TODO: Assignment of o = new Object() is removed
    register(8, "TestSynchronized");
    // TODO: Assignment of o = null is removed, synchronize on null is invalid
    register(8, "TestSynchronizeNull");
    register(8, "TestWhileIterator");
    register(8, "TestReturnTernaryChar");
    register(8, "TestCompoundAssignment");
    // TODO: Fails to decompile empty infinite loop
    register(8, "TestInfiniteLoop");
    // TODO: many problems, wrong else if placement, wrong promotion of while to for, wrong control flow with infinite loop
    register(8, "TestIfLoop");
    register(-1, "TestInheritanceChainCycle");
  }
}
