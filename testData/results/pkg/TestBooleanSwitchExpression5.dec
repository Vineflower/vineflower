package pkg;

public class TestBooleanSwitchExpression5 {
   public void test(int x, int y, int z) {
      int a;
      switch (x) {// 6
         case 0:
            if (y >= 0) {// 7
               return;// 14
            }

            a = 5;
            if (5 >= z) {
               return;
            }
            break;
         case 1:
            if ((y > 0 ? (a = 17) : (a = -1)) >= z) {// 8
               return;
            }
            break;
         case 2:
            if (y > 0) {// 9
               a = 17;
               if (17 >= z && 7 <= z) {
                  return;
               }
            } else {
               if (3 >= z) {
                  return;
               }

               a = -1;
               if (-1 != a) {
                  return;
               }
            }
            break;
         default:
            a = y;// 10
            if (y != 0) {
               return;
            }
      }

      System.out.println(a);// 12
   }

   public void test1(int x, int y, int z) {
      label40: {
         int a;
         label39: {
            switch (x) {// 18
               case 0:
                  if (y < 0) {// 19
                     a = 5;
                     if (5 < z) {
                        break label39;
                     }
                  }
                  break;
               case 1:
                  if ((y > 0 ? (a = 17) : (a = -1)) < z) {// 20
                     break label39;
                  }
                  break;
               case 2:
                  if (y > 0) {// 21
                     a = 17;
                     if (17 < z || 7 > z) {
                        break label39;
                     }
                  } else if (3 < z) {
                     a = -1;
                     if (-1 == a) {
                        break label39;
                     }
                  }
                  break;
               default:
                  a = y;// 22
                  if (y == 0) {
                     break label39;
                  }
            }

            System.out.println("Sad");// 26
            break label40;
         }

         System.out.println(a);// 24
      }

      System.out.println("Done");// 29
   }// 30
}

class 'pkg/TestBooleanSwitchExpression5' {
   method 'test (III)V' {
      0      5
      1      5
      2      5
      3      5
      4      5
      5      5
      6      5
      7      5
      8      5
      9      5
      a      5
      b      5
      c      5
      d      5
      e      5
      f      5
      10      5
      11      5
      12      5
      13      5
      14      5
      15      5
      16      5
      17      5
      18      5
      19      5
      1a      5
      1b      5
      1c      7
      1d      7
      1e      7
      1f      7
      20      11
      22      11
      23      11
      24      12
      25      12
      26      12
      27      12
      2b      17
      2c      17
      2d      17
      2e      17
      2f      17
      30      17
      32      17
      33      17
      37      17
      39      17
      3a      17
      3b      17
      3c      17
      3d      17
      3e      17
      42      22
      43      22
      44      22
      45      22
      46      23
      47      23
      49      23
      4a      23
      4b      24
      4c      24
      4d      24
      4e      24
      4f      24
      50      24
      51      24
      52      24
      53      24
      54      24
      58      28
      59      28
      5a      28
      5b      28
      5c      28
      5d      32
      5f      32
      60      32
      61      33
      62      33
      63      33
      64      33
      65      33
      69      39
      6b      39
      6c      39
      6d      40
      6e      40
      6f      40
      70      45
      71      45
      72      45
      73      45
      74      45
      75      45
      76      45
      77      45
      78      8
   }

   method 'test1 (III)V' {
      0      52
      1      52
      2      52
      3      52
      4      52
      5      52
      6      52
      7      52
      8      52
      9      52
      a      52
      b      52
      c      52
      d      52
      e      52
      f      52
      10      52
      11      52
      12      52
      13      52
      14      52
      15      52
      16      52
      17      52
      18      52
      19      52
      1a      52
      1b      52
      1c      54
      1d      54
      1e      54
      1f      54
      20      55
      22      55
      23      55
      24      56
      25      56
      26      56
      27      56
      2b      62
      2c      62
      2d      62
      2e      62
      2f      62
      30      62
      32      62
      33      62
      37      62
      39      62
      3a      62
      3b      62
      3c      62
      3d      62
      3e      62
      42      67
      43      67
      44      67
      45      67
      46      68
      47      68
      49      68
      4a      68
      4b      69
      4c      69
      4d      69
      4e      69
      4f      69
      50      69
      51      69
      52      69
      53      69
      54      69
      58      72
      59      72
      5a      72
      5b      72
      5c      72
      5d      73
      5f      73
      60      73
      61      74
      62      74
      63      74
      64      74
      65      74
      69      80
      6b      80
      6c      80
      6d      81
      6e      81
      6f      81
      70      90
      71      90
      72      90
      73      90
      74      90
      75      90
      76      90
      77      90
      7b      86
      7c      86
      7d      86
      7e      86
      7f      86
      80      86
      81      86
      82      86
      83      93
      84      93
      85      93
      86      93
      87      93
      88      93
      89      93
      8a      93
      8b      94
   }
}

Lines mapping:
6 <-> 6
7 <-> 8
8 <-> 18
9 <-> 23
10 <-> 40
12 <-> 46
14 <-> 9
18 <-> 53
19 <-> 55
20 <-> 63
21 <-> 68
22 <-> 81
24 <-> 91
26 <-> 87
29 <-> 94
30 <-> 95
