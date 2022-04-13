package org.jetbrains.java.decompiler.main;

import org.jetbrains.java.decompiler.api.ClassSet;
import org.jetbrains.java.decompiler.api.DecompilerInput;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.struct.ContextUnit;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.util.FileSystemWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;

public class LegacyDecompilerInput implements DecompilerInput {
  private final LazyLoader loader;
  private final CollectionClassSet ownClasses = new CollectionClassSet();
  private final CollectionClassSet libraries = new CollectionClassSet();

  LegacyDecompilerInput(IBytecodeProvider provider) {
    this.loader = new LazyLoader(provider);
  }

  @Override
  public ClassSet<?> getOwnClasses() {
    return ownClasses;
  }

  @Override
  public Set<ClassSet<?>> getLibraries() {
    return Collections.singleton(libraries);
  }

  public void addSpace(File file, boolean isOwn) {
    addSpace("", file, isOwn, 0);
  }

  public void addSpace(FileSystem fs, boolean isOwn) {
    String scheme = fs.provider().getScheme();
    if ("jar".equals(scheme)) {
      String fileUri = fs.getPath("/").toUri().getSchemeSpecificPart();
      fileUri = fileUri.substring(0, fileUri.indexOf('!'));
      addSpace(Paths.get(fileUri).toFile(), false);
    } else {
      try {
        addFileSystem(fs, "", new File(fs.toString()), ContextUnit.TYPE_JAR, isOwn);
      } catch (IOException e) {
        DecompilerContext.getLogger().writeMessage("Corrupted file system: " + fs, e);
        throw new RuntimeException(e);
      }
    }
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

      //ContextUnit unit = units.get(path);
      //if (unit == null) {
        //unit = new ContextUnit(ContextUnit.TYPE_FOLDER, null, path, isOwn, saver, decompiledData);
        //units.put(path, unit);
      //}

      if (filename.endsWith(".class")) {
        String name = filename.substring(0, filename.length() - 6);
        addClass(path.isEmpty() ? name : path + "/" + name, isOwn, () -> loader.getClassBytes(file.getAbsolutePath(), null));
      }
      else {
        //unit.addOtherEntry(file.getAbsolutePath(), filename);
      }
    }
  }

  private void addArchive(String externalPath, File file, int type, boolean isOwn) throws IOException {
    DecompilerContext.getLogger().writeMessage("Adding Archive: " + file.getAbsolutePath(), IFernflowerLogger.Severity.INFO);
    FileSystem fs = FileSystemWrapper.getZipFileSystem(file.toPath());
    addFileSystem(fs, externalPath, file, type, isOwn);
  }

  private void addFileSystem(FileSystem fs, String externalPath, File file, int type, boolean isOwn) throws IOException {
    //ContextUnit unit = units.computeIfAbsent(externalPath + "/" + file, k -> new ContextUnit(type, externalPath, file.getName(), isOwn, saver, decompiledData));
    Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        String name;
        if (path.getNameCount() > 2 && "modules".equals(path.getName(0).toString()) && "jrt".equals(path.getFileSystem().provider().getScheme())) {
          name = path.subpath(2, path.getNameCount()).toString();
        } else {
          name = path.toString().substring(1);
        }
        if (name.endsWith(".class")) {
          addClass(name.substring(0, name.length() - 6), isOwn, path);
        } else {
          if ("META-INF/MANIFEST.MF".equals(name)) {
            //unit.setManifest(new Manifest(Files.newInputStream(path)));
          }
          //unit.addOtherEntry(file.getAbsolutePath(), name);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String dirStr = dir.toString();
        //if (dirStr.length() > 1) unit.addDirEntry(dirStr.substring(1));
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void addClass(String name, boolean isOwn, Path path) {
    addClass(name, isOwn, () -> Files.readAllBytes(path));
  }

  private void addClass(String name, boolean isOwn, ClassSet.ClassSupplier supplier) {
    (isOwn ? ownClasses : libraries).classes.put(name, supplier);
  }
}
