package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryLoopReturnFinally {
   private boolean field;

   public void test(File file) {
      try {
         while (this.field) {// 12
            if (file == null) {// 13
               return;// 14
            }

            new Scanner(file);// 17
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();// 20
      } finally {
         System.out.println("Finally");// 22
      }
   }// 24
}

class 'pkg/TestTryLoopReturnFinally' {
   method 'test (Ljava/io/File;)V' {
      0      11
      1      11
      2      11
      3      11
      7      12
      8      12
      9      12
      a      12
      13      13
      18      16
      2c      19
      2d      19
      2e      19
      2f      19
      3c      21
      3d      21
      3e      21
      3f      21
      40      21
      41      21
      42      21
      43      21
      46      23
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
18
19
23
