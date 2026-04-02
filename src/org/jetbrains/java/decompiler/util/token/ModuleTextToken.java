package org.jetbrains.java.decompiler.util.token;

import org.jetbrains.java.decompiler.main.extern.TextTokenVisitor;

public class ModuleTextToken extends TextToken {
  public final String moduleName;

  public ModuleTextToken(int start, int length, boolean declaration, String moduleName) {
    super(start, length, declaration, TokenType.MODULE);
    this.moduleName = moduleName;
  }

  @Override
  public TextToken copy() {
    return new ModuleTextToken(start, length, declaration, moduleName);
  }

  @Override
  public void visit(TextTokenVisitor visitor) {
    visitor.visitModule(new TextRange(start, length), declaration, moduleName);
  }
}
