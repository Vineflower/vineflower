package pkg;

public class TestPatternMatchingInline {
   public void test(Object o) {
      this.accept(o, o instanceof String s && s.length() > 5);// 5
   }// 6

   public void test2(Object o) {
      this.accept(o, o instanceof Boolean b && b);// 9
   }// 10

   private void accept(Object o, boolean b) {
   }// 14
}

class 'pkg/TestPatternMatchingInline' {
   method 'test (Ljava/lang/Object;)V' {
      0      4
      1      4
      2      4
      5      4
      6      4
      7      4
      8      4
      9      4
      a      4
      f      4
      10      4
      11      4
      12      4
      13      4
      14      4
      15      4
      16      4
      17      4
      1d      4
      1e      4
      1f      4
      20      5
   }

   method 'test2 (Ljava/lang/Object;)V' {
      0      8
      1      8
      2      8
      5      8
      6      8
      7      8
      8      8
      9      8
      a      8
      f      8
      10      8
      11      8
      12      8
      13      8
      14      8
      15      8
      16      8
      1c      8
      1d      8
      1e      8
      1f      9
   }

   method 'accept (Ljava/lang/Object;Z)V' {
      0      12
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
9 <-> 9
10 <-> 10
14 <-> 13
