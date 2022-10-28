package pkg;

import java.util.Objects;

public class TestInnerClassExtendJ17 {
   public class Inner {
      public Inner(String s) {
      }// 7
   }

   public static class Inner2 extends TestInnerClassExtendJ17.Inner {
      public Inner2(TestInnerClassExtendJ17 outer, String s) {
         Objects.requireNonNull(outer);// 12
         super(s);
      }// 13
   }
}

class 'pkg/TestInnerClassExtendJ17$Inner' {
   method '<init> (Lpkg/TestInnerClassExtendJ17;Ljava/lang/String;)V' {
      9      7
   }
}

class 'pkg/TestInnerClassExtendJ17$Inner2' {
   method '<init> (Lpkg/TestInnerClassExtendJ17;Ljava/lang/String;)V' {
      1      12
      3      12
      4      12
      5      12
      7      13
      8      13
      9      13
      a      13
      b      14
   }
}

Lines mapping:
7 <-> 8
12 <-> 13
13 <-> 15
Not mapped:
5
