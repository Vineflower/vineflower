package pkg;

public class TestArrayPPMM1 {
   public int[] test(int i) {
      return new int[]{++i};// 5
   }

   public int[] test1(int i) {
      int[] var10000 = new int[]{i, 0};// 9
      var10000[1] = ++i;
      return var10000;
   }

   public int[] test2(int i) {
      return new int[]{++i, i};// 13
   }

   public int[] test3(int i) {
      return new int[]{++i, ++i};// 17
   }
}

class 'pkg/TestArrayPPMM1' {
   method 'test (I)[I' {
      5      4
      6      4
      7      4
      a      4
   }

   method 'test1 (I)[I' {
      5      8
      8      9
      9      9
      a      9
      b      9
      d      9
      e      10
   }

   method 'test2 (I)[I' {
      5      14
      6      14
      7      14
      c      14
      e      14
   }

   method 'test3 (I)[I' {
      5      18
      6      18
      7      18
      c      18
      d      18
      e      18
      11      18
   }
}

Lines mapping:
5 <-> 5
9 <-> 9
13 <-> 15
17 <-> 19
