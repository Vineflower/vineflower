package pkg;

import java.util.function.Supplier;

public class TestSwitchPatternMatchingJ25 {
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
  
  record Test5(Object o) {}
  public String test5(Object o) {
    return switch (o) {
      case Test5(Test5(Test5 t)) -> t.toString();
      default -> null;
    };
  }
  
  record Test6a(int i) {}
  record Test6b(int i) {}
  public String test6(Object o) {
    return switch (o) {
      case Test6a(int i) -> Integer.toString(i);
      case Test6b(int i) -> Integer.toString(i);
      default -> throw new RuntimeException();
    };
  }
}
