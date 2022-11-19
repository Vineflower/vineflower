package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;

public class FieldTextToken extends TextToken {
  public final String className;
  public final String name;
  public final FieldDescriptor descriptor;

  public FieldTextToken(int start, int length, boolean declaration, String className, String name, FieldDescriptor descriptor) {
    super(start, length, declaration);
    this.className = className;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  public FieldTextToken copy() {
    return new FieldTextToken(start, length, declaration, className, name, descriptor);
  }

  @Override
  public void visit(TextTokenVisitor visitor) {
    visitor.visitField(new TextRange(start, length), declaration, className, name, descriptor);
  }
}
