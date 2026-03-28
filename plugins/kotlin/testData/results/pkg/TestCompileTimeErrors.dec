package pkg

public class TestCompileTimeErrors {
   public fun <I, O> test(i: I): O where O : I, O : pkg.TestCompileTimeErrors.Test {
      throw NotImplementedError(null, 1, null)// 10
   }

   public fun test2(i: Int?): pkg.TestCompileTimeErrors.Test? {
      return if (i == null) null else object : pkg.TestCompileTimeErrors.Test {
         public open val testValue: Int?

         {
            this.testValue = `$i`// 16
         }// 14
      }
   }

   public interface Test {
      public val testValue: Int
   }
}

class 'pkg/TestCompileTimeErrors' {
   method 'test (Ljava/lang/Object;)Ljava/lang/Object;' {
      4      4
      5      4
      6      4
      a      4
   }

   method 'test2 (Ljava/lang/Integer;)Lpkg/TestCompileTimeErrors$Test;' {
      0      8
      1      8
      2      8
      3      8
      4      8
      8      8
      9      8
      a      8
      10      8
      11      8
      12      8
      13      8
   }
}

class 'pkg/TestCompileTimeErrors$test2$1' {
   method '<init> (Ljava/lang/Integer;)V' {
      4      12
      5      12
      6      12
      7      12
      8      12
      9      13
   }
}

Lines mapping:
10 <-> 5
14 <-> 14
16 <-> 13
