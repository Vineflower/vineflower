package pkg;

public class TestLocalVariableMerge {
   public boolean a;
   public boolean b;
   public double x;

   public void test(double a, double b, double c) {
      if (this.a) {// 9
         double x = a;// 10
         double y = b;// 11
         double z = c;// 12
         if (this.b) {// 13
            a = this.x;// 14
            b = this.x;// 15
            c = this.x;// 16
         }

         if (x != a) {// 19
            System.out.println("x != a");// 20
         }

         if (y != b) {// 23
            System.out.println("y != b");// 24
         }

         if (z != c) {// 27
            System.out.println("z != c");// 28
         }
      }
   }// 31
}

class 'pkg/TestLocalVariableMerge' {
   method 'test (DDD)V' {
      0      8
      1      8
      2      8
      3      8
      4      8
      5      8
      6      8
      7      9
      8      9
      9      9
      a      10
      b      10
      c      10
      d      11
      e      11
      f      11
      10      11
      11      12
      12      12
      13      12
      14      12
      15      12
      16      12
      17      12
      18      13
      19      13
      1a      13
      1b      13
      1c      13
      1d      14
      1e      14
      1f      14
      20      14
      21      14
      22      15
      23      15
      24      15
      25      15
      26      15
      27      15
      28      18
      29      18
      2a      18
      2b      18
      2c      18
      2d      18
      2e      18
      2f      19
      30      19
      31      19
      32      19
      33      19
      34      19
      35      19
      36      19
      37      22
      38      22
      39      22
      3a      22
      3b      22
      3c      22
      3d      22
      3e      23
      3f      23
      40      23
      41      23
      42      23
      43      23
      44      23
      45      23
      46      26
      47      26
      48      26
      49      26
      4a      26
      4b      26
      4c      26
      4d      26
      4e      27
      4f      27
      50      27
      51      27
      52      27
      53      27
      54      27
      55      27
      56      30
   }
}

Lines mapping:
9 <-> 9
10 <-> 10
11 <-> 11
12 <-> 12
13 <-> 13
14 <-> 14
15 <-> 15
16 <-> 16
19 <-> 19
20 <-> 20
23 <-> 23
24 <-> 24
27 <-> 27
28 <-> 28
31 <-> 31
