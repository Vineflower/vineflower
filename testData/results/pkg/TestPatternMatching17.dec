package pkg;

public class TestPatternMatching17 {
   private Object object;
   private AutoCloseable o1;
   private int x;
   private int y;

   public void testSimple(Object obj) {
      if (obj instanceof String str) {// 5
         System.out.println(str.length());// 6
      }
   }// 8

   public void testInverted(Object obj) {
      if (!(obj instanceof String str)) {// 11
         System.out.println("Oh no");// 12
      }
   }// 14

   public void testCompound(Object obj) {
      if (obj instanceof String str && str.contains("hi")) {// 17
         System.out.println(str.length());// 18
      }
   }// 20

   public void testSimpleLoop(Object obj) {
      while (obj instanceof String) {// 24
         String str = (String)obj;
         System.out.println(str.length());// 25
         obj = str.intern();// 26
      }
   }// 28

   public void testSimpleLoopUnused(Object obj) {
      while (obj instanceof String) {// 31
         String str = (String)obj;
         obj = obj.hashCode() + "";// 32
      }
   }// 34

   public void testInvertedLoop(Object obj) {
      while (!(obj instanceof String)) {// 37
         System.out.println("Oh no");// 38
         obj = obj.toString();// 39
      }

      String str = (String)obj;
      System.out.println(str.hashCode());// 41
   }

   public void testInvertedLoopUnused(Object obj) {
      while (!(obj instanceof String)) {// 45
         System.out.println("Oh no");// 46
         obj = obj.toString();// 47
      }

      String str = (String)obj;
   }

   public void testCompoundLoop(Object obj) {
      while (obj instanceof String str && str.contains("hi")) {// 52
         obj = str.substring(1);// 53
      }
   }// 55

   public boolean testReturn(Object obj) {
      return obj instanceof String s && s.length() > 5;// 58
   }

   public int testReturnTernary(Object obj) {
      return obj instanceof String s ? s.length() : 0;// 62
   }

   public int testReturnTernaryComplex(Object obj) {
      return !(obj instanceof String s && s.length() > 5) && !(obj instanceof Integer) ? 1 : 4;// 66
   }

   public void testLoop(Object obj) {
      while (obj instanceof String s && s.length() > 10) {// 70
         String var3 = s.substring(1);// 71
         obj = var3.substring(1);// 72
         System.out.println(var3);// 74
      }
   }// 76

   public void testSimpleReturn(Object obj) {
      if (!(obj instanceof String str && str.length() > 5)) {// 79 80
         System.out.println("test");// 85
      }
   }// 81 86

   public void testMessyLVT(Object obj) {
      String a = "a";// 90
      String b = "b";// 91
      String c = "c";// 92
      String d = "d";// 93
      String e = "e";// 94
      String f = "f";// 95
      String g = "g";// 96
      String h = "h";// 97
      String var10 = "i";// 98
      if (obj instanceof String str) {// 100
         System.out.println(str.length());// 101
      }
   }// 103

   public void testEmptyHead() throws Throwable {
      if (this.object instanceof String) {// 111
         if (this.o1 != null) {// 112
            this.o1.close();// 113
         }

         this.y = (this.x + 1) % 5;// 116
         if (this.y == 0) {// 117
            System.out.println(0);// 118
         } else {
            System.out.println(1);// 120
         }
      }
   }// 123

   public void reassign(Object o) {
      String s = "hello";// 126

      for (int i = 0; i < 10; i++) {// 127
         if (o instanceof Number) {// 128
            s = "goodbye";// 129
         } else if (o instanceof String) {// 130
            s = (String)o;// 131
         }
      }

      System.out.println(s);// 135
   }// 136

   public void reassignCopy(Object o) {
      String s = "hello";// 139

      for (int i = 0; i < 10; i++) {// 140
         if (o instanceof Number) {// 141
            s = "goodbye";// 142
         } else if (o instanceof String s2) {// 143
            s = s2;// 144
         }
      }

      System.out.println(s);// 148
   }// 149

   public void reassignField(Object o, String s) {
      for (int i = 0; i < 10; i++) {// 152
         if (o instanceof Number) {// 153
            s = "goodbye";// 154
         } else if (o instanceof String) {// 155
            s = (String)o;// 156
         }
      }

      System.out.println(s);// 160
   }// 161

   public void reassignNoUse(Object o) {
      String s = "hello";// 164

      for (int i = 0; i < 10; i++) {// 165
         if (o instanceof Number) {// 166
            s = "goodbye";// 167
         } else if (o instanceof String var5) {// 168 169
            ;
         }
      }
   }// 172

   public String multiCombo(Object o, String s) {
      return o instanceof String s2 && !s.isEmpty() ? s2 + s : s;// 175 176 178
   }

   public void testInLoop(Object[] a) {
      for (Object o : a) {// 182
         if (o instanceof String s && !s.isEmpty()) {// 183
            System.out.println(s);// 184
         }
      }
   }// 187
}

class 'pkg/TestPatternMatching17' {
   method 'testSimple (Ljava/lang/Object;)V' {
      0      9
      1      9
      2      9
      3      9
      4      9
      5      9
      6      9
      b      9
      c      10
      d      10
      e      10
      f      10
      10      10
      11      10
      12      10
      13      10
      14      10
      15      10
      16      12
   }

   method 'testInverted (Ljava/lang/Object;)V' {
      0      15
      1      15
      2      15
      3      15
      4      15
      5      15
      6      15
      b      15
      f      16
      10      16
      11      16
      12      16
      13      16
      14      16
      15      16
      16      16
      17      18
   }

   method 'testCompound (Ljava/lang/Object;)V' {
      0      21
      1      21
      2      21
      3      21
      4      21
      5      21
      6      21
      b      21
      c      21
      d      21
      e      21
      f      21
      10      21
      11      21
      12      21
      13      21
      14      21
      15      22
      16      22
      17      22
      18      22
      19      22
      1a      22
      1b      22
      1c      22
      1d      22
      1e      22
      1f      24
   }

   method 'testSimpleLoop (Ljava/lang/Object;)V' {
      0      27
      1      27
      2      27
      3      27
      7      28
      8      28
      9      28
      a      28
      b      28
      c      29
      d      29
      e      29
      f      29
      10      29
      11      29
      12      29
      13      29
      14      29
      15      29
      16      30
      17      30
      18      30
      19      30
      1a      30
      1e      32
   }

   method 'testSimpleLoopUnused (Ljava/lang/Object;)V' {
      0      35
      1      35
      2      35
      3      35
      7      36
      8      36
      9      36
      a      36
      b      36
      c      37
      d      37
      e      37
      f      37
      10      37
      11      37
      12      37
      13      37
      14      37
      15      37
      19      39
   }

   method 'testInvertedLoop (Ljava/lang/Object;)V' {
      0      42
      1      42
      2      42
      3      42
      4      42
      5      42
      6      42
      7      47
      8      47
      9      47
      a      47
      b      47
      c      48
      d      48
      e      48
      f      43
      10      43
      11      43
      12      43
      13      43
      14      43
      15      43
      16      43
      17      44
      18      44
      19      44
      1a      44
      1b      44
      1f      48
      22      48
      23      48
      24      48
      25      48
      26      49
   }

   method 'testInvertedLoopUnused (Ljava/lang/Object;)V' {
      0      52
      1      52
      2      52
      3      52
      4      52
      5      52
      6      52
      7      57
      8      57
      9      57
      a      57
      b      57
      c      58
      f      53
      10      53
      11      53
      12      53
      13      53
      14      53
      15      53
      16      53
      17      54
      18      54
      19      54
      1a      54
      1b      54
   }

   method 'testCompoundLoop (Ljava/lang/Object;)V' {
      0      61
      1      61
      2      61
      3      61
      4      61
      5      61
      6      61
      b      61
      c      61
      d      61
      e      61
      f      61
      10      61
      11      61
      12      61
      13      61
      14      61
      15      62
      16      62
      17      62
      18      62
      19      62
      1a      62
      1e      64
   }

   method 'testReturn (Ljava/lang/Object;)Z' {
      0      67
      1      67
      2      67
      3      67
      4      67
      5      67
      6      67
      b      67
      c      67
      d      67
      e      67
      f      67
      10      67
      11      67
      12      67
      13      67
      19      67
   }

   method 'testReturnTernary (Ljava/lang/Object;)I' {
      0      71
      1      71
      2      71
      3      71
      4      71
      5      71
      6      71
      b      71
      c      71
      d      71
      e      71
      f      71
      13      71
      14      71
   }

   method 'testReturnTernaryComplex (Ljava/lang/Object;)I' {
      0      75
      1      75
      2      75
      3      75
      4      75
      5      75
      6      75
      b      75
      c      75
      d      75
      e      75
      f      75
      10      75
      11      75
      12      75
      13      75
      14      75
      15      75
      16      75
      17      75
      18      75
      19      75
      1a      75
      1b      75
      1f      75
      20      75
   }

   method 'testLoop (Ljava/lang/Object;)V' {
      0      79
      1      79
      2      79
      3      79
      4      79
      5      79
      6      79
      b      79
      c      79
      d      79
      e      79
      f      79
      10      79
      11      79
      12      79
      13      79
      14      79
      15      80
      16      80
      17      80
      18      80
      19      80
      1a      80
      1b      81
      1c      81
      1d      81
      1e      81
      1f      81
      20      81
      21      82
      22      82
      23      82
      24      82
      25      82
      26      82
      27      82
      2b      84
   }

   method 'testSimpleReturn (Ljava/lang/Object;)V' {
      0      87
      1      87
      2      87
      3      87
      4      87
      5      87
      6      87
      b      87
      c      87
      d      87
      e      87
      f      87
      10      87
      11      87
      12      87
      13      87
      14      90
      15      88
      16      88
      17      88
      18      88
      19      88
      1a      88
      1b      88
      1c      88
      1d      90
   }

   method 'testMessyLVT (Ljava/lang/Object;)V' {
      0      93
      1      93
      2      93
      3      94
      4      94
      5      94
      6      95
      7      95
      8      95
      9      95
      a      96
      b      96
      c      96
      d      96
      e      97
      f      97
      10      97
      11      97
      12      98
      13      98
      14      98
      15      98
      16      99
      17      99
      18      99
      19      99
      1a      100
      1b      100
      1c      100
      1d      100
      1e      101
      1f      101
      20      101
      21      101
      22      102
      23      102
      24      102
      25      102
      26      102
      27      102
      28      102
      2d      102
      2e      103
      2f      103
      30      103
      31      103
      32      103
      33      103
      34      103
      35      103
      36      103
      37      103
      38      105
   }

   method 'testEmptyHead ()V' {
      0      108
      1      108
      2      108
      3      108
      4      108
      5      108
      6      108
      7      108
      8      108
      9      108
      a      109
      b      109
      c      109
      d      109
      e      109
      f      109
      10      109
      11      110
      12      110
      13      110
      14      110
      15      110
      16      110
      17      110
      18      110
      19      110
      1a      113
      1b      113
      1c      113
      1d      113
      1e      113
      1f      113
      20      113
      21      113
      22      113
      23      113
      24      113
      25      113
      26      114
      27      114
      28      114
      29      114
      2a      114
      2b      114
      2c      114
      2d      115
      2e      115
      2f      115
      30      115
      31      115
      32      115
      33      115
      37      117
      38      117
      39      117
      3a      117
      3b      117
      3c      117
      3d      117
      3e      120
   }

   method 'reassign (Ljava/lang/Object;)V' {
      0      123
      1      123
      2      123
      3      125
      4      125
      5      125
      6      125
      7      125
      8      125
      9      125
      a      125
      b      126
      c      126
      d      126
      e      126
      f      126
      10      126
      11      126
      12      127
      13      127
      14      127
      18      128
      19      128
      1a      128
      1b      128
      1c      128
      1d      128
      1e      128
      1f      129
      20      129
      21      129
      22      129
      23      129
      24      125
      25      125
      26      125
      2a      133
      2b      133
      2c      133
      2d      133
      2e      133
      2f      133
      30      133
      31      134
   }

   method 'reassignCopy (Ljava/lang/Object;)V' {
      0      137
      1      137
      2      137
      3      139
      4      139
      5      139
      6      139
      7      139
      8      139
      9      139
      a      139
      b      140
      c      140
      d      140
      e      140
      f      140
      10      140
      11      140
      12      141
      13      141
      14      141
      18      142
      19      142
      1a      142
      1b      142
      1c      142
      1d      142
      1e      142
      23      142
      24      142
      25      143
      26      143
      27      143
      28      139
      29      139
      2a      139
      2e      147
      2f      147
      30      147
      31      147
      32      147
      33      147
      34      147
      35      148
   }

   method 'reassignField (Ljava/lang/Object;Ljava/lang/String;)V' {
      0      151
      1      151
      2      151
      3      151
      4      151
      5      151
      6      151
      7      151
      8      152
      9      152
      a      152
      b      152
      c      152
      d      152
      e      152
      f      153
      10      153
      11      153
      15      154
      16      154
      17      154
      18      154
      19      154
      1a      154
      1b      154
      1c      155
      1d      155
      1e      155
      1f      155
      20      155
      21      151
      22      151
      23      151
      27      159
      28      159
      29      159
      2a      159
      2b      159
      2c      159
      2d      159
      2e      160
   }

   method 'reassignNoUse (Ljava/lang/Object;)V' {
      0      163
      1      163
      2      163
      3      165
      4      165
      5      165
      6      165
      7      165
      8      165
      9      165
      a      165
      b      166
      c      166
      d      166
      e      166
      f      166
      10      166
      11      166
      12      167
      13      167
      14      167
      18      168
      19      168
      1a      168
      1b      168
      1c      168
      1d      168
      1e      168
      23      168
      24      165
      25      165
      26      165
      2a      172
   }

   method 'multiCombo (Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;' {
      0      175
      1      175
      2      175
      3      175
      4      175
      5      175
      6      175
      b      175
      c      175
      d      175
      e      175
      f      175
      10      175
      11      175
      12      175
      13      175
      14      175
      15      175
      16      175
      17      175
      18      175
      19      175
      1b      175
   }

   method 'testInLoop ([Ljava/lang/Object;)V' {
      0      179
      1      179
      2      179
      4      179
      6      179
      7      179
      12      179
      13      179
      14      180
      15      180
      16      180
      17      180
      18      180
      19      180
      1a      180
      1b      180
      21      180
      22      180
      23      180
      24      180
      25      180
      26      180
      27      180
      28      180
      29      180
      2a      180
      2b      181
      2c      181
      2d      181
      2e      181
      2f      181
      30      181
      31      181
      32      181
      33      179
      34      179
      35      179
      39      184
   }
}

Lines mapping:
5 <-> 10
6 <-> 11
8 <-> 13
11 <-> 16
12 <-> 17
14 <-> 19
17 <-> 22
18 <-> 23
20 <-> 25
24 <-> 28
25 <-> 30
26 <-> 31
28 <-> 33
31 <-> 36
32 <-> 38
34 <-> 40
37 <-> 43
38 <-> 44
39 <-> 45
41 <-> 49
45 <-> 53
46 <-> 54
47 <-> 55
52 <-> 62
53 <-> 63
55 <-> 65
58 <-> 68
62 <-> 72
66 <-> 76
70 <-> 80
71 <-> 81
72 <-> 82
74 <-> 83
76 <-> 85
79 <-> 88
80 <-> 88
81 <-> 91
85 <-> 89
86 <-> 91
90 <-> 94
91 <-> 95
92 <-> 96
93 <-> 97
94 <-> 98
95 <-> 99
96 <-> 100
97 <-> 101
98 <-> 102
100 <-> 103
101 <-> 104
103 <-> 106
111 <-> 109
112 <-> 110
113 <-> 111
116 <-> 114
117 <-> 115
118 <-> 116
120 <-> 118
123 <-> 121
126 <-> 124
127 <-> 126
128 <-> 127
129 <-> 128
130 <-> 129
131 <-> 130
135 <-> 134
136 <-> 135
139 <-> 138
140 <-> 140
141 <-> 141
142 <-> 142
143 <-> 143
144 <-> 144
148 <-> 148
149 <-> 149
152 <-> 152
153 <-> 153
154 <-> 154
155 <-> 155
156 <-> 156
160 <-> 160
161 <-> 161
164 <-> 164
165 <-> 166
166 <-> 167
167 <-> 168
168 <-> 169
169 <-> 169
172 <-> 173
175 <-> 176
176 <-> 176
178 <-> 176
182 <-> 180
183 <-> 181
184 <-> 182
187 <-> 185
Not mapped:
42
49
