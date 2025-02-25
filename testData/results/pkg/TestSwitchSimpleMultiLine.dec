package pkg;

import java.util.Random;
import java.util.function.Predicate;

public class TestSwitchSimpleMultiLine {
   public static <T> Predicate<T> predicates() {
      int i = new Random().nextInt();// 8

      return switch (i) {// 9
         case 0 -> object -> true;// 10
         case 1 -> object -> false;// 11
         default -> object -> {
            boolean r1 = new Random().nextBoolean();// 13
            boolean r2 = new Random().nextBoolean();// 14
            return r1 && r2 ? true : new Random().nextBoolean();// 15 16 18
         };
      };
   }
}

class 'pkg/TestSwitchSimpleMultiLine' {
   method 'predicates ()Ljava/util/function/Predicate;' {
      7      7
      8      7
      9      7
      a      7
      b      9
      c      9
      d      9
      e      9
      f      9
      10      9
      11      9
      12      9
      13      9
      14      9
      15      9
      16      9
      17      9
      18      9
      19      9
      1a      9
      1b      9
      1c      9
      1d      9
      1e      9
      1f      9
      20      9
      21      9
      22      9
      23      9
      24      9
      25      9
      26      9
      27      9
      3d      9
   }

   method 'lambda$predicates$0 (Ljava/lang/Object;)Z' {
      0      10
      1      10
   }

   method 'lambda$predicates$1 (Ljava/lang/Object;)Z' {
      0      11
      1      11
   }

   method 'lambda$predicates$2 (Ljava/lang/Object;)Z' {
      7      13
      8      13
      9      13
      a      13
      12      14
      13      14
      14      14
      15      14
      16      15
      17      15
      18      15
      19      15
      1a      15
      1b      15
      1c      15
      1d      15
      1e      15
      27      15
      28      15
      29      15
   }
}

Lines mapping:
8 <-> 8
9 <-> 10
10 <-> 11
11 <-> 12
13 <-> 14
14 <-> 15
15 <-> 16
16 <-> 16
18 <-> 16
Not mapped:
12
