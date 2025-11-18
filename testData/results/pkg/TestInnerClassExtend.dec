package pkg;

public class TestInnerClassExtend {
   public class Inner {
      public Inner(String s) {
      }// 7
   }

   public class Inner2 extends TestInnerClassExtend.Inner {
      public Inner2(String s) {
         super(s);// 12
      }// 13
   }
}

class 'pkg/TestInnerClassExtend$Inner' {
   method '<init> (Lpkg/TestInnerClassExtend;Ljava/lang/String;)V' {
      9      5
   }
}

class 'pkg/TestInnerClassExtend$Inner2' {
   method '<init> (Lpkg/TestInnerClassExtend;Ljava/lang/String;)V' {
      7      10
      8      10
      9      10
      a      10
      b      11
   }
}

Lines mapping:
7 <-> 6
12 <-> 11
13 <-> 12
Not mapped:
5
11
