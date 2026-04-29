package pkg

import java.io.IOException
import kotlin.jvm.internal.SourceDebugExtension

@SourceDebugExtension(["SMAP\nTestTryCatchExpressions.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TestTryCatchExpressions.kt\npkg/TestTryCatchExpressions\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,59:1\n1#2:60\n*E\n"])
public class TestTryCatchExpressions {
   public fun test0(s: String) {
      var var2: java.lang.String
      try {
         var2 = StringsKt.repeat(s as java.lang.CharSequence, 5)// 9
      } catch (var4: RuntimeException) {// 10
         var var10000: java.lang.String = var4.getMessage()// 11
         if (var10000 == null) {
            var10000 = "ERROR"
         }

         var2 = var10000
      }

      System.out.print(var2)// 7
   }// 14

   public fun test1(a: String, b: String) {
      var x: java.lang.String = a// 17
      var var6: TestTryCatchExpressions = this// 19

      var var4: java.lang.String
      var var10000: TestTryCatchExpressions
      try {
         var10000 = var6
         var4 = StringsKt.repeat(x as java.lang.CharSequence, 5)// 20
      } catch (var9: RuntimeException) {// 21
         var10000 = this
         x = b// 22
         var var10001: java.lang.String = var9.getMessage()// 23
         if (var10001 == null) {
            var10001 = "ERROR"
         }

         var4 = var10001
      }

      val var7: java.lang.String = var4// 24
      var6 = var10000

      var var13: java.lang.String
      try {
         var10000 = var6
         var13 = var7
         var4 = StringsKt.repeat(x as java.lang.CharSequence, 5)// 25
      } catch (var8: RuntimeException) {// 26
         var10000 = var10000
         var13 = var4
         var var10002: java.lang.String = var8.getMessage()// 27
         if (var10002 == null) {
            var10002 = "ERROR"
         }

         var4 = var10002
      }

      var10000.test0("$var13$var4")// 18
   }// 30

   public fun test2(a: String, b: String) {
      var var15: java.lang.String = a// 33
      var var7: TestTryCatchExpressions = this

      var var4: java.lang.String
      var var10000: TestTryCatchExpressions
      try {
         var10000 = var7
         if (a.length() != b.length()) {// 35
            return// 36
         }

         var15 = b// 38
         this.test0(b)// 39
         var4 = b// 40
      } catch (var13: IOException) {// 41
         var10000 = this
         var15 = a// 42
         this.test1(a, a)// 43
         var4 = a// 44
      } catch (var14: RuntimeException) {// 46
         var10000 = this// 45
         var15 = if (var15 == a) b else a// 47 49
         var4 = var15// 51
      }

      val var8: java.lang.String = var4// 52
      var7 = var10000

      var var10001: java.lang.String
      try {
         var4 = StringsKt.repeat(var15 as java.lang.CharSequence, 5)
         var15 = var4
         var10000 = var7
         var10001 = var8
         var4 = var4// 53
      } catch (var12: RuntimeException) {// 54
         var10000 = var10000
         var10001 = var4
         System.out.println(var15)// 55
         var var10003: java.lang.String = var12.getMessage()
         if (var10003 == null) {
            var10003 = ""
         }

         var4 = "$var15!!$var10003"// 56
      }

      var10000.test1(var10001, var4)// 34
   }// 58
}

class 'pkg/TestTryCatchExpressions' {
   method 'test0 (Ljava/lang/String;)V' {
      7      10
      8      10
      9      10
      a      10
      b      10
      c      10
      d      10
      e      10
      f      10
      13      11
      14      12
      15      12
      16      12
      17      12
      19      13
      1a      13
      1b      13
      1d      14
      1e      14
      1f      17
      20      20
      22      20
      23      20
      24      20
      25      20
      26      20
      27      20
      28      20
      29      21
   }

   method 'test1 (Ljava/lang/String;Ljava/lang/String;)V' {
      c      24
      d      24
      e      25
      f      25
      10      25
      12      30
      13      30
      14      31
      15      31
      16      31
      17      31
      18      31
      19      31
      1a      31
      1b      31
      1c      31
      1d      31
      21      32
      23      33
      24      33
      25      34
      26      34
      27      35
      28      35
      29      35
      2a      35
      2b      35
      2d      36
      2e      36
      2f      36
      31      37
      32      37
      33      40
      34      40
      35      43
      36      43
      37      43
      38      43
      39      44
      3a      44
      3c      48
      3d      48
      3e      49
      3f      49
      40      50
      41      50
      42      50
      43      50
      44      50
      45      50
      46      50
      47      50
      48      50
      49      50
      4d      51
      4f      52
      50      52
      51      53
      52      53
      53      54
      54      54
      55      54
      56      54
      57      54
      59      55
      5a      55
      5b      55
      5d      56
      5e      56
      5f      59
      60      59
      61      62
      62      62
      63      62
      64      62
      65      62
      66      62
      67      62
      68      62
      69      62
      6a      62
      6b      63
   }

   method 'test2 (Ljava/lang/String;Ljava/lang/String;)V' {
      e      66
      f      66
      10      67
      11      67
      12      67
      14      72
      15      72
      16      73
      17      73
      18      73
      19      73
      1a      73
      1b      73
      1c      73
      1d      73
      1e      73
      1f      73
      20      73
      21      74
      22      77
      23      77
      24      78
      25      78
      26      78
      27      78
      28      78
      29      79
      2a      79
      2b      79
      2f      80
      31      81
      32      81
      33      82
      34      82
      35      83
      36      83
      37      83
      38      83
      39      83
      3a      83
      3b      83
      3c      83
      3d      83
      3e      84
      3f      84
      40      84
      41      84
      42      84
      43      84
      49      86
      4a      86
      4b      87
      4c      87
      50      87
      51      87
      52      87
      53      87
      57      87
      58      85
      59      88
      5a      88
      5b      88
      5c      88
      5d      88
      5e      88
      5f      91
      60      91
      61      91
      62      91
      63      92
      64      92
      66      98
      67      98
      68      99
      69      99
      6a      96
      6b      96
      6c      96
      6d      96
      6e      96
      6f      96
      70      96
      71      96
      72      96
      73      96
      74      97
      75      97
      7f      97
      80      97
      81      97
      87      98
      88      98
      89      99
      8a      99
      8b      100
      8c      100
      8d      100
      8e      100
      92      101
      94      102
      95      102
      96      103
      97      103
      98      104
      99      104
      9a      104
      9b      104
      9d      104
      9e      104
      9f      104
      a0      110
      a1      105
      a2      105
      a3      105
      a4      105
      a5      105
      a7      106
      a8      106
      a9      106
      ab      107
      ac      107
      ad      110
      ae      110
      af      110
      b0      110
      b1      110
      b2      110
      b3      110
      b4      113
      b5      113
      b6      113
      b7      113
      b8      113
      b9      114
   }
}

Lines mapping:
7 <-> 21
9 <-> 11
10 <-> 12
11 <-> 13
14 <-> 22
17 <-> 25
18 <-> 63
19 <-> 26
20 <-> 32
21 <-> 33
22 <-> 35
23 <-> 36
24 <-> 44
25 <-> 51
26 <-> 52
27 <-> 55
30 <-> 64
33 <-> 67
34 <-> 114
35 <-> 74
36 <-> 75
38 <-> 78
39 <-> 79
40 <-> 80
41 <-> 81
42 <-> 83
43 <-> 84
44 <-> 85
45 <-> 87
46 <-> 86
47 <-> 88
49 <-> 88
51 <-> 89
52 <-> 92
53 <-> 101
54 <-> 102
55 <-> 105
56 <-> 111
58 <-> 115
Not mapped:
8
60
