package org.jetbrains.java.decompiler.main.decompiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

public class SingleFileSaver implements IResultSaver {
  private final File target;
  private ZipOutputStream output;
  private Set<String> entries = new HashSet<>();

  public SingleFileSaver(File target) {
    this.target = target;
  }

  @Override
  public void saveFolder(String path) {
    if (!"".equals(path))
      throw new UnsupportedOperationException("Targeted a single output, but tried to create a directory");
  }

  @Override
  public void copyFile(String source, String path, String entryName) {
    throw new UnsupportedOperationException("Targeted a single output, but tried to copy file");
  }

  @Override
  public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
    throw new UnsupportedOperationException("Targeted a single output, but tried to save a class file");
  }

  @Override
  public void createArchive(String path, String archiveName, Manifest manifest) {
    if (output != null)
      throw new UnsupportedOperationException("Attempted to write multiple archives at the same time");
    try {
      FileOutputStream stream = new FileOutputStream(target);
      output = manifest != null ? new JarOutputStream(stream, manifest) : new ZipOutputStream(stream);
    } catch (IOException e) {
      DecompilerContext.getLogger().writeMessage("Cannot create archive " + target, e);
    }
  }

  @Override
  public void saveDirEntry(String path, String archiveName, String entryName) {
    saveClassEntry(path, archiveName, null, entryName, null);
  }

  @Override
  public void copyEntry(String source, String path, String archiveName, String entryName) {
    if (!checkEntry(entryName))
      return;

    try (ZipFile srcArchive = new ZipFile(new File(source))) {
      ZipEntry entry = srcArchive.getEntry(entryName);
      if (entry != null) {
        try (InputStream in = srcArchive.getInputStream(entry)) {
          output.putNextEntry(new ZipEntry(entryName));
          InterpreterUtil.copyStream(in, output);
        }
      }
    }
    catch (IOException ex) {
      String message = "Cannot copy entry " + entryName + " from " + source + " to " + target;
      DecompilerContext.getLogger().writeMessage(message, ex);
    }
  }

  @Override
  public synchronized void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
    if (!checkEntry(entryName))
        return;

    try {
      output.putNextEntry(new ZipEntry(entryName));
      if (content != null)
          output.write(content.getBytes("UTF-8"));
    }
    catch (IOException ex) {
      String message = "Cannot write entry " + entryName + " to " + target;
      DecompilerContext.getLogger().writeMessage(message, ex);
    }
  }

  @Override
  public void closeArchive(String path, String archiveName) {
    try {
      output.close();
      entries.clear();
      output = null;
    }
    catch (IOException ex) {
      DecompilerContext.getLogger().writeMessage("Cannot close " + target, IFernflowerLogger.Severity.WARN);
    }
  }

  private boolean checkEntry(String entryName) {
    boolean added = entries.add(entryName);
    if (!added) {
      String message = "Zip entry " + entryName + " already exists in " + target;
      DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN);
    }
    return added;
  }
}
