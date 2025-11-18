package pkg;

import ext.Direction;

public class TestSwitchExpressionNested1 {
   public int test(Direction dir, Direction dir2) {
      return switch (dir) {// 7
         case NORTH -> 1;// 8
         case SOUTH -> 2;// 9
         case EAST -> {
            switch (dir2) {// 10
               case NORTH:
                  yield 3;// 11
               case SOUTH:
                  yield 4;// 12
               case EAST:
                  yield 5;// 13
               default:
                  yield -1;// 14
            }
         }
         default -> 0;// 16
      };
   }
}

class 'pkg/TestSwitchExpressionNested1' {
   method 'test (Lext/Direction;Lext/Direction;)I' {
      3      6
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
      24      7
      28      8
      2f      10
      33      10
      34      10
      35      10
      36      10
      37      10
      38      10
      39      10
      3a      10
      3b      10
      3c      10
      3d      10
      3e      10
      3f      10
      40      10
      41      10
      42      10
      43      10
      44      10
      45      10
      46      10
      47      10
      48      10
      49      10
      4a      10
      4b      10
      4c      10
      4d      10
      4e      10
      4f      10
      50      12
      54      14
      58      16
      5c      18
      60      21
      61      6
   }
}

Lines mapping:
7 <-> 7
8 <-> 8
9 <-> 9
10 <-> 11
11 <-> 13
12 <-> 15
13 <-> 17
14 <-> 19
16 <-> 22
