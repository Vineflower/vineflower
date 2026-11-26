package org.vineflower.unpick;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.api.plugin.pass.ClassPass;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.util.Key;
import org.vineflower.unpick.parser.UnpickParseException;
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

public class UnpickPass implements ClassPass {
  private static final Key<UnpickDefinitions> DEFINITIONS_KEY = Key.of("unpick-definitions");

  @Override
  public boolean run(ClassesProcessor.ClassNode classNode) {
    UnpickDefinitions defs = loadDefinitions();
    if (defs.isEmpty()) {
      return false;
    }

    GroupTracker groupTracker = new GroupTracker();

    UnpickTargetUtils.identifyTargets(defs, classNode, groupTracker);
    while (UnpickPropagationUtils.propagateGroups(classNode, groupTracker)) {
      // keep propagating while changed
    }
    boolean res = UnpickSubstitutionUtils.substituteConstants(defs, classNode, groupTracker);
    res |= UnpickSubstitutionUtils.substituteConstantFieldInitializers(defs, classNode);
    return res;
  }

  // Loading in Plugin.initialize is too early, we need StructContext to be populated
  private static UnpickDefinitions loadDefinitions() {
    UnpickDefinitions defs = DecompilerContext.getContextProperty(DEFINITIONS_KEY);
    if (defs != null) {
      return defs;
    }

    defs = new UnpickDefinitions();
    DecompilerContext.setProperty(DEFINITIONS_KEY, defs);

    try {
      loadBuiltInDefinitions(defs);
      defs.areDefinitionsFromVineflower = false;
      loadCustomDefinitions(defs);
    } catch (IOException e) {
      DecompilerContext.getLogger().writeMessage("Failed to load unpick definitions", e);
    }

    return defs;
  }

  private static void loadBuiltInDefinitions(UnpickDefinitions defs) throws IOException {
    if (!DecompilerContext.getOption(UnpickOptions.USE_BUILTIN_UNPICK_FILES)) {
      return;
    }

    try (InputStream jdkUnpick = UnpickPass.class.getResourceAsStream("/jdk.unpick")) {
      if (jdkUnpick == null) {
        throw new IOException("Could not find built-in unpick definitions");
      }

      new UnpickV3Reader(new InputStreamReader(jdkUnpick)).accept(defs);
    }
  }

  private static void loadCustomDefinitions(UnpickDefinitions defs) throws IOException {
    String unpickFiles = (String) DecompilerContext.getProperty(UnpickOptions.UNPICK_FILES);
    if (unpickFiles.isEmpty()) {
      return;
    }

    loadCustomDefinitions(Path.of(unpickFiles), defs);
  }

  private static void loadCustomDefinitions(Path path, UnpickDefinitions defs) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<>() {
        @Override
        @NotNull
        public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
          loadCustomDefinitions(file, defs);
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      String fileName = path.getFileName().toString();
      if (fileName.endsWith(".unpick")) {
        try (UnpickV3Reader reader = new UnpickV3Reader(Files.newBufferedReader(path))) {
          reader.accept(defs);
        } catch (UnpickParseException e) {
          DecompilerContext.getLogger().writeMessage(path + ":" + e.getMessage(), IFernflowerLogger.Severity.WARN);
        }
      } else if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
        try (ZipFile zip = new ZipFile(path.toFile())) {
          Enumeration<? extends ZipEntry> entries = zip.entries();
          while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".unpick")) {
              try (InputStream stream = zip.getInputStream(entry)) {
                new UnpickV3Reader(new InputStreamReader(stream)).accept(defs);
              } catch (UnpickParseException e) {
                DecompilerContext.getLogger().writeMessage(path + "!/" + entry.getName() + ":" + e.getMessage(), IFernflowerLogger.Severity.WARN);
              }
            }
          }
        }
      }
    }
  }
}
