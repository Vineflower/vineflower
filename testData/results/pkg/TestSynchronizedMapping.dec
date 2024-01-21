package pkg;

public class TestSynchronizedMapping {
   public int test(int a) {
      synchronized (this) {// 8
         a++;// 9
      }// 10

      return a++;// 11
   }

   public void test2(String a) {
      System.out.println(a);// 15
   }// 16
}

class 'pkg/TestSynchronizedMapping' {
   method 'test (I)I' {
      0      4
      3      4
      4      5
      5      5
      6      5
      8      6
      12      8
      13      8
      14      8
      15      8
   }

   method 'test2 (Ljava/lang/String;)V' {
      0      12
      1      12
      2      12
      3      12
      4      12
      5      12
      6      12
      7      13
   }
}

Lines mapping:
8 <-> 5
9 <-> 6
10 <-> 7
11 <-> 9
15 <-> 13
16 <-> 14
