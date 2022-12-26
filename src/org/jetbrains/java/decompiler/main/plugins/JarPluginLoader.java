package org.jetbrains.java.decompiler.main.plugins;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// Finds plugins included in the main Quiltflower Jar via Jar-In-Jar
public class JarPluginLoader {
  static List<Class<?>> PLUGIN_CLASSES = new ArrayList<>();

  public static void init() {
    try {
      File myFile = new File(JarPluginLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());

      // Ensure we are running out of a file
      if (myFile.exists() && !myFile.isDirectory() && myFile.getPath().endsWith(".jar")) {
        try (JarFile qfJar = new JarFile(myFile)) {
          Iterator<JarEntry> it = qfJar.entries().asIterator();
          while (it.hasNext()) {
            JarEntry next = it.next();

            if (next.getName().startsWith("META-INF/plugins/") && next.getName().endsWith(".jar")) {
              InputStream stream = qfJar.getInputStream(next);
              File pluginFile = File.createTempFile("qf-plugin", "jar");
              pluginFile.deleteOnExit();

              // Copy the jar from inside the main jar to a temp file
              try (OutputStream output = new FileOutputStream(pluginFile)) {
                stream.transferTo(output);
              } catch (IOException ioException) {
                ioException.printStackTrace();
              }

              // TODO: plugins should really say what their main class is from the manifest

              JarFile pluginJar = new JarFile(pluginFile);
              Iterator<JarEntry> pluginIt = pluginJar.entries().asIterator();
              while (pluginIt.hasNext()) {
                JarEntry n = pluginIt.next();
                // Find the serviceloader definition, and use that to load the plugin class
                if (n.getName().contains("META-INF/services/org.jetbrains.java.decompiler.api.Plugin")) {
                  InputStream pStream = pluginJar.getInputStream(n);

                  // Load in the new class
                  String path = new String(pStream.readAllBytes());
                  URLClassLoader child = new URLClassLoader(
                    new URL[] {pluginFile.toURI().toURL()},
                    JarPluginLoader.class.getClassLoader()
                  );

                  Class<?> clazz = Class.forName(path, true, child);
                  PLUGIN_CLASSES.add(clazz);

                  break;
                }
              }
            }
          }
        }
      }

    } catch (Exception e) {
      DecompilerContext.getLogger().writeMessage("Couldn't load plugins!", IFernflowerLogger.Severity.INFO, e);
    }
  }
}
