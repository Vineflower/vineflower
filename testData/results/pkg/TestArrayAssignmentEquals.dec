package pkg;

public class TestArrayAssignmentEquals {
   public int test() {
      int[] a = new int[]{4};// 5
      a[0] *= 2;// 6
      a[0] *= 2;// 7
      a[0] *= 2;// 8
      return a[0] * a[0] * 2;// 9
   }

   public int test1() {
      int[] a = new int[]{4};// 13
      a[0] *= 2;// 14
      a[0] *= 2;// 15
      a[0] *= 2;// 16
      return a[0] *= 2;// 17
   }
}

class 'pkg/TestArrayAssignmentEquals' {
   method 'test ()I' {
      5      4
      7      4
      8      5
      9      5
      c      5
      e      5
      f      6
      10      6
      13      6
      15      6
      16      7
      17      7
      1a      7
      1c      7
      1d      8
      1e      8
      1f      8
      20      8
      21      8
      22      8
      23      8
      24      8
      25      8
      26      8
   }

   method 'test1 ()I' {
      5      12
      7      12
      8      13
      9      13
      c      13
      e      13
      f      14
      10      14
      13      14
      15      14
      16      15
      17      15
      1a      15
      1c      15
      1d      16
      1e      16
      21      16
      24      16
      25      16
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
7 <-> 7
8 <-> 8
9 <-> 9
13 <-> 13
14 <-> 14
15 <-> 15
16 <-> 16
17 <-> 17
