package pkg;

public class TestWhileTernary1 {
   public int test(boolean condition, int a, int b) {
      if (a <= 0) {// 5
         return 0;// 13
      }

      while (condition ? a < b : b > a) {// 6
         System.out.println(a * b);// 7
         a++;// 8
      }

      return 1;// 11
   }
}

class 'pkg/TestWhileTernary1' {
   method 'test (ZII)I' {
      0      4
      1      4
      2      4
      3      4
      4      8
      5      8
      6      8
      7      8
      8      8
      9      8
      a      8
      b      8
      c      8
      10      8
      11      8
      12      8
      13      8
      14      8
      15      9
      16      9
      17      9
      18      9
      19      9
      1a      9
      1b      9
      1c      9
      1d      9
      1e      10
      1f      10
      20      10
      24      13
      25      13
      26      5
      27      5
   }
}

Lines mapping:
5 <-> 5
6 <-> 9
7 <-> 10
8 <-> 11
11 <-> 14
13 <-> 6
