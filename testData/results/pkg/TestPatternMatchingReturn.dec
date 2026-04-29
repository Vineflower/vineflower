package pkg;

public class TestPatternMatchingReturn {
   public String getPattern(Object obj) {
      if (obj instanceof String s) {// 5
         return s;// 6
      } else {
         System.out.println("filler");// 9
         return null;// 11
      }
   }

   public String get(Object obj) {
      if (obj instanceof String) {// 15
         return (String)obj;// 16
      }

      System.out.println("filler");// 19
      return null;// 21
   }
}

class 'pkg/TestPatternMatchingReturn' {
   method 'getPattern (Ljava/lang/Object;)Ljava/lang/String;' {
      0      4
      3      4
      4      4
      5      4
      6      4
      7      4
      8      4
      d      4
      e      5
      f      5
      10      7
      11      7
      12      7
      13      7
      14      7
      15      7
      16      7
      17      7
      18      8
      19      8
   }

   method 'get (Ljava/lang/Object;)Ljava/lang/String;' {
      0      13
      1      13
      2      13
      3      13
      4      13
      5      13
      6      13
      7      14
      8      14
      9      14
      a      14
      b      14
      c      17
      d      17
      e      17
      f      17
      10      17
      11      17
      12      17
      13      17
      14      18
      15      18
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
9 <-> 8
11 <-> 9
15 <-> 14
16 <-> 15
19 <-> 18
21 <-> 19
