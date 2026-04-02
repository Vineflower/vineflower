package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;

public abstract class TextToken {
  protected int start;
  protected int length;
  protected boolean declaration;
  protected TokenType type;

  public TextToken(int start, int length, boolean declaration, TokenType type) {
    this.start = start;
    this.length = length;
    this.declaration = declaration;
    this.type = type;
  }

  public void shift(int amount) {
    this.start += amount;
  }

  public abstract TextToken copy();

  public abstract void visit(TextTokenVisitor visitor);

  public int getStart() {
    return start;
  }

  public int getLength() {
    return length;
  }

  public boolean isDeclaration() {
    return declaration;
  }

  public TokenType getType() {
    return type;
  }
}
