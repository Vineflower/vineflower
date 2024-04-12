package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectNode;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinOptions;

import java.util.HashMap;
import java.util.Map;

public class DefaultArgsMap {
  private final Map<KParameter, Exprent> map;

  private DefaultArgsMap(Map<KParameter, Exprent> map) {
    this.map = map;
  }

  public static DefaultArgsMap from(MethodWrapper defaults, MethodWrapper calling, KParameter[] params) {
    if (defaults == null) {
      return new DefaultArgsMap(Map.of());
    }

    Map<KParameter, Exprent> map = new HashMap<>();

    DirectGraph graph = defaults.getOrBuildGraph();
    for (DirectNode node : graph.nodes) {
      Statement statement = node.statement;
      if (statement instanceof IfStatement ifStatement) {
        Exprent condition = ifStatement.getHeadexprent().getCondition();
        if (!(condition instanceof FunctionExprent function)) continue;
        if (!(function.getLstOperands().get(0) instanceof FunctionExprent bitmask)) continue;
        if (bitmask.getLstOperands().size() != 2) continue;

        Exprent var = bitmask.getLstOperands().get(0);
        Exprent mask = bitmask.getLstOperands().get(1);

        if (!(var instanceof VarExprent) || !(mask instanceof ConstExprent)) continue;

        int maskValue = ((ConstExprent) mask).getIntValue();
        int maskIndex = 0;
        for (int i = 0; i < 32; i++) {
          if ((maskValue & (1 << i)) != 0) {
            maskIndex = i;
            break;
          }
        }

        Exprent expr = ifStatement.getIfstat().getExprents().get(0);
        if (expr instanceof AssignmentExprent assignment) {
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
    String argString = DecompilerContext.getProperty(KotlinOptions.UNKNOWN_DEFAULT_ARG_STRING).toString();

    Exprent expr = map.get(parameter);
    if (expr == null) {
      if (!argString.isEmpty()) {
        buffer.append(" = ").append(argString);
      }

      return buffer;
    }

    buffer.append(" = ").append(expr.toJava(indent));

    return buffer;
  }

  private static void updateExprent(Exprent expr, MethodWrapper calling) {
    if (expr instanceof VarExprent varExpr) {
      StructLocalVariableTableAttribute attr = calling.methodStruct.getLocalVariableAttr();
      if (attr != null) {
        calling.varproc.findLVT(varExpr, 0);
      }
    }

    for (Exprent child : expr.getAllExprents()) {
      updateExprent(child, calling);
    }
  }
}
