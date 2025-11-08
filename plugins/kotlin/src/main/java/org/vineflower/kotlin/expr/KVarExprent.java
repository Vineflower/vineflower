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
  public enum DeclarationType {
    DEFINITION,
    USAGE,
    FOR_LOOP_VARIABLE,
    EXCEPTION_TYPE,
  }

  private DeclarationType declarationType;

  public KVarExprent(int index, VarType varType, VarProcessor processor, BytecodeRange bytecode) {
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
    if (ex instanceof KVarExprent kVarExprent) {
      declarationType = kVarExprent.declarationType;
    } else {
      declarationType = ex.isDefinition() ? DeclarationType.DEFINITION : DeclarationType.USAGE;
    }
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

    if (declarationType == DeclarationType.DEFINITION) {
      VarProcessor processor = getProcessor();

      boolean isFinal = isEffectivelyFinal() ||
        (processor != null && processor.getVarFinal(getVarVersionPair()) != VarTypeProcessor.FinalType.NON_FINAL);

      buffer.append(isFinal ? "val " : "var ");
    }

    buffer.append(getName());

    if (declarationType == DeclarationType.DEFINITION || declarationType == DeclarationType.EXCEPTION_TYPE) {
      buffer.append(": ");
      buffer.append(KTypes.getKotlinType(getDefinitionVarType()));
    }

    return buffer;
  }

  public void setDeclarationType(DeclarationType declarationType) {
    this.declarationType = declarationType;
  }

  @Override
  public Exprent copy() {
    return new KVarExprent((VarExprent) super.copy());
  }
}
