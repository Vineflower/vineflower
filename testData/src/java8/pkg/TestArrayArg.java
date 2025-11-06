package pkg;

import java.util.Arrays;

public class TestArrayArg {
  public void in(Object[] in) {
    Arrays.toString(in);
  }

  public void test(String... data) {
    in(data);
    Arrays.toString(data);
  }

  public static void test1() {
    overloaded(new Object[]{"arg"});
  }

  public static void test1_() {
    overloaded("arg");
  }

  public static void test1_2() {
    overloaded(new Object());
  }

  public static void test2() {
    overloaded((Object) null);
  }

  public static void test2_() {
    overloaded((Object[]) null);
  }

  public static void test3() {
    overloaded1(new String[]{"arg"});
  }

  public static void test3_() {
    overloaded1("arg");
  }

  public static void test4() {
    overloaded1((String) null);
  }

  public static void test4_() {
    overloaded1((String[]) null);
  }

  private static void overloaded(Object o) {
    System.out.println("non-variadic");
  }

  private static void overloaded(Object... o) {
    System.out.println("variadic");
  }

  private static void overloaded1(String s) {
    System.out.println("non-variadic");
  }

  private static void overloaded1(String... s) {
    System.out.println("variadic");
  }
}
