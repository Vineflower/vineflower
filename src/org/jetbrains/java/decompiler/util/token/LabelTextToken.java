package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;

public class LabelTextToken extends TextToken {
  public final String className;
  public final String name;
  public final MethodDescriptor descriptor;
  public final int id;

  public LabelTextToken(int start, int length, boolean declaration, String className, String name, MethodDescriptor descriptor, int id) {
    super(start, length, declaration, TokenType.LABEL);
    this.className = className;
    this.name = name;
    this.descriptor = descriptor;
    this.id = id;
  }

  @Override
  public TextToken copy() {
    return new LabelTextToken(start, length, declaration, className, name, descriptor, id);
  }

  @Override
  public void visit(TextTokenVisitor visitor) {
    visitor.visitLabel(new TextRange(start, length), declaration, className, name, descriptor, id);
  }
}
