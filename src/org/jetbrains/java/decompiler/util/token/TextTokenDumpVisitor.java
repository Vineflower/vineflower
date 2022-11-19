package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;

public class TextTokenDumpVisitor extends TextTokenVisitor {
  private final TextBuffer buffer;
  private StringBuilder textBuilder;

  public TextTokenDumpVisitor(TextTokenVisitor next, TextBuffer buffer) {
    super(next);
    this.buffer = buffer;
  }

  private StringBuilder range(TextRange range) {
    textBuilder.append("(").append(buffer.getPos(range.start));
    textBuilder.append(", ").append(buffer.getPos(range.getEnd()));
    return textBuilder.append(")");
  }

  private StringBuilder declaration(boolean declaration) {
    return textBuilder.append(declaration ? "[declaration]" : "[reference]");
  }

  @Override
  public void start() {
    textBuilder = new StringBuilder("\n/*\nTokens:");
  }

  @Override
  public void visitClass(TextRange range, boolean declaration, String name) {
    super.visitClass(range, declaration, name);
    range(range).append(" class ");
    declaration(declaration).append(" ");
    textBuilder.append(name);
  }

  @Override
  public void visitField(TextRange range, boolean declaration, String className, String name, FieldDescriptor descriptor) {
    super.visitField(range, declaration, className, name, descriptor);
    range(range).append(" field ");
    declaration(declaration).append(" ");
    textBuilder.append(className);
    textBuilder.append("#").append(name);
    textBuilder.append(":").append(descriptor.descriptorString);
  }

  @Override
  public void visitMethod(TextRange range, boolean declaration, String className, String name, MethodDescriptor descriptor) {
    super.visitMethod(range, declaration, className, name, descriptor);
    range(range).append(" method ");
    declaration(declaration).append(" ");
    textBuilder.append(className);
    textBuilder.append("#").append(name);
    textBuilder.append(descriptor.toString());
  }

  private void visitVariable(boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name, VarType type) {
    declaration(declaration).append(" ");
    textBuilder.append(className);
    textBuilder.append("#").append(methodName);
    textBuilder.append(methodDescriptor.toString());
    textBuilder.append(":").append(index);
    textBuilder.append(" ").append(name);
    textBuilder.append(" ").append(type.toString());
  }

  @Override
  public void visitParameter(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name, VarType type) {
    super.visitParameter(range, declaration, className, methodName, methodDescriptor, index, name, type);
    range(range).append(" parameter ");
    visitVariable(declaration, className, methodName, methodDescriptor, index, name, type);
  }

  @Override
  public void visitLocal(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name, VarType type) {
    super.visitLocal(range, declaration, className, methodName, methodDescriptor, index, name, type);
    range(range).append(" local ");
    visitVariable(declaration, className, methodName, methodDescriptor, index, name, type);
  }

  @Override
  public void end() {
    textBuilder.append("*/");
    buffer.append(textBuilder.toString());
  }
}
