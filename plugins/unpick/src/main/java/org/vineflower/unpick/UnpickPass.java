package org.vineflower.unpick;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.vineflower.unpick.parser.UnpickV3Reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnpickPass implements Pass {
  private UnpickDefinitions defs;

  @Override
  public boolean run(PassContext ctx) {
    loadDefinitions();
    if (defs.isEmpty()) {
      return false;
    }

    // TODO
    return false;
  }

  // TODO: move to UnpickPlugin.initialize
  private synchronized void loadDefinitions() {
    if (defs != null) {
      return;
    }

    defs = new UnpickDefinitions();

    try {
      loadBuiltInDefinitions();
      defs.canBeOverwritten = false;
      loadCustomDefinitions();
    } catch (IOException e) {
      DecompilerContext.getLogger().writeMessage("Failed to load unpick definitions", e);
    }
  }

  private void loadBuiltInDefinitions() throws IOException {
    if (!DecompilerContext.getOption(UnpickOptions.USE_BUILTIN_UNPICK_FILES)) {
      return;
    }

    try (InputStream jdkUnpick = UnpickPass.class.getResourceAsStream("jdk.unpick")) {
      if (jdkUnpick == null) {
        throw new IOException("Could not find built-in unpick definitions");
      }

      new UnpickV3Reader(new InputStreamReader(jdkUnpick)).accept(defs);
    }
  }

  private void loadCustomDefinitions() throws IOException {
    String unpickFiles = (String) DecompilerContext.getProperty(UnpickOptions.UNPICK_FILES);
    if (unpickFiles.isEmpty()) {
      return;
    }

    loadCustomDefinitions(Path.of(unpickFiles));
  }

  private void loadCustomDefinitions(Path path) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<>() {
        @Override
        @NotNull
        public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
          loadCustomDefinitions(file);
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      String fileName = path.getFileName().toString();
      if (fileName.endsWith(".unpick")) {
        try (UnpickV3Reader reader = new UnpickV3Reader(Files.newBufferedReader(path))) {
          reader.accept(defs);
        }
      } else if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
        try (ZipFile zip = new ZipFile(path.toFile())) {
          Enumeration<? extends ZipEntry> entries = zip.entries();
          while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".unpick")) {
              try (InputStream stream = zip.getInputStream(entry)) {
                new UnpickV3Reader(new InputStreamReader(stream)).accept(defs);
              }
            }
          }
        }
      }
    }
  }
}
