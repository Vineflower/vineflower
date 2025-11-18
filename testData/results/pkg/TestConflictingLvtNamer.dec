package pkg;

import java.util.function.Supplier;

public class TestConflictingLvtNamer {
   public static Supplier<Object> method(int i, int j, int k, int l, int m, int n) {
      int o = l - i + 1;// 12
      int p = m - j + 1;// 13
      int q = n - k + 1;// 14
      int r = o * p * q;// 15
      return () -> new Object() {// 16
         private final TestConflictingLvtNamer.Position cursor = new TestConflictingLvtNamer.Position();
         private int index;

         void innerMethod() {
            int ix = this.index % o;// 21
            int jx = this.index / o;// 22
            int kx = jx % p;// 23
            int lx = jx / p;// 24
            this.index++;// 25
            this.cursor.set(i + ix, j + kx, k + lx);// 26
         }// 27
      };
   }

   public static final class Position {
      public void set(int i, int j, int k) {
      }// 8
   }
}

class 'pkg/TestConflictingLvtNamer' {
   method 'method (IIIIII)Ljava/util/function/Supplier;' {
      0      6
      1      6
      2      6
      3      6
      4      6
      5      6
      6      6
      7      7
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
      16      8
      17      9
      18      9
      19      9
      1a      9
      1b      9
      1c      9
      1d      9
      1e      9
      1f      9
      20      9
      2d      10
   }

   method 'lambda$method$0 (IIIII)Ljava/lang/Object;' {
      d      10
   }
}

class 'pkg/TestConflictingLvtNamer$1' {
   method 'innerMethod ()V' {
      0      15
      1      15
      2      15
      3      15
      5      15
      6      15
      7      15
      8      15
      9      15
      a      16
      b      16
      c      16
      d      16
      f      16
      10      16
      11      16
      12      16
      13      16
      14      17
      16      17
      17      17
      18      17
      19      17
      1a      17
      1b      18
      1d      18
      1e      18
      1f      18
      20      18
      21      18
      22      18
      23      19
      25      19
      26      19
      27      19
      29      19
      2d      20
      2e      20
      2f      20
      30      20
      32      20
      33      20
      34      20
      35      20
      36      20
      38      20
      39      20
      3a      20
      3b      20
      3c      20
      3e      20
      3f      20
      40      20
      41      20
      42      20
      43      20
      44      20
      45      20
      46      20
      47      21
   }
}

class 'pkg/TestConflictingLvtNamer$Position' {
   method 'set (III)V' {
      0      27
   }
}

Lines mapping:
8 <-> 28
12 <-> 7
13 <-> 8
14 <-> 9
15 <-> 10
16 <-> 11
21 <-> 16
22 <-> 17
23 <-> 18
24 <-> 19
25 <-> 20
26 <-> 21
27 <-> 22
