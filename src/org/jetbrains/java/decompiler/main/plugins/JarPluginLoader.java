package org.jetbrains.java.decompiler.main.plugins;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Finds plugins included in the main Quiltflower Jar via Jar-In-Jar
public class JarPluginLoader {
  static List<Class<?>> PLUGIN_CLASSES = new ArrayList<>();

  public static void init() {
    try {
      File myFile = new File(JarPluginLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());

      // Ensure we are running out of a file
      if (myFile.exists() && !myFile.isDirectory() && myFile.getPath().endsWith(".jar")) {
        URI uri = URI.create("jar:" + myFile.toURI());

        Map<String, String> env = new HashMap<>();
        // TODO: is this needed?
        env.put("create", "true");

        FileSystem zipfs = FileSystems.newFileSystem(uri, env);

        try (Stream<Path> pluginStream = Files.list(zipfs.getPath("META-INF", "plugins"))) {
          List<Path> plugins = pluginStream.collect(Collectors.toList());

          for (Path pluginJar : plugins) {
            FileSystem pluginfs = FileSystems.newFileSystem(pluginJar, (ClassLoader) null);

            Path file = pluginfs.getPath("META-INF", "services", "org.jetbrains.java.decompiler.api.Plugin");
            String pluginClass = Files.readString(file);

            InJarClassLoader loader = new InJarClassLoader(pluginfs);

            Class<?> clazz = Class.forName(pluginClass, true, loader);
            PLUGIN_CLASSES.add(clazz);
          }
        }
      }

    } catch (Exception e) {
      // TODO: use decompiler logger, but it's not ready yet. potentially set up a logger defer system?
//      DecompilerContext.getLogger().writeMessage("Couldn't load plugins!", IFernflowerLogger.Severity.INFO, e);
      e.printStackTrace();
    }
  }
}
