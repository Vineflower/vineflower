package pkg;

import java.util.function.Function;
import java.util.function.Supplier;

public class TestNestedGenerics1 {
   public static <T, R> R test(T t) {
      Function<T, R> instance = new Function<T, R>() {// 8
         public R apply(T t) {
            return null;// 11
         }
      };
      return (R)instance.apply(t);// 14
   }

   public static <T, R> R testInLambda(T t, R r) {
      Supplier<Function<T, R>> functionMaker = () -> new Function<T, R>() {// 18
         public R apply(T t) {
            return r;// 21
         }
      };
      return (R)((Function)functionMaker.get()).apply(t);// 25
   }
}

class 'pkg/TestNestedGenerics1' {
   method 'test (Ljava/lang/Object;)Ljava/lang/Object;' {
      7      7
      8      12
      9      12
      a      12
      b      12
      c      12
      d      12
      e      12
      f      12
   }

   method 'testInLambda (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;' {
      6      16
      7      21
      8      21
      9      21
      a      21
      b      21
      c      21
      d      21
      e      21
      f      21
      10      21
      11      21
      12      21
      13      21
      14      21
      15      21
      16      21
   }

   method 'lambda$testInLambda$0 (Ljava/lang/Object;)Ljava/util/function/Function;' {
      8      16
   }
}

class 'pkg/TestNestedGenerics1$1' {
   method 'apply (Ljava/lang/Object;)Ljava/lang/Object;' {
      0      9
      1      9
   }
}

class 'pkg/TestNestedGenerics1$2' {
   method 'apply (Ljava/lang/Object;)Ljava/lang/Object;' {
      1      18
      2      18
      3      18
      4      18
   }
}

Lines mapping:
8 <-> 8
11 <-> 10
14 <-> 13
18 <-> 17
21 <-> 19
25 <-> 22
