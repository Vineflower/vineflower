package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;

public class LambdaTextToken extends TextToken {
  public final String className;
  public final String name;
  public final MethodDescriptor descriptor;

  public LambdaTextToken(int start, int length, String className, String name, MethodDescriptor descriptor) {
    super(start, length, false);
    this.className = className;
    this.name = name;
    this.descriptor = descriptor;
  }

  @Override
  public LambdaTextToken copy() {
    return new LambdaTextToken(start, length, className, name, descriptor);
  }

  public void shift(int startOffset, int endOffset) {
    start += startOffset;
    length += endOffset - startOffset;
  }

  @Override
  public void visit(TextTokenVisitor visitor) {
    visitor.visitLambda(new TextRange(start, length), className, name, descriptor);
  }
}
