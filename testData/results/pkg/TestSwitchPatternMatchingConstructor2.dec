package pkg;

public class TestSwitchPatternMatchingConstructor2 {
   private TestSwitchPatternMatchingConstructor2(String s) {
      System.out.println(s);// 5
   }// 6

   private TestSwitchPatternMatchingConstructor2(Object s) {
      this(switch (s) {// 9
         case null -> "null";// 10
         default -> "Non-triangle";// 11
      });
   }// 13

   private TestSwitchPatternMatchingConstructor2(Object s, boolean unused) {
      switch (s) {
         default:
            this("Non-triangle");// 16 17
      }
   }// 19
}

class 'pkg/TestSwitchPatternMatchingConstructor2' {
   method '<init> (Ljava/lang/String;)V' {
      4      4
      5      4
      6      4
      7      4
      8      4
      9      4
      a      4
      b      5
   }

   method '<init> (Ljava/lang/Object;)V' {
      1      8
      c      8
      d      8
      e      8
      f      8
      10      8
      11      8
      12      8
      13      8
      14      8
      15      8
      16      8
      17      8
      18      8
      19      8
      1a      8
      1b      8
      1c      8
      1d      8
      1e      8
      1f      8
      20      9
      21      9
      25      10
      26      10
      27      8
      28      8
      29      8
      2a      12
   }

   method '<init> (Ljava/lang/Object;Z)V' {
      1      15
      13      15
      14      15
      15      15
      16      15
      17      15
      18      15
      19      15
      1a      15
      1b      15
      1c      17
      1d      17
      1e      17
      1f      17
      20      17
      21      19
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
9 <-> 9
10 <-> 10
11 <-> 11
13 <-> 13
16 <-> 18
17 <-> 18
19 <-> 20
Not mapped:
4
