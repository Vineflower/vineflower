package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;

public class GenericTextToken extends TextToken {
  public final String className;
  public final String methodName;
  public final MethodDescriptor methodDescriptor;
  public final String genericType;

  public GenericTextToken(int start, int length, boolean declaration, String className, String methodName, MethodDescriptor methodDescriptor, String genericType) {
    super(start, length, declaration, TokenType.GENERIC);
    this.className = className;
    this.methodName = methodName;
    this.methodDescriptor = methodDescriptor;
    this.genericType = genericType;
  }
  
  @Override
  public GenericTextToken copy() {
    return new GenericTextToken(start, length, declaration, className, methodName, methodDescriptor, genericType);
  }

  @Override
  public void visit(TextTokenVisitor visitor) {
    visitor.visitGeneric(new TextRange(start, length), declaration, className, methodName, methodDescriptor, genericType);
  }
}
