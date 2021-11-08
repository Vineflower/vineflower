package pkg;

// https://youtrack.jetbrains.com/issue/IDEA-264719
public class TestOperatorPrecedence {
  static void test() {
    Integer a = 3;
    System.out.println(++a + ++a);
  }

  static void test2() {
    Integer a = 3;
    System.out.println(++a + ++a);
    System.out.println(a);
  }

  static void test3() {
    Integer a = 3;
    System.out.println(++a + ++a + ++a);
  }

  static void testNoBox() {
    int a = 3;
    System.out.println(++a + ++a);
  }
}
