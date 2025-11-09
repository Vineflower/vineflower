package pkg;

public class TestRecordPatterns7 {
   public void test(Object o) {
      if (o instanceof TestRecordPatterns7.A(TestRecordPatterns7.C(TestRecordPatterns7.B(int i1, int i2), int i3), int i4, TestRecordPatterns7.B(int i5, int i6))// 5
         )
       {
         System.out.println(i1 + i2 + i3 + i4 + i5 + i6);// 6
      }
   }// 8

   public void test2(Object o) {
      System.out
         .println(// 11
            switch (o) {
               case TestRecordPatterns7.A(TestRecordPatterns7.C(TestRecordPatterns7.B(int i1, int i2), int i3), int i4, TestRecordPatterns7.B(int i5, int i6)) -> i1// 12
                  + i2
                  + i3
                  + i4
                  + i5
                  + i6;
               default -> 0;// 13
            }
         );
   }

   record A(TestRecordPatterns7.C c, int i, TestRecordPatterns7.B b2) {
   }

   record B(int i1, int i2) {
   }

   record C(TestRecordPatterns7.B b, int i) {
   }
}

class 'pkg/TestRecordPatterns7' {
   method 'test (Ljava/lang/Object;)V' {
      0      4
      1      4
      2      4
      3      4
      4      4
      5      4
      6      4
      25      4
      26      4
      53      4
      54      4
      5e      4
      5f      4
      69      4
      6a      4
      74      4
      75      4
      7f      4
      80      4
      81      7
      82      7
      83      7
      84      7
      85      7
      86      7
      87      7
      88      7
      89      7
      8a      7
      8b      7
      8c      7
      8d      7
      8e      7
      8f      7
      90      7
      91      7
      92      7
      93      7
      94      7
      95      7
      96      7
      97      7
      a9      9
   }

   method 'test2 (Ljava/lang/Object;)V' {
      0      12
      1      12
      2      12
      3      14
      13      14
      14      14
      15      14
      16      14
      17      14
      18      14
      19      14
      1a      14
      1b      14
      1c      14
      1d      14
      1e      14
      1f      14
      20      14
      21      14
      22      14
      23      14
      46      15
      47      15
      77      15
      78      15
      82      15
      83      15
      8d      15
      8e      15
      98      15
      99      15
      a3      15
      a4      15
      a5      15
      a6      15
      ad      16
      ae      16
      af      15
      b1      17
      b2      15
      b4      18
      b5      15
      b7      19
      b8      15
      ba      20
      bb      15
      c1      21
      c5      13
      c6      13
      c7      13
      c8      24
   }
}

Lines mapping:
5 <-> 5
6 <-> 8
8 <-> 10
11 <-> 14
12 <-> 16
13 <-> 22
Not mapped:
15
