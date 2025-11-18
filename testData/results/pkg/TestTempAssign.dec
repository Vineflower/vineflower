package pkg;

public class TestTempAssign {
   public void test(double a, double b) {
      double c = a;// 5
      a = b;// 6
      b = c;// 7
      System.out.println(a);// 9
      System.out.println(b);// 10
   }// 11

   public void test1(double a, double b, double c) {
      if (a > b) {// 15
         c = a;// 16
         a = b;// 17
         b = c;// 18
      }

      System.out.println(a);// 21
      System.out.println(b);// 22
      System.out.println(c);// 23
   }// 24

   public void test2(double a, double b) {
      double c = a;// 27
      System.out.println(a);// 28
      a = b;// 29
      System.out.println(a);// 30
      b = c;// 31
      System.out.println(a);// 33
      System.out.println(b);// 34
   }// 35
}

class 'pkg/TestTempAssign' {
   method 'test (DD)V' {
      0      4
      1      4
      2      4
      3      5
      4      5
      5      6
      6      6
      7      6
      8      7
      9      7
      a      7
      b      7
      c      7
      d      7
      e      7
      f      8
      10      8
      11      8
      12      8
      13      8
      14      8
      15      8
      16      9
   }

   method 'test1 (DDD)V' {
      0      12
      1      12
      2      12
      3      12
      4      12
      5      12
      6      13
      7      13
      8      13
      9      14
      a      14
      b      15
      c      15
      d      15
      e      18
      f      18
      10      18
      11      18
      12      18
      13      18
      14      18
      15      19
      16      19
      17      19
      18      19
      19      19
      1a      19
      1b      19
      1c      20
      1d      20
      1e      20
      1f      20
      20      20
      21      20
      22      20
      23      20
      24      21
   }

   method 'test2 (DD)V' {
      0      24
      1      24
      2      24
      3      25
      4      25
      5      25
      6      25
      7      25
      8      25
      9      25
      a      26
      b      26
      c      27
      d      27
      e      27
      f      27
      10      27
      11      27
      12      27
      13      28
      14      28
      15      28
      16      29
      17      29
      18      29
      19      29
      1a      29
      1b      29
      1c      29
      1d      30
      1e      30
      1f      30
      20      30
      21      30
      22      30
      23      30
      24      31
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
7 <-> 7
9 <-> 8
10 <-> 9
11 <-> 10
15 <-> 13
16 <-> 14
17 <-> 15
18 <-> 16
21 <-> 19
22 <-> 20
23 <-> 21
24 <-> 22
27 <-> 25
28 <-> 26
29 <-> 27
30 <-> 28
31 <-> 29
33 <-> 30
34 <-> 31
35 <-> 32
