package pkg;

import java.util.function.Supplier;

public class TestSwitchPatternMatchingJ21 {
  public void test1(Object o) {
    System.out.println(switch (o) {
      case Integer i -> Integer.toString(i);
      case null, default -> "null";
    });
  }

  public String test2(Object o) {
    return switch (o) {
      case Integer i -> Integer.toString(i);
      case String s -> s;
      default -> "null";
    };
  }

  public String test3(Supplier<Object> o) {
    return switch (o.get()) {
      case Integer i -> Integer.toString(i);
      case String s -> s;
      default -> "null";
    };
  }

  Object test4;
  public String test4() {
    return switch (this.test4) {
      case Integer i -> Integer.toString(i);
      case String s -> s;
      default -> null;
    };
  }

  public void test4(Object o) {
    switch (o) {
      case Integer i:
        System.out.println(Integer.toString(i));
        break;
      case String s:
        System.out.println(s);
        break;
      default:
    }
  }
}
