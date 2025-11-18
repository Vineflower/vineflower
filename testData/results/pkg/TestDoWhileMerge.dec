package pkg;

public class TestDoWhileMerge {
   public void test(boolean b, int j) {
      for (int i = 0; i < j; i++) {// 6
         do {
            System.out.println(i);// 8
            if (++i == 30) {// 9 11
               return;// 12
            }
         } while (b && i < 40);// 14

         System.out.println("test");// 16
      }

      System.out.println("after");// 19
   }// 20

   public void test1(boolean b, int j) {
      for (int i = 0; i < j; i++) {// 23
         System.out.println(1);// 24

         do {
            System.out.println(i);// 27
            if (++i == 30) {// 28 30
               return;// 31
            }
         } while (b && i < 40);// 33
      }

      System.out.println("after");// 36
   }// 37

   public void test2(boolean b, int j) {
      for (int i = 0; i < j; i++) {// 41
         System.out.println(1);// 42

         while (true) {
            System.out.println(i);// 45
            if (++i == 30) {// 46
               return;// 47
            }

            if (!b || i >= 40) {// 50
               break;
            }

            System.out.println(j);// 54
         }
      }
   }// 57

   public void test3() {
      while (1.0 < Math.random()) {// 60
         System.out.println();// 61

         do {
            System.out.println();// 63
         } while (1.0 < Math.random());// 64
      }
   }// 66

   public void test4() {
      while (1.0 > Math.random()) {// 69
         do {
            System.out.println();// 71
            System.out.println();// 72
         } while (1.0 < Math.random());// 73
      }
   }// 75
}

class 'pkg/TestDoWhileMerge' {
   method 'test (ZI)V' {
      0      4
      1      4
      2      4
      3      4
      4      4
      5      4
      6      4
      7      6
      8      6
      9      6
      a      6
      b      6
      c      6
      d      6
      e      7
      f      7
      10      7
      11      7
      12      7
      13      7
      14      7
      15      7
      16      7
      17      8
      18      10
      1c      10
      1d      10
      1e      10
      1f      10
      20      10
      21      10
      22      12
      23      12
      24      12
      25      12
      26      12
      27      12
      28      12
      29      12
      2a      4
      2b      4
      2c      4
      30      15
      31      15
      32      15
      33      15
      34      15
      35      15
      36      15
      37      15
      38      16
   }

   method 'test1 (ZI)V' {
      0      19
      1      19
      2      19
      3      19
      4      19
      5      19
      6      19
      7      20
      8      20
      9      20
      a      20
      b      20
      c      20
      d      20
      e      23
      f      23
      10      23
      11      23
      12      23
      13      23
      14      23
      15      24
      16      24
      17      24
      18      24
      19      24
      1a      24
      1b      24
      1c      24
      1d      24
      1e      25
      1f      27
      23      27
      24      27
      25      27
      26      27
      27      27
      28      27
      29      19
      2a      19
      2b      19
      2f      30
      30      30
      31      30
      32      30
      33      30
      34      30
      35      30
      36      30
      37      31
   }

   method 'test2 (ZI)V' {
      0      34
      1      34
      2      34
      3      34
      4      34
      5      34
      6      34
      7      35
      8      35
      9      35
      a      35
      b      35
      c      35
      d      35
      e      38
      f      38
      10      38
      11      38
      12      38
      13      38
      14      38
      15      39
      16      39
      17      39
      18      39
      19      39
      1a      39
      1b      39
      1c      39
      1d      39
      1e      40
      1f      43
      20      43
      21      43
      22      43
      23      43
      24      43
      25      43
      26      43
      27      43
      28      43
      2c      47
      2d      47
      2e      47
      2f      47
      30      47
      31      47
      32      47
      36      34
      37      34
      38      34
      3c      50
   }

   method 'test3 ()V' {
      0      53
      1      53
      2      53
      3      53
      4      53
      5      53
      6      53
      7      53
      8      54
      9      54
      a      54
      b      54
      c      54
      d      54
      e      57
      f      57
      10      57
      11      57
      12      57
      13      57
      14      58
      15      58
      16      58
      17      58
      18      58
      1f      60
   }

   method 'test4 ()V' {
      0      63
      1      63
      2      63
      3      63
      4      63
      5      63
      6      63
      7      63
      8      65
      9      65
      a      65
      b      65
      c      65
      d      65
      e      66
      f      66
      10      66
      11      66
      12      66
      13      66
      14      67
      15      67
      16      67
      17      67
      18      67
      1f      69
   }
}

Lines mapping:
6 <-> 5
8 <-> 7
9 <-> 8
11 <-> 8
12 <-> 9
14 <-> 11
16 <-> 13
19 <-> 16
20 <-> 17
23 <-> 20
24 <-> 21
27 <-> 24
28 <-> 25
30 <-> 25
31 <-> 26
33 <-> 28
36 <-> 31
37 <-> 32
41 <-> 35
42 <-> 36
45 <-> 39
46 <-> 40
47 <-> 41
50 <-> 44
54 <-> 48
57 <-> 51
60 <-> 54
61 <-> 55
63 <-> 58
64 <-> 59
66 <-> 61
69 <-> 64
71 <-> 66
72 <-> 67
73 <-> 68
75 <-> 70
Not mapped:
51
