public class TestJava1Synchronized {
   public void test1(int var1) {
      synchronized (this) {// 3
         if (var1 == 0) {// 4
            System.out.println("0");// 5
            return;
         }

         System.out.println("1");// 9
      }

      System.out.println("2");// 12
   }

   public void test2(int var1) {
      synchronized (this) {// 16
         for (int var4 = 0; var4 < var1; var4++) {// 17
            System.out.println("hello");// 18
         }
      }
   }

   public void test3() {
      try {
         synchronized (this) {// 24 25
            System.out.println("hello");// 26
         }
      } finally {
         System.out.println("finally");// 29
      }
   }

   public void test4() {
      try {
         System.out.println("try");// 34 35
      } finally {
         synchronized (this) {// 37
            System.out.println("hello");// 38
         }
      }
   }
}

class 'TestJava1Synchronized' {
   method 'test1 (I)V' {
      0      2
      2      2
      3      2
      4      3
      5      3
      6      3
      7      3
      8      4
      9      4
      a      4
      b      4
      c      4
      d      4
      e      4
      f      4
      14      8
      15      8
      16      8
      17      8
      18      8
      19      8
      1a      8
      1b      8
      1e      11
      1f      11
      20      11
      26      5
      29      11
      2a      11
      2c      11
      2d      11
      2e      11
   }

   method 'test2 (I)V' {
      0      15
      2      15
      3      15
      4      16
      5      16
      6      16
      a      17
      b      17
      c      17
      d      17
      e      17
      f      17
      10      17
      11      17
      12      16
      13      16
      14      16
      15      16
      16      16
      17      16
      18      16
      19      16
      1a      16
      1d      20
   }

   method 'test3 ()V' {
      0      24
      2      24
      3      24
      4      25
      5      25
      6      25
      7      25
      8      25
      9      25
      a      25
      b      25
      19      28
      1a      28
      1b      28
      1e      28
      1f      28
      20      28
      21      28
      24      30
   }

   method 'test4 ()V' {
      0      34
      1      34
      2      34
      3      34
      4      34
      5      34
      6      34
      7      34
      d      36
      13      36
      14      36
      17      37
      18      37
      19      37
      1a      37
      1b      37
      1c      37
      1d      37
      1e      37
      20      38
      21      40
   }
}

Lines mapping:
3 <-> 3
4 <-> 4
5 <-> 5
9 <-> 9
12 <-> 12
16 <-> 16
17 <-> 17
18 <-> 18
24 <-> 25
25 <-> 25
26 <-> 26
29 <-> 29
34 <-> 35
35 <-> 35
37 <-> 37
38 <-> 38
Not mapped:
2
6
