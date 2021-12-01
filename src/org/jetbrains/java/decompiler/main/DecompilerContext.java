// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import org.jetbrains.java.decompiler.api.Option;
import org.jetbrains.java.decompiler.api.Options;
import org.jetbrains.java.decompiler.main.collectors.BytecodeSourceMapper;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor;
import org.jetbrains.java.decompiler.main.extern.IVariableNamingFactory;
import org.jetbrains.java.decompiler.modules.renamer.PoolInterceptor;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DecompilerContext {
  public static final String RENAMER_FACTORY = "RENAMER_FACTORY";

  public final Options options;
  public final IFernflowerLogger logger;
  public final StructContext structContext;
  public final ClassesProcessor classProcessor;
  public final PoolInterceptor poolInterceptor;
  public final IVariableNamingFactory renamerFactory;
  public final IFabricJavadocProvider javadocProvider;
  public StructClass currentClass;
  public ClassWrapper currentClassWrapper;
  public ClassesProcessor.ClassNode currentClassNode;
  public MethodWrapper currentMethodWrapper;
  private ImportCollector importCollector;
  private VarProcessor varProcessor;
  private CounterContainer counterContainer;
  private BytecodeSourceMapper bytecodeSourceMapper;

  public DecompilerContext(Options options,
                           IFernflowerLogger logger,
                           StructContext structContext,
                           ClassesProcessor classProcessor,
                           PoolInterceptor interceptor,
                           IVariableNamingFactory renamerFactory,
                           IFabricJavadocProvider javadocProvider) {
    Objects.requireNonNull(options);
    Objects.requireNonNull(logger);
    Objects.requireNonNull(structContext);
    Objects.requireNonNull(classProcessor);

    this.options = options;
    this.logger = logger;
    this.structContext = structContext;
    this.classProcessor = classProcessor;
    this.poolInterceptor = interceptor;
    this.renamerFactory = renamerFactory;
    this.javadocProvider = javadocProvider;
    this.counterContainer = new CounterContainer();
  }

  private DecompilerContext(DecompilerContext ctx) {
    this.options = ctx.options;
    this.logger = ctx.logger;
    this.structContext = ctx.structContext;
    this.classProcessor = ctx.classProcessor;
    this.poolInterceptor = ctx.poolInterceptor;
    this.renamerFactory = ctx.renamerFactory;
    this.javadocProvider = ctx.javadocProvider;
    this.currentClass = ctx.currentClass;
    this.currentClassWrapper = ctx.currentClassWrapper;
    this.currentClassNode = ctx.currentClassNode;
    this.currentMethodWrapper = ctx.currentMethodWrapper;
    this.counterContainer = ctx.counterContainer;
    this.importCollector = ctx.importCollector;
    this.bytecodeSourceMapper = ctx.bytecodeSourceMapper;
    this.varProcessor = ctx.varProcessor;
  }

  // *****************************************************************************
  // context setup and update
  // *****************************************************************************

  private static final ThreadLocal<DecompilerContext> currentContext = new ThreadLocal<>();

  public static DecompilerContext getCurrentContext() {
    return currentContext.get();
  }

  public static void setCurrentContext(DecompilerContext context) {
    currentContext.set(context);
  }

  public static void startClass(ImportCollector importCollector) {
    DecompilerContext context = getCurrentContext();
    context.importCollector = importCollector;
    context.counterContainer = new CounterContainer();
    context.bytecodeSourceMapper = new BytecodeSourceMapper();
  }

  public static void startMethod(VarProcessor varProcessor) {
    DecompilerContext context = getCurrentContext();
    context.varProcessor = varProcessor;
    context.counterContainer = new CounterContainer();
  }

  // *****************************************************************************
  // context access
  // *****************************************************************************

  public static <T> T getOption(Option<T> option) {
    return getCurrentContext().options.get(option);
  }

  public static String getNewLineSeparator() {
    return DecompilerContext.getOption(Option.NEW_LINE_SEPARATOR) == 1 ?
           IFernflowerPreferences.LINE_SEPARATOR_UNX : IFernflowerPreferences.LINE_SEPARATOR_WIN;
  }

  public static IFernflowerLogger getLogger() {
    return getCurrentContext().logger;
  }

  public static StructContext getStructContext() {
    return getCurrentContext().structContext;
  }

  public static ClassesProcessor getClassProcessor() {
    return getCurrentContext().classProcessor;
  }

  public static PoolInterceptor getPoolInterceptor() {
    return getCurrentContext().poolInterceptor;
  }

  public static IVariableNamingFactory getNamingFactory() {
    return getCurrentContext().renamerFactory;
  }

  public static ImportCollector getImportCollector() {
    return getCurrentContext().importCollector;
  }

  public static VarProcessor getVarProcessor() {
    return getCurrentContext().varProcessor;
  }

  public static CounterContainer getCounterContainer() {
    return getCurrentContext().counterContainer;
  }

  public static BytecodeSourceMapper getBytecodeSourceMapper() {
    return getCurrentContext().bytecodeSourceMapper;
  }

  public static StructClass update(StructClass currentClass) {
    DecompilerContext ctx = getCurrentContext();
    StructClass previous = ctx.currentClass;
    ctx.currentClass = currentClass;
    return previous;
  }

  public static StructClass getCurrentClass() {
    return getCurrentContext().currentClass;
  }

  public static ClassWrapper update(ClassWrapper currentClassWrapper) {
    DecompilerContext ctx = getCurrentContext();
    ClassWrapper previous = ctx.currentClassWrapper;
    ctx.currentClassWrapper = currentClassWrapper;
    return previous;
  }

  public static ClassWrapper getCurrentClassWrapper() {
    return getCurrentContext().currentClassWrapper;
  }

  public static ClassesProcessor.ClassNode update(ClassesProcessor.ClassNode currentClassNode) {
    DecompilerContext ctx = getCurrentContext();
    ClassesProcessor.ClassNode previous = ctx.currentClassNode;
    ctx.currentClassNode = currentClassNode;
    return previous;
  }

  public static ClassesProcessor.ClassNode getCurrentClassNode() {
    return getCurrentContext().currentClassNode;
  }

  public static MethodWrapper update(MethodWrapper currentMethodWrapper) {
    DecompilerContext ctx = getCurrentContext();
    MethodWrapper previous = ctx.currentMethodWrapper;
    ctx.currentMethodWrapper = currentMethodWrapper;
    return previous;
  }

  public static MethodWrapper getCurrentMethodWrapper() {
    return getCurrentContext().currentMethodWrapper;
  }

  public static void setContext(DecompilerContext rootContext) {
    DecompilerContext current = DecompilerContext.getCurrentContext();
    if (current == null) {
      DecompilerContext.setCurrentContext(new DecompilerContext(rootContext));
    }
  }}
