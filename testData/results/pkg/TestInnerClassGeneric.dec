package pkg;

public class TestInnerClassGeneric<T extends Comparable> {
   T field;

   public class Inner<T extends Number> {
      private final int i;
      private final T t;

      public Inner(int i, T t) {
         this.i = i;// 12
         this.t = t;// 13
      }// 14
   }
}

class 'pkg/TestInnerClassGeneric$Inner' {
   method '<init> (Lpkg/TestInnerClassGeneric;ILjava/lang/Number;)V' {
      9      10
      a      10
      b      10
      c      10
      d      10
      e      11
      f      11
      10      11
      11      11
      12      11
      13      12
   }
}

Lines mapping:
12 <-> 11
13 <-> 12
14 <-> 13
Not mapped:
11
