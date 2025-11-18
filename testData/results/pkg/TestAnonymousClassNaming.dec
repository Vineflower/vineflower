package pkg;

public class TestAnonymousClassNaming {
   public void run(int i, final String s) throws Exception {
      new Object() /* TestAnonymousClassNaming$1 */ {};
      if (i < 0) {// 6
         throw new Exception() /* TestAnonymousClassNaming$2 */ {// 7
            public String getMessage() {
               return s;// 10
            }
         };
      }
   }// 14

   private static class InnerClass {
      Object o = new Object() /* TestAnonymousClassNaming$InnerClass$1 */ {};
   }
}

class 'pkg/TestAnonymousClassNaming' {
   method 'run (ILjava/lang/String;)V' {
      9      5
      a      5
      b      5
      c      5
      16      6
      17      12
   }
}

class 'pkg/TestAnonymousClassNaming$2' {
   method 'getMessage ()Ljava/lang/String;' {
      1      8
      2      8
      3      8
      4      8
   }
}

Lines mapping:
6 <-> 6
7 <-> 7
10 <-> 9
14 <-> 13
Not mapped:
5
