// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ConsoleDecompiler implements IBytecodeProvider, IResultSaver {
  private final File root;
  private final Fernflower engine;
  private final Map<String, ZipOutputStream> mapArchiveStreams = new HashMap<>();
  private final Map<String, Set<String>> mapArchiveEntries = new HashMap<>();

  // Legacy support
  protected ConsoleDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger) {
    this(destination, options, logger, destination.isDirectory() ? SaveType.LEGACY_CONSOLEDECOMPILER : SaveType.FILE);
  }

  protected ConsoleDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger, SaveType saveType) {
    root = destination;
    engine = new Fernflower(this, saveType == SaveType.LEGACY_CONSOLEDECOMPILER ? this : saveType.getSaver().apply(destination), options, logger);
  }

  public void addSource(File source) {
    engine.addSource(source);
  }

  public void addLibrary(File library) {
    engine.addLibrary(library);
  }

  public void addWhitelist(String prefix) {
    engine.addWhitelist(prefix);
  }

  public void decompileContext() {
    try {
      engine.decompileContext();
    }
    finally {
      engine.clearContext();
    }
  }

  // *******************************************************************
  // Interface IBytecodeProvider
  // *******************************************************************

  @Override
  public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
    File file = new File(externalPath);
    if (internalPath == null) {
      return InterpreterUtil.getBytes(file);
    }
    else {
      try (ZipFile archive = new ZipFile(file)) {
        ZipEntry entry = archive.getEntry(internalPath);
        if (entry == null) throw new IOException("Entry not found: " + internalPath);
        return InterpreterUtil.getBytes(archive, entry);
      }
    }
  }

  // *******************************************************************
  // Interface IResultSaver
  // *******************************************************************

  private String getAbsolutePath(String path) {
    return new File(root, path).getAbsolutePath();
  }

  @Override
  public void saveFolder(String path) {
    File dir = new File(getAbsolutePath(path));
    if (!(dir.mkdirs() || dir.isDirectory())) {
      throw new RuntimeException("Cannot create directory " + dir);
    }
  }

  @Override
  public void copyFile(String source, String path, String entryName) {
    try {
      InterpreterUtil.copyFile(new File(source), new File(getAbsolutePath(path), entryName));
    }
    catch (IOException ex) {
      DecompilerContext.getLogger().writeMessage("Cannot copy " + source + " to " + entryName, ex);
    }
  }

  @Override
  public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
    File file = new File(getAbsolutePath(path), entryName);
    try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
      out.write(content);
    }
    catch (IOException ex) {
      DecompilerContext.getLogger().writeMessage("Cannot write class file " + file, ex);
    }
  }

  @Override
  public void createArchive(String path, String archiveName, Manifest manifest) {
    File file = new File(getAbsolutePath(path), archiveName);
    try {
      if (!(file.createNewFile() || file.isFile())) {
        throw new IOException("Cannot create file " + file);
      }

      FileOutputStream fileStream = new FileOutputStream(file);
      ZipOutputStream zipStream = manifest != null ? new JarOutputStream(fileStream, manifest) : new ZipOutputStream(fileStream);
      mapArchiveStreams.put(file.getPath(), zipStream);
    }
    catch (IOException ex) {
      DecompilerContext.getLogger().writeMessage("Cannot create archive " + file, ex);
    }
  }

  @Override
  public void saveDirEntry(String path, String archiveName, String entryName) {
    if (entryName.lastIndexOf('/') != entryName.length() - 1) {
      entryName += '/';
    }
    saveClassEntry(path, archiveName, null, entryName, null);
  }

  @Override
  public void copyEntry(String source, String path, String archiveName, String entryName) {
    String file = new File(getAbsolutePath(path), archiveName).getPath();

    if (!checkEntry(entryName, file)) {
      return;
    }

    try (ZipFile srcArchive = new ZipFile(new File(source))) {
      ZipEntry entry = srcArchive.getEntry(entryName);
      if (entry != null) {
        try (InputStream in = srcArchive.getInputStream(entry)) {
          ZipOutputStream out = mapArchiveStreams.get(file);
          out.putNextEntry(new ZipEntry(entryName));
          InterpreterUtil.copyStream(in, out);
        }
      }
    }
    catch (IOException ex) {
      String message = "Cannot copy entry " + entryName + " from " + source + " to " + file;
      DecompilerContext.getLogger().writeMessage(message, ex);
    }
  }

  @Override
  public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
    this.saveClassEntry(path, archiveName, qualifiedName, entryName, content, null);
  }

  @Override
  public synchronized void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content, int[] mapping) {
    String file = new File(getAbsolutePath(path), archiveName).getPath();

    if (!checkEntry(entryName, file)) {
      return;
    }

    try {
      ZipOutputStream out = mapArchiveStreams.get(file);
      ZipEntry entry = new ZipEntry(entryName);
      if (mapping != null && DecompilerContext.getOption(IFernflowerPreferences.DUMP_CODE_LINES)) {
        entry.setExtra(this.getCodeLineData(mapping));
      }
      out.putNextEntry(entry);
      if (content != null) {
        out.write(content.getBytes(StandardCharsets.UTF_8));
      }
    }
    catch (IOException ex) {
      String message = "Cannot write entry " + entryName + " to " + file;
      DecompilerContext.getLogger().writeMessage(message, ex);
    }
  }

  private boolean checkEntry(String entryName, String file) {
    Set<String> set = mapArchiveEntries.computeIfAbsent(file, k -> new HashSet<>());

    boolean added = set.add(entryName);
    if (!added) {
      String message = "Zip entry " + entryName + " already exists in " + file;
      DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN);
    }
    return added;
  }

  @Override
  public void closeArchive(String path, String archiveName) {
    String file = new File(getAbsolutePath(path), archiveName).getPath();
    try {
      mapArchiveEntries.remove(file);
      mapArchiveStreams.remove(file).close();
    }
    catch (IOException ex) {
      DecompilerContext.getLogger().writeMessage("Cannot close " + file, IFernflowerLogger.Severity.WARN);
    }
  }

  public enum SaveType {
    LEGACY_CONSOLEDECOMPILER(null), // handled separately
    FOLDER(DirectoryResultSaver::new),
    FILE(SingleFileSaver::new);

    private final Function<File, IResultSaver> saver;

    SaveType(Function<File, IResultSaver> saver) {
      this.saver = saver;
    }

    public Function<File, IResultSaver> getSaver() {
      return saver;
    }
  }
}
