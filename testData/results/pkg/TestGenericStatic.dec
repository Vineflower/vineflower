package pkg;

public class TestGenericStatic<T extends TestGenericStatic.Generic> {
   private final T generic;

   public TestGenericStatic(T generic) {
      this.generic = generic;// 10
   }// 11

   public static void main() {
   }// 15

   private static <T extends TestGenericStatic.Generic> T call(String name, TestGenericStatic<T> holder) {
      return holder.generic;// 18
   }

   public static class Generic {
      void test() {
         System.out.println("Hi!");// 23
      }// 24
   }
}

class 'pkg/TestGenericStatic' {
   method '<init> (Lpkg/TestGenericStatic$Generic;)V' {
      4      6
      5      6
      6      6
      7      6
      8      6
      9      7
   }

   method 'main ()V' {
      0      10
   }

   method 'call (Ljava/lang/String;Lpkg/TestGenericStatic;)Lpkg/TestGenericStatic$Generic;' {
      0      13
      1      13
      2      13
      3      13
      4      13
   }
}

class 'pkg/TestGenericStatic$Generic' {
   method 'test ()V' {
      0      18
      1      18
      2      18
      3      18
      4      18
      5      18
      6      18
      7      18
      8      19
   }
}

Lines mapping:
10 <-> 7
11 <-> 8
15 <-> 11
18 <-> 14
23 <-> 19
24 <-> 20
Not mapped:
9
