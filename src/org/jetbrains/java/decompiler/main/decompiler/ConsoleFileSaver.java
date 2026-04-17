package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.util.token.TextRange;
import org.jetbrains.java.decompiler.util.token.TokenType;

import java.io.Console;
import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

// "Saves" a file to the standard out console
public class ConsoleFileSaver implements IResultSaver {
  private static class ConsoleTokenVisitor extends TextTokenVisitor {
    record Token(TextRange range, TokenType type) implements Comparable<Token> {
      @Override
      public int compareTo(@NotNull ConsoleFileSaver.ConsoleTokenVisitor.Token o) {
        return Integer.compare(range.start, o.range.start);
      }
    }

    private final SortedSet<Token> tokens = new TreeSet<>();
    String content;

    ConsoleTokenVisitor(TextTokenVisitor next) {
      super(next);
    }

    @Override
    public void start(String content) {
      this.content = content;
      super.start(content);
    }

    @Override
    public void visitClass(TextRange range, boolean declaration, String name) {
      tokens.add(new Token(range, TokenType.CLASS));
      super.visitClass(range, declaration, name);
    }

    @Override
    public void visitField(TextRange range, boolean declaration, String className, String name, FieldDescriptor descriptor) {
      tokens.add(new Token(range, TokenType.FIELD));
      super.visitField(range, declaration, className, name, descriptor);
    }

    @Override
    public void visitMethod(TextRange range, boolean declaration, String className, String name, MethodDescriptor descriptor) {
      tokens.add(new Token(range, TokenType.METHOD));
      super.visitMethod(range, declaration, className, name, descriptor);
    }

    @Override
    public void visitParameter(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
      tokens.add(new Token(range, TokenType.PARAMETER));
      super.visitParameter(range, declaration, className, methodName, methodDescriptor, index, name);
    }

    @Override
    public void visitLocal(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
      tokens.add(new Token(range, TokenType.LOCAL_VARIABLE));
      super.visitLocal(range, declaration, className, methodName, methodDescriptor, index, name);
    }

    @Override
    public void visitLabel(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int id) {
      tokens.add(new Token(range, TokenType.LABEL));
      super.visitLabel(range, declaration, className, methodName, methodDescriptor, id);
    }

    @Override
    public void visitModule(TextRange range, boolean declaration, String moduleName) {
      tokens.add(new Token(range, TokenType.MODULE));
      super.visitModule(range, declaration, moduleName);
    }

    @Override
    public void visitGeneric(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, String genericType) {
      tokens.add(new Token(range, TokenType.GENERIC));
      super.visitGeneric(range, declaration, className, methodName, methodDescriptor, genericType);
    }
    
    @Override
    public void visitSyntax(TextRange range, TokenType type) {
      tokens.add(new Token(range, type));
      super.visitSyntax(range, type);
    }
  }

  private static final String ESCAPE_RESET = "\u001B[0m";
  private final Set<ConsoleTokenVisitor> visitors = new HashSet<>();
  private Map<TokenType, String> colorMap;
  private Map<String, SortedSet<ConsoleTokenVisitor.Token>> mappedVisitors;

  public ConsoleFileSaver(File ignored) {
    ConsoleDecompiler.tokenizerInitializer = () -> {
      boolean color = switch (DecompilerContext.getProperty(IFernflowerPreferences.COLORIZE_OUTPUT).toString()) {
        case "always" -> true;
        case "1", "auto" -> isatty();
        default -> false;
      };

      if (color) {
        TextTokenVisitor.addVisitor(next -> {
          ConsoleTokenVisitor visitor = new ConsoleTokenVisitor(next);
          visitors.add(visitor);
          return visitor;
        });
      }
    };
  }

  private boolean isatty() {
    if (System.console() == null) {
      return false;
    }

    // If on new JDK, check `isTerminal`; on old JDK, we're already in a terminal
    try {
      MethodHandle isTerminal = MethodHandles.lookup().findVirtual(Console.class, "isTerminal", MethodType.methodType(boolean.class));
      return (boolean) isTerminal.invokeExact(System.console());
    } catch (Throwable e) {
      return true;
    }
  }

  private void setupColors() {
    if (colorMap != null) {
      return;
    }

    colorMap = new HashMap<>();

    String colors = DecompilerContext.getProperty(IFernflowerPreferences.COLOR_MAP).toString();
    if (colors.isBlank()) {
      colorMap.put(TokenType.CLASS, "\u001B[33m");
      colorMap.put(TokenType.FIELD, "\u001B[3m");
      colorMap.put(TokenType.METHOD, "\u001B[34m");
      colorMap.put(TokenType.PARAMETER, "\u001B[38;5;208m");
      colorMap.put(TokenType.LOCAL_VARIABLE, "\u001B[97m");
      colorMap.put(TokenType.LABEL, "\u001B[37m");
      colorMap.put(TokenType.MODULE, "\u001B[33m");
      colorMap.put(TokenType.GENERIC, "\u001B[38;5;208m");
      colorMap.put(TokenType.KEYWORD, "\u001B[35m");
      colorMap.put(TokenType.OPERATOR, "\u001B[36m");
      colorMap.put(TokenType.PUNCTUATION, "\u001B[36m");
      colorMap.put(TokenType.TEXT, "\u001B[32m");
      colorMap.put(TokenType.NUMBER, "\u001B[38;5;160m");
      colorMap.put(TokenType.COMMENT, "\u001B[90m");
      return;
    }

    for (String color : colors.split(",")) {
      try {
        TokenType type = TokenType.valueOf(color.substring(0, color.indexOf('=')).toUpperCase(Locale.ROOT));
        String value = color.substring(color.indexOf('=') + 1);
        if (value.startsWith("#")) {
          int rgb = Integer.parseInt(value.substring(1), 16);
          value = "\u001B[38;2;" + (rgb >> 16) + ";" + ((rgb >> 8) & 0xFF) + ";" + (rgb & 0xFF) + "m";
        } else if (value.matches("\\d{1,3};\\d{1,3};\\d{1,3}")) {
          value = "\u001B[38;2;" + value + "m";
        } else if (value.matches("^\\d+$")) {
          value = "\u001B[38;5;" + value + "m";
        } else {
          value = switch (value) {
            case "bold" -> "\u001B[1m";
            case "italic" -> "\u001B[3m";
            case "underline" -> "\u001B[4m";
            case "blink" -> "\u001B[5m";
            case "reverse" -> "\u001B[7m";
            case "concealed" -> "\u001B[8m";

            case "black" -> "\u001B[30m";
            case "red" -> "\u001B[31m";
            case "green" -> "\u001B[32m";
            case "yellow" -> "\u001B[33m";
            case "blue" -> "\u001B[34m";
            case "magenta" -> "\u001B[35m";
            case "cyan" -> "\u001B[36m";
            case "white" -> "\u001B[37m";

            default -> value;
          };
        }
        colorMap.put(type, value);
      } catch (IllegalArgumentException ignored) {
      }
    }
  }

  private void setupVisitors() {
    if (mappedVisitors == null) {
      mappedVisitors = visitors.stream()
        .collect(Collectors.toMap(visitor -> visitor.content, visitor -> visitor.tokens));
    }
  }

  private void printClass(String content) {
    setupColors();
    setupVisitors();

    var index = 0;
    for (ConsoleTokenVisitor.Token token : mappedVisitors.get(content)) {
      System.out.print(content.substring(index, token.range().start));
      if (colorMap.containsKey(token.type())) {
        System.out.print(colorMap.get(token.type()));
      }
      System.out.print(content.substring(token.range().start, token.range().getEnd()));
      System.out.print(ESCAPE_RESET);
      index = token.range().getEnd();
    }
    if (index < content.length()) {
      System.out.print(content.substring(index));
    }
    System.out.println();
  }

  @Override
  public void saveFolder(String path) {

  }

  @Override
  public void copyFile(String source, String path, String entryName) {

  }

  @Override
  public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
    System.out.println("==== " + entryName + " ====");
    if (!visitors.isEmpty()) {
      printClass(content);
    } else {
      System.out.println(content);
    }
  }

  @Override
  public void createArchive(String path, String archiveName, Manifest manifest) {

  }

  @Override
  public void saveDirEntry(String path, String archiveName, String entryName) {

  }

  @Override
  public void copyEntry(String source, String path, String archiveName, String entry) {

  }

  @Override
  public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
    System.out.println("==== " + entryName + " ====");
    if (!visitors.isEmpty()) {
      printClass(content);
    } else {
      System.out.println(content);
    }
  }

  @Override
  public void closeArchive(String path, String archiveName) {

  }
}
