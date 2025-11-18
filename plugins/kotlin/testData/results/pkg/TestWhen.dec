package pkg

public class TestWhen {
   public fun testStatement(obj: Any) {
      if (obj == 1) {// 5 6
         System.out.println("1")
      } else if (obj == "2") {// 7
         System.out.println("2")
      } else if (obj is java.lang.Double) {// 8
         System.out.println("Double")
      } else if (obj !is java.lang.Long) {// 9
         System.out.println("Not Long")
      } else {
         System.out.println("else")// 10
      }
   }// 12

   public fun testStatement2(a: Any, b: Any) {
      if (a == 15) {// 16
         System.out.println("a == 15")
      } else if (a == "!!") {// 17
         System.out.println("a == !!")
      } else if (a is Int) {// 18
         System.out.println("a is Int")
      } else if (a is java.lang.String) {// 19
         System.out.println("a is String")
      } else if (b is java.lang.Double) {// 20
         System.out.println("b is Double")
      } else if (a is Unit) {// 21
         System.out.println("a is Unit")
      } else {
         System.out.println("else")// 22
      }
   }// 24

   public fun testStatement3(obj: Any?) {
      if (!(obj == 1)) {// 27 28
         if (obj is java.lang.Double) {// 29
            System.out.println("Double")
         } else if (obj == null) {// 30
            System.out.println("null")
         } else if (obj !is java.lang.String && obj is Int) {// 31 32
            System.out.println((obj as java.lang.Number).intValue() + 1)
         }
      }
   }// 35

   public fun testExpression(obj: Any): Int {
      return if (obj == 1) 1 else (if (obj is java.lang.Double) 2 else (if (obj == "4") 4 else (if (obj !is java.lang.Long) 3 else 5)))// 38 39 40 41 42 43
   }

   public fun testExpression2(obj: Any): Int {
      val text: java.lang.String = if (obj == 1)// 48 49
         "one"
         else
         (if (obj is java.lang.Double) "double" else (if (obj == "4") "four" else (if (obj !is java.lang.Long) "not long" else "something")))// 50 51 52 53
         System.out.println(text)// 55
      return text.length()// 56
   }

   public fun testSwitchExpression(a: Int) {
      var var10000: java.lang.String
      when (a) {// 60
         1 -> var10000 = "one"// 61
         2, 3, 5 -> var10000 = "prime"// 62
         4 -> var10000 = "something"// 63
         else -> var10000 = "something"
      }

      System.out.println(var10000)// 65
   }// 66

   public fun testCompilesToTableSwitch(a: Int) {
      when (a) {// 69
         1 -> System.out.println("The loneliest number")// 70
         2, 3 -> System.out.println("Small prime")// 71
         else -> System.out.println("Number")// 72
      }
   }// 74

   public fun testCompilesToLookupSwitch(a: Int) {
      when (a) {// 77
         1 -> System.out.println("Go for gold")// 78
         9 -> System.out.println("The cat's meow")// 79
         42 -> System.out.println("The answer")// 80
         else -> System.out.println("Number")// 81
      }
   }// 83

   public fun testSwitchWithEmptyCases(a: Int) {
      when (a) {// 86
         1 -> {}
         2, 3 -> System.out.println(a + 4)// 88
         4, 8, 1000 -> System.out.println("even")// 89
         else -> {}
      }
   }// 92

   public fun testNoBranches(a: Any): Int {
      return 0// 95 96
   }

   public fun booleanNightmares(a: Boolean, b: Boolean, c: Boolean, d: Boolean, e: Boolean, f: Boolean, g: Boolean) {
      if (a == (b != c)) {// 101 102
         System.out.println("-_-")
      } else if (a == (b && !e)) {// 103
         System.out.println("xxx")
      } else if (a == (!a || d)) {// 104
         System.out.println("ohno")
      } else if (!a) {// 105
         System.out.println("NIGHTMARE")
      } else if (a == (g || e && f != c)) {// 106
         System.out.println("hello")
      } else {
         System.out.println("else")// 107
      }
   }// 109
}

class 'pkg/TestWhen' {
   method 'testStatement (Ljava/lang/Object;)V' {
      6      4
      8      4
      9      4
      a      4
      b      4
      c      4
      10      4
      11      4
      12      4
      13      5
      14      5
      15      5
      16      5
      17      5
      19      5
      1a      5
      1b      5
      1f      6
      20      6
      21      6
      25      6
      26      6
      27      6
      28      7
      29      7
      2a      7
      2b      7
      2c      7
      2e      7
      2f      7
      30      7
      34      8
      35      8
      36      8
      37      8
      38      8
      39      8
      3a      8
      3b      9
      3c      9
      3d      9
      3e      9
      3f      9
      41      9
      42      9
      43      9
      47      10
      4b      10
      4c      10
      4d      10
      4e      11
      4f      11
      50      11
      51      11
      52      11
      54      11
      55      11
      56      11
      5a      13
      5b      13
      5c      13
      5d      13
      5e      13
      60      13
      61      13
      62      13
      63      15
   }

   method 'testStatement2 (Ljava/lang/Object;Ljava/lang/Object;)V' {
      d      18
      e      18
      f      18
      10      18
      11      18
      12      18
      16      18
      17      18
      18      18
      19      19
      1a      19
      1b      19
      1c      19
      1d      19
      1f      19
      20      19
      21      19
      25      20
      26      20
      27      20
      2b      20
      2c      20
      2d      20
      2e      21
      2f      21
      30      21
      31      21
      32      21
      34      21
      35      21
      36      21
      3a      22
      3b      22
      3c      22
      3d      22
      3e      22
      3f      22
      40      22
      41      23
      42      23
      43      23
      44      23
      45      23
      47      23
      48      23
      49      23
      4d      24
      4e      24
      4f      24
      50      24
      51      24
      52      24
      53      24
      54      25
      55      25
      56      25
      57      25
      58      25
      5a      25
      5b      25
      5c      25
      60      26
      61      26
      62      26
      63      26
      64      26
      65      26
      66      26
      67      27
      68      27
      69      27
      6a      27
      6b      27
      6d      27
      6e      27
      6f      27
      73      28
      74      28
      75      28
      76      28
      77      28
      78      28
      79      28
      7a      29
      7b      29
      7c      29
      7d      29
      7e      29
      80      29
      81      29
      82      29
      86      31
      87      31
      88      31
      89      31
      8a      31
      8c      31
      8d      31
      8e      31
      8f      33
   }

   method 'testStatement3 (Ljava/lang/Object;)V' {
      0      36
      2      36
      3      36
      4      36
      5      36
      6      36
      a      36
      b      36
      c      36
      d      37
      e      37
      f      37
      10      37
      11      37
      12      37
      13      37
      14      38
      15      38
      16      38
      17      38
      18      38
      1a      38
      1b      38
      1c      38
      20      39
      21      39
      22      39
      23      39
      24      40
      25      40
      26      40
      27      40
      28      40
      2a      40
      2b      40
      2c      40
      30      41
      34      41
      35      41
      36      41
      37      41
      38      41
      39      41
      3a      41
      3b      41
      3c      41
      3d      41
      3e      42
      3f      42
      40      42
      41      42
      42      42
      43      42
      44      42
      45      42
      46      42
      48      42
      49      42
      4a      42
      4b      42
      4c      42
      4d      42
      4e      42
      4f      45
   }

   method 'testExpression (Ljava/lang/Object;)I' {
      6      48
      8      48
      9      48
      a      48
      b      48
      c      48
      10      48
      11      48
      12      48
      13      48
      17      48
      18      48
      19      48
      1a      48
      1b      48
      1c      48
      1d      48
      1e      48
      22      48
      23      48
      24      48
      28      48
      29      48
      2a      48
      2b      48
      2f      48
      33      48
      34      48
      35      48
      36      48
      3a      48
      3b      48
   }

   method 'testExpression2 (Ljava/lang/Object;)I' {
      6      52
      8      52
      9      52
      a      52
      b      52
      c      52
      10      52
      11      52
      12      52
      13      53
      14      53
      18      55
      19      55
      1a      55
      1b      55
      1c      55
      1d      55
      1e      55
      1f      55
      20      55
      24      55
      25      55
      26      55
      2a      55
      2b      55
      2c      55
      2d      55
      2e      55
      32      55
      36      55
      37      55
      38      55
      39      55
      3a      55
      3e      55
      3f      55
      40      52
      41      56
      42      56
      43      56
      44      56
      45      56
      46      56
      47      56
      48      57
      49      57
      4a      57
      4b      57
      4c      57
   }

   method 'testSwitchExpression (I)V' {
      0      62
      1      62
      2      62
      3      62
      4      62
      5      62
      6      62
      7      62
      8      62
      9      62
      a      62
      b      62
      c      62
      d      62
      e      62
      f      62
      10      62
      11      62
      12      62
      13      62
      14      62
      15      62
      16      62
      17      62
      18      62
      19      62
      1a      62
      1b      62
      1c      62
      1d      62
      1e      62
      1f      62
      20      62
      21      62
      22      62
      23      62
      24      63
      25      63
      26      64
      29      64
      2a      64
      2b      65
      2e      65
      2f      65
      31      69
      32      69
      33      69
      34      69
      35      69
      36      69
      37      69
      38      70
   }

   method 'testCompilesToTableSwitch (I)V' {
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
      e      73
      f      73
      10      73
      11      73
      12      73
      13      73
      14      73
      15      73
      16      73
      17      73
      18      73
      19      73
      1a      73
      1b      73
      1c      74
      1d      74
      1e      74
      1f      74
      20      74
      22      74
      23      74
      24      74
      25      75
      28      75
      29      75
      2a      75
      2b      75
      2c      75
      2e      75
      2f      75
      30      75
      31      76
      34      76
      35      76
      36      76
      37      76
      38      76
      3a      76
      3b      76
      3c      76
      3d      78
   }

   method 'testCompilesToLookupSwitch (I)V' {
      0      81
      1      81
      2      81
      3      81
      4      81
      5      81
      6      81
      7      81
      8      81
      9      81
      a      81
      b      81
      c      81
      d      81
      e      81
      f      81
      10      81
      11      81
      12      81
      13      81
      14      81
      15      81
      16      81
      17      81
      18      81
      19      81
      1a      81
      1b      81
      1c      81
      1d      81
      1e      81
      1f      81
      20      81
      21      81
      22      81
      23      81
      24      82
      25      82
      26      82
      27      82
      28      82
      2a      82
      2b      82
      2c      82
      2d      83
      30      83
      31      83
      32      83
      33      83
      34      83
      36      83
      37      83
      38      83
      39      84
      3c      84
      3d      84
      3e      84
      3f      84
      40      84
      42      84
      43      84
      44      84
      45      85
      48      85
      49      85
      4a      85
      4b      85
      4c      85
      4e      85
      4f      85
      50      85
      51      87
   }

   method 'testSwitchWithEmptyCases (I)V' {
      0      90
      1      90
      2      90
      3      90
      4      90
      5      90
      6      90
      7      90
      8      90
      9      90
      a      90
      b      90
      c      90
      d      90
      e      90
      f      90
      10      90
      11      90
      12      90
      13      90
      14      90
      15      90
      16      90
      17      90
      18      90
      19      90
      1a      90
      1b      90
      1c      90
      1d      90
      1e      90
      1f      90
      20      90
      21      90
      22      90
      23      90
      24      90
      25      90
      26      90
      27      90
      28      90
      29      90
      2a      90
      2b      90
      2c      90
      2d      90
      2e      90
      2f      90
      30      90
      31      90
      32      90
      33      90
      34      90
      35      90
      36      90
      37      90
      38      90
      39      90
      3a      90
      3b      90
      3f      92
      40      92
      41      92
      43      92
      44      92
      45      92
      46      92
      47      92
      48      92
      49      92
      4a      93
      4d      93
      4e      93
      4f      93
      50      93
      51      93
      53      93
      54      93
      55      93
      56      96
   }

   method 'testNoBranches (Ljava/lang/Object;)I' {
      7      99
      8      99
   }

   method 'booleanNightmares (ZZZZZZZ)V' {
      0      103
      3      103
      4      103
      5      103
      6      103
      7      103
      8      103
      9      103
      f      103
      10      103
      11      103
      12      104
      13      104
      14      104
      15      104
      16      104
      18      104
      19      104
      1a      104
      1e      105
      1f      105
      20      105
      21      105
      22      105
      23      105
      24      105
      25      105
      26      105
      27      105
      28      105
      2e      105
      2f      105
      30      105
      31      106
      32      106
      33      106
      34      106
      35      106
      37      106
      38      106
      39      106
      3d      107
      3e      107
      3f      107
      40      107
      41      107
      42      107
      43      107
      44      107
      45      107
      46      107
      47      107
      4d      107
      4e      107
      4f      107
      50      108
      51      108
      52      108
      53      108
      54      108
      56      108
      57      108
      58      108
      5c      109
      5d      109
      5e      109
      5f      109
      60      109
      61      110
      62      110
      63      110
      64      110
      65      110
      67      110
      68      110
      69      110
      6d      111
      6e      111
      6f      111
      70      111
      71      111
      72      111
      73      111
      74      111
      75      111
      76      111
      77      111
      78      111
      79      111
      7a      111
      7b      111
      7c      111
      7d      111
      7e      111
      84      111
      85      111
      86      111
      87      112
      88      112
      89      112
      8a      112
      8b      112
      8d      112
      8e      112
      8f      112
      93      114
      94      114
      95      114
      96      114
      97      114
      99      114
      9a      114
      9b      114
      9c      116
   }
}

Lines mapping:
5 <-> 5
6 <-> 5
7 <-> 7
8 <-> 9
9 <-> 11
10 <-> 14
12 <-> 16
16 <-> 19
17 <-> 21
18 <-> 23
19 <-> 25
20 <-> 27
21 <-> 29
22 <-> 32
24 <-> 34
27 <-> 37
28 <-> 37
29 <-> 38
30 <-> 40
31 <-> 42
32 <-> 42
35 <-> 46
38 <-> 49
39 <-> 49
40 <-> 49
41 <-> 49
42 <-> 49
43 <-> 49
48 <-> 53
49 <-> 53
50 <-> 56
51 <-> 56
52 <-> 56
53 <-> 56
55 <-> 57
56 <-> 58
60 <-> 63
61 <-> 64
62 <-> 65
63 <-> 66
65 <-> 70
66 <-> 71
69 <-> 74
70 <-> 75
71 <-> 76
72 <-> 77
74 <-> 79
77 <-> 82
78 <-> 83
79 <-> 84
80 <-> 85
81 <-> 86
83 <-> 88
86 <-> 91
88 <-> 93
89 <-> 94
92 <-> 97
95 <-> 100
96 <-> 100
101 <-> 104
102 <-> 104
103 <-> 106
104 <-> 108
105 <-> 110
106 <-> 112
107 <-> 115
109 <-> 117
Not mapped:
15
