package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectGraph;
import org.jetbrains.java.decompiler.modules.decompiler.flow.DirectNode;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructAnnDefaultAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.vineflower.kotlin.KotlinOptions;
import org.vineflower.kotlin.expr.KNewExprent;
import org.vineflower.kotlin.util.KUtils;
import org.vineflower.kt.metadata.deserialization.Flags;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultArgsMap {
  private final Map<KParameter, Exprent> map;
  private final StructMethod defaultMethod;

  private DefaultArgsMap(Map<KParameter, Exprent> map, StructMethod defaultMethod) {
    this.map = map;
    this.defaultMethod = defaultMethod;
  }

  public static DefaultArgsMap from(MethodWrapper defaults, MethodWrapper calling, KParameter[] params) {
    if (defaults == null) {
      return new DefaultArgsMap(Map.of(), null);
    }

    /*
     * Kotlin's methodSupplier to define default arguments is a bit odd. The default values are stored in a separate methodSupplier
     * which is called in place of the actual methodSupplier when defaults are needed. The methodSupplier containing the defaults
     * contains two extra arguments, a bitmask and a value that is always null. The bitmask specifies which arguments
     * should be replaced with defaults, and it corresponds one-to-one with all the parameters of the actual methodSupplier,
     * including non-defaulted ones. Constructors follow the same pattern, but the null extra parameter has a type of
     * `DefaultConstructorMarker` instead of `Object`, and the default wrappers are true constructors.
     *
     * In the case of 32 or more parameters, additional bitmask fields are created as needed.
     *
     * For a methodSupplier defined as `fun foo(arg1: Int = 42, arg2: String, arg3: Any? = null)`,
     * the methodSupplier containing its default values would look like this:
     *
     * String foo$default(int arg1, String arg2, Object arg3, int bitmask, Object alwaysNull) {
     *   if ((bitmask & 0b001) != 0) {
     *    arg1 = 42;
     *   }
     *
     *   if ((bitmask & 0b100) != 0) {
     *     arg3 = null;
     *   }
     *
     *   return foo(arg1, arg2, arg3);
     * }
     *
     * To parse this, the methodSupplier graph is traversed, and for each if statement, the condition of the statement is
     * checked to see if it matches the pattern Kotlin uses, and the if-true branch is checked to see if it is a
     * simple assignment. If both are true, it assumes that the assignment is a default value check and extracts
     * the assigned value.
     *
     * This can get most defaults, but it may fail with very complex defaults, such as a `run` block or other
     * inlined blocks.
     */

    Map<KParameter, Exprent> map = new HashMap<>();

    int startOfBitmasks = 0;

    if (!calling.methodStruct.hasModifier(CodeConstants.ACC_STATIC)) {
      // "this" fills the first variable slot
      startOfBitmasks++;
    }

    for (VarType var : calling.desc().params) {
      startOfBitmasks += var.stackSize;
    }

    DirectGraph graph = defaults.getOrBuildGraph();
    for (DirectNode node : graph.nodes) {
      Statement statement = node.statement;
      if (statement instanceof IfStatement ifStatement) {
        Exprent condition = ifStatement.getHeadexprent().getCondition();
        if (
          !(condition instanceof FunctionExprent function) ||
          !(function.getLstOperands().get(0) instanceof FunctionExprent bitmask) ||
          bitmask.getLstOperands().size() != 2
        ) {
          continue;
        }

        Exprent check = bitmask.getLstOperands().get(0);
        Exprent mask = bitmask.getLstOperands().get(1);

        if (!(check instanceof VarExprent var) || !(mask instanceof ConstExprent)) {
          continue;
        }

        int maskValue = ((ConstExprent) mask).getIntValue();
        int maskIndex = (var.getIndex() - startOfBitmasks) * 32;
        for (int i = 0; i < 32; i++) {
          if ((maskValue & (1 << i)) != 0) {
            maskIndex += i;
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

    if (KUtils.assertionsEnabled()) {
      for (KParameter param : params) {
        assert map.containsKey(param) == Flags.DECLARES_DEFAULT_VALUE.get(param.flags()) : "Parameter " + param.name() + " has default value but no default value was found";
      }
    }

    return new DefaultArgsMap(map, defaults.methodStruct);
  }

  public static DefaultArgsMap fromAnnotation(StructClass cl) {
    KConstructor primaryConstructor = cl.getAttribute(KConstructor.PRIMARY_CONSTRUCTOR);
    if (primaryConstructor == null) {
      return new DefaultArgsMap(Map.of(), null);
    }

    Map<String, StructAnnDefaultAttribute> defaultsByName = cl.getMethods().stream()
      .filter(mt -> mt.getAttribute(KElement.KEY) instanceof KProperty)
      .filter(mt -> mt.hasAttribute(StructGeneralAttribute.ATTRIBUTE_ANNOTATION_DEFAULT))
      .collect(Collectors.toMap(mt -> ((KProperty) mt.getAttribute(KElement.KEY)).name(), mt -> mt.getAttribute(StructGeneralAttribute.ATTRIBUTE_ANNOTATION_DEFAULT)));

    Map<KParameter, Exprent> defaults = new HashMap<>();
    for (KParameter param : primaryConstructor.parameters()) {
      StructAnnDefaultAttribute attr = defaultsByName.get(param.name());
      if (attr == null) {
        continue;
      }

      Exprent kexpr = KUtils.replaceExprent(attr.getDefaultValue());
      Exprent expr = kexpr == null ? attr.getDefaultValue() : kexpr;
      if (expr instanceof KNewExprent newExpr) {
        newExpr.setInAnnotation(true);
      }

      defaults.put(param, expr);
    }

    return new DefaultArgsMap(defaults, null);
  }

  public TextBuffer toJava(KParameter parameter, int indent) {
    TextBuffer buffer = new TextBuffer();
    String argString = DecompilerContext.getProperty(KotlinOptions.UNKNOWN_DEFAULT_ARG_STRING).toString();

    Exprent expr = map.get(parameter);
    if (expr == null) {
      if (!argString.isEmpty()) {
        buffer.appendWhitespace(" ").appendOperator("=").appendWhitespace(" ").appendComment(argString);
      }

      return buffer;
    }

    buffer.appendWhitespace(" ").appendOperator("=").appendWhitespace(" ").append(expr.toJava(indent));

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

  public StructMethod getDefaultMethod() {
    return defaultMethod;
  }
}
