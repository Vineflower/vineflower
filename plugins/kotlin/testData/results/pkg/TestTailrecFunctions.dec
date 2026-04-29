package pkg

public class TestTailrecFunctions {
   public tailrec fun sum(x: Long, sum: Long): Long {
      var var5: TestTailrecFunctions = this// 4

      while (x != 0L) {// 5
         val var7: Long = x - 1L
         val var9: Long = sum + x
         var5 = var5// 6
         x = var7
         sum = var9
      }

      return sum
   }

   public tailrec fun testFinally() {
      try {
         ;
      } finally {
         this.testFinally()// 13
      }
   }// 14

   public tailrec fun testFinallyReturn(): Int {
      try {
         ;
      } finally {
         return this.testFinallyReturn()// 21
      }
   }

   public tailrec fun fooTry() {
      try {
         this.fooTry()// 27
      } catch (var2: java.lang.Throwable) {// 29
      }
   }// 31

   public tailrec fun testTryCatchFinally() {
      try {
         this.testTryCatchFinally()// 35
      } catch (var4: Exception) {// 36
         this.testTryCatchFinally()// 37
      } finally {
         this.testTryCatchFinally()// 39
      }
   }// 41

   public tailrec fun fastPow(x: Long, n: Long, acc: Long = 1L): Long {
      var var7: TestTailrecFunctions = this// 43

      while (n != 0L) {// 44
         if (n % 2 == 0L) {// 45
            val var9: Long = x * x
            val var11: Long = n / 2
            var7 = var7
            x = var9
            n = var11
            acc = acc
         } else {
            val var17: Long = n - 1L
            val var18: Long = acc * x
            var7 = var7// 46
            x = x
            n = var17
            acc = var18
         }
      }

      return acc
   }

   public tailrec fun fastPow(x: Long, n: Long): Long {
      var var5: TestTailrecFunctions = this// 49

      var var10000: Long
      while (true) {
         if (n == 0L) {// 50
            var10000 = 1L
            break
         }

         if (n % 2 != 0L) {// 51
            var10000 = x * var5.fastPow(x, n - 1L)// 52
            break
         }

         val var7: Long = x * x
         val var9: Long = n / 2
         var5 = var5
         x = var7
         n = var9
      }

      return var10000// 53
   }
}

class 'pkg/TestTailrecFunctions' {
   method 'sum (JJ)J' {
      0      4
      1      4
      2      4
      3      6
      4      6
      5      6
      9      14
      a      14
      b      9
      c      9
      f      7
      10      7
      11      7
      12      7
      13      7
      14      8
      15      8
      16      8
      17      8
      18      8
      19      9
      1a      9
      1b      9
      1c      9
      1d      10
      1e      10
      1f      10
      20      11
      21      11
      22      11
   }

   method 'testFinally ()V' {
      5      23
      9      21
      a      21
      b      21
      c      21
   }

   method 'testFinallyReturn ()I' {
      7      29
      8      29
      9      29
      a      29
      b      29
   }

   method 'fooTry ()V' {
      1      35
      2      35
      3      35
      4      35
      5      38
      6      36
      7      38
   }

   method 'testTryCatchFinally ()V' {
      1      42
      2      42
      3      42
      4      42
      c      43
      d      44
      e      44
      f      44
      10      44
      19      46
      1a      46
      1b      46
      1c      46
      1f      48
   }

   method 'fastPow (JJJ)J' {
      0      51
      1      51
      2      51
      3      53
      4      53
      5      53
      7      50
      9      71
      a      71
      b      71
      c      54
      d      54
      e      54
      f      54
      10      54
      11      54
      12      54
      13      54
      14      54
      15      57
      16      57
      19      55
      1a      55
      1b      55
      1c      55
      1d      55
      1e      56
      1f      56
      20      56
      21      56
      22      56
      23      56
      24      60
      25      60
      28      57
      29      57
      2a      57
      2b      57
      2c      58
      2d      58
      2e      58
      2f      59
      30      59
      31      59
      32      60
      33      60
      34      60
      35      60
      39      64
      3a      64
      3d      65
      40      62
      41      62
      42      62
      43      62
      44      62
      45      63
      46      63
      47      63
      48      63
      49      63
      4a      63
      4b      64
      4c      64
      4d      64
      4e      64
      4f      65
      50      65
      51      65
      52      66
      53      66
      54      66
      55      67
      56      67
      57      67
      58      67
   }

   method 'fastPow (JJ)J' {
      0      75
      1      75
      2      75
      3      79
      4      79
      5      79
      6      79
      7      79
      8      79
      9      80
      a      81
      d      84
      e      84
      f      84
      10      84
      11      84
      12      84
      13      84
      14      84
      15      84
      16      91
      17      91
      1a      89
      1b      89
      1c      89
      1d      89
      1e      89
      1f      90
      20      90
      21      90
      22      90
      23      90
      24      90
      25      91
      26      91
      27      91
      28      91
      29      92
      2a      92
      2b      92
      2c      93
      2d      93
      2e      93
      32      85
      33      85
      34      85
      35      85
      36      85
      37      85
      38      85
      39      85
      3a      85
      3b      85
      3c      85
      3d      96
   }
}

Lines mapping:
4 <-> 5
5 <-> 7
6 <-> 10
13 <-> 22
14 <-> 24
21 <-> 30
27 <-> 36
29 <-> 37
31 <-> 39
35 <-> 43
36 <-> 44
37 <-> 45
39 <-> 47
41 <-> 49
43 <-> 52
44 <-> 54
45 <-> 55
46 <-> 65
49 <-> 76
50 <-> 80
51 <-> 85
52 <-> 86
53 <-> 97
Not mapped:
10
15
18
26
34
40
