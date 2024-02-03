package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.HashMap;
import java.util.Map;

public class DefaultArgsMap {
  private final Map<KParameter, Exprent> map;

  private DefaultArgsMap(Map<KParameter, Exprent> map) {
    this.map = map;
  }

  public static DefaultArgsMap from(MethodWrapper defaults, MethodWrapper calling, KParameter[] params, int startIndex) {
    if (defaults == null) {
      return new DefaultArgsMap(Map.of());
    }

    Map<KParameter, Exprent> map = new HashMap<>();

    SequenceStatement sequence = ((SequenceStatement) defaults.root.getFirst());
    for (Statement statement : sequence.getStats()) {
      if (statement instanceof IfStatement) {
        IfStatement ifStatement = (IfStatement) statement;
        Exprent condition = ifStatement.getHeadexprent().getCondition();
        if (!(condition instanceof FunctionExprent)) throw new IllegalStateException("Unexpected exprent type parsing default argument");
        FunctionExprent function = (FunctionExprent) condition;

        if (!(function.getLstOperands().get(0) instanceof FunctionExprent)) throw new IllegalStateException("Unexpected exprent type parsing default argument");
        FunctionExprent bitmask = (FunctionExprent) function.getLstOperands().get(0);

        if (bitmask.getLstOperands().size() != 2) throw new IllegalStateException("Unexpected number of operands parsing default argument");
        Exprent var = bitmask.getLstOperands().get(0);
        Exprent mask = bitmask.getLstOperands().get(1);

        if (!(var instanceof VarExprent) || !(mask instanceof ConstExprent)) throw new IllegalStateException("Unexpected exprent type parsing default argument");

        int maskValue = ((ConstExprent) mask).getIntValue();
        int maskIndex = 0;
        for (int i = 0; i < 32; i++) {
          if ((maskValue & (1 << i)) != 0) {
            maskIndex = i;
            break;
          }
        }

        Exprent expr = ifStatement.getIfstat().getExprents().get(0);
        if (expr instanceof AssignmentExprent) {
          AssignmentExprent assignment = (AssignmentExprent) expr;
          Exprent right = assignment.getRight().copy();
          updateExprent(right, calling);
          KParameter param = params[maskIndex];
          map.put(param, right);
        }
      }
    }

    for (KParameter param : params) {
      assert map.containsKey(param) == param.flags.declaresDefault : "Parameter " + param.name + " has default value but no default value was found";
    }

    return new DefaultArgsMap(map);
  }

  public TextBuffer toJava(KParameter parameter, int indent) {
    TextBuffer buffer = new TextBuffer();

    Exprent expr = map.get(parameter);
    if (expr == null) {
      return buffer.append("...");
    }

    buffer.append(expr.toJava(indent));

    return buffer;
  }

  private static void updateExprent(Exprent expr, MethodWrapper calling) {
    if (expr instanceof VarExprent) {
      VarExprent var = (VarExprent) expr;
      StructLocalVariableTableAttribute attr = calling.methodStruct.getLocalVariableAttr();
      if (attr != null) {
        calling.varproc.findLVT(var, 0);
      }
    }

    for (Exprent child : expr.getAllExprents()) {
      updateExprent(child, calling);
    }
  }
}
