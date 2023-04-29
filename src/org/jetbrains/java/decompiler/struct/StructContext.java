// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IContextSource;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.main.plugins.PluginContext;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMain;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMethodDescriptor;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StructContext {
  private static volatile StructClass SENTINEL_CLASS;

  static StructClass getSentinel() {
    if (SENTINEL_CLASS == null) {
      synchronized (StructContext.class) {
        if (SENTINEL_CLASS == null) {
          try (final InputStream stream = StructContext.class.getResourceAsStream("StructContext.class")) {
            byte[] data = stream.readAllBytes();
            SENTINEL_CLASS = StructClass.create(new DataInputFullStream(data), false);
          } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
          }
        }
      }
    }
    return SENTINEL_CLASS;
  }

  @SuppressWarnings("deprecation")
  private final IBytecodeProvider legacyProvider;
  private final IResultSaver saver;
  private final IDecompiledData decompiledData;
  private final List<ContextUnit> units = new ArrayList<>();
  private final List<ContextUnit> lazyUnits = new ArrayList<>();
  private final Map<String, StructClass> classes = new ConcurrentHashMap<>();
  private final Map<String, String> badlyPlacedClasses = new ConcurrentHashMap<>(); // original -> corrected
  private final Map<String, ContextUnit> unitsByClassName = new ConcurrentHashMap<>();
  private final Map<String, List<String>> abstractNames = new HashMap<>();
  
  private final PluginContext pluginContext = new PluginContext();

  @SuppressWarnings("deprecation")
  public StructContext(IBytecodeProvider legacyProvider, IResultSaver saver, IDecompiledData decompiledData) {
    this.legacyProvider = legacyProvider;
    this.saver = saver;
    this.decompiledData = decompiledData;
  }

  public StructContext(IResultSaver saver, IDecompiledData decompiledData) {
    this.legacyProvider = null;
    this.saver = saver;
    this.decompiledData = decompiledData;
  }

  public StructClass getClass(String name) {
    if (name == null) {
      return null;
    }

    final StructClass ret = this.classes.computeIfAbsent(name, key -> {
      // load class from a context unit
      final ContextUnit unitForClass = this.unitsByClassName.get(key);
      if (unitForClass != null) {
        final StructClass clazz = tryLoadClass(unitForClass, key);
        if (clazz != null) {
          return clazz;
        }
      }
      for (final ContextUnit unit : this.lazyUnits) {
        final StructClass clazz = tryLoadClass(unit, key);
        if (clazz != null) {
          return clazz;
        }
      }
      return getSentinel();
    });
    if (ret == getSentinel()) {
      return null;
    } else {
      final var correctedName = this.badlyPlacedClasses.remove(name);
      // Correct the class location
      if (correctedName != null) {
        this.classes.put(correctedName, ret);
        this.classes.remove(name);
      }
      return ret;
    }
  }

  private StructClass tryLoadClass(final ContextUnit unitForClass, final String key) {
    try {
      DecompilerContext.getLogger().writeMessage("Loading Class: " + key + " from " + unitForClass.getName(), IFernflowerLogger.Severity.INFO);
      final byte[] classBytes = unitForClass.getClassBytes(key);
      if (classBytes == null) {
        return null;
      }
      StructClass clazz = StructClass.create(new DataInputFullStream(classBytes), unitForClass.isOwn());
      if (!key.equals(clazz.qualifiedName)) {
        // also place the class in the right key if it's wrong
        this.unitsByClassName.put(clazz.qualifiedName, unitForClass);
        this.badlyPlacedClasses.put(key, clazz.qualifiedName);
      }
      return clazz;
    } catch (final IOException ex) {
      DecompilerContext.getLogger().writeMessage("Failed to read class " + key + " from " + unitForClass.getName(), IFernflowerLogger.Severity.ERROR, ex);
    }

    return null;
  }

  public boolean hasClass(final String name) {
    if (this.unitsByClassName.containsKey(name)) {
      return true;
    }

    for (final ContextUnit unit : this.lazyUnits) {
      try {
        if (unit.hasClass(name)) {
          return true;
        }
      } catch (final IOException ex) {
        DecompilerContext.getLogger().writeMessage("Failed to check if class " + name + " exists in " + unit.getName(), IFernflowerLogger.Severity.ERROR, ex);
      }
    }

    return false;
  }

  public List<StructClass> getOwnClasses() {
    return this.units.stream()
      .filter(ContextUnit::isOwn)
      .flatMap(unit -> unit.getClassNames().stream())
      .map(name -> Objects.requireNonNull(this.getClass(name), () -> "Could not find class " + name))
      .collect(Collectors.toUnmodifiableList());
  }

  public void reloadContext() throws IOException {
    this.classes.clear();
    this.unitsByClassName.clear();
    this.abstractNames.clear();

    final List<ContextUnit> units = List.copyOf(this.units);
    this.units.clear();
    this.lazyUnits.clear();
    for (ContextUnit unit : units) {
      if (unit.isRoot()) {
        unit.clear();
        this.units.add(unit);
        if (unit.isLazy()) {
          this.lazyUnits.add(unit);
        }
        this.initUnit(unit);
      }
    }
  }

  public void saveContext() {
    for (ContextUnit unit : this.units) {
      if (unit.isOwn()) {
        try {
          unit.save(this::getClass);
        } catch (final IOException ex) {
          DecompilerContext.getLogger().writeMessage("Failed to save data for context unit" + unit.getName(), IFernflowerLogger.Severity.ERROR, ex);
        }
      }
    }
  }

  private static boolean isJarFile(File file) {
    if (!file.isFile()) return false;
    String name = file.getName();
    if (name.endsWith(".jar") || name.endsWith(".zip")) return true;
    if (name.endsWith(".class")) return false;
    try (SeekableByteChannel channel = Files.newByteChannel(file.toPath())) {
      long size = channel.size();
      // The EOCD ZIP record has 22+n bytes depending on the length of the comment.
      if (size < 22) return false;
      int bufferSize = (int) Math.min(size & ~3, 1024);
      channel.position(size - bufferSize);
      ByteBuffer buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN);
      int read = 0;
      while (read < bufferSize) {
        read += channel.read(buffer);
      }
      buffer.flip();
      for (int pos = buffer.limit() - 22; pos >= 0; pos--) {
        if (buffer.getInt(pos) == 0x06054b50) {
          return true;
        }
      }
    } catch (IOException e) {
      DecompilerContext.getLogger().writeMessage("Could not determine if " + file + " contains a JAR file", IFernflowerLogger.Severity.WARN, e);
    }
    return false;
  }

  public void addSpace(File file, boolean isOwn) {
    if (file.isDirectory()) {
      addSpace(new DirectoryContextSource(this.legacyProvider, file), isOwn);
    } else if (isJarFile(file)) {
      // archive
      try {
        addSpace(new JarContextSource(this.legacyProvider, file), isOwn);
      } catch (final IOException ex) {
        final String message = "Invalid archive " + file;
        DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.ERROR, ex);
        throw new UncheckedIOException(message, ex);
      }
    } else {
      try {
        addSpace(new SingleFileContextSource(this.legacyProvider, file), isOwn);
      } catch (final IOException ex) {
        final String message = "Invalid file " + file;
        DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.ERROR, ex);
        throw new UncheckedIOException(message, ex);
      }
    }
  }

  public void addSpace(final IContextSource source, final boolean isOwn) {
    this.addSpace(source, isOwn, true);
  }

  private void addSpace(final IContextSource source, final boolean isOwn, final boolean isRoot) {
    final ContextUnit unit = new ContextUnit(source, isOwn, isRoot, saver, decompiledData);
    this.units.add(unit);
    if (unit.isLazy()) {
      this.lazyUnits.add(unit);
    }
    initUnit(unit);
  }

  public PluginContext getPluginContext() {
    return this.pluginContext;
  }
  
  private void initUnit(final ContextUnit unit) {
    DecompilerContext.getLogger().writeMessage("Scanning classes from " + unit.getName(), IFernflowerLogger.Severity.INFO);
    boolean isOwn = unit.isOwn();
    for (final String clazz : unit.getClassNames()) {
      final ContextUnit existing = this.unitsByClassName.putIfAbsent(clazz, unit);
      if (existing != null) {
        if (!isOwn || existing.isOwn()) continue;

        if (!this.unitsByClassName.replace(clazz, existing, unit)) continue;
      }

      DecompilerContext.getLogger().writeMessage("    " + clazz, IFernflowerLogger.Severity.TRACE);
      if (isOwn) { // pre-load classes
        this.getClass(clazz);
      }
    }

    for (final IContextSource child : unit.getChildContexts()) {
      this.addSpace(child, isOwn, false);
    }
  }

  public boolean instanceOf(String valclass, String refclass) {
    if (valclass.equals(refclass)) {
      return true;
    }

    StructClass cl = this.getClass(valclass);
    // Don't know what we are? We must at least be an object.
    if ((cl == null || cl.superClass == null) && refclass.equals("java/lang/Object")) {
      return true;
    }

    if (cl == null) {
      return false;
    }

    if (cl.superClass != null && this.instanceOf(cl.superClass.getString(), refclass)) {
      return true;
    }

    int[] interfaces = cl.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      String intfc = cl.getPool().getPrimitiveConstant(interfaces[i]).getString();

      if (this.instanceOf(intfc, refclass)) {
        return true;
      }
    }

    return false;
  }

  public StructClass getFirstCommonClass(String firstclass, String secondclass) {
    StructClass fcls = this.getClass(firstclass);
    StructClass scls = this.getClass(secondclass);

    if (fcls != null && scls != null) {
      List<StructClass> clsList = scls.getAllSuperClasses();
      while (fcls != null) {
        if (clsList.contains(fcls)) {
          return fcls;
        }

        fcls = fcls.superClass == null ? null : this.getClass(fcls.superClass.getString());
      }
    }

    return null;
  }

  public void loadAbstractMetadata(String string) {
    for (String line : string.split("\n")) {
      String[] pts = line.split(" ");
      if (pts.length < 4) //class method desc [args...]
        continue;
      GenericMethodDescriptor desc = GenericMain.parseMethodSignature(pts[2]);
      List<String> params = new ArrayList<>();
      for (int x = 0; x < pts.length - 3; x++) {
        for (int y = 0; y < desc.parameterTypes.get(x).stackSize; y++)
            params.add(pts[x+3]);
      }
      this.abstractNames.put(pts[0] + ' '+ pts[1] + ' ' + pts[2], params);
    }
  }

  public String renameAbstractParameter(String className, String methodName, String descriptor, int index, String _default) {
    List<String> params = this.abstractNames.get(className + ' ' + methodName + ' ' + descriptor);
    return params != null && index < params.size() ? params.get(index) : _default;
  }

  public void clear() {
    try {
      this.saver.close();
    } catch (final IOException ex) {
      DecompilerContext.getLogger().writeMessage("Failed to close out result saver", IFernflowerLogger.Severity.ERROR, ex);
    }

    for (final ContextUnit unit : this.units) {
      try {
        unit.close();
      } catch (final Exception ex) {
        DecompilerContext.getLogger().writeMessage("Failed to close context unit " + unit.getName(), IFernflowerLogger.Severity.ERROR, ex);
      }
    }
    this.units.clear();
    this.unitsByClassName.clear();
    this.classes.clear();
  }

}
