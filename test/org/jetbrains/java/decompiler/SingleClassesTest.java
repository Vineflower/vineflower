// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.*;

public class SingleClassesTest extends SingleClassesTestBase {
  @Override
  protected void registerAll() {
    registerSet("Default", this::registerDefault,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.TERNARY_CONDITIONS, "1",
      IFernflowerPreferences.FORCE_JSR_INLINE, "1"
    );
    registerSet("Entire Classpath", this::registerEntireClassPath,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "1"
    );
    registerSet("Java Runtime", this::registerJavaRuntime,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_JAVA_RUNTIME, "1"
    );
    registerSet("Literals", this::registerLiterals,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.LITERALS_AS_IS, "0"
    );
    registerSet("Pattern Matching", this::registerPatternMatching,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.PATTERN_MATCHING, "1"
    );
    registerSet("Ternary Constant Simplification", this::registerTernaryConstantSimplification,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.LITERALS_AS_IS, "0",
      IFernflowerPreferences.TERNARY_CONSTANT_SIMPLIFICATION, "1"
    );
    registerSet("LVT", this::registerLVT,
      IFernflowerPreferences.DECOMPILE_INNER, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1",
      IFernflowerPreferences.ASCII_STRING_CHARACTERS, "1",
      IFernflowerPreferences.REMOVE_SYNTHETIC, "1",
      IFernflowerPreferences.REMOVE_BRIDGE, "1",
      IFernflowerPreferences.USE_DEBUG_VAR_NAMES, "1"
    );
    registerSet("JAD Naming", () -> {
      register(JAVA_8, "TestJADNaming");
    },IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.USE_JAD_VARNAMING, "1"
    );
    registerSet("Try Loop", this::registerTryLoop,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.TRY_LOOP_FIX, "1"
    );
    registerSet("Javadoc", () -> {
      register(JAVA_8, "TestJavadoc");
    }, IFabricJavadocProvider.PROPERTY_NAME, new IFabricJavadocProvider() {
      @Override
      public String getClassDoc(StructClass structClass) {
        return "Class javadoc for '" + structClass.qualifiedName + "'";
      }

      @Override
      public String getFieldDoc(StructClass structClass, StructField structField) {
        return "Field javadoc for '" + structField.getName() + "'";
      }

      @Override
      public String getMethodDoc(StructClass structClass, StructMethod structMethod) {
        return "Method javadoc for '" + structMethod.getName() + "'";
      }
    });
    // TODO: converter renaming different on different platforms?
    registerSet("Renaming", () -> registerFailable(JAVA_8, "TestRenameEntities"),
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.RENAME_ENTITIES, "1"
    );
    registerSet("Complex Condys", () -> register(JASM, "TestComplexCondy"),
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "0",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "0",
      IFernflowerPreferences.DECOMPILE_COMPLEX_CONDYS, "1",
      IFernflowerPreferences.PREFERRED_LINE_LENGTH, "250"
    );
    // TODO: user renamer class test
  }

  private void registerDefault() {
    register(JAVA_8, "TestEnhancedForLoops");
    register(JAVA_8, "TestPrimitiveNarrowing");
    register(JAVA_8, "TestClassFields");
    register(JAVA_8, "TestInterfaceFields");
    register(JAVA_8, "TestClassLambda");
    // TODO: testLoopIpp eliminates loops that it should not!
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
    register(JAVA_8, "TestTryFinally");
    register(JAVA_8, "TestAmbiguousCall");
    register(JAVA_8, "TestSynchronizedMapping");
    register(JAVA_8, "TestAbstractMethods");
    register(JAVA_8, "TestLocalClass");
    register(JAVA_8, "TestChainedCFG");
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
    // TODO: this doesn't compile
    register(JASM, "TestOldECJInner");
    register(JASM, "TestRecursiveLambda");
    register(JAVA_8, "TestEmptyBlocks");
    register(JAVA_8, "TestInvertedFloatComparison");
    register(JAVA_8, "TestPrivateEmptyConstructor");
    register(JAVA_8, "TestQualifiedNew");
    register(KOTLIN, "TestSynchronizedUnprotected");
    register(JAVA_8, "TestInterfaceSuper");
    register(JASM, "TestFieldSingleAccess");
    register(JAVA_8, "package-info");

    register(JAVA_8, "TestInner2");
    register(JAVA_8, "TestInUse");

    register(GROOVY, "TestGroovyClass");
    register(GROOVY, "TestGroovyTrait");
    register(GROOVY, "TestGroovyTryCatch");
    register(JAVA_8, "TestPrivateClasses");
    register(KOTLIN, "TestSuspendLambdaKt");
    register(KOTLIN, "TestRunSuspend");
    register(JAVA_8, "TestGenericArgs");
    register(JAVA_16, "TestRecordEmpty");
    register(JAVA_16, "TestRecordSimple");
    register(JAVA_16, "TestRecordVararg");
    register(JAVA_16, "TestRecordGenericVararg");
    register(JAVA_16, "TestRecordAnno");
    register(JAVA_16, "TestRecordBig");
    register(JAVA_16, "TestRecordGenericSuperclass");
    // TODO: The (double) in front of the (int) should be removed
    register(JAVA_8, "TestMultiCast");
    // TODO: some tests don't have proper if else chains
    register(JAVA_8, "TestComplexIfElseChain");
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
    register(JAVA_8, "TestArrayInitializations");
    register(JAVA_8, "TestTernaryCall");
    register(JAVA_8, "TestAnonymousObject");
    register(JAVA_8, "TestArrayAssignmentEquals");
    register(JAVA_8, "TestArrayCopy");
    register(JAVA_8, "TestArrayDoWhile");
    register(JAVA_8_NODEBUG, "TestArrayNull1");
    // TODO: Object should be int[], cast where there shouldn't be
    register(JAVA_8_NODEBUG, "TestArrayNull2");
    // TODO: Redefinition of array, extra cast
    register(JAVA_8, "TestArrayNullAccess");
    register(JAVA_8, "TestArrayTernary");
    // TODO: Do while loops become standard while loops
    register(JAVA_8, "TestAssignmentInDoWhile");
    register(JAVA_8, "TestBooleanAssignment");
    // TODO: Extraneous cast to boolean
    register(JAVA_8, "TestCastObjectToPrimitive");
    register(JAVA_8, "TestCastPrimitiveToObject");
    register(JAVA_8, "TestDoWhileTrue");
    register(JAVA_8, "TestExtraClass");
    // TODO: Object foreach should be generic
    register(JAVA_8, "TestGenericMapInput");
    register(JAVA_8, "TestGenericNull");
    register(JAVA_8, "TestInlineAssignments");
    // TODO: Cast of (Func) is removed
    register(JAVA_8, "TestInterfaceLambdaCast");
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
    register(JAVA_8, "TestSynchronized");
    register(JAVA_8, "TestSynchronizedLoop");
    register(JAVA_8, "TestSynchronizedTry");
    register(JAVA_8, "TestSynchronizedThrow");
    register(JAVA_8, "TestSynchronizeNull");
    // derived from: IDEA-180373
    register(JAVA_8, "TestSynchronizedTrySharing");
    register(JAVA_8, "TestWhileIterator");
    register(JAVA_8, "TestReturnTernaryChar");
    register(JAVA_8, "TestCompoundAssignment");
    register(JAVA_8, "TestInfiniteLoop");
    register(JAVA_8, "TestIfLoop");
    // Be careful when touching this class file, your IDE might freeze
    register(JASM, "TestInheritanceChainCycle");
    register(JAVA_16, "TestRecordEmptyConstructor");
    register(JAVA_16, "TestRecordInner");
    register(JAVA_16, "TestRecordMixup");
    register(JAVA_8, "TestMultiAssignmentInStaticBlock");
    register(JAVA_8, "TestMultiAssignmentInDynamicBlock");
    register(JAVA_8, "TestNextGaussian");
    register(JAVA_8, "TestLongMethodDeclaration");
    register(JAVA_8, "TestLongMethodInvocation");
    register(JAVA_8, "TestBinaryOperationWrapping");
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
    register(JAVA_8, "TestWhileTernary6");
    register(JAVA_8, "TestWhileTernary7");
    register(JAVA_8, "TestWhileTernary8");
    register(JAVA_8, "TestWhileTernary9");
    register(JAVA_8, "TestWhileTernary10");

    // TODO: multiple problems with ++/--
    register(JAVA_8, "TestOperatorPrecedence");
    register(JAVA_8, "TestMultipleStaticBlocks");
    register(JAVA_8, "TestTrySynchronized");
    register(JASM, "TestEnumStaticField");
    register(JASM, "TestEnumStaticField2");
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
    register(JASM, "TestDoublePopAfterJump");
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
    register(JAVA_16, "TestAssignmentSwitchExpression7");

    register(JAVA_16, "TestBooleanSwitchExpression1");
    register(JAVA_16, "TestBooleanSwitchExpression2");
    register(JAVA_16, "TestBooleanSwitchExpression3");
    register(JAVA_16, "TestBooleanSwitchExpression4");
    register(JAVA_16, "TestBooleanSwitchExpression5");

    register(JAVA_16, "TestInlineSwitchExpression1", "ext/Direction");
    register(JAVA_16, "TestInlineSwitchExpression2");
    register(JAVA_16, "TestInlineSwitchExpression3", "ext/Direction");
    register(JAVA_16, "TestInlineSwitchExpression4");
    register(JAVA_16, "TestInlineSwitchExpression5");
    register(JAVA_16, "TestInlineSwitchExpression6");

    register(JAVA_16, "TestReturnSwitchExpression1");
    register(JAVA_16, "TestReturnSwitchExpression2");
    register(JAVA_16, "TestReturnSwitchExpression3");
    register(JAVA_16, "TestReturnSwitchExpression4");

    register(JAVA_16, "TestConstructorSwitchExpression1");
    register(JAVA_16, "TestConstructorSwitchExpression2");
    register(JAVA_16, "TestAssertSwitchExpression");
    register(JAVA_16, "TestSwitchExpressionFallthrough1");
    register(JAVA_16, "TestConstructorSwitchExpression3");
    register(JAVA_16, "TestSwitchExpressionPPMM");
    // TODO: inner switch expression not created
    register(JAVA_16, "TestSwitchExpressionNested1");
    register(JAVA_16, "TestSwitchExprInvoc");

    register(JAVA_16, "TestAccidentalSwitchExpression");

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
    register(JAVA_8_NODEBUG, "TestIterationOverGenericsWithoutLvt");
    register(JAVA_8_NODEBUG, "TestIterationOverGenericsWithoutLvt1");

    // TODO: "3;" is generally not considered valid java code, fix ternaries not being simplified
    register(JAVA_8, "TestNestedTernaryAssign");
    register(JAVA_8, "TestNestedTernaryCondition");

    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching1");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching2");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching3");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching4");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching5");
    // null labels fall under Pattern Matching for Switch
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching6", "ext/Direction");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching7");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching8");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching9");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching10");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching11");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching12");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching13");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching14");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching15");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching16");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching17");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching18");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching19");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching20");
    // TODO: <unknown> variable, wrong variables are being used.
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching21");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatching22");

    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingLoop");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingInstanceof1");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingInstanceof2");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingInstanceof3");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingInstanceof4");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingReturn1");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingReturn2");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingConstructor1");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingConstructor2");
    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingWithNull");

    register(JAVA_17_PREVIEW, "TestSwitchPatternMatchingFuzz1");
    
    // TODO: non-resugared record patterns reference hidden proxy methods
    register(JAVA_19_PREVIEW, "TestRecordPattern1");
    register(JAVA_19_PREVIEW, "TestRecordPattern2");
    register(JAVA_19_PREVIEW, "TestRecordPattern3");
    // TODO: incorrect use of reassigned variable `var21` in `test2`
    register(JAVA_19_PREVIEW, "TestRecordPattern4");

    register(JASM, "TestCondy");
    register(JASM, "TestBackwardsExceptionHandler");
    register(JASM, "TestLeakyMethod");
    // TODO: issue #164: produces `for (var1 : var1)`
    //   seems to be caused by variable merging
    register(JASM, "TestSelfIterableLoop");

    register(JAVA_8, "TestStaticInit");
    register(JAVA_8_NODEBUG, "TestDoubleNestedClass");
    register(JAVA_8, "TestDuplicateSwitchLocals");

    register(JAVA_8, "TestIfTernary1");
    register(JAVA_8, "TestIfTernary2");
    register(JAVA_8, "TestIfTernary3");
    register(JAVA_8, "TestIfTernaryReturn");
    register(JAVA_8, "TestIfElseTernary1");

    register(JAVA_17, "TestIfTernary1J17");
    register(JAVA_17, "TestIfElseTernary1J17");

    register(JAVA_8, "TestSimpleIf");
    //TODO: figure out why there's no successor
    register(JAVA_8, "TestInlineNoSuccessor");

    register(JAVA_8, "TestEnumArrayStaticInit");

    register(JAVA_16, "TestAssertJ16");

    register(JAVA_8, "TestSynchronizedTernary");

    register(JAVA_8, "TestUnicodeIdentifiers");
    register(JAVA_8, "TestDoubleBraceInitializers");

    register(JAVA_8, "TestPPMM");
    register(JAVA_8, "TestPPMMByte");
    register(JAVA_8, "TestPPMMLong");
    register(JAVA_8, "TestPPMMBoxed");
    register(JAVA_8, "TestPPMMOnObjectField");
    register(JAVA_8, "TestPPMMOnStaticField");
    register(JAVA_17, "TestImplicitlySealedEnum");
    register(JAVA_16, "TestTextBlocks");
    registerRaw(CUSTOM, "Java14Test"); // Used from CFR
    registerRaw(CUSTOM, "JsHurt"); // Used from CFR
    registerRaw(CUSTOM, "TestJsr");
    // TODO: Returns not processing properly
    registerRaw(CUSTOM, "TestJsr2");
    register(JAVA_8, "TestOverrideIndirect");
    registerRaw(CUSTOM, "TestIdeaNotNull");
    // TODO: Synchronized blocks don't work properly
    registerRaw(CUSTOM, "TestHotjava");
    register(JAVA_8, "TestLabeledBreaks");
    // TODO: test9&10- for loop not created, loop extractor needs another pass
    register(JAVA_8, "TestSwitchLoop");
    register(JAVA_8, "TestSwitchFinally");
    // TODO: look at underlying issue with finally and loops here
    // TODO: test5 variable usage in finally block is incorrect, <unknown> variable
    register(JAVA_8, "TestLoopFinally");
    // TODO: #162 produces a `(<unknown>)` cast between 2 booleans
    register(JAVA_8, "TestUnknownCast");
    // TODO: local classes not being put in the right spots
    register(JAVA_8, "TestLocalClassesSwitch"); // Adapted from CFR
    // TODO: return not condensed properly and throw is not put into finally
    register(JAVA_8, "TestFinallyThrow");
    // TODO: this code would be cleaner if return and continue were used instead of inverting the float comparison
    register(JAVA_8, "TestFloatInvertedIfConditionEarlyExit");
    register(JAVA_8, "TestWhile1");
    registerRaw(CUSTOM, "TestEclipseSwitchEnum");
    registerRaw(CUSTOM, "TestEclipseSwitchString");
    registerRaw(CUSTOM, "TestStringConcatJ19");
    register(JAVA_8, "TestNestedAnonymousClass");
    register(JAVA_8, "TestPPMMLoop");
    // TODO: loops not eliminated properly, foreach not created
    register(JAVA_8, "TestForeachMultipleLoops");
    register(JAVA_8, "TestLoopBreak3");
    register(JAVA_8, "TestDoWhileMerge");
    register(JAVA_8, "TestTernaryReturn");
    register(JAVA_8, "TestArrayAssign");
    register(JASM, "TestGoto");
    register(JAVA_8, "TestLambdaQualified");
    register(JAVA_16, "TestTernaryReturn2");
    register(JAVA_8, "TestGenericMap");
    register(JAVA_8, "TestArrayNewAccess");
    register(JAVA_8, "TestAnnotationFormatting");
    register(JAVA_8, "TestConstructorInvoc");
    register(JAVA_8, "TestInterfaceNullInvoc");
    // TODO: extraneous casts
    register(JAVA_8, "TestUnionTypeAssign");
    register(JAVA_8, "TestGenericsInvocUnchecked");
    register(JAVA_8, "TestGenericSuperCast");
    register(JAVA_8, "TestLocalVariableMerge");
    register(JAVA_8, "TestLocalVariableMergeSwitch");
    register(JAVA_8, "TestLoopBreak4");
    register(JAVA_17, "TestDoubleBraceInitializersJ17");
    // TODO: code doesn't compile (redeclares variable)
    register(JAVA_17, "TestDuplicateAssignmentInSwitchExpr");
    register(JAVA_8, "TestLoopMerging2");
    register(JAVA_8, "TestAssertConst");
    register(JAVA_8, "TestLambdaLocalCapture");
    register(JAVA_8, "TestArrayFieldAccess");
    // TODO: needs to inline ppmm properly
    register(JAVA_8, "TestArrayPPMM");
    register(JAVA_8, "TestArrayPPMM1");
    register(JAVA_8, "TestDoubleAdd");
    register(JAVA_8, "TestTempAssign");
    register(JAVA_8, "TestArrayFieldAccess1");
    register(JAVA_8, "TestPPMMMath");
    register(JASM, "TestSwapException");
    register(KOTLIN, "TestKotlinEnumWhen");
    // TODO: causes invalid stack var simplification
    register(JAVA_8, "TestSynchronizedTryReturn");
    // TODO: parsing failure, wrong variable use, invalid variable splitting
    // TODO: finally return parsing wrong
    // TODO: postdom error only when ssau finally is disabled
    register(JAVA_8, "TestTryReturn");
    register(JAVA_8, "TestWhileConditionTernary");

    register(JAVA_17, "TestDefiniteAssignment");

    register(JAVA_8_NODEBUG, "TestNoUse");
    // TODO: var5 is never defined!
    register(JAVA_8_NODEBUG, "TestTryReturnNoDebug");
    register(JAVA_8, "TestArrayAssign2");
    register(JAVA_8, "TestTryLoopNoCatch");
    // TODO: cast is missing
    register(JAVA_8, "TestCollectionItr");
    // TODO: missing qualifier on generic
    register(JAVA_8, "TestListEquals");
    register(JAVA_8, "TestIfElseSwitch");
    register(JAVA_8, "TestInstanceStaticInvoke");
    register(JAVA_8, "TestFinallyBlockVariableUse");
    register(JAVA_8, "TestIntBoolMerge");
    register(JAVA_8_NODEBUG, "TestIntBoolMergeNoDebug");
    register(JAVA_8, "TestInnerClassGeneric");
    // TODO: array access not simplified
    register(JAVA_8, "TestArrayFieldAccess2");
    register(JAVA_8, "TestNestedArrayPP");
    // TODO: variable stores completely ignored due to variable merging
    //   now is fixed by verify variable merges
    register(JAVA_8_NODEBUG, "TestCompoundAssignmentReplace");

    register(JAVA_8, "TestSharedVarIndex");

    // NOTE: regular fernflower fails to merge the variables here, leading to incorrect results in both
    //  Derived from IDEA-291806
    // TODO: test2 now successfully triggers the bug in QuiltFlower
    register(JAVA_8, "TestTryVar");
    register(JAVA_8_NODEBUG, "TestTryVarNoDebug");
    // TODO: order of additions is wrong. Addition over floats isn't associative.
    // Derived from IDEA-291735
    register(JAVA_8, "TestFloatOrderOfOperations");
    // TODO: many unnecessary casts, and not simplifying to `+=`
    register(JAVA_8, "TestMixedCompoundAssignment");
    register(JAVA_8, "TestForeachVardef");
    // TODO: casts to T of static method!
    register(JAVA_8, "TestGenericStaticCall");
    // TODO: Assert is bundled into the loop header
    register(JAVA_8, "TestAssertMerge");
    register(JAVA_8, "TestTernaryAssign");
    register(JAVA_8, "TestLoopReturn");
    // TODO: var is used before it's defined, and it's not correct
    register(JAVA_8, "TestForCyclicVarDef");
    // TODO: merging of trycatch incorrect
    register(JAVA_8, "TestTryCatchNested");
    register(JAVA_8, "TestSwitchTernary");
    register(JAVA_8, "TestBooleanExpressions");
    // TODO: cast not created, incorrect
    register(JAVA_8, "TestObjectBitwise");
    register(JAVA_17, "TestSealedFinal", "SealedInterface");
    register(JAVA_17, "TestSealedRecord", "SealedInterface");
    // TODO: empty switch leaves behind a synthetic field access
    register(JAVA_8, "TestEnumSwitchEmpty");
    // TODO: resugar "constructor references" for array creation
    register(JAVA_8, "TestArrayConstructorReference");

    // TODO: when in different classes, these two decompile incorrectly to super(outer, s)
    register(JAVA_8, "TestInnerClassExtend");
    register(JAVA_17, "TestInnerClassExtendJ17");

    // TODO: include scala stdlibs to avoid "import scala.runtime.ScalaRunTime."
    // this occurs in the plugin's "decompile scala to java" action too
    register(SCALA, "TestCaseClasses", "Option1", "Option1$", "Option2", "Option2$", "Option3", "Option3$", "EnumLike", "EnumLike$");
    register(SCALA, "TestObject", "TestObject$");
    register(SCALA, "TestCompanionObject", "TestCompanionObject$");
    // TODO: foreach array index increment is added into default branch of switch statement
    register(JAVA_8, "TestForeachCrash");
    // TODO: <unknown> value and cast, switch is eliminated, test2 contains entirely invalid code
    register(JAVA_17_PREVIEW, "TestUnknownCastJ17");
    // TODO: These variables shouldn't be merged, and should be split because each version is used once and has a different type use
    register(JAVA_8_NODEBUG, "TestVarIndex");
  }

  private void registerEntireClassPath() {
    // These have better results with the kotlin standard library from the classpath
    register(KOTLIN, "TestKotlinConstructorKt");
    register(KOTLIN, "TestNamedSuspendFun2Kt");
  }

  private void registerJavaRuntime() {
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
    // TODO: continue not explicit
    register(JAVA_8, "TestNestedLoops");
    register(JAVA_8, "TestAnonymousClass");
    // TODO: <undefinedtype> cast and var type
    register(JAVA_16, "TestAnonymousClassJ16");
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
    register(JAVA_16, "TestTryWithResourcesLoopJ16");
    register(JAVA_16, "TestTryWithResourcesFake");
    register(JAVA_16, "TestTryWithResourcesSwitchJ16");
    register(JAVA_16, "TestTryWithResourcesNestedLoop");
    register(JAVA_16, "TestTryWithResourcesFakeTrigger");

    register(JAVA_8, "TestGenericMapEntireClasspath");
    register(JAVA_8, "TestGenericsTernary");
    // TODO: casts on null and (U)
    register(JAVA_8, "TestGenericSuper");
    register(JAVA_8, "TestGenericsQualified");
    // TODO: first method needs a cast
    register(JAVA_8, "TestGenericCast");
    // TODO: shouldn't make foreach
    register(JAVA_8, "TestItrLoop");
    // TODO: shouldn't place diamonds in constructor
    register(JAVA_8, "TestNoGenericDiamonds");
    // TODO: missing cast
    register(JAVA_8, "TestGenericCastSuper");
    // TODO: cast doesn't have generic type
    register(JAVA_8, "TestGenericCastCall");
    // TODO: cast to T, because T of Function isn't remapped to List<T>
    register(JAVA_8, "TestGenericInput");
    // TODO: cast on field assign and setField
    register(JAVA_8, "TestGenericsHierarchy");
    // TODO: casts on the get() inside the lambda
    register(JAVA_8, "TestLambdaGenericCall");
    // TODO: crashes in const type
    register(JAVA_8, "TestTryWithResourcesAfterSwitch");
    register(JAVA_8, "TestArrayConstructorReferenceJrt");
    register(JAVA_8_NODEBUG, "TestVarIndex2");
  }

  private void registerLiterals() {
    register(JAVA_8, "TestFloatPrecision");
    register(JAVA_8, "TestNotFloatPrecision");
    register(JAVA_8, "TestConstantUninlining");
    register(JAVA_8, "TestPiDivision");
  }

  private void registerPatternMatching() {
    // TODO: testReturnTernaryComplex is not the most ideal form of the statement (pattern matching bool not distribution)
    // TODO: test against generics?
    register(JAVA_16, "TestPatternMatching");
    register(JAVA_16, "TestPatternMatchingFake");
    register(JAVA_16, "TestPatternMatchingFakeLoops");
    register(JAVA_16, "TestPatternMatchingFakeLoopsInverted");
    register(JAVA_16, "TestPatternMatchingFakeNew");
    register(JAVA_16, "TestPatternMatchingMerge");
    register(JAVA_16, "TestPatternMatchingStatic");
    // TODO: local variables aren't merged properly, bring out of nodebug when they are
    register(JAVA_16_NODEBUG, "TestPatternMatchingAssign");
    register(JAVA_16, "TestPatternMatchingLocalCapture");

    register(JAVA_17, "TestPatternMatching17");
    register(JAVA_17, "TestPatternMatching17Fake");
    register(JAVA_17, "TestPatternMatching17FakeLoops");
    register(JAVA_17, "TestPatternMatching17FakeLoopsInverted");
    register(JAVA_17, "TestPatternMatching17FakeNew");
    register(JAVA_17, "TestPatternMatchingInteger");
  }

  private void registerTernaryConstantSimplification() {
    register(JAVA_8, "TestReturnTernaryConstantSimplification");
    // TODO: ifOr, redundantIf, nestedIf and nestedIfs aren't reduced to a ternary that would be simplified
    register(JAVA_8, "TestAssignmentTernaryConstantSimplification");
  }

  private void registerLVT() {
    register(JAVA_8, "TestLVT");
    register(JAVA_8, "TestLVTScoping");
    register(JAVA_8, "TestLVTComplex");
    register(JAVA_8, "TestVarType");
    register(JAVA_8, "TestLoopMerging");
  }

  private void registerTryLoop() {
    register(JAVA_8, "TestTryLoop");
    register(JAVA_8, "TestTryLoop2");
    register(JAVA_8, "TestTryLoopRecompile");
    register(JAVA_8, "TestTryLoopSimpleFinally");
    register(JAVA_8, "TestTryLoopReturnFinally");
  }
}
