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
    register(JAVA_8, "TestConstantUninlining");
    register(JAVA_8, "TestPiDivision");
    registerWithDefinitions(JAVA_8, "TestByteToIntCoercion", "Constants");
    registerWithDefinitions(JAVA_8, "TestByteToIntFlagCoercion", "Constants");
    registerWithDefinitions(JAVA_8, "TestCharFormatRequireCast", "Constants");
    registerWithDefinitions(JAVA_8, "TestConstantDeclaration", "Constants");
    registerWithDefinitions(JAVA_8, "TestExpressions", "Constants");
    registerWithDefinitions(JAVA_8, "TestFloatToDoubleCoercion", "Constants");
    registerWithDefinitions(JAVA_8, "TestFloatToDoubleImplicitCast", "Constants");
    registerWithDefinitions(JAVA_8, "TestGlobalClassConstant", "Constants");
    registerWithDefinitions(JAVA_8, "TestGlobalStringConstant", "Constants");
    registerWithDefinitions(JAVA_8, "TestGroupConflict", "Constants");
    registerWithDefinitions(JAVA_8, "TestGroupFormat", "Constants");
    registerWithDefinitions(JAVA_8, "TestInsideAnnotations", "Constants");
    registerWithDefinitions(JAVA_8, "TestIntToLongCoercion", "Constants");
    registerWithDefinitions(JAVA_8, "TestIntToLongFlagCoercion", "Constants");
    registerWithDefinitions(JAVA_8, "TestIntToLongFlagImplicitCast", "Constants");
    registerWithDefinitions(JAVA_8, "TestIntToLongImplicitCast", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownByteConstantsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownByteConstantsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownByteFlagsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownByteFlagsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownCharConstantsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownCharConstantsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownClassConstantsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownClassConstantsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownDoubleConstantsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownDoubleConstantsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownFloatConstantsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownFloatConstantsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownIntConstantsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownIntConstantsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownIntFlagsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownIntFlagsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownLongConstantsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownLongConstantsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownLongFlagsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownLongFlagsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownShortConstantsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownShortConstantsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownShortFlagsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownShortFlagsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownStringConstantsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestKnownStringConstantsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestLambdaGroupedFunctionalInterfaceParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestLambdaGroupedFunctionalInterfaceReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestLambdaInsideToOutsideCapture", "Constants");
    registerWithDefinitions(JAVA_8, "TestLambdaOutsideToInsideCapture", "Constants");
    registerWithDefinitions(JAVA_8, "TestMinusOneFlags", "Constants");
    registerWithDefinitions(JAVA_8, "TestNegatedByteFlagsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestNegatedByteFlagsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestNegatedIntFlagsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestNegatedIntFlagsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestNegatedLongFlagsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestNegatedLongFlagsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestNegatedShortFlagsParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestNegatedShortFlagsReturn", "Constants");
    registerWithDefinitions(JAVA_8, "TestNonStaticConstants", "Constants");
    registerWithDefinitions(JAVA_8, "TestScopes", "Constants");
    registerWithDefinitions(JAVA_8, "TestSomeUnknownFlags", "Constants");
    registerWithDefinitions(JAVA_8, "TestSubclass", "Constants");
    registerWithDefinitions(JAVA_8, "TestSwitchUninlining", "Constants");
    registerWithDefinitions(JAVA_8, "TestTargetAnnotations", "Constants");
    registerWithDefinitions(JAVA_8, "TestUnknownConstants", "Constants");
    registerWithDefinitions(JAVA_8, "TestUnknownFlags", "Constants");
    registerWithDefinitions(JAVA_8, "TestWildcardFieldParameter", "Constants");
    registerWithDefinitions(JAVA_8, "TestZeroFlags", "Constants");
    registerWithDefinitions(JAVA_8, "TestWildcardFieldReturn", "Constants");
    registerWithDefinitions(JAVA_17, "TestSwitchExpressionUninlining"); // TODO: yield has redundant cast
    registerWithDefinitions(JAVA_21, "TestPatternVar");
  }

  private void registerWithDefinitions(TestDefinition.Version version, String testClass, String... others) {
    TestDefinition definition = register(version, testClass, others);
    definition.setOption(UnpickOptions.UNPICK_FILES, Path.of(System.getProperty("vineflower.unpick.files")).resolve(testClass + ".unpick").toString());
  }
}
