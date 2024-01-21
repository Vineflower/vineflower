package pkg;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TestLocalRecord {
   public void test(int i) {
      record Color(int red, int green, int blue) {
      }

      Color color = new Color((i >> 16 & 0xFF) / 255, (i >> 8 & 0xFF) / 255, (i & 0xFF) / 255);// 10
      System.out.println(color);// 11
   }// 12

   public void test2() {
      record R() {
      }

      List<R> list = new ArrayList();// 16
      list.add(new R());// 17
   }// 18

   public void test3() {
      record R() {
         static void nop() {
         }// 22
      }

      Runnable nop = R::nop;// 24
   }// 25

   public void test4() {
      record R() {
      }

      Supplier<R> constr = () -> new R();// 29
   }// 30

   public Supplier<Supplier<Object>> test5() {
      record R() {
      }

      return () -> () -> new R();// 34
   }

   public Supplier<Object> test6() {
      record R() {
      }

      return () -> new R();// 39
   }
}

class 'pkg/TestLocalRecord' {
   method 'test (I)V' {
      4      11
      5      11
      6      11
      7      11
      8      11
      9      11
      a      11
      b      11
      c      11
      d      11
      e      11
      f      11
      10      11
      11      11
      12      11
      13      11
      14      11
      15      11
      16      11
      17      11
      18      11
      19      11
      1a      11
      1b      11
      1c      11
      1d      11
      1e      11
      1f      11
      20      11
      21      11
      22      11
      23      11
      24      11
      28      11
      29      12
      2a      12
      2b      12
      2c      12
      2d      12
      2e      12
      2f      12
      30      13
   }

   method 'test2 ()V' {
      7      19
      8      20
      10      20
      11      20
      12      20
      13      20
      14      20
      16      21
   }

   method 'test3 ()V' {
      5      29
      6      30
   }

   method 'test4 ()V' {
      5      36
      6      37
   }

   method 'lambda$test4$0 ()Lpkg/TestLocalRecord$3R;' {
      7      36
   }

   method 'test5 ()Ljava/util/function/Supplier;' {
      5      43
   }

   method 'lambda$test5$2 ()Ljava/util/function/Supplier;' {
      5      43
   }

   method 'lambda$test5$1 ()Ljava/lang/Object;' {
      7      43
   }

   method 'test6 ()Ljava/util/function/Supplier;' {
      5      50
   }

   method 'lambda$test6$3 ()Ljava/lang/Object;' {
      7      50
   }
}

class 'pkg/TestLocalRecord$2R' {
   method 'nop ()V' {
      0      26
   }
}

Lines mapping:
10 <-> 12
11 <-> 13
12 <-> 14
16 <-> 20
17 <-> 21
18 <-> 22
22 <-> 27
24 <-> 30
25 <-> 31
29 <-> 37
30 <-> 38
34 <-> 44
39 <-> 51
