package pkg;

public class TestStaticBlockNull {
   static final String a;
   static final String b;

   static {
      String s = null;// 8
      b = s;// 9
      a = s;
   }// 10
}

class 'pkg/TestStaticBlockNull' {
   method '<clinit> ()V' {
      0      7
      1      7
      2      8
      4      8
      5      8
      6      8
      7      9
      8      9
      9      9
      a      10
   }
}

Lines mapping:
8 <-> 8
9 <-> 9
10 <-> 11
