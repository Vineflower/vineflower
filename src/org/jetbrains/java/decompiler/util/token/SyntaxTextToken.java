package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;

public class SyntaxTextToken extends TextToken {
  public SyntaxTextToken(int start, int length, TokenType type) {
    super(start, length, false, type);
  }

  @Override
  public TextToken copy() {
    return new SyntaxTextToken(start, length, type);
  }

  @Override
  public void visit(TextTokenVisitor visitor) {

  }
}
