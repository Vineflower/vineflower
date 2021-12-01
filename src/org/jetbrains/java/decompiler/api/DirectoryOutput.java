package org.jetbrains.java.decompiler.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.Manifest;

class DirectoryOutput extends PathBasedOutput {
  public DirectoryOutput(Path path) {
    super(path);
  }

  @Override
  public void writeManifest(Manifest manifest) throws IOException {
  }
}
