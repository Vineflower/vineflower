package pkg;

public class TestInlineAssignments {
   private TestInlineAssignments(int i) {
   }// 6

   public void testCall(int a) {
      int b = a;
      this.accept(a);// 10
      System.out.println(b == a);// 11
   }// 12

   public void testConstructor(int a) {
      int b = a;// 16
      new TestInlineAssignments(a);
      System.out.println(b == a);// 17
   }// 18

   public void testArray(int a) {
      int b = a;// 22
      int[] array = new int[a];
      System.out.println(b == a);// 23
   }// 24

   public void accept(int i) {
   }// 28
}

class 'pkg/TestInlineAssignments' {
   method '<init> (I)V' {
      4      4
   }

   method 'testCall (I)V' {
      0      8
      1      7
      3      7
      4      8
      5      8
      6      8
      7      9
      8      9
      9      9
      a      9
      b      9
      c      9
      d      9
      e      9
      14      9
      15      9
      16      9
      17      10
   }

   method 'testConstructor (I)V' {
      4      13
      6      13
      b      15
      c      15
      d      15
      e      15
      f      15
      10      15
      11      15
      12      15
      18      15
      19      15
      1a      15
      1b      16
   }

   method 'testArray (I)V' {
      0      19
      2      19
      5      20
      6      21
      7      21
      8      21
      9      21
      a      21
      b      21
      c      21
      d      21
      13      21
      14      21
      15      21
      16      22
   }

   method 'accept (I)V' {
      0      25
   }
}

Lines mapping:
6 <-> 5
10 <-> 9
11 <-> 10
12 <-> 11
16 <-> 14
17 <-> 16
18 <-> 17
22 <-> 20
23 <-> 22
24 <-> 23
28 <-> 26
Not mapped:
4
