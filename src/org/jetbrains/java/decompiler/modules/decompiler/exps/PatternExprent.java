package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.modules.decompiler.DecHelper;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.vars.CheckTypesResult;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructRecordComponent;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class PatternExprent extends Exprent implements Pattern {
  private final PatternData data;
  private final VarType varType;
  private final List<Exprent> exprents;
  private final List<@Nullable VarType> varTypes;

  public PatternExprent(PatternData data, VarType type, List<Exprent> exprents) {
    super(Type.PATTERN);
    this.data = data;
    varType = type;
    this.exprents = exprents;
    this.varTypes = new ArrayList<>();

    for (Exprent exprent : exprents) {
      if (!(exprent instanceof Pattern)) {
        ValidationHelper.assertTrue(false, "Illegal input for PatternExprent");
      }
      varTypes.add(null);
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
    return new PatternExprent(data, varType, DecHelper.copyExprentList(exprents));
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
  public CheckTypesResult checkExprTypeBounds() {
    if (this.data instanceof PatternData.RecordPatternData record) {
      CheckTypesResult res = new CheckTypesResult();

      ValidationHelper.assertTrue(record.cl.getRecordComponents() != null, "Must not be null!");
      ValidationHelper.assertTrue(record.cl.getRecordComponents().size() == exprents.size(), "Record component size and expr list size must be equal!");

      // The type lower bound must be
      for (int i = 0; i < exprents.size(); i++) {
        VarType type = varTypes.get(i);
        if (type != null) {
          res.addMinTypeExprent(exprents.get(i), type);
        } else {
          res.addMinTypeExprent(exprents.get(i), new VarType(record.cl.getRecordComponents().get(i).getDescriptor()));
        }
      }

      return res;
    }

    return super.checkExprTypeBounds();
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

  public PatternData getData() {
    return data;
  }

  public List<Exprent> getExprents() {
    return exprents;
  }

  public List<@Nullable VarType> getVarTypes() {
    return varTypes;
  }

  public static PatternData recordData(StructClass cl) {
    return new PatternData.RecordPatternData(cl);
  }

  public sealed interface PatternData {
    record RecordPatternData(StructClass cl) implements PatternData {

    }
  }
}
