package pkg;

public class TestTextBlocks {
  private final String text = """
    Hello!
    This is a text block!
    It's multiple lines long.
    I can use "quotes" in it.
    It's rather cool.
    """;

  public void testLocal() {
    String local = """
    Hello!
    This is a text block!
    It's multiple lines long.
    I can use "quotes" in it.
    It's rather cool.
    """;
  }

  public void testCall() {
    useString("""
    Hello!
    This is a text block!
    It's multiple lines long.
    I can use "quotes" in it.
    It's rather cool.
    """);
  }

  private void useString(String s) {
    System.out.println(s);
  }
}
