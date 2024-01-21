package org.jetbrains.java.decompiler.main.plugins;

import java.nio.file.FileSystem;
import java.nio.file.Files;

/**
 * Loads classes out of the given filesystem.
 */
class InJarClassLoader extends ClassLoader {

  private final FileSystem fs;

  public InJarClassLoader(FileSystem fs, ClassLoader parent) {
    super(parent);
    this.fs = fs;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {

    try {
      String[] path = name.split("\\.");
      path[path.length - 1] += ".class";

      String[] cleaned = new String[path.length - 1];
      System.arraycopy(path, 1, cleaned, 0, path.length - 1);

      byte[] bytes = Files.readAllBytes(fs.getPath(path[0], cleaned));

      return defineClass(name, bytes, 0, bytes.length);
    } catch (Exception e) {
      throw new ClassNotFoundException(name, e);
    }
  }
}
