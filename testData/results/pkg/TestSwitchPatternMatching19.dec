package pkg;

public class TestSwitchPatternMatching19 {
   static void test(TestSwitchPatternMatching19.XXX s) {
      switch (s) {// 5
         case X1:
            System.out.println("x1");// 6
            break;
         case TestSwitchPatternMatching19.A a when a != TestSwitchPatternMatching19.XXX.X1:// 7
            System.out.println("a");
            break;
         case TestSwitchPatternMatching19.B b when b instanceof TestSwitchPatternMatching19.C:// 8
            System.out.println("b");
            break;
         case TestSwitchPatternMatching19.D d when d == d:// 9
            System.out.println("d");
            break;
         case TestSwitchPatternMatching19.XXX var8 when false:
            break;
         case null:
         default:
            System.out.println("f");// 11
      }
   }// 13

   static void test2(TestSwitchPatternMatching19.XXX s) {
      switch (s) {// 16
         case X2:
         case null:
            System.out.println("maybe x2?");// 18
            break;
         case X1:
            System.out.println("x1");// 17
            break;
         case TestSwitchPatternMatching19.XXX var4 when Math.random() > 0.0:// 19
            System.out.println("just making sure!");// 20
            break;
         default:
            System.out.println("default");// 21
      }
   }// 23

   interface A {
   }

   interface B {
   }

   interface C {
   }

   interface D {
   }

   interface E {
   }

   interface F {
   }

   enum XXX
      implements TestSwitchPatternMatching19.A,
      TestSwitchPatternMatching19.B,
      TestSwitchPatternMatching19.C,
      TestSwitchPatternMatching19.D,
      TestSwitchPatternMatching19.E,
      TestSwitchPatternMatching19.F {
      X1,
      X2,
      X3;
   }
}

class 'pkg/TestSwitchPatternMatching19' {
   method 'test (Lpkg/TestSwitchPatternMatching19$XXX;)V' {
      0      4
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
      20      4
      21      4
      22      4
      23      4
      24      4
      25      4
      26      4
      27      4
      28      4
      29      4
      2a      4
      2b      4
      2c      4
      2d      4
      2e      4
      2f      4
      30      6
      31      6
      32      6
      33      6
      34      6
      35      6
      36      6
      37      6
      38      7
      3c      8
      3d      8
      3e      8
      3f      8
      40      8
      41      8
      42      8
      43      8
      49      9
      4a      9
      4b      9
      4c      9
      4d      9
      4e      9
      4f      9
      50      9
      51      10
      55      11
      56      11
      57      11
      58      11
      59      11
      5a      11
      5b      11
      64      12
      65      12
      66      12
      67      12
      68      12
      69      12
      6a      12
      6b      12
      6c      13
      70      14
      71      14
      72      14
      73      14
      74      14
      75      14
      76      14
      77      14
      78      14
      7e      15
      7f      15
      80      15
      81      15
      82      15
      83      15
      84      15
      85      15
      86      16
      94      21
      95      21
      96      21
      97      21
      98      21
      99      21
      9a      21
      9b      21
      9f      23
   }

   method 'test2 (Lpkg/TestSwitchPatternMatching19$XXX;)V' {
      0      26
      b      26
      c      26
      d      26
      e      26
      f      26
      10      26
      11      26
      12      26
      13      26
      14      26
      15      26
      16      26
      17      26
      18      26
      19      26
      1a      26
      1b      26
      1c      26
      1d      26
      1e      26
      1f      26
      20      26
      21      26
      22      26
      23      26
      24      26
      25      26
      26      26
      27      26
      28      32
      29      32
      2a      32
      2b      32
      2c      32
      2d      32
      2e      32
      2f      32
      30      33
      33      29
      34      29
      35      29
      36      29
      37      29
      38      29
      39      29
      3a      29
      3b      30
      40      34
      41      34
      42      34
      43      34
      44      34
      4d      35
      4e      35
      4f      35
      50      35
      51      35
      52      35
      53      35
      54      35
      55      36
      58      38
      59      38
      5a      38
      5b      38
      5c      38
      5d      38
      5e      38
      5f      38
      63      40
   }
}

Lines mapping:
5 <-> 5
6 <-> 7
7 <-> 9
8 <-> 12
9 <-> 15
11 <-> 22
13 <-> 24
16 <-> 27
17 <-> 33
18 <-> 30
19 <-> 35
20 <-> 36
21 <-> 39
23 <-> 41
Not mapped:
10
