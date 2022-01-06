// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main.extern;

import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface IFernflowerPreferences {
  String REMOVE_BRIDGE = "rbr";
  String REMOVE_SYNTHETIC = "rsy";
  String DECOMPILE_INNER = "din";
  String DECOMPILE_CLASS_1_4 = "dc4";
  String DECOMPILE_ASSERTIONS = "das";
  String HIDE_EMPTY_SUPER = "hes";
  String HIDE_DEFAULT_CONSTRUCTOR = "hdc";
  String DECOMPILE_GENERIC_SIGNATURES = "dgs";
  String NO_EXCEPTIONS_RETURN = "ner";
  String ENSURE_SYNCHRONIZED_MONITOR = "esm";
  String DECOMPILE_ENUM = "den";
  String REMOVE_GET_CLASS_NEW = "rgn";
  String LITERALS_AS_IS = "lit";
  String BOOLEAN_TRUE_ONE = "bto";
  String ASCII_STRING_CHARACTERS = "asc";
  String SYNTHETIC_NOT_SET = "nns";
  String UNDEFINED_PARAM_TYPE_OBJECT = "uto";
  String USE_DEBUG_VAR_NAMES = "udv";
  String USE_METHOD_PARAMETERS = "ump";
  String REMOVE_EMPTY_RANGES = "rer";
  String FINALLY_DEINLINE = "fdi";
  String IDEA_NOT_NULL_ANNOTATION = "inn";
  String LAMBDA_TO_ANONYMOUS_CLASS = "lac";
  String BYTECODE_SOURCE_MAPPING = "bsm";
  String IGNORE_INVALID_BYTECODE = "iib";
  String VERIFY_ANONYMOUS_CLASSES = "vac";
  String TERNARY_CONSTANT_SIMPLIFICATION = "tcs";
  String PATTERN_MATCHING = "pam";
  String EXPERIMENTAL_TRY_LOOP_FIX = "tlf";
  String TERNARY_CONDITIONS = "tco";
  String SWITCH_EXPRESSIONS = "swe";
  String SHOW_HIDDEN_STATEMENTS = "shs";
  String OVERRIDE_ANNOTATION = "ovr";
  String SIMPLIFY_STACK_SECOND_PASS = "ssp";

  String INCLUDE_ENTIRE_CLASSPATH = "iec";
  String INCLUDE_JAVA_RUNTIME = "jrt";
  String EXPLICIT_GENERIC_ARGUMENTS = "ega";
  String INLINE_SIMPLE_LAMBDAS = "isl";

  String LOG_LEVEL = "log";
  String MAX_PROCESSING_METHOD = "mpm";
  String RENAME_ENTITIES = "ren";
  String USER_RENAMER_CLASS = "urc";
  String NEW_LINE_SEPARATOR = "nls";
  String INDENT_STRING = "ind";
  String PREFERRED_LINE_LENGTH = "pll";
  String BANNER = "ban";
  String ERROR_MESSAGE = "erm";
  String THREADS = "thr";

  String DUMP_ORIGINAL_LINES = "__dump_original_lines__";
  String UNIT_TEST_MODE = "__unit_test_mode__";

  String LINE_SEPARATOR_WIN = "\r\n";
  String LINE_SEPARATOR_UNX = "\n";

  String USE_JAD_VARNAMING = "jvn";

  String SKIP_EXTRA_FILES = "sef";

  String WARN_INCONSISTENT_INNER_CLASSES = "win";
  String DUMP_BYTECODE_ON_ERROR = "dbe";
  String DUMP_EXCEPTION_ON_ERROR = "dee";
  String DECOMPILER_COMMENTS = "dec";

  Map<String, Object> DEFAULTS = getDefaults();

  static Map<String, Object> getDefaults() {
    Map<String, Object> defaults = new HashMap<>();

    defaults.put(REMOVE_BRIDGE, "1");
    defaults.put(REMOVE_SYNTHETIC, "0");
    defaults.put(DECOMPILE_INNER, "1");
    defaults.put(DECOMPILE_CLASS_1_4, "1");
    defaults.put(DECOMPILE_ASSERTIONS, "1");
    defaults.put(HIDE_EMPTY_SUPER, "1");
    defaults.put(HIDE_DEFAULT_CONSTRUCTOR, "1");
    defaults.put(DECOMPILE_GENERIC_SIGNATURES, "0");
    defaults.put(NO_EXCEPTIONS_RETURN, "1");
    defaults.put(ENSURE_SYNCHRONIZED_MONITOR, "1");
    defaults.put(DECOMPILE_ENUM, "1");
    defaults.put(REMOVE_GET_CLASS_NEW, "1");
    defaults.put(LITERALS_AS_IS, "0");
    defaults.put(BOOLEAN_TRUE_ONE, "1");
    defaults.put(ASCII_STRING_CHARACTERS, "0");
    defaults.put(SYNTHETIC_NOT_SET, "0");
    defaults.put(UNDEFINED_PARAM_TYPE_OBJECT, "1");
    defaults.put(USE_DEBUG_VAR_NAMES, "1");
    defaults.put(USE_METHOD_PARAMETERS, "1");
    defaults.put(REMOVE_EMPTY_RANGES, "1");
    defaults.put(FINALLY_DEINLINE, "1");
    defaults.put(IDEA_NOT_NULL_ANNOTATION, "1");
    defaults.put(LAMBDA_TO_ANONYMOUS_CLASS, "0");
    defaults.put(BYTECODE_SOURCE_MAPPING, "0");
    defaults.put(IGNORE_INVALID_BYTECODE, "0");
    defaults.put(VERIFY_ANONYMOUS_CLASSES, "0");
    defaults.put(TERNARY_CONSTANT_SIMPLIFICATION, "0");
    defaults.put(OVERRIDE_ANNOTATION, "1");
    defaults.put(PATTERN_MATCHING, "0"); // Pattern matching has some issues around negative blocks
    defaults.put(EXPERIMENTAL_TRY_LOOP_FIX, "0"); // Causes issues when decompiling certain classes
    defaults.put(TERNARY_CONDITIONS, "1"); // Ternary conditions are pretty stable so they can go in here
    defaults.put(SWITCH_EXPRESSIONS, "1"); // While still experimental, switch expressions work pretty well
    defaults.put(SHOW_HIDDEN_STATEMENTS, "0"); // Extra debugging that isn't useful in most cases
    defaults.put(SIMPLIFY_STACK_SECOND_PASS, "1"); // Generally produces better bytecode, useful to debug if it does something strange

    defaults.put(INCLUDE_ENTIRE_CLASSPATH, "0");
    defaults.put(INCLUDE_JAVA_RUNTIME, "0");
    defaults.put(EXPLICIT_GENERIC_ARGUMENTS, "0");
    defaults.put(INLINE_SIMPLE_LAMBDAS, "1");

    defaults.put(LOG_LEVEL, IFernflowerLogger.Severity.INFO.name());
    defaults.put(MAX_PROCESSING_METHOD, "0");
    defaults.put(RENAME_ENTITIES, "0");
    defaults.put(NEW_LINE_SEPARATOR, (InterpreterUtil.IS_WINDOWS ? "0" : "1"));
    defaults.put(INDENT_STRING, "   ");
    defaults.put(PREFERRED_LINE_LENGTH, "160");
    defaults.put(BANNER, "");
    // Point users towards reporting bugs if things don't decompile properly
    defaults.put(ERROR_MESSAGE, "Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)");
    defaults.put(UNIT_TEST_MODE, "0");
    defaults.put(DUMP_ORIGINAL_LINES, "0");
    defaults.put(THREADS, String.valueOf(Runtime.getRuntime().availableProcessors()));
    defaults.put(USE_JAD_VARNAMING, "0");
    defaults.put(SKIP_EXTRA_FILES, "0");
    defaults.put(WARN_INCONSISTENT_INNER_CLASSES, "1");
    defaults.put(DUMP_BYTECODE_ON_ERROR, "1");
    defaults.put(DUMP_EXCEPTION_ON_ERROR, "1");
    defaults.put(DECOMPILER_COMMENTS, "1");

    return Collections.unmodifiableMap(defaults);
  }
}
