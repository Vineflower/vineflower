package pkg;

import java.util.function.Function;

public class TestLambdaParamTypes {
   public void f() {
      Function<String, ?> f1a = x -> x;// 7
      Function<String, ?> f1b = x -> x.length() + 1;// 8
      Function<?, ?> f2a = x -> x;// 9
      Function<?, ?> f2b = x -> x.length() + 1;// 10
      this.g(x -> x);// 11
      this.g(x -> x.length() + 1);// 12
      this.h(x -> x);// 13
      this.h(x -> x.length() + 1);// 14
   }// 15

   public void g(Function<?, ?> fn) {
   }// 18

   public <T> void h(Function<T, ?> fn) {
   }// 21
}

class 'pkg/TestLambdaParamTypes' {
   method 'f ()V' {
      5      6
      b      7
      11      8
      17      9
      18      9
      19      10
      1f      10
      20      10
      21      10
      22      11
      28      11
      29      11
      2a      11
      2b      12
      31      12
      32      12
      33      12
      34      13
      3a      13
      3b      13
      3c      13
      3d      14
   }

   method 'lambda$f$0 (Ljava/lang/String;)Ljava/lang/Object;' {
      0      6
      1      6
   }

   method 'lambda$f$1 (Ljava/lang/String;)Ljava/lang/Object;' {
      0      7
      1      7
      2      7
      3      7
      4      7
      5      7
      6      7
      7      7
      8      7
      9      7
   }

   method 'lambda$f$2 (Ljava/lang/String;)Ljava/lang/Object;' {
      0      8
      1      8
   }

   method 'lambda$f$3 (Ljava/lang/String;)Ljava/lang/Object;' {
      0      9
      1      9
      2      9
      3      9
      4      9
      5      9
      6      9
      7      9
      8      9
      9      9
   }

   method 'lambda$f$4 (Ljava/lang/String;)Ljava/lang/Object;' {
      0      10
      1      10
   }

   method 'lambda$f$5 (Ljava/lang/String;)Ljava/lang/Object;' {
      0      11
      1      11
      2      11
      3      11
      4      11
      5      11
      6      11
      7      11
      8      11
      9      11
   }

   method 'lambda$f$6 (Ljava/lang/String;)Ljava/lang/Object;' {
      0      12
      1      12
   }

   method 'lambda$f$7 (Ljava/lang/String;)Ljava/lang/Object;' {
      0      13
      1      13
      2      13
      3      13
      4      13
      5      13
      6      13
      7      13
      8      13
      9      13
   }

   method 'g (Ljava/util/function/Function;)V' {
      0      17
   }

   method 'h (Ljava/util/function/Function;)V' {
      0      20
   }
}

Lines mapping:
7 <-> 7
8 <-> 8
9 <-> 9
10 <-> 10
11 <-> 11
12 <-> 12
13 <-> 13
14 <-> 14
15 <-> 15
18 <-> 18
21 <-> 21
