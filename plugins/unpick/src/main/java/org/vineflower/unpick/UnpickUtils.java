package org.vineflower.unpick;

import org.vineflower.unpick.parser.UnpickV3Writer;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.GroupDefinition;
import org.vineflower.unpick.parser.tree.expr.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class UnpickUtils {
  private UnpickUtils() {
  }

  public static String expressionToString(Expression expr) {
    UnpickV3Writer writer = new UnpickV3Writer();
    writer.visitHeader(4);
    writer.visitGroupDefinition(GroupDefinition.Builder.global(DataType.INT).constant(expr).build());
    List<String> lines = writer.getOutput().lines().collect(Collectors.toCollection(ArrayList::new));
    while (lines.get(lines.size() - 1).isBlank()) {
      lines.remove(lines.size() - 1);
    }
    return lines.get(lines.size() - 1).trim();
  }
}
