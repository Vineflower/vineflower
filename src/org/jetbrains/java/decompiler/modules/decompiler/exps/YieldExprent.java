package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.main.collectors.BytecodeMappingTracer;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.BitSet;
import java.util.List;

public class YieldExprent extends Exprent {
  private final Exprent content;
  private final VarType retType;

  public YieldExprent(Exprent content, VarType retType) {
    super(EXPRENT_YIELD);
    this.content = content;
    this.retType = retType;
  }

  @Override
  protected List<Exprent> getAllExprents(List<Exprent> list) {
    list.add(this.content);
    return list;
  }

  @Override
  public Exprent copy() {
    return new YieldExprent(this.content.copy(), this.retType);
  }

  @Override
  public TextBuffer toJava(int indent, BytecodeMappingTracer tracer) {
    TextBuffer buf = new TextBuffer();
    buf.append("yield ");
    ExprProcessor.getCastedExprent(this.content, this.retType, buf, indent, false, false, false, false, tracer);

    return buf;
  }

  public Exprent getContent() {
    return content;
  }

  @Override
  public VarType getExprType() {
    return this.retType;
  }

  @Override
  public void getBytecodeRange(BitSet values) {

  }
}
