package pkg;

public class TestGenericStaticCall<T> {
   public T t;

   public static void test() {
      method(new Object(), null);// 7
   }// 8

   public void test1() {
      method(this, null);// 11
   }// 12

   public static <T> void method(Object o, T t) {
   }// 16
}

class 'pkg/TestGenericStaticCall' {
   method 'test ()V' {
      7      6
      8      6
      9      6
      a      6
      b      7
   }

   method 'test1 ()V' {
      0      10
      1      10
      2      10
      3      10
      4      10
      5      11
   }

   method 'method (Ljava/lang/Object;Ljava/lang/Object;)V' {
      0      14
   }
}

Lines mapping:
7 <-> 7
8 <-> 8
11 <-> 11
12 <-> 12
16 <-> 15
