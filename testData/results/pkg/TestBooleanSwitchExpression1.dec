package pkg;

public class TestBooleanSwitchExpression1 {
   public void test(int x, int y) {
      switch (x) {// 5
         case 1:
            if (y >= 0) {// 6
               return;// 12
            }
            break;
         case 2:
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

   public void test1(int x, int y) {
      label20: {
         label19: {
            switch (x) {// 15
               case 1:
                  if (y < 0) {// 16
                     break label19;
                  }
                  break;
               case 2:
                  if (y > 0) {// 17
                     break label19;
                  }
                  break;
               default:
                  if (y == 0) {// 18
                     break label19;
                  }
            }

            System.out.println("Sad");// 22
            break label20;
         }

         System.out.println("Nice");// 20
      }

      System.out.println("Done");// 25
   }// 26
}

class 'pkg/TestBooleanSwitchExpression1' {
   method 'test (II)V' {
      0      4
      1      4
      2      4
      3      4
      4      4
      5      4
      6      4
      7      4
      8      4
      9      4
      a      4
      b      4
      c      4
      d      4
      e      4
      f      4
      10      4
      11      4
      12      4
      13      4
      14      4
      15      4
      16      4
      17      4
      18      4
      19      4
      1a      4
      1b      4
      1c      6
      1d      6
      1e      6
      1f      6
      23      11
      24      11
      25      11
      26      11
      2a      16
      2b      16
      2c      16
      2d      16
      2e      21
      2f      21
      30      21
      31      21
      32      21
      33      21
      34      21
      35      21
      36      7
   }

   method 'test1 (II)V' {
      0      27
      1      27
      2      27
      3      27
      4      27
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
      1c      29
      1d      29
      1e      29
      1f      29
      23      34
      24      34
      25      34
      26      34
      2a      39
      2b      39
      2c      39
      2d      39
      2e      48
      2f      48
      30      48
      31      48
      32      48
      33      48
      34      48
      35      48
      39      44
      3a      44
      3b      44
      3c      44
      3d      44
      3e      44
      3f      44
      40      44
      41      51
      42      51
      43      51
      44      51
      45      51
      46      51
      47      51
      48      51
      49      52
   }
}

Lines mapping:
5 <-> 5
6 <-> 7
7 <-> 12
8 <-> 17
10 <-> 22
12 <-> 8
15 <-> 28
16 <-> 30
17 <-> 35
18 <-> 40
20 <-> 49
22 <-> 45
25 <-> 52
26 <-> 53
