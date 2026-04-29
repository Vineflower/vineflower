package pkg

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.Scanner

public class TestTryLoopSimpleFinally {
   private final val field: Boolean

   public fun test(file: File) {
      try {
         while (this.field) {// 14
            Scanner(file)// 15
         }
      } catch (var5: FileNotFoundException) {// 17
         var5.printStackTrace()// 18
      } finally {
         System.out.println("Finally")// 20
      }
   }// 22

   @Throws(java/io/IOException::class)
   public fun test2(x: Int, file: Path) {
      var xx: Int = x// 26

      try {
         while (xx >= 0) {// 28
            val scanner: Scanner = Scanner(file)// 29
            if (xx % 11 == 0) {// 31
               System.out.println("nice")// 32
               return// 33
            }

            xx -= scanner.nextInt()// 36
         }
      } finally {
         System.out.println("Finally")// 39
      }
   }// 40
}

class 'pkg/TestTryLoopSimpleFinally' {
   method 'test (Ljava/io/File;)V' {
      7      12
      8      12
      9      12
      a      12
      12      13
      26      15
      27      16
      28      16
      29      16
      2a      16
      38      18
      39      18
      3a      18
      3b      18
      3c      18
      3e      18
      3f      18
      40      18
      43      20
   }

   method 'test2 (ILjava/nio/file/Path;)V' {
      6      24
      7      24
      9      27
      a      27
      b      27
      c      27
      11      28
      15      28
      16      28
      17      29
      18      29
      19      29
      1a      29
      1b      29
      1c      29
      1d      29
      1e      30
      1f      30
      20      30
      21      30
      22      30
      24      30
      25      30
      26      30
      31      31
      33      34
      34      34
      35      34
      36      34
      37      34
      39      34
      46      39
      4b      37
      4c      37
      4d      37
      4e      37
      4f      37
      51      37
      52      37
      53      37
   }
}

Lines mapping:
14 <-> 13
15 <-> 14
17 <-> 16
18 <-> 17
20 <-> 19
22 <-> 21
26 <-> 25
28 <-> 28
29 <-> 29
31 <-> 30
32 <-> 31
33 <-> 32
36 <-> 35
39 <-> 38
40 <-> 40
Not mapped:
13
21
27
41
