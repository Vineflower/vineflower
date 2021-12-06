package pkg;

public class TestMethodReferenceSameName {
   TestMethodReferenceSameName.R1 r;

   private void foo() {
      (this.r::foo).run();// 22
   }// 23

   class R1 {
      void foo() {
      }// 26
   }
}

class 'pkg/TestMethodReferenceSameName' {
   method 'foo ()V' {
      0      6
      1      6
      2      6
      3      6
      e      6
      f      6
      10      6
      11      6
      12      6
      13      7
   }
}

class 'pkg/TestMethodReferenceSameName$R1' {
   method 'foo ()V' {
      0      11
   }
}

Lines mapping:
22 <-> 7
23 <-> 8
26 <-> 12
