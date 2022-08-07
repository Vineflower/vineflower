// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ConsoleDecompiler implements IBytecodeProvider, IResultSaver {
  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public static void main(String[] args) {
    List<String> params = new ArrayList<String>();
    for (int x = 0; x < args.length; x++) {
      if (args[x].startsWith("-cfg")) {
        String path = null;
        if (args[x].startsWith("-cfg=")) {
          path = args[x].substring(5);
        }
        else if (args.length > x+1) {
          path = args[++x];
        }
        else {
          System.out.println("Must specify a file when using -cfg argument.");
          return;
        }
        Path file = Paths.get(path);
        if (!Files.exists(file)) {
          System.out.println("error: missing config '" + path + "'");
          return;
        }
        try (Stream<String> stream = Files.lines(file)) {
          stream.forEach(params::add);
        } catch (IOException e) {
          System.out.println("error: Failed to read config file '" + path + "'");
          throw new RuntimeException(e);
        }
      }
      else {
        params.add(args[x]);
      }
    }
    args = params.toArray(new String[params.size()]);

    if (Arrays.stream(args).anyMatch(arg -> arg.equals("-h") || arg.equals("--help") || arg.equals("-help"))) {
      String[] help = {
        "Usage: java -jar quiltflower.jar [-<option>=<value>]* [<source>]+ <destination>",
        "At least one source file or directory must be specified.",
        "Options:",
        "--help: Show this help",
        "", "Saving options",
        "A maximum of one of the options can be specified:",
        "--file          - Write the decompiled source to a file",
        "--folder        - Write the decompiled source to a folder",
        "--legacy-saving - Use the legacy console-specific method of saving",
        "If unspecified, the decompiled source will be automatically detected based on destination name.",
        "", "General options",
        "These options can be specified multiple times.",
        "-e=<path>     - Add the specified path to the list of external libraries",
        "-only=<class> - Only decompile the specified class",
        "", "Additional options",
        "These options take the last specified value.",
        "They each are specified with a three-character name followed by an equals sign, followed by the value.",
        "Booleans are traditionally indicated with `0` or `1`, but may also be specified with `true` or `false`.",
        "Because of this, an option indicated as a boolean may actually be a number."
      };

      for (String line : help) {
        System.out.println(line);
      }

      List<Field> fields = Arrays.stream(IFernflowerPreferences.class.getDeclaredFields())
        .filter(field -> field.getType() == String.class)
        .collect(Collectors.toList());

      Map<String, Object> defaults = IFernflowerPreferences.DEFAULTS;

      for (Field field : fields) {
        IFernflowerPreferences.Name name = field.getAnnotation(IFernflowerPreferences.Name.class);
        IFernflowerPreferences.Description description = field.getAnnotation(IFernflowerPreferences.Description.class);

        String paramName;
        try {
          paramName = (String) field.get(null);
        } catch (IllegalAccessException e) {
          continue;
        }

        if (paramName.length() != 3) {
          continue;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("-").append(paramName).append("=<");

        String type;
        String defaultValue = (String) defaults.get(paramName);
        if (defaultValue == null) {
          sb.append("string>");
          type = null;
        } else if (defaultValue.equals("0") || defaultValue.equals("1")) {
          sb.append("bool>  ");
          type = "bool";
        } else {
          try {
            Integer.parseInt(defaultValue);
            sb.append("int>   ");
            type = "int";
          } catch (NumberFormatException e) {
            sb.append("string>");
            type = "string";
          }
        }

        sb.append(" - ");

        if (name != null) {
          sb.append(name.value());
        } else {
          sb.append(field.getName());
        }

        if (description != null) {
          sb.append(": ").append(description.value());
        }

        if (type != null) {
          sb.append(" (default: ");
          switch (type) {
            case "bool":
              sb.append(defaultValue.equals("1"));
              break;
            case "int":
              sb.append(defaultValue);
              break;
            case "string":
              sb.append('"').append(defaultValue).append('"');
              break;
          }
          sb.append(")");
        }

        System.out.println(sb);
      }
      return;
    }

    if (args.length < 2) {
      String example = System.getProperty("os.name").toLowerCase().contains("win") ?
        "c:\\my\\source\\ c:\\my.jar d:\\decompiled\\" :
        "~/my/source/ ~/my.jar ~/decompiled/";

      System.out.println(
        "Usage: java -jar quiltflower.jar [-<option>=<value>]* [<source>]+ <destination>\n" +
        "Example: java -jar quiltflower.jar -dgs=true " + example + "\n" +
        "For all options, run with -help"
      );
      return;
    }

    Map<String, Object> mapOptions = new HashMap<>();
    List<File> sources = new ArrayList<>();
    List<File> libraries = new ArrayList<>();
    Set<String> whitelist = new HashSet<>();

    SaveType userSaveType = null;
    boolean isOption = true;
    for (int i = 0; i < args.length - 1; ++i) { // last parameter - destination
      String arg = args[i];

      switch (arg) {
        case "--file":
          if (userSaveType != null) {
            throw new RuntimeException("Multiple save types specified");
          }

          userSaveType = SaveType.FILE;
          continue;
        case "--folder":
          if (userSaveType != null) {
            throw new RuntimeException("Multiple save types specified");
          }

          userSaveType = SaveType.FOLDER;
          continue;
        case "--legacy-saving":
          if (userSaveType != null) {
            throw new RuntimeException("Multiple save types specified");
          }

          userSaveType = SaveType.LEGACY_CONSOLEDECOMPILER;
          continue;
      }

      if (isOption && arg.length() > 5 && arg.charAt(0) == '-' && arg.charAt(4) == '=') {
        String value = arg.substring(5);
        if ("true".equalsIgnoreCase(value)) {
          value = "1";
        }
        else if ("false".equalsIgnoreCase(value)) {
          value = "0";
        }

        mapOptions.put(arg.substring(1, 4), value);
      }
      else {
        isOption = false;

        if (arg.startsWith("-e=")) {
          addPath(libraries, arg.substring(3));
        }
        else if (arg.startsWith("-only=")) {
          whitelist.add(arg.substring(6));
        }
        else {
          addPath(sources, arg);
        }
      }
    }

    if (sources.isEmpty()) {
      System.out.println("error: no sources given");
      return;
    }

    String name = args[args.length - 1];

    SaveType saveType = SaveType.FOLDER;
    File destination = new File(name);

    if (userSaveType == null) {
      if (destination.getName().contains(".zip") || destination.getName().contains(".jar")) {
        saveType = SaveType.FILE;

        if (destination.getParentFile() != null) {
          destination.getParentFile().mkdirs();
        }
      } else {
        destination.mkdirs();
      }
    } else {
      saveType = userSaveType;
    }


    PrintStreamLogger logger = new PrintStreamLogger(System.out);
    ConsoleDecompiler decompiler = new ConsoleDecompiler(destination, mapOptions, logger, saveType);

    for (File library : libraries) {
      decompiler.addLibrary(library);
    }
    for (File source : sources) {
      decompiler.addSource(source);
    }
    for (String prefix : whitelist) {
      decompiler.addWhitelist(prefix);
    }

    decompiler.decompileContext();
  }

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  private static void addPath(List<? super File> list, String path) {
    File file = new File(path);
    if (file.exists()) {
      list.add(file);
    }
    else {
      System.out.println("warn: missing '" + path + "', ignored");
    }
  }

  // *******************************************************************
  // Implementation
  // *******************************************************************

  private final File root;
  private final Fernflower engine;
  private final Map<String, ZipOutputStream> mapArchiveStreams = new HashMap<>();
  private final Map<String, Set<String>> mapArchiveEntries = new HashMap<>();

  // Legacy support
  protected ConsoleDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger) {
    this(destination, options, logger, destination.isDirectory() ? SaveType.LEGACY_CONSOLEDECOMPILER : SaveType.FILE);
  }

  protected ConsoleDecompiler(File destination, Map<String, Object> options, IFernflowerLogger logger, SaveType saveType) {
    root = destination;
    engine = new Fernflower(this, saveType == SaveType.LEGACY_CONSOLEDECOMPILER ? this : saveType.getSaver().apply(destination), options, logger);
  }

  public void addSource(File source) {
    engine.addSource(source);
  }

  public void addLibrary(File library) {
    engine.addLibrary(library);
  }

  public void addWhitelist(String prefix) {
    engine.addWhitelist(prefix);
  }

  public void decompileContext() {
    try {
      engine.decompileContext();
    }
    finally {
      engine.clearContext();
    }
  }

  // *******************************************************************
  // Interface IBytecodeProvider
  // *******************************************************************

  @Override
  public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
    File file = new File(externalPath);
    if (internalPath == null) {
      return InterpreterUtil.getBytes(file);
    }
    else {
      try (ZipFile archive = new ZipFile(file)) {
        ZipEntry entry = archive.getEntry(internalPath);
        if (entry == null) throw new IOException("Entry not found: " + internalPath);
        return InterpreterUtil.getBytes(archive, entry);
      }
    }
  }

  // *******************************************************************
  // Interface IResultSaver
  // *******************************************************************

  private String getAbsolutePath(String path) {
    return new File(root, path).getAbsolutePath();
  }

  @Override
  public void saveFolder(String path) {
    File dir = new File(getAbsolutePath(path));
    if (!(dir.mkdirs() || dir.isDirectory())) {
      throw new RuntimeException("Cannot create directory " + dir);
    }
  }

  @Override
  public void copyFile(String source, String path, String entryName) {
    try {
      InterpreterUtil.copyFile(new File(source), new File(getAbsolutePath(path), entryName));
    }
    catch (IOException ex) {
      DecompilerContext.getLogger().writeMessage("Cannot copy " + source + " to " + entryName, ex);
    }
  }

  @Override
  public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
    File file = new File(getAbsolutePath(path), entryName);
    try (Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
      out.write(content);
    }
    catch (IOException ex) {
      DecompilerContext.getLogger().writeMessage("Cannot write class file " + file, ex);
    }
  }

  @Override
  public void createArchive(String path, String archiveName, Manifest manifest) {
    File file = new File(getAbsolutePath(path), archiveName);
    try {
      if (!(file.createNewFile() || file.isFile())) {
        throw new IOException("Cannot create file " + file);
      }

      FileOutputStream fileStream = new FileOutputStream(file);
      ZipOutputStream zipStream = manifest != null ? new JarOutputStream(fileStream, manifest) : new ZipOutputStream(fileStream);
      mapArchiveStreams.put(file.getPath(), zipStream);
    }
    catch (IOException ex) {
      DecompilerContext.getLogger().writeMessage("Cannot create archive " + file, ex);
    }
  }

  @Override
  public void saveDirEntry(String path, String archiveName, String entryName) {
    if (entryName.lastIndexOf('/') != entryName.length() - 1) {
      entryName += '/';
    }
    saveClassEntry(path, archiveName, null, entryName, null);
  }

  @Override
  public void copyEntry(String source, String path, String archiveName, String entryName) {
    String file = new File(getAbsolutePath(path), archiveName).getPath();

    if (!checkEntry(entryName, file)) {
      return;
    }

    try (ZipFile srcArchive = new ZipFile(new File(source))) {
      ZipEntry entry = srcArchive.getEntry(entryName);
      if (entry != null) {
        try (InputStream in = srcArchive.getInputStream(entry)) {
          ZipOutputStream out = mapArchiveStreams.get(file);
          out.putNextEntry(new ZipEntry(entryName));
          InterpreterUtil.copyStream(in, out);
        }
      }
    }
    catch (IOException ex) {
      String message = "Cannot copy entry " + entryName + " from " + source + " to " + file;
      DecompilerContext.getLogger().writeMessage(message, ex);
    }
  }

  @Override
  public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
    this.saveClassEntry(path, archiveName, qualifiedName, entryName, content, null);
  }

  @Override
  public synchronized void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content, int[] mapping) {
    String file = new File(getAbsolutePath(path), archiveName).getPath();

    if (!checkEntry(entryName, file)) {
      return;
    }

    try {
      ZipOutputStream out = mapArchiveStreams.get(file);
      ZipEntry entry = new ZipEntry(entryName);
      if (mapping != null && DecompilerContext.getOption(IFernflowerPreferences.DUMP_CODE_LINES)) {
        entry.setExtra(this.getCodeLineData(mapping));
      }
      out.putNextEntry(entry);
      if (content != null) {
        out.write(content.getBytes(StandardCharsets.UTF_8));
      }
    }
    catch (IOException ex) {
      String message = "Cannot write entry " + entryName + " to " + file;
      DecompilerContext.getLogger().writeMessage(message, ex);
    }
  }

  private boolean checkEntry(String entryName, String file) {
    Set<String> set = mapArchiveEntries.computeIfAbsent(file, k -> new HashSet<>());

    boolean added = set.add(entryName);
    if (!added) {
      String message = "Zip entry " + entryName + " already exists in " + file;
      DecompilerContext.getLogger().writeMessage(message, IFernflowerLogger.Severity.WARN);
    }
    return added;
  }

  @Override
  public void closeArchive(String path, String archiveName) {
    String file = new File(getAbsolutePath(path), archiveName).getPath();
    try {
      mapArchiveEntries.remove(file);
      mapArchiveStreams.remove(file).close();
    }
    catch (IOException ex) {
      DecompilerContext.getLogger().writeMessage("Cannot close " + file, IFernflowerLogger.Severity.WARN);
    }
  }

  public enum SaveType {
    LEGACY_CONSOLEDECOMPILER(null), // handled separately
    FOLDER(DirectoryResultSaver::new),
    FILE(SingleFileSaver::new);

    private final Function<File, IResultSaver> saver;

    SaveType(Function<File, IResultSaver> saver) {
      this.saver = saver;
    }

    public Function<File, IResultSaver> getSaver() {
      return saver;
    }
  }
}
