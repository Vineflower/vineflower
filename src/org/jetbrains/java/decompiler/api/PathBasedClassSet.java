package org.jetbrains.java.decompiler.api;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;

public class PathBasedClassSet implements ClassSet<Path> {
  protected final Path root;

  public PathBasedClassSet(Path root) {
    this.root = root;
  }

  protected String getClassName(Path path) {
    Path relative = root.relativize(path);
    String strPath = relative.toString();
    if (strPath.endsWith(".class")) {
      return strPath.substring(0, strPath.length() - 6);
    }
    return null;
  }

  @Override
  public Map<String, Path> getClassNames() throws IOException {
    Map<String, Path> classes = new LinkedHashMap<>();
    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        String name = getClassName(file);
        if (name != null) classes.put(name, file);
        return FileVisitResult.CONTINUE;
      }
    });
    return classes;
  }

  @Override
  public byte[] getClass(Path path) throws IOException {
    return Files.readAllBytes(path);
  }

  public static PathBasedClassSet of(FileSystem fs) {
    return new PathBasedClassSet(fs.getPath("/"));
  }

  //public static PathBasedClassSet ofArchive(Path archive) {

  //}
}
