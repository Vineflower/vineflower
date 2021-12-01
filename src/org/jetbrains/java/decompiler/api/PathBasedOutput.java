package org.jetbrains.java.decompiler.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Manifest;

public class PathBasedOutput implements DecompilerOutput {
  protected final Path root;

  public PathBasedOutput(Path root) {
    this.root = root;
  }

  @Override
  public void createDirectory(String path) throws IOException {
    Files.createDirectories(this.root.resolve(path));
  }

  @Override
  public void writeFile(String path, byte[] data) throws IOException {
    Files.write(this.root.resolve(path), data);
  }

  @Override
  public void writeManifest(Manifest manifest) throws IOException {
    manifest.write(Files.newOutputStream(this.root.resolve("META-INF/MANIFEST.MF")));
  }

  @Override
  public void writeClass(String path, String content, int[] mapping) throws IOException {
    Files.write(this.root.resolve(path), content.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public DecompilerOutput openArchive(String name) throws IOException {
    return new ArchiveOutput(this.root.resolve(name));
  }

  @Override
  public void close() throws Exception {
    FileSystem fs = this.root.getFileSystem();
    if (fs != FileSystems.getDefault()) {
      this.root.getFileSystem().close();
    }
  }
}
