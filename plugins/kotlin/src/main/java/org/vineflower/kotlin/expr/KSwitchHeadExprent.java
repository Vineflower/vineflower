package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.SwitchHeadExprent;
import org.jetbrains.java.decompiler.util.TextBuffer;

public class KSwitchHeadExprent extends SwitchHeadExprent implements KExprent {
  public KSwitchHeadExprent(SwitchHeadExprent ex) {
    super(ex.getValue(), ex.bytecode);
    setCaseValues(ex.getCaseValues());
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();

    buf.appendKeyword("when")
      .appendWhitespace(" ")
      .appendPunctuation("(")
      .append(getValue().toJava(indent))
      .appendPunctuation(")");

    buf.addStartBytecodeMapping(bytecode);
    return buf;
  }
}
