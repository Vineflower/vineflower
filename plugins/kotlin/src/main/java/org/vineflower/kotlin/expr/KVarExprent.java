package org.vineflower.kotlin.expr;

import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarTypeProcessor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinWriter;
import org.vineflower.kotlin.util.KTypes;

import java.util.BitSet;

public class KVarExprent extends VarExprent implements KExprent {
  private boolean isExceptionType;

  public KVarExprent(int index, VarType varType, VarProcessor processor, BitSet bytecode) {
    super(index, varType, processor, bytecode);
  }

  public KVarExprent(VarExprent ex) {
    this(ex.getIndex(), ex.getVarType(), ex.getProcessor(), ex.bytecode);
    this.setStack(ex.isStack());
    this.setClassDef(ex.isClassDef());
    this.setVersion(ex.getVersion());
    // FIXME: breaks tests by replacing name with "<set-?>"
//    this.setLVT(ex.getLVT());
    this.setEffectivelyFinal(ex.isEffectivelyFinal());
    this.setDefinition(ex.isDefinition());
  }

  @Override
  public String getName() {
    String name = super.getName();

    if (name.startsWith("this@") || name.equals("this")) {
      return name;
    }

    return KotlinWriter.toValidKotlinIdentifier(name);
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buffer = new TextBuffer();

    buffer.addBytecodeMapping(bytecode);

    boolean definition = isDefinition();
    if (definition && !isExceptionType) {
      VarProcessor processor = getProcessor();

      boolean isFinal = isEffectivelyFinal() ||
        (processor != null && processor.getVarFinal(getVarVersionPair()) != VarTypeProcessor.FinalType.NON_FINAL);

      buffer.append(isFinal ? "val " : "var ");
    }

    buffer.append(getName());

    if (definition || isExceptionType) {
      buffer.append(": ");
      buffer.append(KTypes.getKotlinType(getDefinitionVarType()));
    }

    return buffer;
  }

  public void setExceptionType(boolean isExceptionType) {
    this.isExceptionType = isExceptionType;
  }

  @Override
  public Exprent copy() {
    return new KVarExprent((VarExprent) super.copy());
  }
}
