// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main;

import net.fabricmc.fernflower.api.IFabricJavadocProvider;
import org.jetbrains.java.decompiler.api.Decompiler;
import org.jetbrains.java.decompiler.api.DecompilerOutput;
import org.jetbrains.java.decompiler.api.Option;
import org.jetbrains.java.decompiler.api.Options;
import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.extern.*;
import org.jetbrains.java.decompiler.modules.renamer.ConverterHelper;
import org.jetbrains.java.decompiler.modules.renamer.IdentifierConverter;
import org.jetbrains.java.decompiler.modules.renamer.PoolInterceptor;
import org.jetbrains.java.decompiler.struct.IDecompiledData;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.util.JADNameProvider;
import org.jetbrains.java.decompiler.util.JrtFinder;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.ClasspathScanner;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Fernflower implements IDecompiledData, Decompiler {
  private final StructContext structContext;
  private final ClassesProcessor classProcessor;
  private final IIdentifierRenamer helper;
  private final IdentifierConverter converter;

  public Fernflower(IBytecodeProvider input, DecompilerOutput output, Options customProperties, IFernflowerLogger logger, IFabricJavadocProvider javadocProvider) {
    this(input, new ResultSaverAdapter(output), customProperties, logger, javadocProvider);
  }

  public Fernflower(IBytecodeProvider provider, IResultSaver saver, Map<String, Object> customProperties, IFernflowerLogger logger) {
    this(provider, saver, convertOptions(customProperties), logger, (IFabricJavadocProvider) customProperties.get(IFabricJavadocProvider.PROPERTY_NAME));
  }

  private static Options convertOptions(Map<String, Object> customProperties) {
    Options options = new Options();
    for (Map.Entry<String, Object> e : customProperties.entrySet()) {
      if (IFabricJavadocProvider.PROPERTY_NAME.equals(e.getKey())) continue;
      parseOption(options, e.getKey(), e.getValue());
    }
    return options;
  }

  @SuppressWarnings("unchecked")
  private static <T> void parseOption(Options options, String name, Object value) {
    Option<T> option = (Option<T>) Option.byShortName(name);
    if (option == null) throw new IllegalArgumentException("Unknown option " + name);
    if (option.type.isInstance(value)) {
      options.with(option, (T) value);
      return;
    }
    if (!(value instanceof String)) throw new IllegalArgumentException("Cannot convert value '" + value + "'");
    String strValue = (String) value;
    if (option.type == Boolean.class) {
      options.with(option, (T) (Boolean) "1".equals(strValue));
    } else if (option.type == Integer.class) {
      options.with(option, (T) (Integer) Integer.parseInt(strValue));
    } else if (option.type == Class.class) {
      try {
        options.with(option, (T) Class.forName(strValue));
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException(e);
      }
    } else if (option.type.isEnum()) {
      //noinspection rawtypes
      options.with(option, (T) Enum.valueOf((Class) option.type, strValue.toUpperCase(Locale.ROOT)));
    } else {
      throw new IllegalArgumentException("Cannot convert value '" + value + "' to " + option.type);
    }
  }

  private Fernflower(IBytecodeProvider provider, IResultSaver saver, Options options, IFernflowerLogger logger, IFabricJavadocProvider javadocProvider) {
    logger.setSeverity(options.get(Option.LOG_LEVEL));

    structContext = new StructContext(saver, this, new LazyLoader(provider));
    classProcessor = new ClassesProcessor(structContext);

    PoolInterceptor interceptor = null;
    if (options.get(Option.RENAME_ENTITIES)) {
      helper = loadHelper(options.get(Option.USER_RENAMER_CLASS), logger);
      interceptor = new PoolInterceptor();
      converter = new IdentifierConverter(structContext, helper, interceptor);
    }
    else {
      helper = null;
      converter = null;
    }

    IVariableNamingFactory renamerFactory = null;
    Class<IVariableNamingFactory> factoryClazz = options.get(Option.RENAMER_FACTORY);
    if (factoryClazz != null) {
      try {
        renamerFactory = factoryClazz.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        logger.writeMessage("Error loading renamer factory class: " + factoryClazz, e);
      }
    }
    if (renamerFactory == null) {
      if (options.get(Option.USE_JAD_VARNAMING)) {
        renamerFactory = new JADNameProvider.JADNameProviderFactory();
      } else {
        renamerFactory = new IdentityRenamerFactory();
      }
    }

    DecompilerContext context = new DecompilerContext(options, logger, structContext, classProcessor, interceptor, renamerFactory, javadocProvider);
    DecompilerContext.setCurrentContext(context);

    String vendor = System.getProperty("java.vendor", "missing vendor");
    String javaVersion = System.getProperty("java.version", "missing java version");
    String jvmVersion = System.getProperty("java.vm.version", "missing jvm version");
    logger.writeMessage(String.format("JVM info: %s - %s - %s", vendor, javaVersion, jvmVersion), IFernflowerLogger.Severity.INFO);

    if (DecompilerContext.getOption(Option.INCLUDE_ENTIRE_CLASSPATH)) {
      ClasspathScanner.addAllClasspath(structContext);
    } else if (DecompilerContext.getOption(Option.INCLUDE_JAVA_RUNTIME)) {
      JrtFinder.addJrt(structContext);
    }
  }

  private static IIdentifierRenamer loadHelper(Class<?> cls, IFernflowerLogger logger) {
    if (cls != null) {
      try {
        return (IIdentifierRenamer) cls.getDeclaredConstructor().newInstance();
      }
      catch (Exception e) {
        logger.writeMessage("Cannot create renamer '" + cls + "'", IFernflowerLogger.Severity.WARN, e);
      }
    }

    return new ConverterHelper();
  }

  public void addSource(File source) {
    structContext.addSpace(source, true);
  }

  public void addLibrary(File library) {
    structContext.addSpace(library, false);
  }

  public void decompileContext() {
    if (converter != null) {
      converter.rename();
    }

    classProcessor.loadClasses(helper);

    structContext.saveContext();
  }

  public void addWhitelist(String prefix) {
    classProcessor.addWhitelist(prefix);
  }

  public void clearContext() {
    DecompilerContext.setCurrentContext(null);
  }

  @Override
  public String getClassEntryName(StructClass cl, String entryName) {
    ClassNode node = classProcessor.getMapRootClasses().get(cl.qualifiedName);
    if (node == null || node.type != ClassNode.CLASS_ROOT) {
      return null;
    }
    else if (converter != null) {
      String simpleClassName = cl.qualifiedName.substring(cl.qualifiedName.lastIndexOf('/') + 1);
      return entryName.substring(0, entryName.lastIndexOf('/') + 1) + simpleClassName + ".java";
    }
    else {
      return entryName.substring(0, entryName.lastIndexOf(".class")) + ".java";
    }
  }

  @Override
  public String getClassContent(StructClass cl) {
    try {
      TextBuffer buffer = new TextBuffer(ClassesProcessor.AVERAGE_CLASS_SIZE);
      buffer.append(DecompilerContext.getOption(Option.BANNER));
      classProcessor.writeClass(cl, buffer);
      return buffer.toString();
    }
    catch (Throwable t) {
      DecompilerContext.getLogger().writeMessage("Class " + cl.qualifiedName + " couldn't be fully decompiled.", t);
      return null;
    }
  }
}