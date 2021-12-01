package org.jetbrains.java.decompiler.api;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class ArchiveOutput extends PathBasedOutput {
  public ArchiveOutput(FileSystem fs) {
    super(fs.getPath("/"));
  }

  public ArchiveOutput(Path path) throws IOException {
    this(createZipFile(path));
  }

  private static FileSystem createZipFile(Path path) throws IOException {
    URI uri = URI.create("jar:" + path.toUri());
    Map<String, String> options = new HashMap<>();
    options.put("create", "true");
    return FileSystems.newFileSystem(uri, options);
  }

  @Override
  public DecompilerOutput openArchive(String name) {
    return this;
  }
}
