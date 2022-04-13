package org.jetbrains.java.decompiler.util;

import org.jetbrains.java.decompiler.api.ClassSet;
import org.jetbrains.java.decompiler.api.DecompilerInput;
import org.jetbrains.java.decompiler.api.PathBasedClassSet;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

public class JrtClassSet extends PathBasedClassSet {
  private static JrtClassSet instance;
  private static DecompilerInput input;

  private final boolean isJrtFs;

  private JrtClassSet(FileSystem fs) {
    super(fs.getPath("/"));
    this.isJrtFs = "jrt".equals(fs.provider().getScheme());
  }

  private JrtClassSet(Path jar) throws IOException {
    this(FileSystemWrapper.getZipFileSystem(jar));
  }

  @Override
  protected String getClassName(Path path) {
    if (path.getNameCount() > 2 && "modules".equals(path.getName(0).toString()) && isJrtFs) {
      return super.getClassName(path.subpath(2, path.getNameCount()));
    }
    return super.getClassName(path);
  }

  public static DecompilerInput createInput() {
    if (input == null) {
      input = new DecompilerInput() {
        @Override
        public ClassSet<?> getOwnClasses() {
          return null;
        }

        @Override
        public Set<ClassSet<?>> getLibraries() {
          return Collections.singleton(getInstance());
        }
      };
    }
    return input;
  }

  public static JrtClassSet getInstance() {
    if (instance == null) instance = find();
    return instance;
  }

  private static JrtClassSet find() {
    IFernflowerLogger logger = DecompilerContext.getLogger();
    try {
      URL objectUrl = Object.class.getResource("/java/lang/Object.class");
      if (objectUrl == null) {
        logger.writeMessage("Could not locate Java runtime", IFernflowerLogger.Severity.WARN);
        return null;
      }
      URI objectUri = objectUrl.toURI();
      switch (objectUri.getScheme()) {
        case "jar":
          String fileUri = objectUri.getRawSchemeSpecificPart();
          fileUri = fileUri.substring(0, fileUri.indexOf('!'));
          return new JrtClassSet(Paths.get(new URI(fileUri)));
        case "jrt":
          return new JrtClassSet(Paths.get(objectUri).getFileSystem());
        default:
          logger.writeMessage("Unknown file system for Java runtime: " + objectUri.getScheme(), IFernflowerLogger.Severity.WARN);
      }
    } catch (URISyntaxException e) {
      logger.writeMessage("Could not locate Java runtime", IFernflowerLogger.Severity.WARN, e);
    } catch (IOException e) {
      logger.writeMessage("Error opening Java runtime JAR", IFernflowerLogger.Severity.ERROR, e);
    }
    return null;
  }
}
