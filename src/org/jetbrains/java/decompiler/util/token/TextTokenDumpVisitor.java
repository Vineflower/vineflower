package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.util.TextBuffer;

public class TextTokenDumpVisitor extends TextTokenVisitor {
  private final TextBuffer buffer;
  private TextBuffer text;

  public TextTokenDumpVisitor(TextTokenVisitor next, TextBuffer buffer) {
    super(next);
    this.buffer = buffer;
  }

  private TextBuffer range(TextRange range) {
    text.append("(").append(buffer.getPos(range.start));
    text.append(", ").append(buffer.getPos(range.getEnd()));
    return text.append(")");
  }

  private TextBuffer declaration(boolean declaration) {
    return text.append(declaration ? "[declaration]" : "[reference]");
  }

  @Override
  public void start(String content) {
    super.start(content);
    text = new TextBuffer();
    text.appendLineSeparator()
      .append("/*").appendLineSeparator()
      .append("Tokens:").appendLineSeparator();
  }

  @Override
  public void visitClass(TextRange range, boolean declaration, String name) {
    super.visitClass(range, declaration, name);
    range(range).append(" class ");
    declaration(declaration).append(" ");
    text.append(name);
    text.appendLineSeparator();
  }

  @Override
  public void visitField(TextRange range, boolean declaration, String className, String name, FieldDescriptor descriptor) {
    super.visitField(range, declaration, className, name, descriptor);
    range(range).append(" field ");
    declaration(declaration).append(" ");
    text.append(className);
    text.append("#").append(name);
    text.append(":").append(descriptor.descriptorString);
    text.appendLineSeparator();
  }

  @Override
  public void visitMethod(TextRange range, boolean declaration, String className, String name, MethodDescriptor descriptor) {
    super.visitMethod(range, declaration, className, name, descriptor);
    range(range).append(" method ");
    declaration(declaration).append(" ");
    text.append(className);
    text.append("#").append(name);
    text.append(descriptor.toString());
    text.appendLineSeparator();
  }

  private void visitVariable(boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
    declaration(declaration).append(" ");
    text.append(className);
    text.append("#").append(methodName);
    text.append(methodDescriptor.toString());
    text.append("(").append(index);
    text.append(":").append(name).append(")");
    text.appendLineSeparator();
  }

  @Override
  public void visitParameter(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
    super.visitParameter(range, declaration, className, methodName, methodDescriptor, index, name);
    range(range).append(" parameter ");
    visitVariable(declaration, className, methodName, methodDescriptor, index, name);
  }

  @Override
  public void visitLocal(TextRange range, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name) {
    super.visitLocal(range, declaration, className, methodName, methodDescriptor, index, name);
    range(range).append(" local ");
    visitVariable(declaration, className, methodName, methodDescriptor, index, name);
  }

  @Override
  public void end() {
    super.end();
    text.append("*/").appendLineSeparator();
    buffer.append(text.convertToStringAndAllowDataDiscard());
  }
}
