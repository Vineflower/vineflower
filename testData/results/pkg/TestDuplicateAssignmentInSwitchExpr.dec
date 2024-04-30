package pkg;

public class TestDuplicateAssignmentInSwitchExpr {
   void foo(int bar) {
      int num = switch (bar) {// 5
         case 0 -> {
            int num;
            yield num = 1;// 6
         }
         default -> 0;// 7
      };
      System.out.println(num);// 9
   }// 10
}

class 'pkg/TestDuplicateAssignmentInSwitchExpr' {
   method 'foo (I)V' {
      0      4
      1      4
      2      4
      3      4
      4      4
      5      4
      6      4
      7      4
      8      4
      9      4
      a      4
      b      4
      c      4
      d      4
      e      4
      f      4
      10      4
      11      4
      12      4
      13      4
      14      7
      16      7
      1a      9
      1b      4
      1c      11
      1d      11
      1e      11
      1f      11
      20      11
      21      11
      22      11
      23      12
   }
}

Lines mapping:
5 <-> 5
6 <-> 8
7 <-> 10
9 <-> 12
10 <-> 13
