package pkg;

import java.util.function.Function;

public class TestTextTokens {
  private Bee bee = new Bee();

  public void foo() {
    bar("Hello world");
    bee.buzz();

    method(method(s -> method(method(s1 -> s1 + " really long string to cause code reformatting")) + s)).apply("Lorem ipsum dolor sit amet");
  }

  private static void bar(String s) {
    System.out.println(s);
  }

  private static Function<String, String> method(Function<String, String> extraLongVariableNameToReachPreferredLineLengthAndCauseWrapping) {
    return extraLongVariableNameToReachPreferredLineLengthAndCauseWrapping.andThen(str -> str.replace(" ", ""));
  }

  @Deprecated
  private class Bee {
    public void buzz() {
      bar("bzzz");
    }
  }
}
