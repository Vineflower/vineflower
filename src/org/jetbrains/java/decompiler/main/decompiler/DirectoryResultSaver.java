package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Manifest;

public final class DirectoryResultSaver implements IResultSaver {
  private final Path root;

  public DirectoryResultSaver(File root) {
    this.root = root.toPath();
  }

  @Override
  public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
    Path entryPath = this.root.resolve(entryName);

    try (BufferedWriter writer = Files.newBufferedWriter(entryPath)) {
      if (content != null) {
        writer.write(content);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to save class", e);
    }
  }

  @Override
  public void saveDirEntry(String path, String archiveName, String entryName) {
    Path entryPath = this.root.resolve(entryName);
    try {
      Files.createDirectories(entryPath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to save directory", e);
    }
  }

  @Override
  public void createArchive(String path, String archiveName, Manifest manifest) {
  }

  @Override
  public void saveFolder(String path) {
  }

  @Override
  public void copyFile(String source, String path, String entryName) {
  }

  @Override
  public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
  }

  @Override
  public void copyEntry(String source, String path, String archiveName, String entry) {
  }

  @Override
  public void closeArchive(String path, String archiveName) {
  }
}