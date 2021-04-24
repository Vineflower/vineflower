package pkg;

class TestInner2 {
   private TestInner2() {
   }// 4

   private TestInner2(int a) {
   }// 5

   class Another extends TestInner2 {
   }

   class Another2 extends TestInner2 {
      Another2() {
         super(2);// 19
      }// 20
   }

   static class AnotherStatic extends TestInner2 {
   }

   static class AnotherStatic2 extends TestInner2 {
      AnotherStatic2() {
         super(2);// 25
      }// 26
   }
}

class 'pkg/TestInner2' {
   method '<init> ()V' {
      4      4
   }

   method '<init> (I)V' {
      4      7
   }
}

class 'pkg/TestInner2$Another2' {
   method '<init> (Lpkg/TestInner2;)V' {
      6      14
      8      14
      9      14
      a      14
      b      15
   }
}

class 'pkg/TestInner2$AnotherStatic2' {
   method '<init> ()V' {
      1      23
      3      23
      4      23
      5      23
      6      24
   }
}

Lines mapping:
4 <-> 5
5 <-> 8
19 <-> 15
20 <-> 16
25 <-> 24
26 <-> 25
Not mapped:
18
