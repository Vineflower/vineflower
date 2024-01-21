package pkg;

import java.util.function.Function;

@MoreAnnotations.NestedAnnotation
public class TestTextTokens {
  private Bee bee = new Bee();

  public void foo() {
    bar("Hello world");
    bee.buzz();

    method(method(s -> method(method(s1 -> s1 + " really long string to cause code reformatting" + s)) + s)).apply("Lorem ipsum dolor sit amet");
  }

  private static void bar(String s) {
    System.out.println(s);
  }

  private static Function<String, String> method(Function<String, String> extraLongVariableNameToReachPreferredLineLengthAndCauseWrapping) {
    String s = "Hello world";
    return extraLongVariableNameToReachPreferredLineLengthAndCauseWrapping.andThen(str -> str.replace(" ", ""));
  }

  @Deprecated
  private class Bee {
    private String bee = "bee";
    private Bee parentThis = TestTextTokens.this.bee;

    public void buzz() {
      bar("bzzz");
    }

    public void foo() {
      TestTextTokens.this.foo();
    }
  }
}
