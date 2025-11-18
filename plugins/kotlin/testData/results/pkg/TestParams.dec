package pkg

public class TestParams {
   public fun printMessageUnit(message: String) {
      System.out.println(message)// 5
   }// 6

   public fun printMessageVoid(message: String) {
      System.out.println(message)// 9
   }// 10

   public fun multiply(x: Int, y: Int): Int {
      return x * y// 12
   }

   public fun multiplyBraces(x: Int, y: Int): Int {
      return x * y// 15
   }

   public fun printMessageWithPrefix(message: String, prefix: String = "Info") {
      System.out.println("[$prefix] $message")// 19
   }// 20

   public fun callPrintMessage() {
      printMessageWithPrefix$default(this, "Test", null, 2, null)// 23
      this.printMessageWithPrefix("Test", "Debug")// 24
   }// 25
}

class 'pkg/TestParams' {
   method 'printMessageUnit (Ljava/lang/String;)V' {
      6      4
      7      4
      8      4
      9      4
      a      4
      b      4
      c      4
      d      5
   }

   method 'printMessageVoid (Ljava/lang/String;)V' {
      6      8
      7      8
      8      8
      9      8
      a      8
      b      8
      c      8
      d      9
   }

   method 'multiply (II)I' {
      0      12
      1      12
      2      12
      3      12
   }

   method 'multiplyBraces (II)I' {
      0      16
      1      16
      2      16
      3      16
   }

   method 'printMessageWithPrefix (Ljava/lang/String;Ljava/lang/String;)V' {
      6      19
      7      19
      c      20
      d      20
      e      20
      f      20
      10      20
      11      20
      12      20
      13      20
      14      20
      15      20
      17      20
      18      20
      19      20
      1a      21
   }

   method 'callPrintMessage ()V' {
      0      24
      1      24
      2      24
      3      24
      4      24
      5      24
      6      24
      7      24
      8      24
      9      25
      a      25
      b      25
      c      25
      d      25
      e      25
      f      25
      10      25
      11      26
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
9 <-> 9
10 <-> 10
12 <-> 13
15 <-> 17
19 <-> 21
20 <-> 22
23 <-> 25
24 <-> 26
25 <-> 27
