package pkg;

public class TestArrayNullAccess {
   public int test() {
      byte[] array = new byte[4];// 5
      int n = array.length;// 6
      array = null;// 7
      return array[3];// 8
   }
}

class 'pkg/TestArrayNullAccess' {
   method 'test ()I' {
      0      4
      3      4
      4      5
      5      5
      6      5
      7      6
      8      6
      9      7
      a      7
      b      7
      c      7
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
7 <-> 7
8 <-> 8
