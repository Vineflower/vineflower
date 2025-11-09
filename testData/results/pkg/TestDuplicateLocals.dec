package pkg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class TestDuplicateLocals {
   public static final Function<Object, Predicate<Object>> A = var0 -> var0x -> true;
   private int i = 42;

   public void test1(List<List<Object>> var1) {
      System.out.println(var1);
      var1.forEach(var0 -> {
         List var1x = var0;
         System.out.println(var0);
         var0.forEach(var1xx -> System.out.println(var1x));
      });
   }

   public static void test2(List<List<Object>> var0) {
      System.out.println(var0);
      var0.forEach(var0x -> {
         System.out.println(var0x);
         var0x.forEach(var0xx -> System.out.println(var0xx));
      });
   }

   public void test3(List<List<Object>> var1) {
      System.out.println(var1);
      var1.forEach(var0 -> {
         int var1x = var0.size();
         System.out.println(var0);
         var0.forEach(var1xx -> System.out.println(var1x));
      });
   }

   public void test4(Map<String, List<Object>> var1) {
      System.out.println(var1);
      var1.forEach((var1x, var2) -> {
         System.out.println(var1x);
         var1.forEach((var1xx, var2x) -> System.out.println(var1x));
      });
   }

   public static void test5(Optional<Object> var0) {
      var0.ifPresent(var0x -> System.out.println(var0x));
   }

   public void test6(Optional<Object> var1) {
      var1.ifPresent(var1x -> System.out.println(this.i + " " + var1x));
   }

   public static Integer test7(int var0) {
      return (Integer)new HashMap().computeIfAbsent(var0, var0x -> var0x + 1);
   }

   public class Inner {
      public Integer test7(int var1) {
         return (Integer)new HashMap().computeIfAbsent(var1, var1x -> var1x + TestDuplicateLocals.this.i);
      }
   }

   interface Inner2 {
      TestDuplicateLocals.Inner2 A = var0 -> var0x -> true;

      Predicate<Object> getPredicate(Object var1);
   }
}

class 'pkg/TestDuplicateLocals' {
   method 'lambda$static$1 (Ljava/lang/Object;)Ljava/util/function/Predicate;' {
      5      10
   }

   method 'lambda$null$0 (Ljava/lang/Object;)Z' {
      0      10
      1      10
   }

   method '<init> ()V' {
      5      11
      6      11
   }

   method 'test1 (Ljava/util/List;)V' {
      0      14
      1      14
      2      14
      3      14
      4      14
      5      14
      6      14
      7      15
      d      15
      e      15
      f      15
      10      15
      11      15
      12      20
   }

   method 'lambda$test1$3 (Ljava/util/List;)V' {
      0      16
      1      16
      2      17
      3      17
      4      17
      5      17
      6      17
      7      17
      8      17
      9      18
      10      18
      11      18
      12      18
      13      18
      14      18
      15      19
   }

   method 'lambda$null$2 (Ljava/util/List;Ljava/lang/Object;)V' {
      0      18
      1      18
      2      18
      3      18
      4      18
      5      18
      6      18
      7      18
   }

   method 'test2 (Ljava/util/List;)V' {
      0      23
      1      23
      2      23
      3      23
      4      23
      5      23
      6      23
      7      24
      d      24
      e      24
      f      24
      10      24
      11      24
      12      28
   }

   method 'lambda$test2$5 (Ljava/util/List;)V' {
      0      25
      1      25
      2      25
      3      25
      4      25
      5      25
      6      25
      7      26
      d      26
      e      26
      f      26
      10      26
      11      26
      12      27
   }

   method 'lambda$null$4 (Ljava/lang/Object;)V' {
      0      26
      1      26
      2      26
      3      26
      4      26
      5      26
      6      26
      7      26
   }

   method 'test3 (Ljava/util/List;)V' {
      0      31
      1      31
      2      31
      3      31
      4      31
      5      31
      6      31
      7      32
      d      32
      e      32
      f      32
      10      32
      11      32
      12      37
   }

   method 'lambda$test3$7 (Ljava/util/List;)V' {
      0      33
      1      33
      2      33
      3      33
      4      33
      5      33
      6      33
      7      34
      8      34
      9      34
      a      34
      b      34
      c      34
      d      34
      e      35
      15      35
      16      35
      17      35
      18      35
      19      35
      1a      36
   }

   method 'lambda$null$6 (ILjava/lang/Object;)V' {
      0      35
      1      35
      2      35
      3      35
      4      35
      5      35
      6      35
      7      35
   }

   method 'test4 (Ljava/util/Map;)V' {
      0      40
      1      40
      2      40
      3      40
      4      40
      5      40
      6      40
      7      41
      e      41
      f      41
      10      41
      11      41
      12      41
      13      45
   }

   method 'lambda$test4$9 (Ljava/util/Map;Ljava/lang/String;Ljava/util/List;)V' {
      0      42
      1      42
      2      42
      3      42
      4      42
      5      42
      6      42
      7      43
      e      43
      f      43
      10      43
      11      43
      12      43
      13      44
   }

   method 'lambda$null$8 (Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V' {
      0      43
      1      43
      2      43
      3      43
      4      43
      5      43
      6      43
      7      43
   }

   method 'test5 (Ljava/util/Optional;)V' {
      0      48
      6      48
      7      48
      8      48
      9      49
   }

   method 'lambda$test5$10 (Ljava/lang/Object;)V' {
      0      48
      1      48
      2      48
      3      48
      4      48
      5      48
      6      48
      7      48
   }

   method 'test6 (Ljava/util/Optional;)V' {
      0      52
      7      52
      8      52
      9      52
      a      53
   }

   method 'lambda$test6$11 (Ljava/lang/Object;)V' {
      0      52
      1      52
      2      52
      a      52
      b      52
      c      52
      d      52
      11      52
      12      52
      16      52
      1a      52
      1b      52
      1c      52
      1d      52
      1e      52
      1f      52
      20      52
   }

   method 'test7 (I)Ljava/lang/Integer;' {
      7      56
      8      56
      9      56
      a      56
      10      56
      11      56
      12      56
      13      56
      14      56
      15      56
      16      56
   }

   method 'lambda$test7$12 (Ljava/lang/Integer;)Ljava/lang/Integer;' {
      0      56
      1      56
      2      56
      3      56
      4      56
      5      56
      6      56
      7      56
      8      56
      9      56
   }
}

class 'pkg/TestDuplicateLocals$Inner' {
   method 'test7 (I)Ljava/lang/Integer;' {
      7      61
      8      61
      9      61
      a      61
      11      61
      12      61
      13      61
      14      61
      15      61
      16      61
      17      61
   }

   method 'lambda$test7$0 (Ljava/lang/Integer;)Ljava/lang/Integer;' {
      0      61
      1      61
      2      61
      3      61
      5      61
      6      61
      7      61
      8      61
      9      61
      a      61
      b      61
      c      61
      d      61
      e      61
      f      61
   }
}

class 'pkg/TestDuplicateLocals$Inner2' {
   method 'lambda$static$1 (Ljava/lang/Object;)Ljava/util/function/Predicate;' {
      5      66
   }

   method 'lambda$null$0 (Ljava/lang/Object;)Z' {
      0      66
      1      66
   }
}

Lines mapping:
