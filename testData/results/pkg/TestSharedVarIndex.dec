package pkg;

import java.util.List;

public class TestSharedVarIndex {
   public void StringArrayVsString() {
      String[] b = new String[0];// 8
      String bx = "lol";// 11
   }// 12

   public void lambda(List<Integer> l) {
      int i = 0;// 16
      System.out.println(i);// 17
      l.forEach(ix -> System.out.println(ix + 1));// 19
   }// 20
}

class 'pkg/TestSharedVarIndex' {
   method 'StringArrayVsString ()V' {
      0      6
      4      6
      5      7
      6      7
      7      7
      8      8
   }

   method 'lambda (Ljava/util/List;)V' {
      0      11
      1      11
      2      12
      3      12
      4      12
      5      12
      6      12
      7      12
      8      12
      9      13
      f      13
      10      13
      11      13
      12      13
      13      13
      14      14
   }

   method 'lambda$lambda$0 (Ljava/lang/Integer;)V' {
      0      13
      1      13
      2      13
      3      13
      4      13
      5      13
      6      13
      7      13
      8      13
      9      13
      a      13
      b      13
      c      13
   }
}

Lines mapping:
8 <-> 7
11 <-> 8
12 <-> 9
16 <-> 12
17 <-> 13
19 <-> 14
20 <-> 15
