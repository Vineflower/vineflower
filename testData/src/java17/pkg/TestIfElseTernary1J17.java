package pkg;

public class TestIfElseTernary1J17 {
  public void test0(boolean condition, int a, int b, int c) {
    if (condition ? a < b : b > c) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void test1(boolean condition, int a, int b, int c) {
    if (condition
      ? (a < b ? a == 0 : b == 0)
      : (b > c)) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void test2(boolean condition, int a, int b, int c) {
    if (condition
      ? (a < b)
      : (b > c ? b == 15 : a == 15)) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void test3(boolean condition, int a, int b, int c) {
    if (condition
      ? (a < b ? a == 0 : b == 0)
      : (b > c ? b == 15 : a == 15)) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void test4(boolean condition, int a, int b, int c) {
    if ((condition ? a + c > b : a < b + c)
      ? a < b
      : b > c) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void test5(boolean condition, int a, int b, int c) {
    if ((condition ? a + c > b : a < b + c)
      ? (a < b ? a == 0 : b == 0)
      : (b > c)) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void test6(boolean condition, int a, int b, int c) {
    if ((condition ? a + c > b : a < b + c)
      ? (a < b)
      : (b > c ? b == 15 : a == 15)) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void test7(boolean condition, int a, int b, int c) {
    if ((condition ? a + c > b : a < b + c)
      ? (a < b ? a == 0 : b == 0)
      : (b > c ? b == 15 : a == 15)) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void test8(boolean condition, int a, int b, int c) {
    if (a == b || b == c
      ? a != b
      : a > b && b > c
      ? a < b + c && a > 3 * c
      : condition) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void test8b(boolean condition, int a, int b, int c) {
    if (a == b && b == c
      ? !condition
      : a > b && b > c
      ? a < b + c && a > 3 * c
      : condition) {
      System.out.println(1);
    } else {
      System.out.println(2);
    }
  }

  public void testFuzz1() {
    int[] vvv1 = new int[0], vvv2 = new int[0], vvv3 = new int[0];
    if (vvv1 == null && vvv2 == null || vvv2 != null ? vvv3 != null : vvv2 == null ? vvv2 == null : vvv3 != null && vvv1 == null)
      synchronized (this)
      {
        ;
        vvv2 = vvv3;
        System.out.println(vvv2);
        long[][] vvv4 = new long[0][];
        vvv3 = vvv1;
      }
    else
    {
      try
      {
        vvv1 = vvv3;
        int vvv5 = 209;
      }
      catch (Exception vvv6)
      {
        System.out.println(vvv6);
        System.out.println(vvv3);
        int vvv7[][][][], vvv8 = 104, vvv9[][][][][] = new int[0][][][][], vvv10;
        throw new RuntimeException();
      }
    }
    if (false && vvv2 != null && vvv2 != null)
    {
      try
      {
        System.out.println("Hi");
        System.out.println("Hi");
      }
      finally
      {
        vvv2 = vvv3;
      }
    }
    else
      vvv3 = vvv2;
    System.out.println(vvv3);
    vvv1 = vvv2;
    System.out.println(vvv1);
    vvv2 = vvv1;
    System.out.println(vvv1);
    int vvv11 = -103;
    try
    {
      ;
      vvv3 = vvv2;
      throw new RuntimeException();
    }
    finally
    {
      Object vvv12 = null;
      vvv11 *= 314;
      System.out.println(vvv2);
      throw new RuntimeException();
    }
  }
}
