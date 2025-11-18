package pkg;

public class TestRecordPatterns5 {
   public void test1(TestRecordPatterns5.R r) {
      if (r instanceof TestRecordPatterns5.R(Integer x, String s)) {// 7
         System.out.println(x);// 8
         System.out.println(s);// 9
      }
   }// 11

   public void test2(TestRecordPatterns5.R r) {
      if (r instanceof TestRecordPatterns5.R(Integer x, String s) && s.length() > 10) {// 14
         System.out.println(x);// 15
         System.out.println(s);// 16
      }
   }// 18

   public void test3(TestRecordPatterns5.R r) {
      if (r instanceof TestRecordPatterns5.R(Integer x, String s) && s.length() > 10 && x == 3) {// 21
         System.out.println(x);// 22
         System.out.println(s);// 23
      }
   }// 25

   record R(Number a, String b) {
   }
}

class 'pkg/TestRecordPatterns5' {
   method 'test1 (Lpkg/TestRecordPatterns5$R;)V' {
      0      4
      1      4
      2      4
      3      4
      4      4
      5      4
      6      4
      1c      4
      25      4
      26      4
      27      5
      28      5
      29      5
      2a      5
      2b      5
      2c      5
      2d      5
      2e      6
      2f      6
      30      6
      31      6
      32      6
      33      6
      34      6
      35      6
      47      8
   }

   method 'test2 (Lpkg/TestRecordPatterns5$R;)V' {
      0      11
      1      11
      2      11
      3      11
      4      11
      5      11
      6      11
      1c      11
      25      11
      26      11
      27      11
      28      11
      29      11
      2a      11
      2b      11
      2c      11
      2d      11
      2e      11
      2f      11
      30      11
      31      12
      32      12
      33      12
      34      12
      35      12
      36      12
      37      12
      38      13
      39      13
      3a      13
      3b      13
      3c      13
      3d      13
      3e      13
      3f      13
      51      15
   }

   method 'test3 (Lpkg/TestRecordPatterns5$R;)V' {
      0      18
      1      18
      2      18
      3      18
      4      18
      5      18
      6      18
      1c      18
      25      18
      26      18
      27      18
      28      18
      29      18
      2a      18
      2b      18
      2c      18
      2d      18
      2e      18
      2f      18
      30      18
      31      18
      32      18
      33      18
      34      18
      35      18
      36      18
      37      18
      38      18
      39      19
      3a      19
      3b      19
      3c      19
      3d      19
      3e      19
      3f      19
      40      20
      41      20
      42      20
      43      20
      44      20
      45      20
      46      20
      47      20
      59      22
   }
}

Lines mapping:
7 <-> 5
8 <-> 6
9 <-> 7
11 <-> 9
14 <-> 12
15 <-> 13
16 <-> 14
18 <-> 16
21 <-> 19
22 <-> 20
23 <-> 21
25 <-> 23
