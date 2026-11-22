package pkg

import java.io.File
import java.io.FileNotFoundException
import java.util.Scanner

public class TestTryLoopRecompile {
   private final val field: Boolean

   public fun test(file: File) {
      while (true) {
         try {
            if (!this.field) {// 13
               break
            }

            Scanner(file)// 14
         } catch (var3: FileNotFoundException) {// 17
            var3.printStackTrace()// 18
         }
      }
   }// 21
}

class 'pkg/TestTryLoopRecompile' {
   method 'test (Ljava/io/File;)V' {
      8      12
      9      12
      a      12
      b      12
      c      12
      d      12
      e      12
      13      16
      1b      17
      1c      18
      1d      18
      1e      18
      1f      18
      20      21
   }
}

Lines mapping:
13 <-> 13
14 <-> 17
17 <-> 18
18 <-> 19
21 <-> 22
Not mapped:
11
12
15
