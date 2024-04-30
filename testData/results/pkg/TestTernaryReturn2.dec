package pkg;

public class TestTernaryReturn2 {
   public TestTernaryReturn2.T test(TestTernaryReturn2.T in) {
      return new TestTernaryReturn2.T(get(in.toString()), new TestTernaryReturn2.T("test" + (b(in) ? in.test() : "1")).test());// 5
   }

   private static boolean b(TestTernaryReturn2.T in) {
      return in != null;// 9
   }

   private static String get(Object o) {
      return "hi" + o.toString();// 13
   }

   private class T {
      private final String s;
      private final String s2;

      private T(String s) {
         this.s = s;// 21
         this.s2 = "";// 22
      }// 23

      private T(String s, Object... a) {
         this.s = s;// 26
         this.s2 = a[0].toString();// 27
      }// 28

      private String test() {
         return this.s + this.s2;// 31
      }
   }
}

class 'pkg/TestTernaryReturn2' {
   method 'test (Lpkg/TestTernaryReturn2$T;)Lpkg/TestTernaryReturn2$T;' {
      5      4
      6      4
      7      4
      8      4
      9      4
      a      4
      b      4
      17      4
      18      4
      19      4
      1a      4
      1b      4
      1c      4
      1d      4
      1e      4
      1f      4
      20      4
      21      4
      25      4
      26      4
      27      4
      28      4
      29      4
      2a      4
      2b      4
      2f      4
      30      4
      31      4
      36      4
   }

   method 'b (Lpkg/TestTernaryReturn2$T;)Z' {
      0      8
      1      8
      2      8
      3      8
      9      8
   }

   method 'get (Ljava/lang/Object;)Ljava/lang/String;' {
      0      12
      1      12
      2      12
      3      12
      4      12
      5      12
      6      12
      7      12
      8      12
      9      12
   }
}

class 'pkg/TestTernaryReturn2$T' {
   method '<init> (Lpkg/TestTernaryReturn2;Ljava/lang/String;)V' {
      9      20
      a      20
      b      20
      c      20
      d      20
      e      21
      f      21
      10      21
      11      21
      12      21
      13      21
      14      22
   }

   method '<init> (Lpkg/TestTernaryReturn2;Ljava/lang/String;[Ljava/lang/Object;)V' {
      9      25
      a      25
      b      25
      c      25
      d      25
      e      26
      f      26
      10      26
      11      26
      12      26
      13      26
      14      26
      15      26
      16      26
      17      26
      18      27
   }

   method 'test ()Ljava/lang/String;' {
      0      30
      1      30
      2      30
      3      30
      4      30
      5      30
      6      30
      7      30
      8      30
      9      30
      a      30
      b      30
      c      30
      d      30
   }
}

Lines mapping:
5 <-> 5
9 <-> 9
13 <-> 13
21 <-> 21
22 <-> 22
23 <-> 23
26 <-> 26
27 <-> 27
28 <-> 28
31 <-> 31
Not mapped:
20
24
