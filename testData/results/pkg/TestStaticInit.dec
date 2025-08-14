package pkg;

import java.util.function.Supplier;

public class TestStaticInit {
   static final Supplier<TestStaticInit> X = () -> TestStaticInit.Inner.Y;// 7
   static final TestStaticInit Y = null;// 8

   static class Inner {
      static final TestStaticInit Y = null;// 11
   }
}

class 'pkg/TestStaticInit' {
   method 'lambda$static$0 ()Lpkg/TestStaticInit;' {
      0      5
      1      5
      2      5
      3      5
   }

   method '<clinit> ()V' {
      8      6
   }
}

class 'pkg/TestStaticInit$Inner' {
   method '<clinit> ()V' {
      0      9
   }
}

Lines mapping:
7 <-> 6
8 <-> 7
11 <-> 10
