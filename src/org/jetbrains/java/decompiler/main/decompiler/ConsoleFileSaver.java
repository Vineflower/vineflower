package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.util.token.TextRange;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Manifest;

// "Saves" a file to the standard out console
public class ConsoleFileSaver implements IResultSaver {
  private static class ConsoleTokenVisitor extends TextTokenVisitor {
    record Token(TextRange range, Type type) implements Comparable<Token> {
      @Override
      public int compareTo(@NotNull ConsoleFileSaver.ConsoleTokenVisitor.Token o) {
        return Integer.compare(range.start, o.range.start);
      }

      enum Type {
        CLASS(2),
        FIELD(3),
        METHOD(4),
        PARAMETER(5),
        LOCAL(6),

        ;

        private final int color;

        Type(int color) {
          this.color = color;
        }

        String getEscapeCode() {
          return "\u001B[3" + color + "m";
        }
      }
    }

    private final Set<Token> tokens = new TreeSet<>();
    private static final String ESCAPE_RESET = "\u001B[0m";

    ConsoleTokenVisitor(TextTokenVisitor next) {
      super(next);
    }

    @Override
    public void visitClass(TextRange range, boolean declaration, String name) {
      tokens.add(new Token(range, Token.Type.CLASS));
      super.visitClass(range, declaration, name);
    }

    @Override
    public void visitField(TextRange range, boolean declaration, String className, String name, FieldDescriptor descriptor) {
      tokens.add(new Token(range, Token.Type.FIELD));
      super.visitField(range, declaration, className, name, descriptor);
    }

    @Override
    public void visitMethod(TextRange range, boolean declaration, String className, String name, MethodDescriptor descriptor) {
      tokens.add(new Token(range, Token.Type.METHOD));
      super.visitMethod(range, declaration, className, name, descriptor);
    }

    @Override
    public void visitParameter(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
      tokens.add(new Token(range, Token.Type.PARAMETER));
      super.visitParameter(range, declaration, className, methodName, methodDescriptor, index, name);
    }

    @Override
    public void visitLocal(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
      tokens.add(new Token(range, Token.Type.LOCAL));
      super.visitLocal(range, declaration, className, methodName, methodDescriptor, index, name);
    }

    void printClass(String content) {
      var index = 0;
      for (Token token : tokens) {
        System.out.print(content.substring(index, token.range().start));
        System.out.print(token.type().getEscapeCode());
        System.out.print(content.substring(token.range().start, token.range().getEnd()));
        System.out.print(ESCAPE_RESET);
        index = token.range().getEnd();
      }
      if (index < content.length()) {
        System.out.print(content.substring(index));
      }
      System.out.println();
    }
  }

  private ConsoleTokenVisitor visitor;

  public ConsoleFileSaver(File ignored) {
    ConsoleDecompiler.tokenizerInitializer = () -> {
      boolean color = switch (DecompilerContext.getProperty(IFernflowerPreferences.COLORIZE_OUTPUT).toString()) {
        case "always" -> true;
        case "1", "auto" -> System.console() != null;
        default -> false;
      };

      if (color) {
        TextTokenVisitor.addVisitor(next -> {
          visitor = new ConsoleTokenVisitor(next);
          return visitor;
        });
      }
    };
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
    if (visitor != null) {
      visitor.printClass(content);
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
    if (visitor != null) {
      visitor.printClass(content);
    } else {
      System.out.println(content);
    }
  }

  @Override
  public void closeArchive(String path, String archiveName) {

  }
}
