package pkg;

public class TestDuplicateSwitchLocals {
   void test(int foo) {
      switch (foo) {// 5
         case 0:
            int x = 10;// 7

            for (int i = 0; i < x; i++) {// 8
               System.out.println(i);
            }
            break;
         case 1:
            int y = 11;// 11

            for (int i = 0; i < y; i++) {// 12
               System.out.println(i);
            }
            break;
         case 2: {
            int z = 2;// 15
            System.out.println(z);// 16
         }
         case 3: {
            int z = 3;// 19
            System.out.println(z);// 20
         }
      }
   }// 23
}

class 'pkg/TestDuplicateSwitchLocals' {
   method 'test (I)V' {
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
      1c      4
      1d      4
      1e      4
      1f      4
      20      6
      21      6
      22      6
      23      8
      24      8
      25      8
      26      8
      27      8
      28      8
      29      8
      2a      9
      2b      9
      2c      9
      2d      9
      2e      9
      2f      9
      30      9
      31      8
      32      8
      33      8
      3a      13
      3b      13
      3c      13
      3d      15
      3e      15
      3f      15
      40      15
      41      15
      42      15
      43      15
      44      15
      45      15
      46      16
      47      16
      48      16
      49      16
      4a      16
      4b      16
      4c      16
      4d      16
      4e      15
      4f      15
      50      15
      57      20
      58      20
      59      20
      5a      21
      5b      21
      5c      21
      5d      21
      5e      21
      5f      21
      60      21
      61      21
      62      24
      63      24
      64      24
      65      25
      66      25
      67      25
      68      25
      69      25
      6a      25
      6b      25
      6c      25
      6d      28
   }
}

Lines mapping:
5 <-> 5
7 <-> 7
8 <-> 9
11 <-> 14
12 <-> 16
15 <-> 21
16 <-> 22
19 <-> 25
20 <-> 26
23 <-> 29
Not mapped:
9
13
