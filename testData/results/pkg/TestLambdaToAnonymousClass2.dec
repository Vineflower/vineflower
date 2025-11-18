package pkg;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public class TestLambdaToAnonymousClass2 {
   public void test() {
      BiConsumer<List<String>, Integer> $ = new BiConsumer() {// 8
         public accept(List l, Integer i) {
            Character[] a = (Character[])l.stream().map(new Function() {// 9
               public apply(String st) {
                  return st.charAt(i);// 10
               }
            }).toArray(new IntFunction() {
               public apply(int x$0) {
                  return new Character[x$0];// 11
               }
            });
         }// 12
      };
   }// 13
}

class 'pkg/TestLambdaToAnonymousClass2' {
   method 'test ()V' {
      5      9
      6      22
   }

   method 'lambda$test$2 (Ljava/util/List;Ljava/lang/Integer;)V' {
      0      11
      1      11
      2      11
      3      11
      4      11
      5      11
      c      11
      d      11
      e      11
      f      11
      10      11
      16      15
      17      15
      18      15
      19      15
      1a      15
      1b      11
      1c      11
      1d      11
      1e      11
      1f      20
   }

   method 'lambda$null$0 (Ljava/lang/Integer;Ljava/lang/String;)Ljava/lang/Character;' {
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
      a      13
      b      13
   }

   method 'lambda$null$1 (I)[Ljava/lang/Character;' {
      0      17
      4      17
   }
}

Lines mapping:
8 <-> 10
9 <-> 12
10 <-> 14
11 <-> 18
12 <-> 21
13 <-> 23
