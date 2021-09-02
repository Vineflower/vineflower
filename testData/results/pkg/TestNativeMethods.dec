package pkg;

public class TestNativeMethods {
   public native void foo();

   public int bar(int i) {
      return i * i;// 7
   }

   protected native void baz();

   public void bar1(String s) {
      System.out.println(s);// 13
   }// 14
}

class 'pkg/TestNativeMethods' {
   method 'bar (I)I' {
      0      6
      1      6
      2      6
      3      6
   }

   method 'bar1 (Ljava/lang/String;)V' {
      0      12
      1      12
      2      12
      3      12
      4      12
      5      12
      6      12
      7      13
   }
}

Lines mapping:
7 <-> 7
13 <-> 13
14 <-> 14
