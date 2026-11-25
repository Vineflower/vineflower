package org.vineflower.unpick;

import org.jetbrains.java.decompiler.SingleClassesTestBase;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.nio.file.Path;

import static org.jetbrains.java.decompiler.SingleClassesTestBase.TestDefinition.Version.*;

public class UnpickTest extends SingleClassesTestBase {
  @Override
  protected void registerAll() {
    registerSet("Unpick", this::registerUnpick,
      IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.INCLUDE_JAVA_RUNTIME, "1"
    );
  }

  private void registerUnpick() {
    register(JAVA_8, "TestJdk");
    register(JAVA_8, "TestFloatPrecision");
    register(JAVA_8, "TestNotFloatPrecision");
    register(JAVA_8, "TestConstantUninlining");
    register(JAVA_8, "TestPiDivision");
    registerWithDefinitions(JAVA_8, "TestByteToIntCoercion");
    registerWithDefinitions(JAVA_8, "TestByteToIntFlagCoercion");
    registerWithDefinitions(JAVA_8, "TestConstantDeclaration");
    registerWithDefinitions(JAVA_8, "TestExpressions");
    registerWithDefinitions(JAVA_8, "TestFloatToDoubleCoercion");
    registerWithDefinitions(JAVA_8, "TestFloatToDoubleImplicitCast");
    registerWithDefinitions(JAVA_8, "TestGlobalClassConstant");
    registerWithDefinitions(JAVA_8, "TestGlobalStringConstant");
    registerWithDefinitions(JAVA_8, "TestIntToLongCoercion");
    registerWithDefinitions(JAVA_8, "TestIntToLongFlagCoercion");
    registerWithDefinitions(JAVA_8, "TestIntToLongFlagImplicitCast");
    registerWithDefinitions(JAVA_8, "TestIntToLongImplicitCast");
    registerWithDefinitions(JAVA_8, "TestKnownByteConstantsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownByteConstantsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownByteFlagsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownByteFlagsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownCharConstantsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownCharConstantsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownClassConstantsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownClassConstantsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownDoubleConstantsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownDoubleConstantsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownFloatConstantsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownFloatConstantsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownIntConstantsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownIntConstantsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownIntFlagsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownIntFlagsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownLongConstantsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownLongConstantsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownLongFlagsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownLongFlagsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownShortConstantsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownShortConstantsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownShortFlagsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownShortFlagsReturn");
    registerWithDefinitions(JAVA_8, "TestKnownStringConstantsParameter");
    registerWithDefinitions(JAVA_8, "TestKnownStringConstantsReturn");
    registerWithDefinitions(JAVA_8, "TestLambdaGroupedFunctionalInterfaceParameter");
    registerWithDefinitions(JAVA_8, "TestLambdaGroupedFunctionalInterfaceReturn");
    registerWithDefinitions(JAVA_8, "TestLambdaInsideToOutsideCapture");
    registerWithDefinitions(JAVA_8, "TestLambdaOutsideToInsideCapture");
    registerWithDefinitions(JAVA_8, "TestMinusOneFlags");
    registerWithDefinitions(JAVA_8, "TestNegatedByteFlagsParameter");
    registerWithDefinitions(JAVA_8, "TestNegatedByteFlagsReturn");
    registerWithDefinitions(JAVA_8, "TestNegatedIntFlagsParameter");
    registerWithDefinitions(JAVA_8, "TestNegatedIntFlagsReturn");
    registerWithDefinitions(JAVA_8, "TestNegatedLongFlagsParameter");
    registerWithDefinitions(JAVA_8, "TestNegatedLongFlagsReturn");
    registerWithDefinitions(JAVA_8, "TestNegatedShortFlagsParameter");
    registerWithDefinitions(JAVA_8, "TestNegatedShortFlagsReturn");
    registerWithDefinitions(JAVA_8, "TestNonStaticConstants");
    registerWithDefinitions(JAVA_8, "TestSomeUnknownFlags");
    registerWithDefinitions(JAVA_8, "TestSubclass");
    registerWithDefinitions(JAVA_8, "TestTargetAnnotations");
    registerWithDefinitions(JAVA_8, "TestUnknownConstants");
    registerWithDefinitions(JAVA_8, "TestUnknownFlags");
    registerWithDefinitions(JAVA_8, "TestWildcardFieldParameter");
    registerWithDefinitions(JAVA_8, "TestZeroFlags");
    registerWithDefinitions(JAVA_8, "TestWildcardFieldReturn");
    register(JAVA_17, "TestJdk17");
  }

  private void registerWithDefinitions(TestDefinition.Version version, String testClass) {
    TestDefinition definition = register(version, testClass, "Constants");
    definition.setOption(UnpickOptions.UNPICK_FILES, Path.of(System.getProperty("vineflower.unpick.files")).resolve(testClass + ".unpick").toString());
  }
}
