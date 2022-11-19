package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;

public class VariableTextToken extends TextToken {
  public final boolean parameter;
  public final String className;
  public final String methodName;
  public final MethodDescriptor methodDescriptor;
  public final int index;
  public final String name;
  public final VarType type;

  public VariableTextToken(int start, int length, boolean declaration, boolean parameter, String className, String methodName, MethodDescriptor methodDescriptor, int index, String name, VarType type) {
    super(start, length, declaration);
    this.parameter = parameter;
    this.className = className;
    this.methodName = methodName;
    this.methodDescriptor = methodDescriptor;
    this.index = index;
    this.name = name;
    this.type = type;
  }

  @Override
  public VariableTextToken copy() {
    return new VariableTextToken(start, length, declaration, parameter, className, methodName, methodDescriptor, index, name, type);
  }

  @Override
  public void visit(TextTokenVisitor visitor) {
    if (parameter) {
      visitor.visitParameter(new TextRange(start, length), declaration, className, methodName, methodDescriptor, index, name, type);
    } else {
      visitor.visitLocal(new TextRange(start, length), declaration, className, methodName, methodDescriptor, index, name, type);
    }
  }
}
