package pkg;

public abstract class TestLocalClassNaming {
   void foo() {
      class Local /* TestLocalClassNaming$1Local */ {
         int i = 4;

         Local() {
            this.i++;// 9
         }// 10
      }

      new Local();
   }// 13
}

class 'pkg/TestLocalClassNaming$1Local' {
   method '<init> (Lpkg/TestLocalClassNaming;)V' {
      e      8
      10      8
      11      8
      12      8
      14      8
      18      9
   }
}

class 'pkg/TestLocalClassNaming' {
   method 'foo ()V' {
      9      13
   }
}

Lines mapping:
9 <-> 9
10 <-> 10
13 <-> 14
Not mapped:
6
8
12
