package pkg;

public class TestMethodParametersAttr {
   TestMethodParametersAttr(int p01) {
   }// 20

   void m1(int p02) {
   }// 21

   static void m2(int p03) {
   }// 22

   void local() {
      class Local {
         Local(int p31) {
         }// 37

         void m(int p32) {
         }// 38
      }

   }// 40

   class C1 {
      C1(int p11) {
      }// 25

      void m(int p12) {
      }// 26
   }

   static class C2 {
      C2(int p21) {
      }// 30

      void m1(int p22) {
      }// 31

      static void m2(int p23) {
      }// 32
   }

   abstract class C3 {
      abstract void m1(int var1);

      abstract void m2(int var1);
   }

   abstract static class C4 {
      abstract void m1(int var1);

      abstract void m2(int var1);
   }

   interface I1 {
      void m1(int var1);

      void m2(int var1);
   }
}

class 'pkg/TestMethodParametersAttr' {
   method '<init> (I)V' {
      4      4
   }

   method 'm1 (I)V' {
      0      7
   }

   method 'm2 (I)V' {
      0      10
   }

   method 'local ()V' {
      0      21
   }
}

class 'pkg/TestMethodParametersAttr$1Local' {
   method '<init> (Lpkg/TestMethodParametersAttr;I)V' {
      9      15
   }

   method 'm (I)V' {
      0      18
   }
}

class 'pkg/TestMethodParametersAttr$C1' {
   method '<init> (Lpkg/TestMethodParametersAttr;I)V' {
      9      25
   }

   method 'm (I)V' {
      0      28
   }
}

class 'pkg/TestMethodParametersAttr$C2' {
   method '<init> (I)V' {
      4      33
   }

   method 'm1 (I)V' {
      0      36
   }

   method 'm2 (I)V' {
      0      39
   }
}

Lines mapping:
20 <-> 5
21 <-> 8
22 <-> 11
25 <-> 26
26 <-> 29
30 <-> 34
31 <-> 37
32 <-> 40
37 <-> 16
38 <-> 19
40 <-> 22
