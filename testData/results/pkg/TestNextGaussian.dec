package pkg;

public class TestNextGaussian {
   private boolean haveNextNextGaussian = false;// 4
   private double nextNextGaussian = 0.0;// 5

   public synchronized double nextGaussian() {
      if (this.haveNextNextGaussian) {// 9
         this.haveNextNextGaussian = false;// 10
         return this.nextNextGaussian;// 11
      }

      double v1;
      double v2;
      double s;
      do {
         v1 = 2.0 * this.nextDouble() - 1.0;// 15
         v2 = 2.0 * this.nextDouble() - 1.0;// 16
         s = v1 * v1 + v2 * v2;// 17
      } while (s >= 1.0 || s == 0.0);// 18

      double multiplier = StrictMath.sqrt(-2.0 * StrictMath.log(s) / s);// 19
      this.nextNextGaussian = v2 * multiplier;// 20
      this.haveNextNextGaussian = true;// 21
      return v1 * multiplier;// 22
   }

   private double nextDouble() {
      return 0.0;// 27
   }
}

class 'pkg/TestNextGaussian' {
   method '<init> ()V' {
      5      3
      a      4
   }

   method 'nextGaussian ()D' {
      0      7
      1      7
      2      7
      3      7
      4      7
      5      7
      6      7
      7      8
      8      8
      9      8
      a      8
      b      8
      c      9
      d      9
      e      9
      f      9
      10      9
      11      16
      12      16
      13      16
      14      16
      15      16
      16      16
      17      16
      18      16
      19      16
      1a      16
      1b      16
      1c      17
      1d      17
      1e      17
      1f      17
      20      17
      21      17
      22      17
      23      17
      24      17
      25      17
      26      17
      27      18
      28      18
      29      18
      2a      18
      2b      18
      2c      18
      2d      18
      2e      18
      2f      18
      30      19
      31      19
      32      19
      33      19
      37      19
      38      19
      39      19
      3a      19
      3b      19
      3c      19
      3d      19
      3e      21
      3f      21
      40      21
      41      21
      42      21
      43      21
      44      21
      45      21
      46      21
      47      21
      48      21
      49      21
      4a      21
      4b      21
      4c      21
      4d      21
      4e      21
      4f      22
      50      22
      51      22
      52      22
      53      22
      54      22
      55      22
      56      22
      57      23
      58      23
      59      23
      5a      23
      5b      23
      5c      24
      5d      24
      5e      24
      5f      24
      60      24
   }

   method 'nextDouble ()D' {
      0      28
      1      28
   }
}

Lines mapping:
4 <-> 4
5 <-> 5
9 <-> 8
10 <-> 9
11 <-> 10
15 <-> 17
16 <-> 18
17 <-> 19
18 <-> 20
19 <-> 22
20 <-> 23
21 <-> 24
22 <-> 25
27 <-> 29
Not mapped:
3
