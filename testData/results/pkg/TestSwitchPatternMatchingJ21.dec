package pkg;

import java.util.function.Supplier;

public class TestSwitchPatternMatchingJ21 {
   Object test4;

   public void test1(Object o) {
      System.out.println(switch (o) {// 7
         case Integer i -> Integer.toString(i);// 8
         case null, default -> "null";// 9
      });
   }// 11

   public String test2(Object o) {
      return switch (o) {// 14
         case Integer i -> Integer.toString(i);// 15
         case String s -> s;// 16
         default -> "null";// 17
      };
   }

   public String test3(Supplier<Object> o) {
      return switch (o.get()) {// 22
         case Integer i -> Integer.toString(i);// 23
         case String s -> s;// 24
         default -> "null";// 25
      };
   }

   public String test4() {
      return switch (this.test4) {// 31
         case Integer i -> Integer.toString(i);// 32
         case String s -> s;// 33
         default -> null;// 34
      };
   }

   public void test4(Object o) {
      switch (o) {// 39
         case Integer i:// 40
            System.out.println(Integer.toString(i));// 41
            break;// 42
         case String s:// 43
            System.out.println(s);// 44
            break;// 45
         default:
      }
   }// 48
}

class 'pkg/TestSwitchPatternMatchingJ21' {
   method 'test1 (Ljava/lang/Object;)V' {
      0      8
      1      8
      2      8
      3      8
      e      8
      f      8
      10      8
      11      8
      12      8
      13      8
      14      8
      15      8
      16      8
      17      8
      18      8
      19      8
      1a      8
      1b      8
      1c      8
      1d      8
      1e      8
      1f      8
      20      8
      21      8
      22      8
      23      8
      24      8
      25      8
      26      8
      27      8
      2c      9
      2d      9
      2e      9
      2f      9
      30      9
      31      9
      32      9
      33      9
      34      9
      35      9
      39      10
      3a      10
      3b      8
      3c      8
      3d      8
      3e      12
   }

   method 'test2 (Ljava/lang/Object;)Ljava/lang/String;' {
      0      15
      10      15
      11      15
      12      15
      13      15
      14      15
      15      15
      16      15
      17      15
      18      15
      19      15
      1a      15
      1b      15
      1c      15
      1d      15
      1e      15
      1f      15
      20      15
      21      15
      22      15
      23      15
      24      15
      25      15
      26      15
      27      15
      28      15
      29      15
      2a      15
      2b      15
      30      16
      31      16
      32      16
      33      16
      34      16
      35      16
      36      16
      37      16
      38      16
      39      16
      41      17
      42      17
      43      17
      44      17
      48      18
      49      18
      4a      15
   }

   method 'test3 (Ljava/util/function/Supplier;)Ljava/lang/String;' {
      0      23
      1      23
      2      23
      3      23
      4      23
      5      23
      15      23
      16      23
      17      23
      18      23
      19      23
      1a      23
      1b      23
      1c      23
      1d      23
      1e      23
      1f      23
      20      23
      21      23
      22      23
      23      23
      24      23
      25      23
      26      23
      27      23
      28      23
      29      23
      2a      23
      2b      23
      2c      23
      2d      23
      2e      23
      2f      23
      34      24
      35      24
      36      24
      37      24
      38      24
      39      24
      3a      24
      3b      24
      3c      24
      3d      24
      45      25
      46      25
      47      25
      48      25
      4c      26
      4d      26
      4e      23
   }

   method 'test4 ()Ljava/lang/String;' {
      0      31
      1      31
      2      31
      3      31
      13      31
      14      31
      15      31
      16      31
      17      31
      18      31
      19      31
      1a      31
      1b      31
      1c      31
      1d      31
      1e      31
      1f      31
      20      31
      21      31
      22      31
      23      31
      24      31
      25      31
      26      31
      27      31
      28      31
      29      31
      2a      31
      2b      31
      30      32
      31      32
      32      32
      33      32
      34      32
      35      32
      36      32
      37      32
      3f      33
      40      33
      41      33
      42      33
      46      34
      47      31
   }

   method 'test4 (Ljava/lang/Object;)V' {
      0      39
      10      39
      11      39
      12      39
      13      39
      14      39
      15      39
      16      39
      17      39
      18      39
      19      39
      1a      39
      1b      39
      1c      39
      1d      39
      1e      39
      1f      39
      20      39
      21      39
      22      39
      23      39
      24      39
      25      39
      26      39
      27      39
      28      39
      29      39
      2a      39
      2b      39
      30      40
      31      40
      32      41
      33      41
      34      41
      35      41
      36      41
      37      41
      38      41
      39      41
      3a      41
      3b      41
      3c      41
      3d      41
      3e      41
      3f      41
      40      42
      47      43
      48      43
      49      44
      4a      44
      4b      44
      4c      44
      4d      44
      4e      44
      4f      44
      50      44
      51      45
      54      48
   }
}

Lines mapping:
7 <-> 9
8 <-> 10
9 <-> 11
11 <-> 13
14 <-> 16
15 <-> 17
16 <-> 18
17 <-> 19
22 <-> 24
23 <-> 25
24 <-> 26
25 <-> 27
31 <-> 32
32 <-> 33
33 <-> 34
34 <-> 35
39 <-> 40
40 <-> 41
41 <-> 42
42 <-> 43
43 <-> 44
44 <-> 45
45 <-> 46
48 <-> 49
