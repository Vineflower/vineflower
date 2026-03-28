package pkg

import java.io.File
import java.io.FileNotFoundException
import java.util.Scanner

public class TestTryLoopReturnFinally {
   private final val field: Boolean

   public fun test(file: File?) {
      try {
         while (this.field) {// 12
            if (file == null) {// 13
               return// 14
            }

            Scanner(file)// 17
         }
      } catch (var5: FileNotFoundException) {
         var5.printStackTrace()// 20
      } finally {
         System.out.println("Finally")// 22
      }
   }// 24
}

class 'pkg/TestTryLoopReturnFinally' {
   method 'test (Ljava/io/File;)V' {
      1      11
      2      11
      3      11
      4      11
      8      12
      9      12
      a      12
      b      12
      16      13
      1b      16
      30      19
      31      19
      32      19
      33      19
      41      21
      42      21
      43      21
      44      21
      45      21
      47      21
      48      21
      49      21
      4c      23
   }
}

Lines mapping:
12 <-> 12
13 <-> 13
14 <-> 14
17 <-> 17
20 <-> 20
22 <-> 22
24 <-> 24
Not mapped:
11
19
23
