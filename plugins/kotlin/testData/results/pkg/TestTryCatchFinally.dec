package pkg

public class TestTryCatchFinally {
   public fun test1(x: String?) {
      try {
         System.out.println("sout1")// 21
      } catch (var8: Exception) {
         try {
            System.out.println("sout2")// 24
         } catch (var7: Exception) {// 25
         }
      } finally {
         System.out.println("finally")// 31
      }
   }// 33

   @Throws(java/lang/Exception::class)
   public fun foo(a: Int): Int {
      if (a < 1) {// 37
         throw RuntimeException()// 38
      } else if (a < 5) {// 39
         return a// 40
      } else {
         throw Exception()// 42
      }
   }

   public fun test(a: String): Int {
      try {
         return Integer.parseInt(a)// 48
      } catch (var5: Exception) {// 49
         System.out.println("Error$var5")// 50
      } finally {
         System.out.println("Finally")// 52
      }

      return -1// 53 54
   }

   public fun testEmptyCatch(msg: String?) {
      try {
         System.out.println(msg)// 59
      } catch (var5: RuntimeException) {// 60
      } finally {
         System.out.println("Bye")// 62
      }
   }// 64
}

class 'pkg/TestTryCatchFinally' {
   method 'test1 (Ljava/lang/String;)V' {
      1      5
      2      5
      3      5
      4      5
      5      5
      7      5
      8      5
      9      5
      18      8
      19      8
      1b      8
      1c      8
      1d      8
      1e      8
      1f      8
      20      8
      21      8
      25      9
      33      12
      34      12
      35      12
      36      12
      37      12
      39      12
      3a      12
      3b      12
      3e      14
   }

   method 'foo (I)I' {
      0      18
      1      18
      2      18
      3      18
      4      18
      c      19
      d      20
      e      20
      f      20
      10      20
      11      20
      12      21
      13      21
      1b      23
   }

   method 'test (Ljava/lang/String;)I' {
      7      29
      8      29
      9      29
      a      29
      b      29
      17      30
      18      31
      19      31
      1a      31
      1b      31
      1c      31
      1d      31
      1e      31
      1f      31
      20      31
      22      31
      23      31
      24      31
      2e      36
      32      33
      33      33
      34      33
      35      33
      36      33
      38      33
      39      33
      3a      33
      3d      36
   }

   method 'testEmptyCatch (Ljava/lang/String;)V' {
      1      41
      2      41
      3      41
      4      41
      5      41
      6      41
      7      41
      14      42
      22      44
      23      44
      24      44
      25      44
      26      44
      28      44
      29      44
      2a      44
      2d      46
   }
}

Lines mapping:
21 <-> 6
24 <-> 9
25 <-> 10
31 <-> 13
33 <-> 15
37 <-> 19
38 <-> 20
39 <-> 21
40 <-> 22
42 <-> 24
48 <-> 30
49 <-> 31
50 <-> 32
52 <-> 34
53 <-> 37
54 <-> 37
59 <-> 42
60 <-> 43
62 <-> 45
64 <-> 47
Not mapped:
20
22
23
32
47
58
63
