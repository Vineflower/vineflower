package pkg;

public class TestCustomProcessor {
  static StringTemplate.Processor<Object, Error> processor;

  public void test() {
    String s = "Hello";
    int i = 42;
    double d = 3.14159;

    Object result = processor."Text: \{s} \{i} \{d}";
  }
}
