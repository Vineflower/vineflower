package pkg;

public class TestClassFields {
   private static int[] sizes = new int[TestClassFields.names.length];// 14
   private static String[] names = new String[]{"name1", "name2"};// 9
   private static final int SIZE = TestClassFields.Inner.staticMutable;// 16

   private static class Inner {
      private static int staticMutable = 3;// 5
   }
}

class 'pkg/TestClassFields' {
   method '<clinit> ()V' {
      6      4
      7      4
      b      4
      c      4
      11      3
      12      3
      13      3
      14      3
      1a      5
      1b      5
      1c      5
   }
}

class 'pkg/TestClassFields$Inner' {
   method '<clinit> ()V' {
      0      8
   }
}

Lines mapping:
5 <-> 9
9 <-> 5
14 <-> 4
16 <-> 6
Not mapped:
17
