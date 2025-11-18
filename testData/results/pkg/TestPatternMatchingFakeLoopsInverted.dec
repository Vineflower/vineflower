package pkg;

public class TestPatternMatchingFakeLoopsInverted {
   void test1(Object o) {
      while (!(o instanceof String)) {// 5
         String s = (String)o;// 6
         System.out.println(s.length());// 7
      }
   }// 9

   void test2(Object o) {
      while (!(o instanceof String x)) {// 12
         String s = (String)o;// 13
         System.out.println(s);// 14
      }
   }

   void test3(Object o) {
      while (o.hashCode() >= 0 && !(o instanceof String)) {// 19
         String s = (String)o;// 20
         System.out.println(s);// 21
      }
   }// 23

   void test3B(Object o) {
      while (o.hashCode() >= 0 && !(o instanceof String)) {// 26
         String s = (String)o;// 27
         System.out.println(s);// 28
      }

      System.out.println("bye");// 31
   }// 32

   void test4(Object o) {
      while (o.hashCode() >= 0 && !(o instanceof String x)) {// 35
         String s = (String)o;// 36
         System.out.println(s);// 37
      }
   }// 39

   void test4x(Object o) {
      while (o.hashCode() >= 0 && !(o instanceof String x && !x.isEmpty())) {// 42
         String s = (String)o;// 43
         System.out.println(s);// 44
      }
   }// 46

   void test4B(Object o) {
      while (o.hashCode() >= 0 && !(o instanceof String x)) {// 49
         String s = (String)o;// 50
         System.out.println(s);// 51
      }

      System.out.println("bye");// 53
   }// 54

   void test4xB(Object o) {
      while (o.hashCode() >= 0 && !(o instanceof String x && !x.isEmpty())) {// 57
         String s = (String)o;// 58
         System.out.println(s);// 59
      }

      System.out.println("bye");// 61
   }// 62

   void test3Swap(Object o) {
      while (!(o instanceof String) && o.hashCode() >= 0) {// 65
         String s = (String)o;// 66
         System.out.println(s);// 67
      }
   }// 69

   void test3BSwap(Object o) {
      while (!(o instanceof String) && o.hashCode() >= 0) {// 72
         String s = (String)o;// 73
         System.out.println(s);// 74
      }

      System.out.println("bye");// 77
   }// 78

   void test4Swap(Object o) {
      while (!(o instanceof String x) && o.hashCode() >= 0) {// 81
         String s = (String)o;// 82
         System.out.println(s);// 83
      }
   }// 85

   void test4xSwap(Object o) {
      while (!(o instanceof String x && !x.isEmpty()) && o.hashCode() >= 0) {// 88
         String s = (String)o;// 89
         System.out.println(s);// 90
      }
   }// 92

   void test4BSwap(Object o) {
      while (!(o instanceof String x) && o.hashCode() >= 0) {// 95
         String s = (String)o;// 96
         System.out.println(s);// 97
      }

      System.out.println("bye");// 99
   }// 100

   void test4xBSwap(Object o) {
      while (!(o instanceof String x && !x.isEmpty()) && o.hashCode() >= 0) {// 103
         String s = (String)o;// 104
         System.out.println(s);// 105
      }

      System.out.println("bye");// 107
   }// 108

   public void testSet(Object obj) {
      String s = "Hi";// 112

      while (!(obj instanceof String)) {// 113
         s = (String)obj;// 114
      }

      System.out.println(s);// 117
   }// 118

   void test1A(Object o) {
      while (!(o instanceof String)) {// 121
         String s = (String)o;// 122
         System.out.println(s.length());// 123
      }

      String u = (String)o;// 125
      System.out.println(u.hashCode() + u.length());// 126
   }// 127

   void test2A(Object o) {
      while (!(o instanceof String x)) {// 130
         String s = (String)o;// 131
         System.out.println(s);// 132
      }

      String var5 = (String)o;// 134
      System.out.println(var5.hashCode() + var5.length());// 135
   }

   void test3A(Object o) {
      while (o.hashCode() >= 0 && !(o instanceof String)) {// 139
         String s = (String)o;// 140
         System.out.println(s);// 141
      }

      String u = (String)o;// 144
      System.out.println(u.hashCode() + u.length());// 145
   }// 146

   void test4A(Object o) {
      while (o.hashCode() >= 0 && !(o instanceof String x)) {// 149
         String s = (String)o;// 150
         System.out.println(s);// 151
      }

      String u = (String)o;// 154
      System.out.println(u.hashCode() + u.length());// 155
   }// 156

   void test4xA(Object o) {
      while (o.hashCode() >= 0 && !(o instanceof String x && !x.isEmpty())) {// 159
         String s = (String)o;// 160
         System.out.println(s);// 161
      }

      String u = (String)o;// 164
      System.out.println(u.hashCode() + u.length());// 165
   }// 166

   void test3ASwap(Object o) {
      while (!(o instanceof String) && o.hashCode() >= 0) {// 169
         String s = (String)o;// 170
         System.out.println(s);// 171
      }

      String u = (String)o;// 174
      System.out.println(u.hashCode() + u.length());// 175
   }// 176

   void test4ASwap(Object o) {
      while (!(o instanceof String x) && o.hashCode() >= 0) {// 180
         String s = (String)o;// 181
         System.out.println(s);// 182
      }

      String u = (String)o;// 185
      System.out.println(u.hashCode() + u.length());// 186
   }// 187

   void test4xASwap(Object o) {
      while (!(o instanceof String x && !x.isEmpty()) && o.hashCode() >= 0) {// 190
         String s = (String)o;// 191
         System.out.println(s);// 192
      }

      String u = (String)o;// 195
      System.out.println(u.hashCode() + u.length());// 196
   }// 197

   public void testSetA(Object obj) {
      String s = "Hi";// 200

      while (!(obj instanceof String)) {// 201
         s = (String)obj;// 202
      }

      String u = (String)obj;// 205
      System.out.println(u.hashCode() + u.length());// 206
   }// 207
}

class 'pkg/TestPatternMatchingFakeLoopsInverted' {
   method 'test1 (Ljava/lang/Object;)V' {
      0      4
      1      4
      2      4
      3      4
      4      4
      5      4
      6      4
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
      6      11
      7      11
      8      11
      d      11
      e      15
      11      12
      12      12
      13      12
      14      12
      15      12
      16      13
      17      13
      18      13
      19      13
      1a      13
      1b      13
      1c      13
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
      d      34
      e      34
      f      34
      14      34
      18      35
      19      35
      1a      35
      1b      35
      1c      35
      1d      36
      1e      36
      1f      36
      20      36
      21      36
      22      36
      23      36
      27      38
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
      d      48
      e      48
      f      48
      14      48
      18      49
      19      49
      1a      49
      1b      49
      1c      49
      1d      50
      1e      50
      1f      50
      20      50
      21      50
      22      50
      23      50
      27      53
      28      53
      29      53
      2a      53
      2b      53
      2c      53
      2d      53
      2e      53
      2f      54
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
      7      116
      8      116
      9      116
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

   method 'test1A (Ljava/lang/Object;)V' {
      0      124
      1      124
      2      124
      3      124
      4      124
      5      124
      6      124
      7      125
      8      125
      9      125
      a      125
      b      125
      c      126
      d      126
      e      126
      f      126
      10      126
      11      126
      12      126
      13      126
      14      126
      15      126
      19      129
      1a      129
      1b      129
      1c      129
      1d      129
      1e      130
      1f      130
      20      130
      21      130
      22      130
      23      130
      24      130
      25      130
      26      130
      27      130
      28      130
      29      130
      2a      130
      2b      130
      2c      130
      2d      131
   }

   method 'test2A (Ljava/lang/Object;)V' {
      0      134
      3      134
      4      134
      5      134
      6      134
      7      134
      8      134
      d      134
      e      139
      11      135
      12      135
      13      135
      14      135
      15      135
      16      136
      17      136
      18      136
      19      136
      1a      136
      1b      136
      1c      136
      20      139
      21      139
      22      139
      24      140
      25      140
      26      140
      28      140
      29      140
      2a      140
      2c      140
      2d      140
      2e      140
      30      140
      31      140
      32      140
   }

   method 'test3A (Ljava/lang/Object;)V' {
      0      144
      1      144
      2      144
      3      144
      4      144
      5      144
      6      144
      7      144
      8      144
      9      144
      a      144
      b      144
      c      144
      d      144
      e      145
      f      145
      10      145
      11      145
      12      145
      13      146
      14      146
      15      146
      16      146
      17      146
      18      146
      19      146
      1d      149
      1e      149
      1f      149
      20      149
      21      149
      22      150
      23      150
      24      150
      25      150
      26      150
      27      150
      28      150
      29      150
      2a      150
      2b      150
      2c      150
      2d      150
      2e      150
      2f      150
      30      150
      31      151
   }

   method 'test4A (Ljava/lang/Object;)V' {
      0      154
      1      154
      2      154
      3      154
      4      154
      5      154
      6      154
      7      154
      a      154
      b      154
      c      154
      d      154
      e      154
      f      154
      14      154
      18      155
      19      155
      1a      155
      1b      155
      1c      155
      1d      156
      1e      156
      1f      156
      20      156
      21      156
      22      156
      23      156
      27      159
      28      159
      29      159
      2a      159
      2b      159
      2c      160
      2d      160
      2e      160
      2f      160
      30      160
      31      160
      32      160
      33      160
      34      160
      35      160
      36      160
      37      160
      38      160
      39      160
      3a      160
      3b      161
   }

   method 'test4xA (Ljava/lang/Object;)V' {
      0      164
      1      164
      2      164
      3      164
      4      164
      5      164
      6      164
      7      164
      a      164
      b      164
      c      164
      d      164
      e      164
      f      164
      14      164
      15      164
      16      164
      17      164
      18      164
      19      164
      1a      164
      1b      164
      1c      165
      1d      165
      1e      165
      1f      165
      20      165
      21      166
      22      166
      23      166
      24      166
      25      166
      26      166
      27      166
      2b      169
      2c      169
      2d      169
      2e      169
      2f      169
      30      170
      31      170
      32      170
      33      170
      34      170
      35      170
      36      170
      37      170
      38      170
      39      170
      3a      170
      3b      170
      3c      170
      3d      170
      3e      170
      3f      171
   }

   method 'test3ASwap (Ljava/lang/Object;)V' {
      0      174
      1      174
      2      174
      3      174
      4      174
      5      174
      6      174
      7      174
      8      174
      9      174
      a      174
      b      174
      c      174
      d      174
      e      175
      f      175
      10      175
      11      175
      12      175
      13      176
      14      176
      15      176
      16      176
      17      176
      18      176
      19      176
      1d      179
      1e      179
      1f      179
      20      179
      21      179
      22      180
      23      180
      24      180
      25      180
      26      180
      27      180
      28      180
      29      180
      2a      180
      2b      180
      2c      180
      2d      180
      2e      180
      2f      180
      30      180
      31      181
   }

   method 'test4ASwap (Ljava/lang/Object;)V' {
      0      184
      3      184
      4      184
      5      184
      6      184
      7      184
      8      184
      d      184
      11      184
      12      184
      13      184
      14      184
      15      184
      16      184
      17      184
      18      185
      19      185
      1a      185
      1b      185
      1c      185
      1d      186
      1e      186
      1f      186
      20      186
      21      186
      22      186
      23      186
      27      189
      28      189
      29      189
      2a      189
      2b      189
      2c      190
      2d      190
      2e      190
      2f      190
      30      190
      31      190
      32      190
      33      190
      34      190
      35      190
      36      190
      37      190
      38      190
      39      190
      3a      190
      3b      191
   }

   method 'test4xASwap (Ljava/lang/Object;)V' {
      0      194
      3      194
      4      194
      5      194
      6      194
      7      194
      8      194
      d      194
      e      194
      f      194
      10      194
      11      194
      12      194
      13      194
      14      194
      15      194
      16      194
      17      194
      18      194
      19      194
      1a      194
      1b      194
      1c      195
      1d      195
      1e      195
      1f      195
      20      195
      21      196
      22      196
      23      196
      24      196
      25      196
      26      196
      27      196
      2b      199
      2c      199
      2d      199
      2e      199
      2f      199
      30      200
      31      200
      32      200
      33      200
      34      200
      35      200
      36      200
      37      200
      38      200
      39      200
      3a      200
      3b      200
      3c      200
      3d      200
      3e      200
      3f      201
   }

   method 'testSetA (Ljava/lang/Object;)V' {
      0      204
      1      204
      2      204
      3      206
      4      206
      5      206
      6      206
      7      206
      8      206
      9      206
      a      207
      b      207
      c      207
      d      207
      e      207
      12      210
      13      210
      14      210
      15      210
      16      210
      17      211
      18      211
      19      211
      1a      211
      1b      211
      1c      211
      1d      211
      1e      211
      1f      211
      20      211
      21      211
      22      211
      23      211
      24      211
      25      211
      26      212
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
122 <-> 126
123 <-> 127
125 <-> 130
126 <-> 131
127 <-> 132
130 <-> 135
131 <-> 136
132 <-> 137
134 <-> 140
135 <-> 141
139 <-> 145
140 <-> 146
141 <-> 147
144 <-> 150
145 <-> 151
146 <-> 152
149 <-> 155
150 <-> 156
151 <-> 157
154 <-> 160
155 <-> 161
156 <-> 162
159 <-> 165
160 <-> 166
161 <-> 167
164 <-> 170
165 <-> 171
166 <-> 172
169 <-> 175
170 <-> 176
171 <-> 177
174 <-> 180
175 <-> 181
176 <-> 182
180 <-> 185
181 <-> 186
182 <-> 187
185 <-> 190
186 <-> 191
187 <-> 192
190 <-> 195
191 <-> 196
192 <-> 197
195 <-> 200
196 <-> 201
197 <-> 202
200 <-> 205
201 <-> 207
202 <-> 208
205 <-> 211
206 <-> 212
207 <-> 213
Not mapped:
8
15
16
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
124
133
136
142
152
162
172
183
193
