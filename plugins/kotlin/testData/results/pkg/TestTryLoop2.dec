package pkg

import java.io.File
import java.io.FileNotFoundException
import java.util.Scanner

public class TestTryLoop2 {
   private final val field: Boolean

   public fun test(file: File) {
      while (true) {
         try {
            if (!this.field) {// 13
               return// 24
            }

            Scanner(file)// 14
         } catch (var3: FileNotFoundException) {// 20
            var3.printStackTrace()// 21
         }
      }
   }
}

class 'pkg/TestTryLoop2' {
   method 'test (Ljava/io/File;)V' {
      8      12
      9      12
      a      12
      b      12
      c      12
      d      12
      e      12
      13      16
      1e      17
      1f      18
      20      18
      21      18
      22      18
      26      13
   }
}

Lines mapping:
13 <-> 13
14 <-> 17
20 <-> 18
21 <-> 19
24 <-> 14
Not mapped:
11
12
16
19
