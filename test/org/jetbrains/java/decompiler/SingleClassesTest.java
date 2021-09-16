// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.*;

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
    register(JAVA_8, "TestEnhancedForLoops");
    register(JAVA_8, "TestPrimitiveNarrowing");
    register(JAVA_8, "TestClassFields");
    register(JAVA_8, "TestInterfaceFields");
    register(JAVA_8, "TestClassLambda");
    register(JAVA_8, "TestClassLoop");
    register(JAVA_8, "TestClassSwitch");
    register(JAVA_8, "TestClassVar");
    register(JAVA_8, "TestClassNestedInitializer");
    register(JAVA_8, "TestDeprecations");
    register(JAVA_8, "TestExtendsList");
    register(JAVA_8, "TestMethodParameters");
    register(JAVA_8, "TestMethodParametersAttr");
    register(JAVA_8, "TestCodeConstructs");
    register(JAVA_8, "TestConstants");
    register(JAVA_8, "TestEnum");
    register(JAVA_8, "TestDebugSymbols");
    registerRaw(CUSTOM, "InvalidMethodSignature");
    register(JAVA_8, "TestAnonymousClassConstructor");
    register(JAVA_8, "TestInnerClassConstructor");
    register(CUSTOM, "v11/TestInnerClassConstructor");
    register(JAVA_8, "TestTryCatchFinally");
    register(JAVA_8, "TestAmbiguousCall");
    register(JAVA_8, "TestSynchronizedMapping");
    register(JAVA_8, "TestAbstractMethods");
    register(JAVA_8, "TestLocalClass");
    register(JAVA_8, "TestInnerLocal");
    register(JAVA_8, "TestInnerSignature");
    register(JAVA_8, "TestParameterizedTypes");
    register(JAVA_8, "TestStringConcat");
    register(JAVA_9, "TestJava9StringConcat");
    registerRaw(JAVA_9, "module-info");
    register(JAVA_11, "TestJava11StringConcat");
    register(JAVA_8, "TestMethodReferenceSameName");
    register(JAVA_8, "TestMethodReferenceLetterClass");
    register(JAVA_8, "TestConstructorReference");
    register(JAVA_8, "TestMemberAnnotations");
    register(JAVA_8, "MoreAnnotations");
    register(JAVA_8, "TypeAnnotations");
    register(JAVA_8, "TestStaticNameClash");
    register(JAVA_8, "TestExtendingSubclass");
    register(JAVA_8, "TestSyntheticAccess");
    register(KOTLIN, "TestIllegalVarName");
    register(JAVA_8, "TestIffSimplification");
    register(JAVA_8, "TestAsserts");
    register(JAVA_8, "TestLocalsNames");
    register(JAVA_8, "TestAnonymousParamNames");
    register(JAVA_8, "TestAccessReplace");
    register(JAVA_8, "TestStringLiterals");

    register(JAVA_8, "TestClassFields");
    register(JAVA_8, "TestClashName", "SharedName1",
      "SharedName2", "SharedName3", "SharedName4", "NonSharedName",
      "TestClashNameParent", "ext/TestClashNameParent", "TestClashNameIface", "ext/TestClashNameIface");
    register(JAVA_8, "TestSwitchOnEnum");
    register(JAVA_8, "TestSwitchOnStrings");
    register(JAVA_8, "TestLambdaParams");
    register(JAVA_8, "TestInterfaceMethods");
    register(JAVA_8, "TestConstType");
    register(JASM, "TestPop2OneDoublePop2");
    register(JASM, "TestPop2OneLongPop2");
    register(JASM, "TestPop2TwoIntPop2");
    register(JASM, "TestPop2TwoIntTwoPop");
    register(JAVA_8, "TestSuperInner", "TestSuperInnerBase");
    register(JASM, "TestMissingConstructorCallGood");
    register(JASM, "TestMissingConstructorCallBad");
    register(JAVA_8, "TestEmptyBlocks");
    register(JAVA_8, "TestInvertedFloatComparison");
    register(JAVA_8, "TestPrivateEmptyConstructor");
    // TODO: the local variable name there is wildly mangled
    register(KOTLIN, "TestSynchronizedUnprotected");
    register(JAVA_8, "TestInterfaceSuper");
    register(JASM, "TestFieldSingleAccess");
    register(JAVA_8, "package-info");

    register(JAVA_8, "TestInner2");
    register(JAVA_8, "TestInUse");

    register(GROOVY, "TestGroovyClass");
    register(GROOVY, "TestGroovyTrait");
    register(JAVA_8, "TestPrivateClasses");
    register(KOTLIN, "TestSuspendLambdaKt");
    register(JAVA_8, "TestGenericArgs");
    register(JAVA_16, "TestRecordEmpty");
    register(JAVA_16, "TestRecordSimple");
    register(JAVA_16, "TestRecordVararg");
    register(JAVA_16, "TestRecordGenericVararg");
    register(JAVA_16, "TestRecordAnno");
    // TODO: The (double) in front of the (int) should be removed
    register(JAVA_8, "TestMultiCast");
    // TODO: The ternary here needs to be removed
    register(JAVA_8, "TestNestedLambdas");
    register(JAVA_8, "TestSwitchAssign");
    register(JAVA_8, "TestSwitchReturn");
    // TODO: Turned into for loops
    register(JAVA_8, "TestWhileCondition");
    register(JAVA_8, "TestLocalScopes");
    register(JAVA_8, "TestInterfaceSubclass");
    register(JAVA_8, "TestGenericStatic");
    register(JAVA_8, "TestAssignmentInLoop");
    register(JAVA_8, "TestArrays");
    register(JAVA_8, "TestArrayForeach");
    // TODO: I'm pretty sure this test opened the gates of hell somewhere. We need to figure out what's causing that
    register(JAVA_8, "TestTernaryCall");
    register(JAVA_8, "TestAnonymousObject");
    register(JAVA_8, "TestArrayAssignmentEquals");
    // TODO: Loop becomes infinte loop where it should be assignment in loop
    register(JAVA_8, "TestArrayCopy");
    register(JAVA_8, "TestArrayDoWhile");
    // TODO: Creating a new object where the array should be set to null
    register(JAVA_8, "TestArrayNull1");
    // TODO: Object should be int[], cast where there shouldn't be
    register(JAVA_8, "TestArrayNull2");
    // TODO: Redefinition of array, extra cast
    register(JAVA_8, "TestArrayNullAccess");
    register(JAVA_8, "TestArrayTernary");
    // TODO: Do while loops become standard while loops
    register(JAVA_8, "TestAssignmentInDoWhile");
    // TODO: Assignment of a = a is removed
    register(JAVA_8, "TestBooleanAssignment");
    register(JAVA_8, "TestCastPrimitiveToObject");
    register(JAVA_8, "TestDoWhileTrue");
    register(JAVA_8, "TestExtraClass");
    // TODO: Object foreach should be generic
    register(JAVA_8, "TestGenericMapInput");
    register(JAVA_8, "TestGenericNull");
    register(JAVA_8, "TestInlineAssignments");
    // TODO: Cast of (Func) is removed
    register(JAVA_8, "TestInterfaceLambdaCast");
    // TODO: Local scope is removed, replaced with boolean cast
    register(JAVA_8, "TestLocalScopeClash");
    register(JAVA_8, "TestMultiBoolean");
    register(JAVA_8, "TestNestedFor");
    register(JAVA_8, "TestNestedLoops2");
    register(JAVA_8, "TestOverloadedNull");
    register(JAVA_8, "TestReturnIf");
    // TODO: Shift equals is broken, and bitwise should be x & (x >> 2)
    register(JAVA_8, "TestShiftAssignmentInCall");
    register(JAVA_8, "TestSplitColorComponents");
    // TODO: extra casts on assignment
    register(JAVA_8, "TestStaticBlockNull");
    register(JAVA_8, "TestStringLiteral");
    register(JAVA_8, "TestSwitchStringHashcodeCollision");
    // TODO: Assignment of o = new Object() is removed
    register(JAVA_8, "TestSynchronized");
    // TODO: Assignment of o = null is removed, synchronize on null is invalid
    register(JAVA_8, "TestSynchronizeNull");
    register(JAVA_8, "TestWhileIterator");
    register(JAVA_8, "TestReturnTernaryChar");
    register(JAVA_8, "TestCompoundAssignment");
    register(JAVA_8, "TestInfiniteLoop");
    // TODO: many problems, wrong else if placement, wrong promotion of while to for, wrong control flow with infinite loop
    register(JAVA_8, "TestIfLoop");
    // Be careful when touching this class file, your IDE might freeze
    register(JASM, "TestInheritanceChainCycle");
    register(JAVA_16, "TestRecordEmptyConstructor");
    register(JAVA_16, "TestRecordInner");
    register(JAVA_16, "TestRecordMixup");
    register(JAVA_8, "TestMultiAssignmentInStaticBlock");
    register(JAVA_8, "TestNextGaussian");
    register(JAVA_8, "TestLoopBreak");
    register(JAVA_8, "TestLoopBreak2");
    register(JAVA_8, "TestSimpleWhile");
    register(JAVA_8, "TestLoopBreakException");
    register(JAVA_8, "TestWhileTernary1");
    register(JAVA_8, "TestWhileTernary2");
    register(JAVA_8, "TestWhileTernaryFake");
    register(JAVA_8, "TestWhileTernary3");
    register(JAVA_8, "TestWhileTernary4");
    register(JAVA_8, "TestWhileTernary5");
    // TODO: do-while loop fails to inline ternary
    register(JAVA_8, "TestWhileTernary6");
    register(JAVA_8, "TestWhileTernary7");
    // TODO: complex ternaries are not supported
    register(JAVA_8, "TestWhileTernary8");
    register(JAVA_8, "TestTryLoop");
    register(JAVA_8, "TestTryLoopRecompile");
    register(JAVA_8, "TestTryLoopSimpleFinally");
    // TODO: Still doesn't properly decompile, loop needs to be in the try block
    register(JAVA_8, "TestTryLoopReturnFinally");
    register(JAVA_8, "TestOperatorPrecedence");
    register(JAVA_8, "TestMultipleStaticBlocks");
    register(JAVA_8, "TestTrySynchronized");
    // TODO: fields must be placed after enum members
    register(JASM, "TestEnumStaticField");
    // Noted to say this would produce different code each time but it does not from testing
    register(JASM, "TestIrreducible");
    register(JAVA_8, "TestForContinue");
    // TODO: this needs a flat statement structure
    register(JAVA_8, "TestExceptionElse");
    register(JAVA_8, "TestGenericCasts");
    register(JAVA_8, "TestNativeMethods");
    register(JAVA_8, "TestThrowLoop");
    register(JAVA_8, "TestShiftLoop");
    register(JASM, "TestDoubleCast");
    register(JAVA_16, "TestLocalEnum");
    register(JAVA_16, "TestLocalInterface");
    register(JAVA_16, "TestLocalRecord");
    register(JAVA_9, "TestPrivateInterfaceMethod");

    register(JAVA_16, "TestAssignmentSwitchExpression1");
    register(JAVA_16, "TestAssignmentSwitchExpression2");
    register(JAVA_16, "TestAssignmentSwitchExpression3");
    register(JAVA_16, "TestAssignmentSwitchExpression4");
    register(JAVA_16, "TestAssignmentSwitchExpression5");
    register(JAVA_16, "TestAssignmentSwitchExpression6");
    register(JAVA_16, "TestInlineSwitchExpression1");
    register(JAVA_16, "TestInlineSwitchExpression2");
    register(JAVA_16, "TestInlineSwitchExpression3");
    register(JAVA_16, "TestInlineSwitchExpression4");
    register(JAVA_16, "TestReturnSwitchExpression1");
    register(JAVA_16, "TestReturnSwitchExpression2");
    register(JAVA_16, "TestReturnSwitchExpression3");
    register(JAVA_16, "TestReturnSwitchExpression4");
    register(JAVA_16, "TestConstructorSwitchExpression1");
    register(JAVA_16, "TestConstructorSwitchExpression2");

    register(JAVA_16_PREVIEW, "TestSealedClasses");
    register(JAVA_16_PREVIEW, "PermittedSubClassA", "TestSealedClasses");
    register(JAVA_16_PREVIEW, "PermittedSubClassB", "PermittedSubClassA", "TestSealedClasses");
    register(JAVA_16_PREVIEW, "TestSealedInterfaces");
    register(JAVA_16_PREVIEW, "PermittedSubInterfaceA", "TestSealedInterfaces");
    register(JAVA_16_PREVIEW, "PermittedSubInterfaceB", "PermittedSubInterfaceA", "TestSealedInterfaces");
    register(JAVA_16_PREVIEW, "PermittedSubClassC", "TestSealedInterfaces");
    register(JAVA_16_PREVIEW, "PermittedSubClassD", "PermittedSubClassC", "TestSealedInterfaces");
    register(JAVA_16_PREVIEW, "PermittedSubClassE", "TestSealedInterfaces");

    register(JAVA_8_NODEBUG, "TestDuplicateLocals");

    register(JAVA_8, "TestMethodHandles");
    register(JAVA_9, "TestVarHandles");
    // TODO: fix duplicate naming, propagate renames to all method calls
    register(JASM, "TestIllegalMethodNames");
    register(JAVA_8, "TestSwitchOnlyDefault");
    register(JAVA_8, "TestSwitchEmpty");
    register(JAVA_8, "TestSwitchDefaultBefore");
    // TODO: fix all the <unknown>s
    register(JAVA_8_NODEBUG, "TestIterationOverGenericsWithoutLvt");
    register(JAVA_8_NODEBUG, "TestIterationOverGenericsWithoutLvt1");


    // TODO: "3;" is generally not considered valid java code, fix ternaries not being simplified
    register(JAVA_8, "TestNestedTernaryAssign");
  }
}
