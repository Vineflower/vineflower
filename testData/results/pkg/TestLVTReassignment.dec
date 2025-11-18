package pkg;

public class TestLVTReassignment {
   public void test() {
      double one = 1.0;// 5
      double shouldBeOne = one;// 6
      one = 0.0;// 7
      if (one > 1.0) {// 8
      }

      this.blackhole(shouldBeOne);// 11
   }// 12

   void blackhole(double value) {
   }// 16
}

class 'pkg/TestLVTReassignment' {
   method 'test ()V' {
      0      4
      1      4
      2      5
      3      5
      4      6
      5      6
      6      7
      7      7
      8      7
      9      7
      a      7
      b      7
      c      10
      d      10
      e      10
      f      10
      10      10
      11      11
   }

   method 'blackhole (D)V' {
      0      14
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
7 <-> 7
8 <-> 8
11 <-> 11
12 <-> 12
16 <-> 15
