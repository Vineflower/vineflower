package pkg;

public class TestSwitchPatternMatchingInstanceof4 {
  public void test(Object o) {
    switch (o) {
      case String s && o instanceof Class<?> clzz && clzz.getSigners()[0] instanceof Class<?> c2 -> System.out.println(clzz + "" + c2);
      default -> System.out.println("Default");
    }
  }
}
