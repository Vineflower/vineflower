package pkg

public class TestTryCatchThrowable {
   public fun testEmptyCatch() {
      try {
         System.out.println("Hi")// 6
      } catch (var2: java.lang.Throwable) {// 7
      }
   }// 9

   public fun testEmptyCatchWithTail() {
      try {
         System.out.println("Hi")// 13
      } catch (var2: java.lang.Throwable) {// 14
      }

      System.out.println("tail")// 17
   }// 18

   public fun testCatchWithReturn(): String? {
      try {
         System.out.println("Hi")// 22
         return "5"// 23
      } catch (var2: java.lang.Throwable) {// 24
         return null// 25
      }
   }

   public fun testCatchWithReturnAndTail(): String? {
      try {
         System.out.println("Hi")// 31
      } catch (var2: java.lang.Throwable) {// 32
         return null// 33
      }

      System.out.println("Ho")// 35
      return "bye"// 36
   }

   public fun testCatchNested(): String? {
      try {
         System.out.println("Hi")// 41
         return "hello"// 42
      } catch (var4: java.lang.Throwable) {
         try {
            System.out.println("Hi!")// 45
            return "5"// 46
         } catch (var3: java.lang.Throwable) {// 47
            return var3.getMessage()// 48
         }
      }
   }

   public fun testCatchWithRethrow(): String {
      try {
         System.out.println("Hi")// 55
         return "5"// 56
      } catch (var2: java.lang.Throwable) {// 57
         System.out.println("Oh no")// 58
         throw var2// 59
      }
   }

   public fun testCatchAndOtherEmptyCatch() {
      try {
         System.out.println("Hi")// 66
      } catch (var2: RuntimeException) {// 67
      } catch (var3: java.lang.Throwable) {// 68
         System.out.println("Oh no")// 69
      }
   }// 71

   public fun testCatchAndOtherNonEmptyCatch() {
      try {
         System.out.println("Hi")// 75
      } catch (var2: RuntimeException) {// 76
         System.out.println("Hello")// 77
      } catch (var3: java.lang.Throwable) {// 78
         System.out.println("Oh no")// 79
      }
   }// 81
}

class 'pkg/TestTryCatchThrowable' {
   method 'testEmptyCatch ()V' {
      1      5
      2      5
      4      5
      5      5
      6      5
      7      5
      8      5
      9      5
      a      5
      e      6
      f      8
   }

   method 'testEmptyCatchWithTail ()V' {
      1      12
      2      12
      4      12
      5      12
      6      12
      7      12
      8      12
      9      12
      a      12
      e      13
      f      16
      10      16
      11      16
      12      16
      13      16
      15      16
      16      16
      17      16
      18      17
   }

   method 'testCatchWithReturn ()Ljava/lang/String;' {
      1      21
      2      21
      3      21
      4      21
      5      21
      7      21
      8      21
      9      21
      a      22
      b      22
      c      22
      d      23
      e      24
      f      24
   }

   method 'testCatchWithReturnAndTail ()Ljava/lang/String;' {
      1      30
      2      30
      3      30
      4      30
      5      30
      7      30
      8      30
      9      30
      d      31
      e      32
      f      32
      10      35
      11      35
      12      35
      13      35
      14      35
      16      35
      17      35
      18      35
      19      36
      1a      36
      1b      36
   }

   method 'testCatchNested ()Ljava/lang/String;' {
      1      41
      2      41
      3      41
      4      41
      5      41
      7      41
      8      41
      9      41
      a      42
      b      42
      c      42
      f      45
      10      45
      11      45
      12      45
      13      45
      15      45
      16      45
      17      45
      18      46
      19      46
      1a      46
      1b      47
      1c      48
      1d      48
      1e      48
      1f      48
      20      48
   }

   method 'testCatchWithRethrow ()Ljava/lang/String;' {
      1      55
      2      55
      3      55
      4      55
      5      55
      7      55
      8      55
      9      55
      a      56
      b      56
      c      56
      d      57
      e      58
      f      58
      10      58
      11      58
      12      58
      14      58
      15      58
      16      58
      17      59
      18      59
   }

   method 'testCatchAndOtherEmptyCatch ()V' {
      1      65
      2      65
      3      65
      4      65
      5      65
      7      65
      8      65
      9      65
      d      66
      11      67
      12      68
      13      68
      14      68
      15      68
      16      68
      18      68
      19      68
      1a      68
      1b      70
   }

   method 'testCatchAndOtherNonEmptyCatch ()V' {
      1      74
      2      74
      3      74
      4      74
      5      74
      7      74
      8      74
      9      74
      d      75
      e      76
      f      76
      10      76
      11      76
      12      76
      14      76
      15      76
      16      76
      1a      77
      1b      78
      1c      78
      1d      78
      1e      78
      1f      78
      21      78
      22      78
      23      78
      24      80
   }
}

Lines mapping:
6 <-> 6
7 <-> 7
9 <-> 9
13 <-> 13
14 <-> 14
17 <-> 17
18 <-> 18
22 <-> 22
23 <-> 23
24 <-> 24
25 <-> 25
31 <-> 31
32 <-> 32
33 <-> 33
35 <-> 36
36 <-> 37
41 <-> 42
42 <-> 43
45 <-> 46
46 <-> 47
47 <-> 48
48 <-> 49
55 <-> 56
56 <-> 57
57 <-> 58
58 <-> 59
59 <-> 60
66 <-> 66
67 <-> 67
68 <-> 68
69 <-> 69
71 <-> 71
75 <-> 75
76 <-> 76
77 <-> 77
78 <-> 78
79 <-> 79
81 <-> 81
Not mapped:
5
12
21
30
40
43
44
54
65
74
