package pkg

public class TestClassicStringInterpolation {
   public final val x: Int = 5// 16

   public fun stringInterpolation(x: Int, y: String) {
      System.out.println("$x $y")// 5
   }// 6

   public fun testConstant(x: Int) {
      System.out.println("$x 5")// 9
   }// 10

   public fun testExpression(x: Int) {
      System.out.println("$x ${x + 1}")// 13
   }// 14

   public fun testProperty() {
      System.out.println("${this.x}!")// 18
   }// 19

   public fun testLiteralClass() {
      System.out.println("${TestClassicStringInterpolation::class.java}!")// 22
   }// 23
}

class 'pkg/TestClassicStringInterpolation' {
   method '<init> ()V' {
      5      3
   }

   method 'stringInterpolation (ILjava/lang/String;)V' {
      d      6
      16      6
      1a      6
      1b      6
      1c      6
      1d      6
      1e      6
      1f      6
      21      6
      22      6
      23      6
      24      7
   }

   method 'testConstant (I)V' {
      7      10
      10      10
      11      10
      12      10
      13      10
      14      10
      15      10
      17      10
      18      10
      19      10
      1a      11
   }

   method 'testExpression (I)V' {
      7      14
      10      14
      11      14
      12      14
      16      14
      17      14
      18      14
      19      14
      1a      14
      1b      14
      1d      14
      1e      14
      1f      14
      20      15
   }

   method 'testProperty ()V' {
      7      18
      8      18
      9      18
      a      18
      13      18
      14      18
      15      18
      16      18
      17      18
      18      18
      1a      18
      1b      18
      1c      18
      1d      19
   }

   method 'testLiteralClass ()V' {
      7      22
      8      22
      11      22
      12      22
      13      22
      14      22
      15      22
      16      22
      18      22
      19      22
      1a      22
      1b      23
   }
}

Lines mapping:
5 <-> 7
6 <-> 8
9 <-> 11
10 <-> 12
13 <-> 15
14 <-> 16
16 <-> 4
18 <-> 19
19 <-> 20
22 <-> 23
23 <-> 24
Not mapped:
3
