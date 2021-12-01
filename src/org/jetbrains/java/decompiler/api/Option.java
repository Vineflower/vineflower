package org.jetbrains.java.decompiler.api;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IIdentifierRenamer;
import org.jetbrains.java.decompiler.main.extern.IVariableNamingFactory;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Option<T> {
  private static final Map<String, Option<?>> BY_SHORT_NAME = new HashMap<>();
  private static final Map<String, Option<?>> BY_LONG_NAME = new HashMap<>();

  public static final Option<Boolean> REMOVE_BRIDGE = new Option<>(IFernflowerPreferences.REMOVE_BRIDGE, "remove-bridge", true);
  public static final Option<Boolean> REMOVE_SYNTHETIC = new Option<>(IFernflowerPreferences.REMOVE_SYNTHETIC, "remove-synthetic", false);
  public static final Option<Boolean> DECOMPILE_INNER = new Option<>(IFernflowerPreferences.DECOMPILE_INNER, "decompile-inner-classes", true);
  public static final Option<Boolean> DECOMPILE_CLASS_1_4 = new Option<>(IFernflowerPreferences.DECOMPILE_CLASS_1_4, "decompile-class-1.4", true);
  public static final Option<Boolean> DECOMPILE_ASSERTIONS = new Option<>(IFernflowerPreferences.DECOMPILE_ASSERTIONS, "decompile-assert", true);
  public static final Option<Boolean> HIDE_EMPTY_SUPER = new Option<>(IFernflowerPreferences.HIDE_EMPTY_SUPER, "hide-empty-super", true);
  public static final Option<Boolean> HIDE_DEFAULT_CONSTRUCTOR = new Option<>(IFernflowerPreferences.HIDE_DEFAULT_CONSTRUCTOR, "hide-default-constructor", true);
  public static final Option<Boolean> DECOMPILE_GENERIC_SIGNATURES = new Option<>(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "decompile-generic-signatures", false);
  public static final Option<Boolean> NO_EXCEPTIONS_RETURN = new Option<>(IFernflowerPreferences.NO_EXCEPTIONS_RETURN, "no-exceptions-return", true);
  public static final Option<Boolean> ENSURE_SYNCHRONIZED_MONITOR = new Option<>(IFernflowerPreferences.ENSURE_SYNCHRONIZED_MONITOR, "ensure-synchronized-monitor", true);
  public static final Option<Boolean> DECOMPILE_ENUM = new Option<>(IFernflowerPreferences.DECOMPILE_ENUM, "decompile-enum", true);
  public static final Option<Boolean> REMOVE_GET_CLASS_NEW = new Option<>(IFernflowerPreferences.REMOVE_GET_CLASS_NEW, "remove-get-class", true);
  public static final Option<Boolean> LITERALS_AS_IS = new Option<>(IFernflowerPreferences.LITERALS_AS_IS, "literals-as-is", false);
  public static final Option<Boolean> BOOLEAN_TRUE_ONE = new Option<>(IFernflowerPreferences.BOOLEAN_TRUE_ONE, "boolean-true-one", true);
  public static final Option<Boolean> ASCII_STRING_CHARACTERS = new Option<>(IFernflowerPreferences.ASCII_STRING_CHARACTERS, "ascii-strings", false);
  public static final Option<Boolean> SYNTHETIC_NOT_SET = new Option<>(IFernflowerPreferences.SYNTHETIC_NOT_SET, "synthetic-not-set", false);
  public static final Option<Boolean> UNDEFINED_PARAM_TYPE_OBJECT = new Option<>(IFernflowerPreferences.UNDEFINED_PARAM_TYPE_OBJECT, "undefined-param-type-object", true);
  public static final Option<Boolean> USE_DEBUG_VAR_NAMES = new Option<>(IFernflowerPreferences.USE_DEBUG_VAR_NAMES, "use-debug-var-names", true);
  public static final Option<Boolean> USE_METHOD_PARAMETERS = new Option<>(IFernflowerPreferences.USE_METHOD_PARAMETERS, "use-method-parameters", true);
  public static final Option<Boolean> REMOVE_EMPTY_RANGES = new Option<>(IFernflowerPreferences.REMOVE_EMPTY_RANGES, "remove-empty-ranges", true);
  public static final Option<Boolean> FINALLY_DEINLINE = new Option<>(IFernflowerPreferences.FINALLY_DEINLINE, "deinline-finally", true);
  public static final Option<Boolean> IDEA_NOT_NULL_ANNOTATION = new Option<>(IFernflowerPreferences.IDEA_NOT_NULL_ANNOTATION, "idea-notnull", true);
  public static final Option<Boolean> LAMBDA_TO_ANONYMOUS_CLASS = new Option<>(IFernflowerPreferences.LAMBDA_TO_ANONYMOUS_CLASS, "lambda-to-anonymous-class", false);
  public static final Option<Boolean> BYTECODE_SOURCE_MAPPING = new Option<>(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "bytecode-source-mapping", false);
  public static final Option<Boolean> IGNORE_INVALID_BYTECODE = new Option<>(IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "ignore-invalid-bytecode", false);
  public static final Option<Boolean> VERIFY_ANONYMOUS_CLASSES = new Option<>(IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "verify-anonymous-classes", false);
  public static final Option<Boolean> TERNARY_CONSTANT_SIMPLIFICATION = new Option<>(IFernflowerPreferences.TERNARY_CONSTANT_SIMPLIFICATION, "ternary-constant-simplification", false);
  public static final Option<Boolean> PATTERN_MATCHING = new Option<>(IFernflowerPreferences.PATTERN_MATCHING, "pattern-matching", false);
  public static final Option<Boolean> EXPERIMENTAL_TRY_LOOP_FIX = new Option<>(IFernflowerPreferences.EXPERIMENTAL_TRY_LOOP_FIX, "try-loop-fix", false);
  public static final Option<Boolean> TERNARY_CONDITIONS = new Option<>(IFernflowerPreferences.TERNARY_CONDITIONS, "ternary-conditions", true);
  public static final Option<Boolean> INCLUDE_ENTIRE_CLASSPATH = new Option<>(IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "include-entire-classpath", false);
  public static final Option<Boolean> INCLUDE_JAVA_RUNTIME = new Option<>(IFernflowerPreferences.INCLUDE_JAVA_RUNTIME, "include-java-runtime", false);
  public static final Option<Boolean> EXPLICIT_GENERIC_ARGUMENTS = new Option<>(IFernflowerPreferences.EXPLICIT_GENERIC_ARGUMENTS, "explicit-generic-arguments", false);
  public static final Option<Boolean> INLINE_SIMPLE_LAMBDAS = new Option<>(IFernflowerPreferences.INLINE_SIMPLE_LAMBDAS, "inline-simple-lambdas", true);
  public static final Option<IFernflowerLogger.Severity> LOG_LEVEL = new Option<>(IFernflowerPreferences.LOG_LEVEL, "log-level", IFernflowerLogger.Severity.INFO);
  public static final Option<Integer> MAX_PROCESSING_METHOD = new Option<>(IFernflowerPreferences.MAX_PROCESSING_METHOD, "method-processing-timeout", 0);
  public static final Option<Boolean> RENAME_ENTITIES = new Option<>(IFernflowerPreferences.RENAME_ENTITIES, "rename-entities", false);
  public static final Option<Class<IIdentifierRenamer>> USER_RENAMER_CLASS = classOption(IFernflowerPreferences.USER_RENAMER_CLASS, "user-renamer-class", IIdentifierRenamer.class);
  public static final Option<Integer> NEW_LINE_SEPARATOR = new Option<>(IFernflowerPreferences.NEW_LINE_SEPARATOR, "new-line-separator", InterpreterUtil.IS_WINDOWS ? 0 : 1);
  public static final Option<String> INDENT_STRING = new Option<>(IFernflowerPreferences.INDENT_STRING, "indent-string", "   ");
  public static final Option<String> BANNER = new Option<>(IFernflowerPreferences.BANNER, "banner", "");
  public static final Option<Integer> THREADS = new Option<>(IFernflowerPreferences.THREADS, "threads", Runtime.getRuntime().availableProcessors());
  public static final Option<Boolean> DUMP_ORIGINAL_LINES = new Option<>(IFernflowerPreferences.DUMP_ORIGINAL_LINES, "dump-original-lines", false);
  public static final Option<Boolean> UNIT_TEST_MODE = new Option<>(IFernflowerPreferences.UNIT_TEST_MODE, "unit-test-mode", false);
  public static final Option<Boolean> USE_JAD_VARNAMING = new Option<>(IFernflowerPreferences.USE_JAD_VARNAMING, "use-jad-varnaming", false);
  public static final Option<Boolean> SKIP_EXTRA_FILES = new Option<>(IFernflowerPreferences.SKIP_EXTRA_FILES, "skip-extra-files", false);
  public static final Option<Boolean> WARN_INCONSISTENT_INNER_CLASSES = new Option<>(IFernflowerPreferences.WARN_INCONSISTENT_INNER_CLASSES, "warn-inconsistent-inner-classes", true);
  public static final Option<Boolean> DUMP_BYTECODE_ON_ERROR = new Option<>(IFernflowerPreferences.DUMP_BYTECODE_ON_ERROR, "dump-bytecode-on-error", true);
  public static final Option<Boolean> DUMP_EXCEPTION_ON_ERROR = new Option<>(IFernflowerPreferences.DUMP_EXCEPTION_ON_ERROR, "dump-exception-on-error", true);
  public static final Option<Class<IVariableNamingFactory>> RENAMER_FACTORY = classOption(DecompilerContext.RENAMER_FACTORY, "renamer-factory", IVariableNamingFactory.class);

  public final Class<T> type;
  public final String shortName;
  public final String longName;
  public final T defaultValue;
  public final Predicate<T> isValid;

  @SuppressWarnings("unchecked")
  private Option(String shortName, String longName, T defaultValue) {
    this((Class<T>) defaultValue.getClass(), shortName, longName, defaultValue, value -> true);
  }

  private Option(Class<T> type, String shortName, String longName, T defaultValue, Predicate<T> isValid) {
    this.type = type;
    this.shortName = shortName;
    this.longName = longName;
    this.defaultValue = defaultValue;
    this.isValid = isValid;
    BY_SHORT_NAME.put(shortName, this);
    BY_LONG_NAME.put(longName, this);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <T> Option<Class<T>> classOption(String shortName, String longName, Class<T> superClass) {
    return new Option<>((Class<Class<T>>) (Class) Class.class, shortName, longName, null, superClass::isAssignableFrom);
  }

  public static Option<?> byShortName(String shortName) {
    return BY_SHORT_NAME.get(shortName);
  }

  public static Option<?> byLongName(String longName) {
    return BY_LONG_NAME.get(longName);
  }
}
