package pkg;

public class TestSynchronizeNull {
   // $VF: Added assignment to ensure synchronized validity
   public void test() {
      new Object();
      Object var2 = null;// 6
      Object var5;
      synchronized (var5 = null) {
         System.out.println("Hi");// 7
      }
   }// 9

   // $VF: Added assignment to ensure synchronized validity
   public void test1() {
      Object var2 = null;// 13
      Object var5;
      synchronized (var5 = null) {
         System.out.println("Hi");// 14
      }
   }// 16

   // $VF: Added assignment to ensure synchronized validity
   public void test2() {
      Object var2 = null;// 20
      Object var6;
      synchronized (var6 = null) {
         Object var5 = null;// 21
         System.out.println("Hi");// 22
      }
   }// 24
}

class 'pkg/TestSynchronizeNull' {
   method 'test ()V' {
      8      6
      c      6
      d      8
      e      9
      f      9
      10      9
      11      9
      12      9
      13      9
      14      9
      15      9
      20      11
   }

   method 'test1 ()V' {
      0      15
      4      15
      5      17
      6      18
      7      18
      8      18
      9      18
      a      18
      b      18
      c      18
      d      18
      18      20
   }

   method 'test2 ()V' {
      0      24
      4      24
      5      26
      6      27
      7      27
      8      28
      9      28
      a      28
      b      28
      c      28
      d      28
      e      28
      f      28
      1a      30
   }
}

Lines mapping:
6 <-> 7
7 <-> 10
9 <-> 12
13 <-> 16
14 <-> 19
16 <-> 21
20 <-> 25
21 <-> 28
22 <-> 29
24 <-> 31
Not mapped:
5
8
15
23
