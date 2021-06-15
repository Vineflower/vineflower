// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger.Severity;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMain;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMethodDescriptor;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.Manifest;

public class StructContext {
  private final IResultSaver saver;
  private final IDecompiledData decompiledData;
  private final LazyLoader loader;
  private final Map<String, ContextUnit> units = new HashMap<>();
  private final Map<String, ClassProvider> classes = new HashMap<>();
  private final Map<String, StructClass> ownClasses = new HashMap<>();
  private final Map<String, List<String>> abstractNames = new HashMap<>();
  private final Map<File, FileSystem> zipFiles = new HashMap<>();

  public StructContext(IResultSaver saver, IDecompiledData decompiledData, LazyLoader loader) {
    this.saver = saver;
    this.decompiledData = decompiledData;
    this.loader = loader;

    ContextUnit defaultUnit = new ContextUnit(ContextUnit.TYPE_FOLDER, null, "", true, saver, decompiledData);
    units.put("", defaultUnit);
  }

  public StructClass getClass(String name) {
    ClassProvider provider = classes.get(name);
    if (provider == null) {
      return null;
    }
    return provider.get();
  }

  public void reloadContext() throws IOException {
    for (Map.Entry<String, ContextUnit> e : units.entrySet()) {
      ContextUnit unit = e.getValue();
      for (StructClass cl : unit.getClasses()) {
        classes.remove(cl.qualifiedName);
      }

      unit.reload(loader);

      // adjust global class collection
      for (StructClass cl : unit.getClasses()) {
        classes.put(cl.qualifiedName, new ClassProvider(cl));
      }
    }
  }

  public void saveContext() {
    for (ContextUnit unit : units.values()) {
      if (unit.isOwn()) {
        unit.save();
      }
    }
  }

  public void addSpace(File file, boolean isOwn) {
    addSpace("", file, isOwn, 0);
  }

  private void addSpace(String path, File file, boolean isOwn, int level) {
    if (file.isDirectory()) {
      if (level == 1) path += file.getName();
      else if (level > 1) path += "/" + file.getName();

      File[] files = file.listFiles();
      if (files != null) {
        for (int i = files.length - 1; i >= 0; i--) {
          addSpace(path, files[i], isOwn, level + 1);
        }
      }
    }
    else {
      String filename = file.getName();

      boolean isArchive = false;
      try {
        if (filename.endsWith(".jar")) {
          isArchive = true;
          addArchive(path, file, ContextUnit.TYPE_JAR, isOwn);
        }
        else if (filename.endsWith(".zip")) {
          isArchive = true;
          addArchive(path, file, ContextUnit.TYPE_ZIP, isOwn);
        }
      }
      catch (IOException ex) {
        String message = "Corrupted archive file: " + file;
        DecompilerContext.getLogger().writeMessage(message, ex);
        throw new RuntimeException(ex);
      }
      if (isArchive) {
        return;
      }

      ContextUnit unit = units.get(path);
      if (unit == null) {
        unit = new ContextUnit(ContextUnit.TYPE_FOLDER, null, path, isOwn, saver, decompiledData);
        units.put(path, unit);
      }

      if (filename.endsWith(".class")) {
        addClass(unit, null, path, filename, isOwn, () -> loader.getClassBytes(file.getAbsolutePath(), null));
      }
      else {
        unit.addOtherEntry(file.getAbsolutePath(), filename);
      }
    }
  }

  private FileSystem getZipFileSystem(File file) throws IOException {
    try {
      return zipFiles.computeIfAbsent(file, f -> {
        URI uri = null;
        try {
          uri = new URI("jar:file", null, f.toURI().getPath(), null);
          return FileSystems.newFileSystem(uri, Collections.emptyMap());
        } catch (FileSystemAlreadyExistsException e) {
          return FileSystems.getFileSystem(uri);
        } catch (URISyntaxException e) {
          throw new RuntimeException(e);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private void addArchive(String externalPath, File file, int type, boolean isOwn) throws IOException {
    DecompilerContext.getLogger().writeMessage("Adding Archive: " + file.getAbsolutePath(), Severity.INFO);
    FileSystem fs = getZipFileSystem(file);
    ContextUnit unit = units.computeIfAbsent(externalPath + "/" + file.getName(), k -> new ContextUnit(type, externalPath, file.getName(), isOwn, saver, decompiledData));
    Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        String name = path.toString().substring(1);
        if (name.endsWith(".class")) {
          addClass(unit, name.substring(0, name.length() - 6), file.getAbsolutePath(), path.toString().substring(1), isOwn, path);
        } else {
          if ("META-INF/MANIFEST.MF".equals(name)) {
            unit.setManifest(new Manifest(Files.newInputStream(path)));
          }
          unit.addOtherEntry(file.getAbsolutePath(), name);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String dirStr = dir.toString();
        if (dirStr.length() > 1) unit.addDirEntry(dirStr.substring(1));
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void addClass(ContextUnit unit, String name, String externalPath, String internalPath, boolean isOwn, Path path) {
    addClass(name, isOwn, new ClassProvider(unit, externalPath, internalPath, isOwn, () -> Files.readAllBytes(path)));
  }

  private void addClass(ContextUnit unit, String name, String externalPath, String internalPath, boolean isOwn, ClassSupplier supplier) {
    addClass(name, isOwn, new ClassProvider(unit, externalPath, internalPath, isOwn, supplier));
  }

  private void addClass(String name, boolean isOwn, ClassProvider provider) {
    if (name == null || name.isEmpty()) {
      name = provider.get().qualifiedName;
    }
    classes.put(name, provider);
    if (isOwn) ownClasses.put(name, provider.get());
  }

  public void addData(String path, String cls, byte[] data, boolean isOwn) throws IOException {
    ContextUnit unit = units.get(path);
    if (unit == null) {
      unit = new ContextUnit(ContextUnit.TYPE_FOLDER, path, cls, isOwn, saver, decompiledData);
      units.put(path, unit);
    }

    addClass(unit, cls.substring(0, cls.length() - 6), path, cls, isOwn, () -> data);
  }

  public Map<String, StructClass> getOwnClasses() {
    return ownClasses;
  }

  public boolean hasClass(String name) {
    return classes.containsKey(name);
  }

  public boolean instanceOf(String valclass, String refclass) {
    if (valclass.equals(refclass)) {
      return true;
    }

    StructClass cl = this.getClass(valclass);
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

  class ClassProvider {
    final ContextUnit unit;
    private final String externalPath;
    private final String internalPath;
    private volatile ClassSupplier supplier;
    private final boolean own;
    private StructClass value;

    ClassProvider(ContextUnit unit, String externalPath, String internalPath, boolean own, ClassSupplier supplier) {
      this.unit = unit;
      this.externalPath = externalPath;
      this.internalPath = internalPath;
      this.own = own;
      this.supplier = supplier;
    }

    ClassProvider(StructClass value) {
      this.unit = null;
      this.externalPath = null;
      this.internalPath = null;
      this.supplier = null;
      this.own = value.isOwn();
      this.value = value;
    }

    public StructClass get() {
      StructClass v = value;
      if (v != null) return v;
      synchronized (this) {
        if (supplier == null) return value;
        try {
          DecompilerContext.getLogger().writeMessage("  Loading Class: " + internalPath, Severity.INFO);
          byte[] data = supplier.get();
          StructClass cl = StructClass.create(new DataInputFullStream(data), own, loader);
          unit.addClass(cl, internalPath);
          loader.addClassLink(cl.qualifiedName, new LazyLoader.Link(externalPath, internalPath, data));
          value = cl;
          supplier = null;
          return cl;
        } catch (IOException ex) {
          String message = "Corrupted class file: " + internalPath;
          DecompilerContext.getLogger().writeMessage(message, ex);
          throw new RuntimeException(ex);
        }
      }
    }
  }

  interface ClassSupplier {
    byte[] get() throws IOException;
  }
}
