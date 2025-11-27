package pkg;

import java.util.Random;

public enum TestConstructorSwitchExpression3 {
   T1,
   T2;

   TestConstructorSwitchExpression3(int i) {
      System.out.println(i);// 18
   }// 19

   private static int get() {
      return new Random().nextInt(3);// 22
   }

   // $VF: Failed to inline enum fields
   static {
      T1 = new TestConstructorSwitchExpression3(switch (get()) {// 6
         case 0 -> 1;// 7
         case 1 -> 2;// 8
         default -> 3;// 9
      });

      T2 = new TestConstructorSwitchExpression3(switch (get()) {// 11
         case 0 -> 1;// 12
         case 1 -> 2;// 13
         default -> 3;// 14
      });
   }// 5
}

class 'pkg/TestConstructorSwitchExpression3' {
   method '<init> (Ljava/lang/String;II)V' {
      6      9
      7      9
      8      9
      9      9
      a      9
      b      9
      c      9
      d      10
   }

   method 'get ()I' {
      7      13
      8      13
      9      13
      a      13
      b      13
   }

   method '<clinit> ()V' {
      7      18
      8      18
      9      18
      a      18
      b      18
      c      18
      d      18
      e      18
      f      18
      10      18
      11      18
      12      18
      13      18
      14      18
      15      18
      16      18
      17      18
      18      18
      19      18
      1a      18
      1b      18
      1c      18
      1d      18
      1e      18
      1f      18
      20      18
      21      18
      22      18
      23      18
      24      19
      28      20
      2c      21
      30      18
      31      18
      32      18
      3a      24
      3b      24
      3c      24
      3d      24
      3e      24
      3f      24
      40      24
      41      24
      42      24
      43      24
      44      24
      45      24
      46      24
      47      24
      48      24
      49      24
      4a      24
      4b      24
      4c      24
      4d      24
      4e      24
      4f      24
      50      24
      51      24
      52      24
      53      24
      54      24
      55      24
      56      24
      57      24
      58      25
      5c      26
      60      27
      64      24
      65      24
      66      24
      6d      29
   }
}

Lines mapping:
5 <-> 30
6 <-> 19
7 <-> 20
8 <-> 21
9 <-> 22
11 <-> 25
12 <-> 26
13 <-> 27
14 <-> 28
18 <-> 10
19 <-> 11
22 <-> 14
Not mapped:
17
