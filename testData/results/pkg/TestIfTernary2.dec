package pkg;

public class TestIfTernary2 {
   public boolean test(Object a1, Object a2, Object b1, Object b2) {
      return (a1 == null ? b1 == null : b2.equals(b1)) && (a2 == null ? b2 == null : b1.equals(a1));// 9 10
   }

   public String test1(Object a1, Object a2, Object b1, Object b2) {
      if (a1 == null ? b1 == null : b2 == null) {// 19
         return (a2 == null ? b2 != null : b1 != null) ? "2" : "1";// 20 21 24
      } else {
         return "3";// 27
      }
   }

   public void test2(Object a1, Object a2, Object b1, Object b2) {
      if (a1 == null ? b1 == null : b2 == null) {// 31
         if (a2 == null ? b2 == null : b1 == null) {// 32
            System.out.println(1);// 33
         }

         System.out.println(2);// 36
      }

      System.out.println(3);// 39
   }// 40

   public String test3(Object a1, Object a2, Object b1, Object b2) {
      if (a1 == null ? b1 == null : b2 == null) {// 43
         System.out.println(2);// 44

         while (a1 == a2) {// 46
            a1 = a2.toString();// 47
         }

         if (b2 == null) {// 50
            System.out.println("hello");// 51
         }
      }

      return "3";// 55
   }
}

class 'pkg/TestIfTernary2' {
   method 'test (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z' {
      0      4
      1      4
      2      4
      3      4
      4      4
      5      4
      6      4
      7      4
      b      4
      c      4
      d      4
      e      4
      f      4
      10      4
      14      4
      15      4
      16      4
      17      4
      18      4
      19      4
      1a      4
      1b      4
      1c      4
      20      4
      21      4
      22      4
      23      4
      24      4
   }

   method 'test1 (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/String;' {
      0      8
      1      8
      2      8
      3      8
      4      8
      5      8
      6      8
      7      8
      b      8
      c      8
      d      8
      e      8
      f      8
      10      9
      11      9
      12      9
      13      9
      14      9
      15      9
      16      9
      17      9
      18      9
      1c      9
      1d      9
      1e      9
      1f      9
      20      9
      21      9
      23      9
      24      9
      26      11
      27      11
      28      11
   }

   method 'test2 (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V' {
      0      16
      1      16
      2      16
      3      16
      4      16
      5      16
      6      16
      7      16
      b      16
      c      16
      d      16
      e      16
      f      16
      10      17
      11      17
      12      17
      13      17
      14      17
      15      17
      16      17
      17      17
      18      17
      1c      17
      1d      17
      1e      17
      1f      17
      20      18
      21      18
      22      18
      23      18
      24      18
      25      18
      26      18
      27      21
      28      21
      29      21
      2a      21
      2b      21
      2c      21
      2d      21
      2e      24
      2f      24
      30      24
      31      24
      32      24
      33      24
      34      24
      35      25
   }

   method 'test3 (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/String;' {
      0      28
      1      28
      2      28
      3      28
      4      28
      5      28
      6      28
      7      28
      b      28
      c      28
      d      28
      e      28
      f      28
      10      29
      11      29
      12      29
      13      29
      14      29
      15      29
      16      29
      17      31
      18      31
      19      31
      1a      31
      1b      31
      1c      32
      1d      32
      1e      32
      1f      32
      20      32
      24      35
      25      35
      26      35
      27      35
      28      35
      29      36
      2a      36
      2b      36
      2c      36
      2d      36
      2e      36
      2f      36
      30      36
      31      40
      32      40
      33      40
   }
}

Lines mapping:
9 <-> 5
10 <-> 5
19 <-> 9
20 <-> 10
21 <-> 10
24 <-> 10
27 <-> 12
31 <-> 17
32 <-> 18
33 <-> 19
36 <-> 22
39 <-> 25
40 <-> 26
43 <-> 29
44 <-> 30
46 <-> 32
47 <-> 33
50 <-> 36
51 <-> 37
55 <-> 41
Not mapped:
11
15
