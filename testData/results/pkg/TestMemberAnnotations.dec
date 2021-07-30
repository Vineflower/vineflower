package pkg;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

class TestMemberAnnotations {
   @TestMemberAnnotations.A("const")
   public static final int CONST = 42;
   @TestMemberAnnotations.A("field")
   private int f;

   @TestMemberAnnotations.A("return")
   private int f(@TestMemberAnnotations.A("arg") int i) {
      return i + this.f + 42;// 12
   }

   @Retention(RetentionPolicy.RUNTIME)
   @interface A {
      String value() default "";
   }
}

class 'pkg/TestMemberAnnotations' {
   method 'f (I)I' {
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
}

Lines mapping:
12 <-> 14
