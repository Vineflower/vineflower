package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.modules.decompiler.DecHelper;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class PatternExprent extends Exprent implements Pattern {
  private final VarType varType;
  private final List<Exprent> exprents;

  public PatternExprent(VarType type, List<Exprent> exprents) {
    super(Type.PATTERN);
    varType = type;
    this.exprents = exprents;

    for (Exprent exprent : exprents) {
      if (!(exprent instanceof Pattern)) {
        ValidationHelper.assertTrue(false, "Illegal input for PatternExprent");
      }
    }
  }

  @Override
  protected List<Exprent> getAllExprents(List<Exprent> list) {
    list.addAll(exprents);
    return list;
  }

  @Override
  public VarType getExprType() {
    return varType;
  }

  @Override
  public Exprent copy() {
    return new PatternExprent(varType, DecHelper.copyExprentList(exprents));
  }

  @Override
  public TextBuffer toJava(int indent) {
    TextBuffer buf = new TextBuffer();
    buf.appendCastTypeName(varType);
    buf.append("(");

    for (int i = 0; i < exprents.size(); i++) {
      buf.append(exprents.get(i).toJava());

      if (i < exprents.size() - 1) {
        buf.append(", ");
      }
    }

    buf.append(")");

    return buf;
  }

  @Override
  public void getBytecodeRange(BitSet values) {
    measureBytecode(values, exprents);
    measureBytecode(values);
  }

  @Override
  @NotNull
  public List<VarExprent> getPatternVars() {
    List<VarExprent> vars = new ArrayList<>();

    for (Exprent e : exprents) {
      if (e instanceof Pattern p) {
        vars.addAll(p.getPatternVars());
      } else {
        ValidationHelper.assertTrue(false, "Illegal input in PatternExprent");
      }
    }

    return vars;
  }
}
