package pkg

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Scanner
import java.util.ServiceConfigurationError

public class TestTryLoop {
   private final val field: Boolean

   public fun test(file: File) {
      try {
         while (this.field) {// 14
            Scanner(file)// 15
         }
      } catch (var3: FileNotFoundException) {// 17
         var3.printStackTrace()// 18
      }
   }// 20

   public fun hasNext(p: Path, f: MutableIterator<File?>): Boolean {
      while (true) {
         try {
            if (Files.exists(p, arrayOfNulls(0))) {// 26
               val var8: File = f.next() as File// 27
               return true// 28
            }

            return false// 29
         } catch (var6: ServiceConfigurationError) {// 30
            System.out.println(1)// 31
         } catch (var7: NoClassDefFoundError) {// 32
            System.out.println(2)// 33
         }
      }
   }
}

class 'pkg/TestTryLoop' {
   method 'test (Ljava/io/File;)V' {
      7      14
      8      14
      9      14
      a      14
      12      15
      1a      17
      1b      18
      1c      18
      1d      18
      1e      18
      1f      20
   }

   method 'hasNext (Ljava/nio/file/Path;Ljava/util/Iterator;)Z' {
      10      25
      11      25
      15      25
      16      25
      17      25
      18      25
      19      25
      1a      25
      1b      26
      1c      26
      1d      26
      1e      26
      1f      26
      20      26
      21      26
      22      26
      23      26
      24      26
      25      27
      26      27
      27      30
      28      30
      29      31
      2b      32
      2e      32
      2f      32
      30      32
      31      32
      32      32
      33      32
      34      32
      35      32
      39      33
      3b      34
      3e      34
      3f      34
      40      34
      41      34
      42      34
      43      34
      44      34
      45      34
   }
}

Lines mapping:
14 <-> 15
15 <-> 16
17 <-> 18
18 <-> 19
20 <-> 21
26 <-> 26
27 <-> 27
28 <-> 28
29 <-> 31
30 <-> 32
31 <-> 33
32 <-> 34
33 <-> 35
Not mapped:
13
23
24
25
