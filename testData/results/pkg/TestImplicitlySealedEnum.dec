package pkg;

public enum TestImplicitlySealedEnum {
   A,
   B {
      @Override
      int getX() {
         return 2;// 7
      }
   };

   int getX() {
      return 1;// 12
   }
}

class 'pkg/TestImplicitlySealedEnum$1' {
   method 'getX ()I' {
      0      7
      1      7
   }
}

class 'pkg/TestImplicitlySealedEnum' {
   method 'getX ()I' {
      0      12
      1      12
   }
}

Lines mapping:
7 <-> 8
12 <-> 13
