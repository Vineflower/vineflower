package pkg;

public class TestPatternMatchingFakeLoops {
   void test1(Object o) {
      while (o instanceof String) {// 5
         String s = (String)o;// 6
         System.out.println(s.length());// 7
      }
   }// 9

   void test2(Object o) {
      while (o instanceof String x) {// 12
         String s = (String)o;// 13
         System.out.println(s);// 14
      }
   }// 16

   void test3(Object o) {
      while (o.hashCode() < 0 || o instanceof String) {// 19
         String s = (String)o;// 20
         System.out.println(s);// 21
      }
   }// 23

   void test3B(Object o) {
      while (o.hashCode() < 0 || o instanceof String) {// 26
         String s = (String)o;// 27
         System.out.println(s);// 28
      }

      System.out.println("bye");// 31
   }// 32

   void test4(Object o) {
      while (o.hashCode() < 0 || o instanceof String var2) {// 35
         String s = (String)o;// 36
         System.out.println(s);// 37
      }
   }// 39

   void test4x(Object o) {
      while (o.hashCode() < 0 || o instanceof String x && !x.isEmpty()) {// 42
         String s = (String)o;// 43
         System.out.println(s);// 44
      }
   }// 46

   void test4B(Object o) {
      while (o.hashCode() < 0 || o instanceof String var2) {// 49
         String s = (String)o;// 50
         System.out.println(s);// 51
      }

      System.out.println("bye");// 53
   }// 54

   void test4xB(Object o) {
      while (o.hashCode() < 0 || o instanceof String x && !x.isEmpty()) {// 57
         String s = (String)o;// 58
         System.out.println(s);// 59
      }

      System.out.println("bye");// 61
   }// 62

   void test3Swap(Object o) {
      while (o instanceof String || o.hashCode() < 0) {// 65
         String s = (String)o;// 66
         System.out.println(s);// 67
      }
   }// 69

   void test3BSwap(Object o) {
      while (o instanceof String || o.hashCode() < 0) {// 72
         String s = (String)o;// 73
         System.out.println(s);// 74
      }

      System.out.println("bye");// 77
   }// 78

   void test4Swap(Object o) {
      while (o instanceof String x || o.hashCode() < 0) {// 81
         String s = (String)o;// 82
         System.out.println(s);// 83
      }
   }// 85

   void test4xSwap(Object o) {
      while (o instanceof String x && !x.isEmpty() || o.hashCode() < 0) {// 88
         String s = (String)o;// 89
         System.out.println(s);// 90
      }
   }// 92

   void test4BSwap(Object o) {
      while (o instanceof String x || o.hashCode() < 0) {// 95
         String s = (String)o;// 96
         System.out.println(s);// 97
      }

      System.out.println("bye");// 99
   }// 100

   void test4xBSwap(Object o) {
      while (o instanceof String x && !x.isEmpty() || o.hashCode() < 0) {// 103
         String s = (String)o;// 104
         System.out.println(s);// 105
      }

      System.out.println("bye");// 107
   }// 108

   public void testSet(Object obj) {
      String s = "Hi";// 112

      while (obj instanceof String) {// 113
         s = (String)obj;// 114
      }

      System.out.println(s);// 117
   }// 118

   public void testModifyLeak(Object obj) {
      Integer i = null;// 121

      while (obj instanceof Integer) {// 122
         i = (Integer)obj;// 123
         if (i > 4) {// 125
            i = i + 3;// 126
         }
      }

      if (i != null) {// 130
         System.out.println(i);// 131
      }
   }// 133
}

class 'pkg/TestPatternMatchingFakeLoops' {
   method 'test1 (Ljava/lang/Object;)V' {
      0      4
      1      4
      2      4
      3      4
      7      5
      8      5
      9      5
      a      5
      b      5
      c      6
      d      6
      e      6
      f      6
      10      6
      11      6
      12      6
      13      6
      14      6
      15      6
      19      8
   }

   method 'test2 (Ljava/lang/Object;)V' {
      0      11
      3      11
      4      11
      5      11
      d      11
      e      12
      f      12
      10      12
      11      12
      12      12
      13      13
      14      13
      15      13
      16      13
      17      13
      18      13
      19      13
      1d      15
   }

   method 'test3 (Ljava/lang/Object;)V' {
      0      18
      1      18
      2      18
      3      18
      4      18
      5      18
      6      18
      7      18
      8      18
      9      18
      a      18
      b      18
      c      18
      d      18
      e      19
      f      19
      10      19
      11      19
      12      19
      13      20
      14      20
      15      20
      16      20
      17      20
      18      20
      19      20
      1d      22
   }

   method 'test3B (Ljava/lang/Object;)V' {
      0      25
      1      25
      2      25
      3      25
      4      25
      5      25
      6      25
      7      25
      8      25
      9      25
      a      25
      b      25
      c      25
      d      25
      e      26
      f      26
      10      26
      11      26
      12      26
      13      27
      14      27
      15      27
      16      27
      17      27
      18      27
      19      27
      1d      30
      1e      30
      1f      30
      20      30
      21      30
      22      30
      23      30
      24      30
      25      31
   }

   method 'test4 (Ljava/lang/Object;)V' {
      0      34
      1      34
      2      34
      3      34
      4      34
      5      34
      6      34
      7      34
      a      34
      b      34
      c      34
      14      34
      15      35
      16      35
      17      35
      18      35
      19      35
      1a      36
      1b      36
      1c      36
      1d      36
      1e      36
      1f      36
      20      36
      24      38
   }

   method 'test4x (Ljava/lang/Object;)V' {
      0      41
      1      41
      2      41
      3      41
      4      41
      5      41
      6      41
      7      41
      a      41
      b      41
      c      41
      d      41
      e      41
      f      41
      14      41
      15      41
      16      41
      17      41
      18      41
      19      41
      1a      41
      1b      41
      1c      42
      1d      42
      1e      42
      1f      42
      20      42
      21      43
      22      43
      23      43
      24      43
      25      43
      26      43
      27      43
      2b      45
   }

   method 'test4B (Ljava/lang/Object;)V' {
      0      48
      1      48
      2      48
      3      48
      4      48
      5      48
      6      48
      7      48
      a      48
      b      48
      c      48
      14      48
      15      49
      16      49
      17      49
      18      49
      19      49
      1a      50
      1b      50
      1c      50
      1d      50
      1e      50
      1f      50
      20      50
      24      53
      25      53
      26      53
      27      53
      28      53
      29      53
      2a      53
      2b      53
      2c      54
   }

   method 'test4xB (Ljava/lang/Object;)V' {
      0      57
      1      57
      2      57
      3      57
      4      57
      5      57
      6      57
      7      57
      a      57
      b      57
      c      57
      d      57
      e      57
      f      57
      14      57
      15      57
      16      57
      17      57
      18      57
      19      57
      1a      57
      1b      57
      1c      58
      1d      58
      1e      58
      1f      58
      20      58
      21      59
      22      59
      23      59
      24      59
      25      59
      26      59
      27      59
      2b      62
      2c      62
      2d      62
      2e      62
      2f      62
      30      62
      31      62
      32      62
      33      63
   }

   method 'test3Swap (Ljava/lang/Object;)V' {
      0      66
      1      66
      2      66
      3      66
      4      66
      5      66
      6      66
      7      66
      8      66
      9      66
      a      66
      b      66
      c      66
      d      66
      e      67
      f      67
      10      67
      11      67
      12      67
      13      68
      14      68
      15      68
      16      68
      17      68
      18      68
      19      68
      1d      70
   }

   method 'test3BSwap (Ljava/lang/Object;)V' {
      0      73
      1      73
      2      73
      3      73
      4      73
      5      73
      6      73
      7      73
      8      73
      9      73
      a      73
      b      73
      c      73
      d      73
      e      74
      f      74
      10      74
      11      74
      12      74
      13      75
      14      75
      15      75
      16      75
      17      75
      18      75
      19      75
      1d      78
      1e      78
      1f      78
      20      78
      21      78
      22      78
      23      78
      24      78
      25      79
   }

   method 'test4Swap (Ljava/lang/Object;)V' {
      0      82
      3      82
      4      82
      5      82
      6      82
      7      82
      8      82
      d      82
      11      82
      12      82
      13      82
      14      82
      15      82
      16      82
      17      82
      18      83
      19      83
      1a      83
      1b      83
      1c      83
      1d      84
      1e      84
      1f      84
      20      84
      21      84
      22      84
      23      84
      27      86
   }

   method 'test4xSwap (Ljava/lang/Object;)V' {
      0      89
      3      89
      4      89
      5      89
      6      89
      7      89
      8      89
      d      89
      e      89
      f      89
      10      89
      11      89
      12      89
      13      89
      14      89
      15      89
      16      89
      17      89
      18      89
      19      89
      1a      89
      1b      89
      1c      90
      1d      90
      1e      90
      1f      90
      20      90
      21      91
      22      91
      23      91
      24      91
      25      91
      26      91
      27      91
      2b      93
   }

   method 'test4BSwap (Ljava/lang/Object;)V' {
      0      96
      3      96
      4      96
      5      96
      6      96
      7      96
      8      96
      d      96
      11      96
      12      96
      13      96
      14      96
      15      96
      16      96
      17      96
      18      97
      19      97
      1a      97
      1b      97
      1c      97
      1d      98
      1e      98
      1f      98
      20      98
      21      98
      22      98
      23      98
      27      101
      28      101
      29      101
      2a      101
      2b      101
      2c      101
      2d      101
      2e      101
      2f      102
   }

   method 'test4xBSwap (Ljava/lang/Object;)V' {
      0      105
      3      105
      4      105
      5      105
      6      105
      7      105
      8      105
      d      105
      e      105
      f      105
      10      105
      11      105
      12      105
      13      105
      14      105
      15      105
      16      105
      17      105
      18      105
      19      105
      1a      105
      1b      105
      1c      106
      1d      106
      1e      106
      1f      106
      20      106
      21      107
      22      107
      23      107
      24      107
      25      107
      26      107
      27      107
      2b      110
      2c      110
      2d      110
      2e      110
      2f      110
      30      110
      31      110
      32      110
      33      111
   }

   method 'testSet (Ljava/lang/Object;)V' {
      0      114
      1      114
      2      114
      3      116
      4      116
      5      116
      6      116
      a      117
      b      117
      c      117
      d      117
      e      117
      12      120
      13      120
      14      120
      15      120
      16      120
      17      120
      18      120
      19      121
   }

   method 'testModifyLeak (Ljava/lang/Object;)V' {
      0      124
      1      124
      2      126
      3      126
      4      126
      5      126
      9      127
      a      127
      b      127
      c      127
      d      127
      e      128
      f      128
      10      128
      11      128
      12      128
      13      128
      14      128
      15      128
      16      129
      17      129
      18      129
      19      129
      1a      129
      1b      129
      1c      129
      1d      129
      1e      129
      1f      129
      23      133
      24      133
      25      133
      26      133
      27      134
      28      134
      29      134
      2a      134
      2b      134
      2c      134
      2d      134
      2e      136
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
7 <-> 7
9 <-> 9
12 <-> 12
13 <-> 13
14 <-> 14
16 <-> 16
19 <-> 19
20 <-> 20
21 <-> 21
23 <-> 23
26 <-> 26
27 <-> 27
28 <-> 28
31 <-> 31
32 <-> 32
35 <-> 35
36 <-> 36
37 <-> 37
39 <-> 39
42 <-> 42
43 <-> 43
44 <-> 44
46 <-> 46
49 <-> 49
50 <-> 50
51 <-> 51
53 <-> 54
54 <-> 55
57 <-> 58
58 <-> 59
59 <-> 60
61 <-> 63
62 <-> 64
65 <-> 67
66 <-> 68
67 <-> 69
69 <-> 71
72 <-> 74
73 <-> 75
74 <-> 76
77 <-> 79
78 <-> 80
81 <-> 83
82 <-> 84
83 <-> 85
85 <-> 87
88 <-> 90
89 <-> 91
90 <-> 92
92 <-> 94
95 <-> 97
96 <-> 98
97 <-> 99
99 <-> 102
100 <-> 103
103 <-> 106
104 <-> 107
105 <-> 108
107 <-> 111
108 <-> 112
112 <-> 115
113 <-> 117
114 <-> 118
117 <-> 121
118 <-> 122
121 <-> 125
122 <-> 127
123 <-> 128
125 <-> 129
126 <-> 130
130 <-> 134
131 <-> 135
133 <-> 137
Not mapped:
8
15
22
29
38
45
52
60
68
75
84
91
98
106
