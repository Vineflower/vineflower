package org.jetbrains.java.decompiler.main.extern;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.util.token.TextRange;

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

  public void start(String content) {
    if (next != null) {
      next.start(content);
    }
  }

  public void visitClass(TextRange range, boolean declaration, String name) {
    if (next != null) {
      next.visitClass(range, declaration, name);
    }
  }

  public void visitField(TextRange range, boolean declaration, String className, String name, FieldDescriptor descriptor) {
    if (next != null) {
      next.visitField(range, declaration, className, name, descriptor);
    }
  }

  public void visitMethod(TextRange range, boolean declaration, String className, String name, MethodDescriptor descriptor) {
    if (next != null) {
      next.visitMethod(range, declaration, className, name, descriptor);
    }
  }

  public void visitParameter(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
    if (next != null) {
      next.visitParameter(range, declaration, className, methodName, methodDescriptor, index, name);
    }
  }

  public void visitLocal(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
    if (next != null) {
      next.visitLocal(range, declaration, className, methodName, methodDescriptor, index, name);
    }
  }

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
