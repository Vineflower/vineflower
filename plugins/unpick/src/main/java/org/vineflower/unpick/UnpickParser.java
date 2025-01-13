package org.vineflower.unpick;

import org.vineflower.unpick.def.UnpickTarget;

import java.util.*;

public class UnpickParser {
  /*
unpick 1

const unpick_name int pkg/Destination
  1 = VAL_1
  2 = VAL_2

const float_pis float
  1.5707964 = Math.PI / 2

param * float_pis .*

param 1 unpick_name pkg/Use.method(I)V
return unpick_name pkg/Use.method()I
  */

  public static void main(String[] args) {
    System.out.println(parse("unpick 1\n" +
      "\n" +
      "const unpick_name int pkg/Destination\n" +
      "  VAL_1 = 1\n" +
      "  VAL_2 = 2\n" +
      "\n" +
      "param 1 unpick_name pkg/Use.method(I)V\n" +
      "return unpick_name pkg/Use.method()I"));
  }

  public static List<UnpickTarget> parse(String string) {
    System.out.println(string);

    String[] split = string.split("\\n");
    if (split.length < 1) {
      return List.of();
    }

    String first = split[0];
    if (!first.equals("unpick 1")) {
      return List.of();
    }

    Map<String, List<UnpickTarget>> namedTargets = new HashMap<>();

    String current = null;

    for (String s : split) {
      if (s.indexOf('#') != -1) {
        s = s.substring(0, s.indexOf('#'));
      }

      if (s.isBlank()) {
        continue;
      }

      boolean leading = !s.stripLeading().equals(s);
      String[] components = s.stripLeading().split("\\s");
      if (components[0].equals("const") || components[0].equals("bits")) {
        parseDefinition(components);
      }
    }

    return new ArrayList<>();
  }

  private static String parseDefinition(String[] line) {
    String sKind = line[0];
    if (sKind.equals("const")) {

    } else if (sKind.equals("bits")) {

    }

    String name = line[1];

    String sType = line[2];

    String destination = line[3];

    return null;
  }
}
