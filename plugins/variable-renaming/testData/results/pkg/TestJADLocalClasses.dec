package pkg;

public class TestJADLocalClasses {
   public void function() {
      int i = 0;// 5

      class Test {
         Test() {
            int j = 0;// 8
         }// 9
      }

   }// 11
}

class 'pkg/TestJADLocalClasses' {
   method 'function ()V' {
      0      4
      1      4
      2      12
   }
}

class 'pkg/TestJADLocalClasses$1Test' {
   method '<init> (Lpkg/TestJADLocalClasses;)V' {
      9      8
      a      8
      b      9
   }
}

Lines mapping:
5 <-> 5
8 <-> 9
9 <-> 10
11 <-> 13
Not mapped:
7
