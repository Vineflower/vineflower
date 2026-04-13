package org.jetbrains.java.decompiler.main.extern;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.util.token.TextRange;
import org.jetbrains.java.decompiler.util.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public abstract class TextTokenVisitor {
  private static final String PROPERTY_NAME = "text_token_visitor";
  public static final TextTokenVisitor EMPTY = new TextTokenVisitor() {};

  private final TextTokenVisitor next;

  public TextTokenVisitor(TextTokenVisitor next) {
    this.next = next;
  }

  private TextTokenVisitor() {
    this.next = null;
  }

  @SuppressWarnings("unchecked")
  private static List<Factory> getFactories() {
    List<Factory> property = (List<Factory>) DecompilerContext.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new ArrayList<>();
      DecompilerContext.setProperty(PROPERTY_NAME, property);
    }

    return property;
  }

  public static void addVisitor(Factory factory) {
    getFactories().add(factory);
  }

  private static Factory chainFactories() {
    return getFactories().stream().reduce(Factory::andThen).orElse(v -> v);
  }

  public static TextTokenVisitor createVisitor() {
    return chainFactories().create(EMPTY);
  }

  public static TextTokenVisitor createVisitor(Factory factory) {
    return chainFactories().andThen(factory).create(EMPTY);
  }

  @MustBeInvokedByOverriders
  public void start(String content) {
    if (next != null) {
      next.start(content);
    }
  }

  @MustBeInvokedByOverriders
  public void visitClass(TextRange range, boolean declaration, String name) {
    if (next != null) {
      next.visitClass(range, declaration, name);
    }
  }

  @MustBeInvokedByOverriders
  public void visitField(TextRange range, boolean declaration, String className, String name, FieldDescriptor descriptor) {
    if (next != null) {
      next.visitField(range, declaration, className, name, descriptor);
    }
  }

  @MustBeInvokedByOverriders
  public void visitMethod(TextRange range, boolean declaration, String className, String name, MethodDescriptor descriptor) {
    if (next != null) {
      next.visitMethod(range, declaration, className, name, descriptor);
    }
  }

  @MustBeInvokedByOverriders
  public void visitParameter(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
    if (next != null) {
      next.visitParameter(range, declaration, className, methodName, methodDescriptor, index, name);
    }
  }

  @MustBeInvokedByOverriders
  public void visitLocal(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
    if (next != null) {
      next.visitLocal(range, declaration, className, methodName, methodDescriptor, index, name);
    }
  }

  @MustBeInvokedByOverriders
  public void visitLabel(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int id) {
    if (next != null) {
      next.visitLabel(range, declaration, className, methodName, methodDescriptor, id);
    }
  }

  @MustBeInvokedByOverriders
  public void visitModule(TextRange range, boolean declaration, String moduleName) {
    if (next != null) {
      next.visitModule(range, declaration, moduleName);
    }
  }

  @MustBeInvokedByOverriders
  public void visitGeneric(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, String genericType) {
    if (next != null) {
      next.visitGeneric(range, declaration, className, methodName, methodDescriptor, genericType);
    }
  }

  @MustBeInvokedByOverriders
  public void visitSyntax(TextRange range, TokenType type) {
    if (next != null) {
      next.visitSyntax(range, type);
    }
  }

  @MustBeInvokedByOverriders
  public void end() {
    if (next != null) {
      next.end();
    }
  }

  public interface Factory {
    TextTokenVisitor create(TextTokenVisitor next);

    default Factory andThen(Factory after) {
      return next -> after.create(create(next));
    }
  }
}
