package pkg;

import java.io.IOException;

public class TestDefiniteAssignment {
   void testExample16$1$$1(int v) throws IOException {
      int k;
      if (v > 0 && (k = System.in.read()) >= 0) {// 8
         System.out.println(k);// 9
      }
   }// 10

   void testExample16$1$$2(int n) {
      while (true) {
         int k = n;// 16
         if (k >= 5) {// 17
            System.out.println(k);// 20
            return;// 22
         }

         n = 6;// 18
      }
   }

   void testExample16$1$$3modified(int n, int m) {
      int k;
      while (true) {
         if (n >= 4) {// 26
            k = m;
            if (m >= 5) {
               break;
            }
         }

         k = n;// 27
         if (k >= 5) {// 28
            break;
         }

         n = 6;// 29
      }

      System.out.println(k);// 31
   }// 32

   void testAssignments(int n, boolean bool) {
      if (bool) {// 36
         int a = n;
         if (n > 0 || (a = -n) > 100) {
            System.out.println(a);// 37
         }
      }

      label43: {
         if (!bool) {// 41
            int b = n;
            if ((b = n * b) <= 0) {
               System.out.println(b);// 44
               break label43;
            }
         }

         System.out.println("b");// 42
      }

      double cFake = 0.01;// 48
      System.out.println(cFake);// 49
      if (n < 1.0 - n && (cFake = n + 5) > cFake * cFake - cFake / 2.0 ? !(n > cFake) : !(n < 5.0 - (cFake = n))) {// 53
         cFake += 5.0;// 57
      } else {
         System.out.println(cFake);// 54
         cFake += 2.0;// 55
      }

      System.out.println(cFake);// 59
      double d;
      if ((d = n) > 0.0) {// 63
         System.out.println(d);// 64
      }
   }// 67

   void testBooleanNormalness(int n) {
      int p = n * 2;// 71
      int k;
      if (n < 5 && (k = n + 5) > 0 && (p /= k) != 0) {// 72
         System.out.println("hi");// 73
      }

      p += 8;// 75
      System.out.println(p);// 76
   }// 77

   void testBooleanNormalnessInline(int n) {
      int p = n * 2;// 81
      int k;
      Boolean.valueOf(n < 5 && (k = n + 5) > 0 && (p /= k) != 0);// 82
      p += 8;// 83
      System.out.println(p);// 84
   }// 85

   void nestedTernaries(int a, int b, int c) {
      label47: {
         int x;
         if (a > 0) {// 89
            if (c >= b) {
               break label47;
            }

            x = b;
            if (b <= 0) {
               break label47;
            }
         } else {
            x = c;
            if (c >= 0 && c != b) {
               break label47;
            }
         }

         System.out.println(x);// 90
      }

      int y;
      label38: {
         int z;
         label37: {
            label50: {
               if (a > 0) {// 94
                  if (c >= b) {
                     break label50;
                  }

                  y = b;
                  if (b <= 0) {
                     break label50;
                  }
               } else {
                  y = c;
                  if (c >= 0 && c != b) {
                     break label50;
                  }
               }

               if (1 <= b - c || (z = b - c) == a) {
                  break label38;
               }
               break label37;
            }

            y = 5;
            z = a;
            if (5 == a) {
               break label38;
            }
         }

         System.out.println(z);// 97
      }

      System.out.println(y);// 99
   }// 100
}

class 'pkg/TestDefiniteAssignment' {
   method 'testExample16$1$$1 (I)V' {
      0      7
      1      7
      2      7
      3      7
      4      7
      5      7
      6      7
      7      7
      8      7
      9      7
      b      7
      c      7
      d      7
      e      7
      f      8
      10      8
      11      8
      12      8
      13      8
      14      8
      15      8
      16      10
   }

   method 'testExample16$1$$2 (I)V' {
      0      14
      1      14
      2      15
      3      15
      4      15
      5      15
      6      15
      a      20
      b      20
      c      20
      10      16
      11      16
      12      16
      13      16
      14      16
      15      16
      16      16
      17      17
   }

   method 'testExample16$1$$3modified (II)V' {
      0      27
      1      27
      2      27
      3      27
      4      27
      5      28
      7      28
      8      29
      9      29
      a      29
      b      29
      c      34
      d      34
      e      35
      f      35
      10      35
      11      35
      12      35
      16      39
      17      39
      18      39
      1c      42
      1d      42
      1e      42
      1f      42
      20      42
      21      42
      22      42
      23      43
   }

   method 'testAssignments (IZ)V' {
      0      46
      1      46
      2      46
      3      46
      4      47
      6      47
      7      48
      8      48
      9      48
      a      48
      b      48
      d      48
      e      48
      f      48
      10      48
      11      48
      12      48
      13      49
      14      49
      15      49
      16      49
      17      49
      18      49
      19      49
      1a      54
      1b      54
      1c      54
      1d      54
      1e      55
      20      55
      21      55
      22      56
      23      56
      24      56
      26      56
      27      56
      28      56
      29      56
      2a      56
      2b      62
      2c      62
      2d      62
      2e      62
      2f      62
      30      62
      31      62
      32      62
      36      57
      37      57
      38      57
      39      57
      3a      57
      3b      57
      3c      57
      3d      57
      3e      65
      3f      65
      40      65
      41      65
      42      65
      43      66
      44      66
      45      66
      46      66
      47      66
      48      66
      49      66
      4a      66
      4b      67
      4c      67
      4d      67
      4e      67
      4f      67
      50      67
      51      67
      52      67
      53      67
      54      67
      55      67
      56      67
      57      67
      58      67
      5a      67
      5b      67
      5c      67
      5d      67
      5e      67
      5f      67
      60      67
      61      67
      62      67
      63      67
      64      67
      65      67
      66      67
      67      67
      68      67
      69      67
      6a      67
      6b      67
      6c      67
      6d      67
      6e      67
      6f      67
      70      67
      71      67
      72      67
      74      67
      75      67
      76      67
      77      67
      78      67
      79      67
      7a      67
      7e      67
      7f      67
      80      67
      81      67
      82      67
      83      67
      84      67
      85      67
      86      70
      87      70
      88      70
      89      70
      8a      70
      8b      70
      8c      70
      8d      70
      90      71
      91      71
      92      71
      94      71
      95      71
      9b      68
      9c      68
      9d      68
      9f      68
      a0      68
      a1      74
      a2      74
      a3      74
      a4      74
      a5      74
      a6      74
      a7      74
      a8      74
      a9      76
      aa      76
      ac      76
      ad      76
      ae      76
      af      76
      b0      76
      b1      76
      b2      76
      bb      76
      bc      76
      bd      76
      be      77
      bf      77
      c0      77
      c1      77
      c2      77
      c3      77
      c4      77
      c5      77
      c6      79
   }

   method 'testBooleanNormalness (I)V' {
      0      82
      1      82
      2      82
      3      82
      4      84
      5      84
      6      84
      7      84
      8      84
      9      84
      a      84
      b      84
      d      84
      e      84
      f      84
      10      84
      12      84
      15      84
      16      84
      17      84
      18      84
      19      85
      1a      85
      1b      85
      1c      85
      1d      85
      1e      85
      1f      85
      20      85
      21      88
      22      88
      23      88
      24      89
      25      89
      26      89
      27      89
      28      89
      29      89
      2a      89
      2b      90
   }

   method 'testBooleanNormalnessInline (I)V' {
      0      93
      1      93
      2      93
      3      93
      4      95
      5      95
      6      95
      7      95
      8      95
      9      95
      a      95
      b      95
      d      95
      e      95
      f      95
      10      95
      12      95
      15      95
      16      95
      17      95
      18      95
      1e      95
      1f      95
      20      95
      22      96
      23      96
      24      96
      25      97
      26      97
      27      97
      28      97
      29      97
      2a      97
      2b      97
      2c      98
   }

   method 'nestedTernaries (III)V' {
      0      103
      1      103
      2      103
      3      103
      4      104
      5      104
      6      104
      7      104
      8      104
      9      108
      b      108
      c      108
      d      109
      e      109
      f      109
      13      113
      15      113
      16      113
      17      114
      18      114
      19      114
      1a      114
      1b      114
      1c      114
      1d      114
      1e      114
      1f      119
      20      119
      21      119
      22      119
      23      119
      24      119
      25      119
      26      119
      27      127
      28      127
      29      127
      2a      127
      2b      128
      2c      128
      2d      128
      2e      128
      2f      128
      30      132
      32      132
      33      132
      34      133
      35      133
      36      133
      3a      137
      3c      137
      3d      137
      3e      138
      3f      138
      40      138
      41      138
      42      138
      43      138
      44      138
      45      138
      46      143
      47      143
      48      143
      49      143
      4a      143
      4b      143
      4c      143
      4d      143
      4e      143
      4f      143
      51      143
      52      143
      53      143
      54      143
      55      143
      56      143
      5a      149
      5c      149
      5d      149
      5e      150
      60      150
      61      150
      62      151
      63      151
      64      151
      65      156
      66      156
      67      156
      68      156
      69      156
      6a      156
      6b      156
      6c      156
      6d      159
      6e      159
      6f      159
      70      159
      71      159
      72      159
      73      159
      74      159
      75      160
   }
}

Lines mapping:
8 <-> 8
9 <-> 9
10 <-> 11
16 <-> 15
17 <-> 16
18 <-> 21
20 <-> 17
22 <-> 18
26 <-> 28
27 <-> 35
28 <-> 36
29 <-> 40
31 <-> 43
32 <-> 44
36 <-> 47
37 <-> 50
41 <-> 55
42 <-> 63
44 <-> 58
48 <-> 66
49 <-> 67
53 <-> 68
54 <-> 71
55 <-> 72
57 <-> 69
59 <-> 75
63 <-> 77
64 <-> 78
67 <-> 80
71 <-> 83
72 <-> 85
73 <-> 86
75 <-> 89
76 <-> 90
77 <-> 91
81 <-> 94
82 <-> 96
83 <-> 97
84 <-> 98
85 <-> 99
89 <-> 104
90 <-> 120
94 <-> 128
97 <-> 157
99 <-> 160
100 <-> 161
