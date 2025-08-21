package pkg;

public interface TestInterfaceSuper {
   default void defaultMethod() {
   }// 4

   class Impl implements TestInterfaceSuper {
      @Override
      public void defaultMethod() {
         TestInterfaceSuper.super.defaultMethod();// 8
      }// 9
   }
}

class 'pkg/TestInterfaceSuper' {
   method 'defaultMethod ()V' {
      0      4
   }
}

class 'pkg/TestInterfaceSuper$Impl' {
   method 'defaultMethod ()V' {
      1      9
      2      9
      3      9
      4      10
   }
}

Lines mapping:
4 <-> 5
8 <-> 10
9 <-> 11
