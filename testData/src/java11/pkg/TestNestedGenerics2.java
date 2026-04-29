package pkg;

public class TestNestedGenerics2 {
  public static <T, R> R test(T t) {
    var instance = new Object() {
      R run(T t) {
        return null;
      }
    };
    return instance.run(t);
  }
}
