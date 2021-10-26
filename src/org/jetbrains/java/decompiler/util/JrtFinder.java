package org.jetbrains.java.decompiler.util;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger.Severity;
import org.jetbrains.java.decompiler.struct.StructContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JrtFinder {
  public static void addJrt(StructContext ctx) {
    IFernflowerLogger logger = DecompilerContext.getLogger();
    try {
      URL objectUrl = Object.class.getResource("/java/lang/Object.class");
      if (objectUrl == null) {
        logger.writeMessage("Could not locate Java runtime", Severity.WARN);
        return;
      }
      URI objectUri = objectUrl.toURI();
      switch (objectUri.getScheme()) {
        case "jar":
          String fileUri = objectUri.getSchemeSpecificPart();
          fileUri = fileUri.substring(0, fileUri.indexOf('!'));
          Path jarPath = Paths.get(new URI(fileUri));
          ctx.addSpace(jarPath.toFile(), false);
          break;
        case "jrt":
          Path objectPath = Paths.get(objectUri);
          FileSystem jrtFs = objectPath.getFileSystem();
          ctx.addSpace(jrtFs, false);
          break;
        default:
          logger.writeMessage("Unknown file system for Java runtime: " + objectUri.getScheme(), Severity.WARN);
      }
    } catch (URISyntaxException e) {
      logger.writeMessage("Could not locate Java runtime", Severity.WARN, e);
    }
  }
}
