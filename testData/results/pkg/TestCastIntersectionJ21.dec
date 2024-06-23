package pkg;

public class TestCastIntersectionJ21 {
   public void test1(TestCastIntersectionJ21.I1 i1) {
      this.method((TestCastIntersectionJ21.I1 & TestCastIntersectionJ21.I2)i1);// 5
   }// 6

   public void test2(TestCastIntersectionJ21.I2 i2) {
      this.method((TestCastIntersectionJ21.I1 & TestCastIntersectionJ21.I2)i2);// 9
   }// 10

   public void test3(TestCastIntersectionJ21.I1 i1) {
      var i = (TestCastIntersectionJ21.I1 & TestCastIntersectionJ21.I2)i1;// 13
      this.method(i);// 14
   }// 15

   public void test4(TestCastIntersectionJ21.I2 i2) {
      var i = (TestCastIntersectionJ21.I1 & TestCastIntersectionJ21.I2)i2;// 18
      this.method(i);// 19
   }// 20

   public void test5(TestCastIntersectionJ21.I2 i2) {
      var i = (TestCastIntersectionJ21.I1 & TestCastIntersectionJ21.I2)i2;// 23
      ((TestCastIntersectionJ21.I2)i).method();// 24
   }// 25

   public <I extends TestCastIntersectionJ21.I1 & TestCastIntersectionJ21.I2> void method(I i) {
   }// 28

   private static class I1 {
   }

   private interface I2 {
      void method();
   }
}

class 'pkg/TestCastIntersectionJ21' {
   method 'test1 (Lpkg/TestCastIntersectionJ21$I1;)V' {
      0      4
      1      4
      5      4
      6      4
      7      4
      8      4
      9      4
      a      4
      b      5
   }

   method 'test2 (Lpkg/TestCastIntersectionJ21$I2;)V' {
      0      8
      1      8
      2      8
      3      8
      4      8
      5      8
      6      8
      7      8
      8      9
   }

   method 'test3 (Lpkg/TestCastIntersectionJ21$I1;)V' {
      0      12
      4      12
      5      12
      6      12
      7      12
      8      13
      9      13
      a      13
      b      13
      c      13
      d      14
   }

   method 'test4 (Lpkg/TestCastIntersectionJ21$I2;)V' {
      0      17
      1      17
      2      17
      3      17
      4      17
      5      18
      6      18
      7      18
      8      18
      9      18
      a      19
   }

   method 'test5 (Lpkg/TestCastIntersectionJ21$I2;)V' {
      0      22
      1      22
      2      22
      3      22
      4      22
      5      23
      6      23
      7      23
      8      23
      9      23
      a      23
      b      23
      c      23
      d      23
      e      24
   }

   method 'method (Lpkg/TestCastIntersectionJ21$I1;)V' {
      0      27
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
9 <-> 9
10 <-> 10
13 <-> 13
14 <-> 14
15 <-> 15
18 <-> 18
19 <-> 19
20 <-> 20
23 <-> 23
24 <-> 24
25 <-> 25
28 <-> 28
