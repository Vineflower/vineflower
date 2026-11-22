package pkg;

import java.io.IOException;

public class TestSwitchExpressionIfBlocks {
   public void test(int i1, int i2) throws IOException {
      int r = switch (i1) {// 8
         case 0 -> {
            if (i2 == 0) {// 10
               System.out.println();// 11
               yield 0;// 12
            } else {
               yield i2;// 14
            }
         }
         case 1 -> {
            if (i2 == 0) {// 18
               System.out.println();// 19
               yield 1;// 20
            } else {
               yield i1;// 22
            }
         }
         default -> throw new IllegalArgumentException();// 25
      };
      if (r == 0) {// 27
         throw new IllegalArgumentException();// 28
      }
   }// 30
}

class 'pkg/TestSwitchExpressionIfBlocks' {
   method 'test (II)V' {
      0      6
      1      6
      2      6
      3      6
      4      6
      5      6
      6      6
      7      6
      8      6
      9      6
      a      6
      b      6
      c      6
      d      6
      e      6
      f      6
      10      6
      11      6
      12      6
      13      6
      14      6
      15      6
      16      6
      17      6
      18      6
      19      6
      1a      6
      1b      6
      1c      8
      1d      8
      1e      8
      1f      8
      20      9
      21      9
      22      9
      23      9
      24      9
      25      9
      26      10
      2a      12
      2e      16
      2f      16
      30      16
      31      16
      32      17
      33      17
      34      17
      35      17
      36      17
      37      17
      38      18
      3c      20
      47      23
      48      6
      49      25
      4a      25
      4b      25
      4c      25
      54      26
      55      28
   }
}

Lines mapping:
8 <-> 7
10 <-> 9
11 <-> 10
12 <-> 11
14 <-> 13
18 <-> 17
19 <-> 18
20 <-> 19
22 <-> 21
25 <-> 24
27 <-> 26
28 <-> 27
30 <-> 29
