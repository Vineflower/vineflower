package pkg;

public class TestNestedLambdas {
   public void test() {
      int x = accept(i -> {// 6
         if (i == 0) {// 7
            accept(j -> j == 0 ? accept(k -> i + j) : i * j);// 8 9 10 12
         }

         return i;// 17
      });
      System.out.println(x);// 20
   }// 21

   private static int accept(TestNestedLambdas.Func func) {
      return func.get(0);// 24
   }

   @FunctionalInterface
   private interface Func {
      int get(int var1);
   }
}

class 'pkg/TestNestedLambdas' {
   method 'test ()V' {
      5      4
      6      4
      7      4
      8      4
      9      11
      a      11
      b      11
      c      11
      d      11
      e      11
      f      11
      10      12
   }

   method 'lambda$test$2 (I)I' {
      0      5
      1      5
      2      5
      3      5
      a      6
      b      6
      c      6
      e      9
      f      9
   }

   method 'lambda$null$1 (II)I' {
      0      6
      1      6
      2      6
      3      6
      b      6
      c      6
      d      6
      f      6
      10      6
      11      6
   }

   method 'lambda$null$0 (III)I' {
      0      6
      1      6
      2      6
      3      6
   }

   method 'accept (Lpkg/TestNestedLambdas$Func;)I' {
      0      15
      1      15
      2      15
      3      15
      4      15
      5      15
      6      15
      7      15
   }
}

Lines mapping:
6 <-> 5
7 <-> 6
8 <-> 7
9 <-> 7
10 <-> 7
12 <-> 7
17 <-> 10
20 <-> 12
21 <-> 13
24 <-> 16
