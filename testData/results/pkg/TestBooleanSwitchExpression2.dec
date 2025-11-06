package pkg;

public class TestBooleanSwitchExpression2 {
   public void test(String x, int y) {
      switch (x) {// 5
         case "a":
            if (y >= 0) {// 6
               return;// 12
            }
            break;
         case "b":
            if (y <= 0) {// 7
               return;
            }
            break;
         default:
            if (y != 0) {// 8
               return;
            }
      }

      System.out.println("Nice");// 10
   }

   public void test1(String x, int y) {
      String var3 = x;// 15
      byte var4 = -1;
      switch (var3.hashCode()) {
         case 97:
            if (var3.equals("a")) {
               var4 = 0;
            }
            break;
         case 98:
            if (var3.equals("b")) {
               var4 = 1;
            }
      }

      label30: {
         label29: {
            switch (var4) {
               case 0:
                  if (y < 0) {// 16
                     break label29;
                  }
                  break;
               case 1:
                  if (y > 0) {// 17
                     break label29;
                  }
                  break;
               default:
                  if (y == 0) {// 18
                     break label29;
                  }
            }

            System.out.println("Sad");// 22
            break label30;
         }

         System.out.println("Nice");// 20
      }

      System.out.println("Done");// 25
   }// 26
}

class 'pkg/TestBooleanSwitchExpression2' {
   method 'test (Ljava/lang/String;I)V' {
      0      4
      25      5
      26      5
      34      10
      35      10
      41      4
      42      4
      43      4
      44      4
      45      4
      46      4
      47      4
      48      4
      49      4
      4a      4
      4b      4
      4c      4
      4d      4
      4e      4
      4f      4
      50      4
      51      4
      52      4
      53      4
      54      4
      55      4
      56      4
      57      4
      58      4
      59      4
      5a      4
      5b      4
      5c      6
      5d      6
      5e      6
      5f      6
      63      11
      64      11
      65      11
      66      11
      6a      16
      6b      16
      6c      16
      6d      16
      71      21
      72      21
      73      21
      74      21
      75      21
      76      21
      77      21
      78      21
      79      7
   }

   method 'test1 (Ljava/lang/String;I)V' {
      0      25
      1      25
      2      26
      3      26
      4      26
      5      27
      6      27
      7      27
      8      27
      9      27
      a      27
      b      27
      c      27
      d      27
      e      27
      f      27
      10      27
      11      27
      12      27
      13      27
      14      27
      15      27
      16      27
      17      27
      18      27
      19      27
      1a      27
      1b      27
      1c      27
      1d      27
      1e      27
      1f      27
      20      27
      21      27
      22      27
      23      27
      24      29
      25      29
      26      29
      27      29
      28      29
      29      29
      2a      29
      2b      29
      2c      29
      2d      30
      2e      30
      2f      30
      33      34
      34      34
      35      34
      36      34
      37      34
      38      34
      39      34
      3a      34
      3b      34
      3c      35
      3d      35
      3e      35
      3f      41
      40      41
      41      41
      42      41
      43      41
      44      41
      45      41
      46      41
      47      41
      48      41
      49      41
      4a      41
      4b      41
      4c      41
      4d      41
      4e      41
      4f      41
      50      41
      51      41
      52      41
      53      41
      54      41
      55      41
      56      41
      57      41
      58      41
      59      41
      5a      41
      5b      41
      5c      43
      5d      43
      5e      43
      5f      43
      63      48
      64      48
      65      48
      66      48
      6a      53
      6b      53
      6c      53
      6d      53
      71      62
      72      62
      73      62
      74      62
      75      62
      76      62
      77      62
      78      62
      7c      58
      7d      58
      7e      58
      7f      58
      80      58
      81      58
      82      58
      83      58
      84      65
      85      65
      86      65
      87      65
      88      65
      89      65
      8a      65
      8b      65
      8c      66
   }
}

Lines mapping:
5 <-> 5
6 <-> 7
7 <-> 12
8 <-> 17
10 <-> 22
12 <-> 8
15 <-> 26
16 <-> 44
17 <-> 49
18 <-> 54
20 <-> 63
22 <-> 59
25 <-> 66
26 <-> 67
