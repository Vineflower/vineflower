package org.jetbrains.java.decompiler.modules.serializer;

import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.util.Pair;

import java.util.*;

public final class ExprParser {
  private static final Map<String, TapestryDeserializer> DESERIALIZERS = new HashMap<>();
  static {
    DESERIALIZERS.put(Exprent.Type.CONST.getPrettyId(), ConstExprent::fromTapestry);
    DESERIALIZERS.put(Exprent.Type.ASSIGNMENT.getPrettyId(), AssignmentExprent::fromTapestry);
    DESERIALIZERS.put(Exprent.Type.ARRAY.getPrettyId(), ArrayExprent::fromTapestry);
    DESERIALIZERS.put(Exprent.Type.INVOCATION.getPrettyId(), InvocationExprent::fromTapestry);
    DESERIALIZERS.put(Exprent.Type.IF.getPrettyId(), IfExprent::fromTapestry);
    DESERIALIZERS.put(Exprent.Type.FUNCTION.getPrettyId(), FunctionExprent::fromTapestry);
    DESERIALIZERS.put(Exprent.Type.EXIT.getPrettyId(), ExitExprent::fromTapestry);
    DESERIALIZERS.put(Exprent.Type.FIELD.getPrettyId(), FieldExprent::fromTapestry);
    DESERIALIZERS.put(Exprent.Type.VAR.getPrettyId(), VarExprent::fromTapestry);
  }


  public static Exprent parse(String expr) {
    String[] s = expr
      .replaceAll("\\]", "] ")
      .replaceAll("\\s+", " ")
      .split(" ");

    Block block = parseArguments(s[0].substring(1), s, 1).a;

    return DESERIALIZERS.get(block.name).deserialize(block);
  }

  // return: [arg, end]
  private static Pair<Block, Integer> parseArguments(String name, String[] args, int start) {
    List<Arg> arguments = new ArrayList<>();
    int length = args.length;

    for (int i = start; i < length; i++) {
      String arg = args[i];
      if (arg.startsWith("[")) {
        Pair<Block, Integer> block = parseArguments(arg.substring(1), args, i + 1);
        i = block.b;
        arguments.add(block.a);
      } else {
        arguments.add(new Literal(arg.replace("]", "")));
      }

      if (arg.endsWith("]")) {
        arguments.removeIf(a -> a instanceof Literal && ((Literal) a).value.isEmpty());
        return Pair.of(new Block(name, arguments), i);
      }
    }

    return Pair.of(new Block(name, arguments), args.length - 1);
  }

  public static abstract class Arg {
    public abstract String getNextString();
    public abstract Exprent getNextExprent();

    public abstract Type peekNext();
  }

  private static final class Literal extends Arg {
    private final String value;

    private Literal(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return "L{" + value + "}";
    }

    @Override
    public String getNextString() {
      throw new IllegalStateException("Cannot call this from a literal");
    }

    @Override
    public Exprent getNextExprent() {
      throw new IllegalStateException("Cannot call this from a literal");
    }

    @Override
    public Type peekNext() {
      throw new IllegalStateException("Cannot call this from a literal");
    }
  }

  private static final class Block extends Arg {
    private final String name;

    private final List<Arg> args;

    private int pointer = 0;

    private Block(String name, List<Arg> args) {
      this.name = name;
      this.args = args;
    }

    @Override
    public String toString() {
      return "B{(" + name + ") " + args.toString() + "}";
    }

    @Override
    public String getNextString() {
      Arg arg = args.get(pointer++);

      if (arg instanceof Literal) {
        return ((Literal) arg).value;
      } else {
        throw new IllegalStateException("Wanted string, found expression instead");
      }
    }

    @Override
    public Exprent getNextExprent() {
      Arg arg = args.get(pointer++);

      if (arg instanceof Block) {
        return DESERIALIZERS.get(((Block) arg).name).deserialize(arg);
      } else {
        throw new IllegalStateException("Wanted expression, found string instead");
      }
    }

    @Override
    public Type peekNext() {
      if (pointer >= args.size()) {
        return Type.END;
      }

      Arg arg = args.get(pointer);

      if (arg instanceof Literal) {
        return Type.STRING;
      } else {
        return Type.EXPRENT;
      }
    }
  }

  public enum Type {
    STRING,
    EXPRENT,
    END
  }
}
