package org.jetbrains.java.decompiler.api;

import net.fabricmc.fernflower.api.IFabricResultSaver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.Manifest;

public interface DecompilerOutput extends AutoCloseable {
  void createDirectory(String path) throws IOException;
  void writeFile(String path, byte[] data) throws IOException;
  void writeManifest(Manifest manifest) throws IOException;
  void writeClass(String path, String content, int[] mapping) throws IOException;
  DecompilerOutput openArchive(String name) throws IOException;

  static DecompilerOutput directory(Path path) {
    return new DirectoryOutput(path);
  }

  static DecompilerOutput archive(Path path) throws IOException {
    return new ArchiveOutput(path);
  }
}
