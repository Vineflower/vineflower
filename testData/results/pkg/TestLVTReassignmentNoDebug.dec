package pkg;

public class TestLVTReassignmentNoDebug {
   public void test() {
      double var1 = 1.0;
      double var3 = var1;
      var1 = 0.0;
      if (var1 > 1.0) {
      }

      this.blackhole(var3);
   }

   void blackhole(double var1) {
   }
}

class 'pkg/TestLVTReassignmentNoDebug' {
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
