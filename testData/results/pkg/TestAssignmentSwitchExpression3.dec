package pkg;

import java.util.Random;

public class TestAssignmentSwitchExpression3 {
   public void test(int x) {
      Random random = switch (x) {// 7
         case -5, -4, -3, -2, -1 -> {
            int seed = x >> 2;// 14
            yield new Random(seed);// 15
         }
         default -> throw new IllegalStateException("Unexpected value: " + x);// 17
         case 1, 2, 3, 4, 5 -> {
            long seed = System.currentTimeMillis() - x * 1000;// 9
            yield new Random(seed);// 10
         }
         case 6, 7, 8, 9, 10 -> new Random();
      };
      System.out.println(random.nextInt());// 19
   }// 20
}

class 'pkg/TestAssignmentSwitchExpression3' {
   method 'test (I)V' {
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
      1c      6
      1d      6
      1e      6
      1f      6
      20      6
      21      6
      22      6
      23      6
      24      6
      25      6
      26      6
      27      6
      28      6
      29      6
      2a      6
      2b      6
      2c      6
      2d      6
      2e      6
      2f      6
      30      6
      31      6
      32      6
      33      6
      34      6
      35      6
      36      6
      37      6
      38      6
      39      6
      3a      6
      3b      6
      3c      6
      3d      6
      3e      6
      3f      6
      40      6
      41      6
      42      6
      43      6
      44      6
      45      6
      46      6
      47      6
      48      6
      49      6
      4a      6
      4b      6
      4c      6
      4d      6
      4e      6
      4f      6
      50      13
      51      13
      52      13
      53      13
      54      13
      55      13
      56      13
      57      13
      58      13
      59      13
      5a      13
      5f      14
      70      8
      71      8
      72      8
      73      8
      78      9
      79      9
      84      11
      85      11
      86      11
      87      11
      88      11
      89      11
      8d      11
      8e      6
      8f      18
      90      18
      91      18
      92      18
      93      18
      94      18
      95      18
      96      18
      97      18
      98      18
      99      19
   }
}

Lines mapping:
7 <-> 7
9 <-> 14
10 <-> 15
14 <-> 9
15 <-> 10
17 <-> 12
19 <-> 19
20 <-> 20
Not mapped:
12
