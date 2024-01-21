// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main.extern;

import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.lang.annotation.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface IFernflowerPreferences {
  @Name("Remove Bridge Methods")
  @Description("Removes any methods that are marked as bridge from the decompiled output.")
  @ShortName("rbr")
  @Type(Type.BOOLEAN)
  String REMOVE_BRIDGE = "remove-bridge";

  @Name("Remove Synthetic Methods And Fields")
  @Description("Removes any methods and fields that are marked as synthetic from the decompiled output.")
  @ShortName("rsy")
  @Type(Type.BOOLEAN)
  String REMOVE_SYNTHETIC = "remove-synthetic";

  @Name("Decompile Inner Classes")
  @Description("Process inner classes and add them to the decompiled output.")
  @ShortName("din")
  @Type(Type.BOOLEAN)
  String DECOMPILE_INNER = "decompile-inner";

  @Name("Decompile Java 4 class references")
  @Description("Resugar the Java 1-4 class reference format instead of leaving the synthetic code.")
  @ShortName("dc4")
  @Type(Type.BOOLEAN)
  String DECOMPILE_CLASS_1_4 = "decompile-java4";

  @Name("Decompile Assertions")
  @Description("Decompile assert statements.")
  @ShortName("das")
  @Type(Type.BOOLEAN)
  String DECOMPILE_ASSERTIONS = "decompile-assert";

  @Name("Hide Empty super()")
  @Description("Hide super() calls with no parameters.")
  @ShortName("hes")
  @Type(Type.BOOLEAN)
  String HIDE_EMPTY_SUPER = "hide-empty-super";

  @Name("Hide Default Constructor")
  @Description("Hide constructors with no parameters and no code.")
  @ShortName("hdc")
  @Type(Type.BOOLEAN)
  String HIDE_DEFAULT_CONSTRUCTOR = "hide-default-constructor";

  @Name("Decompile Generics")
  @Description("Decompile generics in classes, methods, fields, and variables.")
  @ShortName("dgs")
  @Type(Type.BOOLEAN)
  String DECOMPILE_GENERIC_SIGNATURES = "decompile-generics";

  @Name("Incorporate returns in try-catch blocks")
  @Description("Integrate returns better in try-catch blocks instead of storing them in a temporary variable.")
  @ShortName("ner")
  @Type(Type.BOOLEAN)
  String INCORPORATE_RETURNS = "incorporate-returns";

  @Name("Ensure synchronized ranges are complete")
  @Description("If a synchronized block has a monitorenter without any corresponding monitorexit, try to deduce where one should be to ensure the synchronized is correctly decompiled.")
  @ShortName("esm")
  @Type(Type.BOOLEAN)
  String ENSURE_SYNCHRONIZED_MONITOR = "ensure-synchronized-monitors";

  @Name("Decompile Enums")
  @Description("Decompile enums.")
  @ShortName("den")
  @Type(Type.BOOLEAN)
  String DECOMPILE_ENUM = "decompile-enums";

  @Name("Decompile Preview Features")
  @Description("Decompile features marked as preview or incubating in the latest Java versions.")
  @ShortName("dpr")
  @Type(Type.BOOLEAN)
  String DECOMPILE_PREVIEW = "decompile-preview";

  @Name("Remove reference getClass()")
  @Description("Remove synthetic getClass() calls created by code such as 'obj.new Inner()'.")
  @ShortName("rgn")
  @Type(Type.BOOLEAN)
  String REMOVE_GET_CLASS_NEW = "remove-getclass";

  @Name("Keep Literals As Is")
  @Description("Keep NaN, infinities, and pi values as is without resugaring them.")
  @ShortName("lit")
  @Type(Type.BOOLEAN)
  String LITERALS_AS_IS = "keep-literals";

  @Name("Represent boolean as 0/1")
  @Description("Represent integers 0 and 1 as booleans.")
  @ShortName("bto")
  @Type(Type.BOOLEAN)
  String BOOLEAN_TRUE_ONE = "boolean-as-int";

  @Name("ASCII String Characters")
  @Description("Encode non-ASCII characters in string and character literals as Unicode escapes.")
  @ShortName("asc")
  @Type(Type.BOOLEAN)
  String ASCII_STRING_CHARACTERS = "ascii-strings";

  @Name("Synthetic Not Set")
  @Description("Treat some known structures as synthetic even when not explicitly set.")
  @ShortName("nns")
  @Type(Type.BOOLEAN)
  String SYNTHETIC_NOT_SET = "synthetic-not-set";

  @Name("Treat Undefined Param Type As Object")
  @Description("Treat nameless types as java.lang.Object.")
  @ShortName("uto")
  @Type(Type.BOOLEAN)
  String UNDEFINED_PARAM_TYPE_OBJECT = "undefined-as-object";

  @Name("Use LVT Names")
  @Description("Use LVT names for local variables and parameters instead of var<index>_<version>.")
  @ShortName("udv")
  @Type(Type.BOOLEAN)
  String USE_DEBUG_VAR_NAMES = "use-lvt-names";

  @Name("Use Method Parameters")
  @Description("Use method parameter names, as given in the MethodParameters attribute.")
  @ShortName("ump")
  @Type(Type.BOOLEAN)
  String USE_METHOD_PARAMETERS = "use-method-parameters";

  @Name("Remove Empty try-catch blocks")
  @Description("Remove try-catch blocks with no code.")
  @ShortName("rer")
  @Type(Type.BOOLEAN)
  String REMOVE_EMPTY_RANGES = "remove-empty-try-catch";

  @Name("Decompile Finally")
  @Description("Decompile finally blocks.")
  @ShortName("fdi")
  @Type(Type.BOOLEAN)
  String FINALLY_DEINLINE = "decompile-finally";

  @Name("Decompile Lambdas as Anonymous Classes")
  @Description("Decompile lambda expressions as anonymous classes.")
  @ShortName("lac")
  @Type(Type.BOOLEAN)
  String LAMBDA_TO_ANONYMOUS_CLASS = "lambda-to-anonymous-class";

  @Name("Bytecode to Source Mapping")
  @Description("Map Bytecode to source lines.")
  @ShortName("bsm")
  @Type(Type.BOOLEAN)
  String BYTECODE_SOURCE_MAPPING = "bytecode-source-mapping";

  @Name("Dump Code Lines")
  @Description("Dump line mappings to output archive zip entry extra data.")
  @ShortName("dcl")
  @Type(Type.BOOLEAN)
  String DUMP_CODE_LINES = "dump-code-lines";

  @Name("Ignore Invalid Bytecode")
  @Description("Ignore bytecode that is malformed.")
  @ShortName("iib")
  @Type(Type.BOOLEAN)
  String IGNORE_INVALID_BYTECODE = "ignore-invalid-bytecode";

  @Name("Verify Anonymous Classes")
  @Description("Verify that anonymous classes are local.")
  @ShortName("vac")
  @Type(Type.BOOLEAN)
  String VERIFY_ANONYMOUS_CLASSES = "verify-anonymous-classes";

  @Name("Ternary Constant Simplification")
  @Description("Fold branches of ternary expressions that have boolean true and false constants.")
  @ShortName("tcs")
  @Type(Type.BOOLEAN)
  String TERNARY_CONSTANT_SIMPLIFICATION = "ternary-constant-simplification";

  @Name("Pattern Matching")
  @Description("Decompile with if and switch pattern matching enabled.")
  @ShortName("pam")
  @Type(Type.BOOLEAN)
  String PATTERN_MATCHING = "pattern-matching";

  @Name("Try-Loop fix")
  @Description("Fixes rare cases of malformed decompilation when try blocks are found inside of while loops")
  @ShortName("tlf")
  @Type(Type.BOOLEAN)
  String TRY_LOOP_FIX = "try-loop-fix";

  @Name("[Experimental] Ternary In If Conditions")
  @Description("Tries to collapse if statements that have a ternary in their condition.")
  @ShortName("tco")
  @Type(Type.BOOLEAN)
  String TERNARY_CONDITIONS = "ternary-in-if";

  @Name("Decompile Switch Expressions")
  @Description("Decompile switch expressions in modern Java class files.")
  @ShortName("swe")
  @Type(Type.BOOLEAN)
  String SWITCH_EXPRESSIONS = "decompile-switch-expressions";

  @Name("[Debug] Show hidden statements")
  @Description("Display hidden code blocks for debugging purposes.")
  @ShortName("shs")
  @Type(Type.BOOLEAN)
  String SHOW_HIDDEN_STATEMENTS = "show-hidden-statements";

  @Name("Override Annotation")
  @Description("Display override annotations for methods known to the decompiler.")
  @ShortName("ovr")
  @Type(Type.BOOLEAN)
  String OVERRIDE_ANNOTATION = "override-annotation";

  @Name("Second-Pass Stack Simplification")
  @Description("Simplify variables across stack bounds to resugar complex statements.")
  @ShortName("ssp")
  @Type(Type.BOOLEAN)
  String SIMPLIFY_STACK_SECOND_PASS = "simplify-stack";

  @Name("[Experimental] Verify Variable Merges")
  @Description("Tries harder to verify the validity of variable merges. If there are strange variable recompilation issues, this is a good place to start.")
  @ShortName("vvm")
  @Type(Type.BOOLEAN)
  String VERIFY_VARIABLE_MERGES = "verify-merges";

  @Name("Include Entire Classpath")
  @Description("Give the decompiler information about every jar on the classpath.")
  @ShortName("iec")
  @Type(Type.BOOLEAN)
  String INCLUDE_ENTIRE_CLASSPATH = "include-classpath";

  @Name("Include Java Runtime")
  @Description("Give the decompiler information about the Java runtime, either 1 or current for the current runtime, or a path to another runtime")
  @ShortName("jrt")
  @Type(Type.STRING)
  String INCLUDE_JAVA_RUNTIME = "include-runtime";

  @Name("Explicit Generic Arguments")
  @Description("Put explicit diamond generic arguments on method calls.")
  @ShortName("ega")
  @Type(Type.BOOLEAN)
  String EXPLICIT_GENERIC_ARGUMENTS = "explicit-generics";

  @Name("Inline Simple Lambdas")
  @Description("Remove braces on simple, one line, lambda expressions.")
  @ShortName("isl")
  @Type(Type.BOOLEAN)
  String INLINE_SIMPLE_LAMBDAS = "inline-simple-lambdas";

  @Name("Logging Level")
  @Description("Logging level. Must be one of: 'info', 'debug', 'warn', 'error'.")
  @ShortName("log")
  @Type(Type.STRING)
  String LOG_LEVEL = "log-level";

  @Name("[DEPRECATED] Max time to process method")
  @Description("Maximum time in seconds to process a method. This is deprecated, do not use.")
  @ShortName("mpm")
  @Type(Type.INTEGER)
  String MAX_PROCESSING_METHOD = "max-time-per-method";

  @Name("Rename Members")
  @Description("Rename classes, fields, and methods with a number suffix to help in deobfuscation.")
  @ShortName("ren")
  @Type(Type.BOOLEAN)
  String RENAME_ENTITIES = "rename-members";

  @Name("User Renamer Class")
  @Description("Path to a class that implements IIdentifierRenamer.")
  @ShortName("urc")
  @Type(Type.STRING)
  String USER_RENAMER_CLASS = "user-renamer-class";

  @Name("[DEPRECATED] New Line Seperator")
  @Description("Use \\n instead of \\r\\n for new lines. Deprecated, do not use.")
  @ShortName("nls")
  @Type(Type.BOOLEAN)
  @DynamicDefaultValue("Disabled on Windows, enabled on other systems")
  String NEW_LINE_SEPARATOR = "new-line-separator";

  @Name("Indent String")
  @Description("A string of spaces or tabs that is placed for each indent level.")
  @ShortName("ind")
  @Type(Type.STRING)
  String INDENT_STRING = "indent-string";

  @Name("Preferred line length")
  @Description("Max line length before formatting is applied.")
  @ShortName("pll")
  @Type(Type.INTEGER)
  String PREFERRED_LINE_LENGTH = "preferred-line-length";

  @Name("Banner")
  @Description("A message to display at the top of the decompiled file.")
  @ShortName("ban")
  @Type(Type.STRING)
  String BANNER = "banner";

  @Name("Error Message")
  @Description("Message to display when an error occurs in the decompiler.")
  @ShortName("erm")
  @Type(Type.STRING)
  String ERROR_MESSAGE = "error-message";

  @Name("Thread Count")
  @Description("How many threads to use to decompile.")
  @DynamicDefaultValue("all available processors")
  @ShortName("thr")
  @Type(Type.INTEGER)
  String THREADS = "thread-count";

  String DUMP_ORIGINAL_LINES = "__dump_original_lines__";
  String UNIT_TEST_MODE = "__unit_test_mode__";

  String LINE_SEPARATOR_WIN = "\r\n";
  String LINE_SEPARATOR_UNX = "\n";

  @Name("Skip Extra Files")
  @Description("Skip copying non-class files from the input folder or file to the output")
  @ShortName("sef")
  @Type(Type.BOOLEAN)
  String SKIP_EXTRA_FILES = "skip-extra-files";

  @Name("Warn about inconsistent inner attributes")
  @Description("Warn about inconsistent inner class attributes")
  @ShortName("win")
  @Type(Type.BOOLEAN)
  String WARN_INCONSISTENT_INNER_CLASSES = "warn-inconsistent-inner-attributes";

  @Name("Dump Bytecode On Error")
  @Description("Put the bytecode in the method body when an error occurs.")
  @ShortName("dbe")
  @Type(Type.BOOLEAN)
  String DUMP_BYTECODE_ON_ERROR = "dump-bytecode-on-error";

  @Name("Dump Exceptions On Error")
  @Description("Put the exception message in the method body or source file when an error occurs.")
  @ShortName("dee")
  @Type(Type.BOOLEAN)
  String DUMP_EXCEPTION_ON_ERROR = "dump-exception-on-error";

  @Name("Decompiler Comments")
  @Description("Sometimes, odd behavior of the bytecode or unfixable problems occur. This enables or disables the adding of those to the decompiled output.")
  @ShortName("dec")
  @Type(Type.BOOLEAN)
  String DECOMPILER_COMMENTS = "decompiler-comments";

  @Name("SourceFile comments")
  @Description("Add debug comments showing the class SourceFile attribute if present.")
  @ShortName("sfc")
  @Type(Type.BOOLEAN)
  String SOURCE_FILE_COMMENTS = "sourcefile-comments";

  @Name("Decompile complex constant-dynamic expressions")
  @Description("Some constant-dynamic expressions can't be converted to a single Java expression with identical run-time behaviour. This decompiles them to a similar non-lazy expression, marked with a comment.")
  @ShortName("dcc")
  @Type(Type.BOOLEAN)
  String DECOMPILE_COMPLEX_CONDYS = "decompile-complex-constant-dynamic";

  @Name("Force JSR inline")
  @Description("Forces the processing of JSR instructions even if the class files shouldn't contain it (Java 7+)")
  @ShortName("fji")
  @Type(Type.BOOLEAN)
  String FORCE_JSR_INLINE = "force-jsr-inline";

  @Name("Dump Text Tokens")
  @Description("Dump Text Tokens on each class file")
  @ShortName("dtt")
  @Type(Type.BOOLEAN)
  String DUMP_TEXT_TOKENS = "dump-text-tokens";

  @Name("Remove Imports")
  @Description("Remove import statements from the decompiled code")
  @ShortName("rim")
  @Type(Type.BOOLEAN)
  String REMOVE_IMPORTS = "remove-imports";

  @Name("Mark Corresponding Synthetics")
  @Description("Mark lambdas and anonymous and local classes with their respective synthetic constructs")
  @ShortName("mcs")
  String MARK_CORRESPONDING_SYNTHETICS = "mark-corresponding-synthetics";

  Map<String, Object> DEFAULTS = getDefaults();

  static Map<String, Object> getDefaults() {
    Map<String, Object> defaults = new HashMap<>();

    defaults.put(REMOVE_BRIDGE, "1");
    defaults.put(REMOVE_SYNTHETIC, "1");
    defaults.put(DECOMPILE_INNER, "1");
    defaults.put(DECOMPILE_CLASS_1_4, "1");
    defaults.put(DECOMPILE_ASSERTIONS, "1");
    defaults.put(HIDE_EMPTY_SUPER, "1");
    defaults.put(HIDE_DEFAULT_CONSTRUCTOR, "1");
    defaults.put(DECOMPILE_GENERIC_SIGNATURES, "1");
    defaults.put(INCORPORATE_RETURNS, "1");
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
    defaults.put(LAMBDA_TO_ANONYMOUS_CLASS, "0");
    defaults.put(BYTECODE_SOURCE_MAPPING, "0");
    defaults.put(DUMP_CODE_LINES, "0");
    defaults.put(IGNORE_INVALID_BYTECODE, "0");
    defaults.put(VERIFY_ANONYMOUS_CLASSES, "0");
    defaults.put(TERNARY_CONSTANT_SIMPLIFICATION, "0");
    defaults.put(OVERRIDE_ANNOTATION, "1");
    defaults.put(PATTERN_MATCHING, "1"); // Pattern matching is relatively stable
    defaults.put(TRY_LOOP_FIX, "1"); // Try loop fix is stable, and fixes hard to notice bugs
    defaults.put(TERNARY_CONDITIONS, "1"); // Ternary conditions are stable and don't cause many issues currently
    defaults.put(SWITCH_EXPRESSIONS, "1"); // While still experimental, switch expressions work pretty well
    defaults.put(SHOW_HIDDEN_STATEMENTS, "0"); // Extra debugging that isn't useful in most cases
    defaults.put(SIMPLIFY_STACK_SECOND_PASS, "1"); // Generally produces better bytecode, useful to debug if it does something strange
    defaults.put(VERIFY_VARIABLE_MERGES, "0"); // Produces more correct code in rare cases, but hurts code cleanliness in the majority of cases. Default off until a better fix is created.
    defaults.put(DECOMPILE_PREVIEW, "1"); // Preview features are useful to decompile in almost all cases

    defaults.put(INCLUDE_ENTIRE_CLASSPATH, "0");
    defaults.put(INCLUDE_JAVA_RUNTIME, "");
    defaults.put(EXPLICIT_GENERIC_ARGUMENTS, "0");
    defaults.put(INLINE_SIMPLE_LAMBDAS, "1");

    defaults.put(LOG_LEVEL, IFernflowerLogger.Severity.INFO.name());
    defaults.put(MAX_PROCESSING_METHOD, "0");
    defaults.put(RENAME_ENTITIES, "0");
    defaults.put(NEW_LINE_SEPARATOR, "1");
    defaults.put(INDENT_STRING, "   ");
    defaults.put(PREFERRED_LINE_LENGTH, "160");
    defaults.put(BANNER, "");
    // Point users towards reporting bugs if things don't decompile properly
    defaults.put(ERROR_MESSAGE, "Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)");
    defaults.put(UNIT_TEST_MODE, "0");
    defaults.put(DUMP_ORIGINAL_LINES, "0");
    defaults.put(THREADS, String.valueOf(Runtime.getRuntime().availableProcessors()));
    defaults.put(SKIP_EXTRA_FILES, "0");
    defaults.put(WARN_INCONSISTENT_INNER_CLASSES, "1");
    defaults.put(DUMP_BYTECODE_ON_ERROR, "1");
    defaults.put(DUMP_EXCEPTION_ON_ERROR, "1");
    defaults.put(DECOMPILER_COMMENTS, "1");
    defaults.put(SOURCE_FILE_COMMENTS, "0");
    defaults.put(DECOMPILE_COMPLEX_CONDYS, "0");
    defaults.put(FORCE_JSR_INLINE, "0");
    defaults.put(DUMP_TEXT_TOKENS, "0");
    defaults.put(REMOVE_IMPORTS, "0");
    defaults.put(MARK_CORRESPONDING_SYNTHETICS, "0");

    return Collections.unmodifiableMap(defaults);
  }

  /**
   * A human-friendly name for an option.
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Name {
    String value();
  }

  /**
   * A short description of an option.
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Description {
    String value();
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface DynamicDefaultValue {
    String value();
  }
  /**
   * The "short name" of an option. This is the older syntax,
   * such as {@code -dgs=1}. It is here to ensure some amount
   * of backwards compatibility, and as such is not considered
   * a "documented" option.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface ShortName {
    String value();
  }

  /**
   * Indicates the given type of the option. This is not a method
   * by which to identify what to pass to the option (as all options
   * should be passed as strings), but rather a more descriptive
   * type without needing to infer it from the default value.
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface Type {
    String value();
    
    String BOOLEAN = "bool";
    String INTEGER = "int";
    String STRING = "string";
  }
}
