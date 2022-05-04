package pkg;

public class TestInstanceStaticInvoke {
   private TestInstanceStaticInvoke inst;

   public void test() {
      new TestInstanceStaticInvoke();
      method();// 7
   }// 8

   public void test1(TestInstanceStaticInvoke param) {
      method();// 11
   }// 12

   public void test2() {
      method();// 15
   }// 16

   public static void method() {
   }// 20
}

class 'pkg/TestInstanceStaticInvoke' {
   method 'test ()V' {
      8      7
      9      7
      a      7
      b      8
   }

   method 'test1 (Lpkg/TestInstanceStaticInvoke;)V' {
      2      11
      3      11
      4      11
      5      12
   }

   method 'test2 ()V' {
      5      15
      6      15
      7      15
      8      16
   }

   method 'method ()V' {
      0      19
   }
}

Lines mapping:
7 <-> 8
8 <-> 9
11 <-> 12
12 <-> 13
15 <-> 16
16 <-> 17
20 <-> 20
