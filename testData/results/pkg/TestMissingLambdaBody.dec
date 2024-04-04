package pkg;

import ext.TestMissingLambdaBodyExtra;
import java.util.function.Consumer;

public class TestMissingLambdaBody extends TestMissingLambdaBodyExtra {
   public void init() {
      this.visit(x$0 -> {
         Integer var10000 = (Integer)this.add(x$0);// 9
      });
   }// 10

   public void visit(Consumer<Integer> var1) {
      var1.accept("Foo".hashCode());// 13
      var1.accept("Bar".length());// 14
   }// 15
}

class 'pkg/TestMissingLambdaBody' {
   method 'init ()V' {
      0      7
      7      7
      8      7
      9      7
      a      10
   }

   method 'lambda$init$0 (Lpkg/TestMissingLambdaBody;Ljava/lang/Number;)V' {
      0      8
      1      8
      2      8
      3      8
      4      8
      5      8
      6      8
      7      8
      9      9
   }

   method 'visit (Ljava/util/function/Consumer;)V' {
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
      c      13
      d      13
      e      14
      f      14
      10      14
      11      14
      12      14
      13      14
      14      14
      15      14
      16      14
      17      14
      18      14
      19      14
      1a      14
      1b      14
      1c      15
   }
}

Lines mapping:
9 <-> 9
10 <-> 11
13 <-> 14
14 <-> 15
15 <-> 16
