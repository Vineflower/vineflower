package pkg;

import java.util.List;
import java.util.function.BiConsumer;

public class TestLambdaNaming {
   public void test() {
      Runnable f = /* lambda$test$0 */ () -> System.out.println("test");// 8
      BiConsumer<List<String>, Integer> g = /* lambda$test$3 */ (l, i) -> {// 9
         Character[] a = (Character[])l.stream().map(/* lambda$null$1 */ st -> st.charAt(i)).toArray(/* lambda$null$2 */ Character[]::new);// 10 11 12
      };// 13
   }// 14
}

class 'pkg/TestLambdaNaming' {
   method 'test ()V' {
      5      7
      b      8
      c      11
   }

   method 'lambda$test$0 ()V' {
      0      7
      1      7
      2      7
      3      7
      4      7
      5      7
      6      7
      7      7
      8      7
   }

   method 'lambda$test$3 (Ljava/util/List;Ljava/lang/Integer;)V' {
      0      9
      1      9
      2      9
      3      9
      4      9
      5      9
      c      9
      d      9
      e      9
      f      9
      10      9
      16      9
      17      9
      18      9
      19      9
      1a      9
      1b      9
      1c      9
      1d      9
      1e      9
      1f      10
   }

   method 'lambda$null$1 (Ljava/lang/Integer;Ljava/lang/String;)Ljava/lang/Character;' {
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
      a      9
      b      9
   }
}

Lines mapping:
8 <-> 8
9 <-> 9
10 <-> 10
11 <-> 10
12 <-> 10
13 <-> 11
14 <-> 12
