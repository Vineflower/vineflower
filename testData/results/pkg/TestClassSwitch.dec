package pkg;

public class TestClassSwitch {
   public void testCaseOrder(int a) {
      switch (a) {// 22
         case 5:
            System.out.println(5);// 27
         default:
            return;// 29
         case 13:
            System.out.println(13);// 24
      }
   }// 25

   public void testFallThrough(int a, int b) {
      int x = 17;// 33
      switch (a) {// 35
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 14:
         case 15:
         case 16:
         default:
            break;
         case 13:
            System.out.println(13);// 37
         case 5:
            System.out.println(5);// 39
            x += 17;// 40
            break;// 41
         case 17:
            System.out.println(17);// 43
            if (b > 0) {// 44
               x = 5000;// 45
            }
         case 18:
         case 19:
         case 20:
            System.out.println("hi");// 50
            x += 170000;// 51
      }

      System.out.println(x);// 55
   }// 56
}

class 'pkg/TestClassSwitch' {
   method 'testCaseOrder (I)V' {
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
      1c      10
      1d      10
      1e      10
      1f      10
      20      10
      21      10
      22      10
      23      10
      24      12
      25      6
      26      6
      27      6
      28      6
      29      6
      2a      6
      2b      6
      2c      8
   }

   method 'testFallThrough (II)V' {
      0      15
      1      15
      2      15
      3      16
      4      16
      5      16
      6      16
      7      16
      8      16
      9      16
      a      16
      b      16
      c      16
      d      16
      e      16
      f      16
      10      16
      11      16
      12      16
      13      16
      14      16
      15      16
      16      16
      17      16
      18      16
      19      16
      1a      16
      1b      16
      1c      16
      1d      16
      1e      16
      1f      16
      20      16
      21      16
      22      16
      23      16
      24      16
      25      16
      26      16
      27      16
      28      16
      29      16
      2a      16
      2b      16
      2c      16
      2d      16
      2e      16
      2f      16
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
      3a      16
      3b      16
      3c      16
      3d      16
      3e      16
      3f      16
      40      16
      41      16
      42      16
      43      16
      44      16
      45      16
      46      16
      47      16
      48      16
      49      16
      4a      16
      4b      16
      4c      16
      4d      16
      4e      16
      4f      16
      50      16
      51      16
      52      16
      53      16
      54      30
      55      30
      56      30
      57      30
      58      30
      59      30
      5a      30
      5b      30
      5c      32
      5d      32
      5e      32
      5f      32
      60      32
      61      32
      62      32
      63      33
      64      33
      65      33
      66      34
      69      36
      6a      36
      6b      36
      6c      36
      6d      36
      6e      36
      6f      36
      70      36
      71      37
      72      37
      73      37
      74      37
      75      38
      76      38
      77      38
      78      38
      79      43
      7a      43
      7b      43
      7c      43
      7d      43
      7e      43
      7f      43
      80      43
      82      44
      83      44
      85      44
      86      47
      87      47
      88      47
      89      47
      8a      47
      8b      47
      8c      47
      8d      48
   }
}

Lines mapping:
22 <-> 5
24 <-> 11
25 <-> 13
27 <-> 7
29 <-> 9
33 <-> 16
35 <-> 17
37 <-> 31
39 <-> 33
40 <-> 34
41 <-> 35
43 <-> 37
44 <-> 38
45 <-> 39
50 <-> 44
51 <-> 45
55 <-> 48
56 <-> 49
