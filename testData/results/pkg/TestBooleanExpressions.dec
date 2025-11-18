package pkg;

public class TestBooleanExpressions {
   public int field;
   public TestBooleanExpressions[] array;

   public int test(int i) {
      return i > 0 && i < 10 && this.array[i].field > 0 ? this.array[i].field : -1;// 9
   }

   public int test1() {
      return this.array[0].field;// 13
   }

   public int test2() {
      return this.get().array[0].field;// 17
   }

   public TestBooleanExpressions get() {
      return this;// 21
   }
}

class 'pkg/TestBooleanExpressions' {
   method 'test (I)I' {
      0      7
      1      7
      2      7
      3      7
      4      7
      5      7
      6      7
      7      7
      8      7
      9      7
      a      7
      b      7
      c      7
      d      7
      e      7
      f      7
      10      7
      11      7
      12      7
      13      7
      14      7
      15      7
      16      7
      17      7
      18      7
      19      7
      1a      7
      1b      7
      1c      7
      1d      7
      1e      7
      22      7
      23      7
   }

   method 'test1 ()I' {
      0      11
      1      11
      2      11
      3      11
      4      11
      5      11
      6      11
      7      11
      8      11
      9      11
   }

   method 'test2 ()I' {
      0      15
      1      15
      2      15
      3      15
      4      15
      5      15
      6      15
      7      15
      8      15
      9      15
      a      15
      b      15
      c      15
   }

   method 'get ()Lpkg/TestBooleanExpressions;' {
      0      19
      1      19
   }
}

Lines mapping:
9 <-> 8
13 <-> 12
17 <-> 16
21 <-> 20
