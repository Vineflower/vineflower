package pkg;

import java.lang.annotation.ElementType;
import java.util.Scanner;

public class TestUnknownCastJ17 {
   public void test() {
      ElementType vvv1 = ElementType.METHOD;// 8
      synchronized (this) {// 9
         System.out.println(vvv1);// 10

         for (int var19 = switch (new Object()) {// 11
            default -> {
               ElementType var5 = ElementType.METHOD;// 13

               try {
                  Scanner vvv3 = new Scanner(System.in);// 15

                  try {
                     String var21 = "Hi!";// 16
                  } catch (Throwable var15) {
                     try {
                        vvv3.close();
                     } catch (Throwable var13) {
                        var15.addSuppressed(var13);
                     }

                     throw var15;
                  }

                  vvv3.close();// 17
               } finally {
                  System.out.println(vvv1);// 18
               }

               yield 140;// 19
            }
         }; --var19 <= 395; var19 -= 6) {// 20
            int vvv6 = -91;// 21
            System.out.println(vvv1);// 22
         }
      }

      try {
         throw new RuntimeException();// 28
      } catch (Exception var14) {// 29
      }
   }// 31

   public void test2() {
      ElementType vvv1 = ElementType.METHOD;// 34
      synchronized (this) {// 35
         System.out.println(vvv1);// 36
         System.out.println(vvv1);// 37

         for (int var9 = switch (new Object()) {// 38
            default -> {
               ElementType var5 = ElementType.METHOD;// 40
               System.out.println(vvv1);// 42
               yield 140;
            }
         }; --var9 <= 395; var9 -= 6) {// 43
            int vvv6 = -91;// 44
            System.out.println(vvv1);// 45
         }
      }

      try {
         throw new RuntimeException();// 51
      } catch (Exception var7) {// 52
      }
   }// 54
}

class 'pkg/TestUnknownCastJ17' {
   method 'test ()V' {
      0      7
      1      7
      2      7
      3      7
      4      8
      7      8
      8      9
      9      9
      a      9
      b      9
      c      9
      d      9
      e      9
      27      11
      28      11
      29      11
      2a      11
      2b      11
      2c      11
      2d      11
      2e      11
      2f      11
      30      13
      31      13
      32      13
      33      13
      34      13
      39      16
      3a      16
      3b      16
      3f      16
      40      19
      41      19
      42      19
      43      19
      44      30
      45      30
      46      30
      47      30
      4d      22
      4e      22
      4f      22
      50      22
      54      23
      56      24
      57      24
      58      24
      59      24
      5a      24
      5b      24
      5c      24
      5d      27
      5e      27
      5f      27
      67      35
      68      35
      69      35
      6c      32
      6d      32
      6e      32
      6f      32
      70      32
      71      32
      72      32
      7a      37
      7b      37
      7c      37
      7d      37
      7e      37
      7f      37
      80      37
      81      37
      82      37
      83      37
      84      38
      85      38
      86      38
      87      38
      88      39
      89      39
      8a      39
      8b      39
      8c      39
      8d      39
      8e      39
      8f      37
      90      37
      91      37
      a8      44
      a9      45
      aa      47
   }

   method 'test2 ()V' {
      0      50
      1      50
      2      50
      3      50
      4      51
      7      51
      8      52
      9      52
      a      52
      b      52
      c      52
      d      52
      e      52
      f      53
      10      53
      11      53
      12      53
      13      53
      14      53
      15      53
      2e      55
      2f      55
      30      55
      31      55
      32      55
      33      55
      34      55
      35      55
      36      55
      37      55
      38      57
      39      57
      3a      57
      3b      57
      3c      57
      3d      58
      3e      58
      3f      58
      40      58
      41      58
      42      58
      43      58
      44      59
      45      59
      46      59
      48      61
      49      61
      4a      61
      4b      61
      4c      61
      4d      61
      4e      61
      4f      61
      50      61
      51      61
      52      62
      53      62
      54      62
      55      62
      56      63
      57      63
      58      63
      59      63
      5a      63
      5b      63
      5c      63
      5d      61
      5e      61
      5f      61
      76      68
      77      69
      78      71
   }
}

Lines mapping:
8 <-> 8
9 <-> 9
10 <-> 10
11 <-> 12
13 <-> 14
15 <-> 17
16 <-> 20
17 <-> 31
18 <-> 33
19 <-> 36
20 <-> 38
21 <-> 39
22 <-> 40
28 <-> 45
29 <-> 46
31 <-> 48
34 <-> 51
35 <-> 52
36 <-> 53
37 <-> 54
38 <-> 56
40 <-> 58
42 <-> 59
43 <-> 62
44 <-> 63
45 <-> 64
51 <-> 69
52 <-> 70
54 <-> 72
Not mapped:
26
49
