package pkg;

import java.util.function.Function;

public class TestOverrideIndirect {
   public interface A {
   }

   public interface B {
   }

   public interface Magic extends Function<TestOverrideIndirect.A, TestOverrideIndirect.B> {
      String name();
   }

   public class Sparkles implements TestOverrideIndirect.Magic {
      @Override
      public String name() {
         return "Sparkles";// 21
      }

      public TestOverrideIndirect.B apply(TestOverrideIndirect.A a) {
         return null;// 26
      }
   }
}

class 'pkg/TestOverrideIndirect$Sparkles' {
   method 'name ()Ljava/lang/String;' {
      0      18
      1      18
      2      18
   }

   method 'apply (Lpkg/TestOverrideIndirect$A;)Lpkg/TestOverrideIndirect$B;' {
      0      22
      1      22
   }
}

Lines mapping:
21 <-> 19
26 <-> 23
