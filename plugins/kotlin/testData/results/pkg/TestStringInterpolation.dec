package pkg

public class TestStringInterpolation {
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
      System.out.println("${TestStringInterpolation::class.java}!")// 22
   }// 23
}

class 'pkg/TestStringInterpolation' {
   method '<init> ()V' {
      5      3
   }

   method 'stringInterpolation (ILjava/lang/String;)V' {
      6      6
      7      6
      8      6
      9      6
      a      6
      b      6
      c      6
      d      6
      e      6
      f      6
      11      6
      12      6
      13      6
      14      7
   }

   method 'testConstant (I)V' {
      0      10
      1      10
      2      10
      3      10
      4      10
      5      10
      6      10
      7      10
      8      10
      a      10
      b      10
      c      10
      d      11
   }

   method 'testExpression (I)V' {
      0      14
      1      14
      2      14
      3      14
      4      14
      5      14
      6      14
      7      14
      8      14
      9      14
      a      14
      b      14
      d      14
      e      14
      f      14
      10      15
   }

   method 'testProperty ()V' {
      0      18
      1      18
      2      18
      3      18
      4      18
      5      18
      6      18
      7      18
      8      18
      9      18
      a      18
      b      18
      d      18
      e      18
      f      18
      10      19
   }

   method 'testLiteralClass ()V' {
      0      22
      1      22
      2      22
      3      22
      4      22
      5      22
      6      22
      7      22
      8      22
      9      22
      b      22
      c      22
      d      22
      e      23
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
