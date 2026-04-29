package pkg;

public class TestArrayFieldAccess {
   private int[] array = new int[10];// 4
   private int index = 1;// 5
   private int value;

   public void test() {
      this.value = this.array[this.index]++;// 9
   }// 10

   public void test1() {
      this.value = ++this.array[this.index];// 13
   }// 14

   public void test2() {
      this.value = this.array[this.index]++;// 17
      if (this.value != 2) {// 19
         System.out.println(this.value);// 23
      }
   }// 20 24

   public void test3() {
      int i = this.array[this.index]++;// 27 28
      this.value = i;// 29
   }// 30

   public void test4() {
      this.value = ++this.array[this.index];// 33
   }// 34

   public void test5() {
      if (this.array[this.index]++ == 3) {// 37
         System.out.println(this.array[this.index]);// 38
      }
   }// 40

   public void test6() {
      if (++this.array[this.index] == 3) {// 43
         System.out.println(this.array[this.index]);// 44
      }
   }// 46
}

class 'pkg/TestArrayFieldAccess' {
   method '<init> ()V' {
      5      3
      6      3
      d      4
   }

   method 'test ()V' {
      0      8
      1      8
      2      8
      3      8
      4      8
      5      8
      6      8
      7      8
      8      8
      a      8
      d      8
      f      8
      10      8
      11      8
      12      9
   }

   method 'test1 ()V' {
      0      12
      1      12
      2      12
      3      12
      4      12
      5      12
      6      12
      7      12
      8      12
      a      12
      c      12
      e      12
      f      12
      10      12
      11      12
      12      13
   }

   method 'test2 ()V' {
      0      16
      1      16
      2      16
      3      16
      4      16
      5      16
      6      16
      7      16
      8      16
      a      16
      d      16
      f      16
      10      16
      11      16
      12      17
      13      17
      14      17
      15      17
      16      17
      17      17
      18      17
      19      17
      1a      20
      1b      18
      1c      18
      1d      18
      1e      18
      1f      18
      20      18
      21      18
      22      18
      23      18
      24      18
      25      20
   }

   method 'test3 ()V' {
      0      23
      1      23
      2      23
      3      23
      4      23
      5      23
      6      23
      7      23
      8      23
      9      23
      14      23
      16      24
      17      24
      18      24
      19      24
      1a      24
      1b      25
   }

   method 'test4 ()V' {
      0      28
      9      28
      a      28
      b      28
      c      28
      d      28
      e      28
      f      28
      10      28
      11      28
      13      28
      15      28
      16      28
      17      28
      18      28
      19      29
   }

   method 'test5 ()V' {
      0      32
      1      32
      2      32
      3      32
      4      32
      5      32
      6      32
      7      32
      9      32
      c      32
      e      32
      f      32
      10      32
      11      32
      12      33
      13      33
      14      33
      15      33
      16      33
      17      33
      18      33
      19      33
      1a      33
      1b      33
      1c      33
      1d      33
      1e      33
      1f      33
      20      33
      21      35
   }

   method 'test6 ()V' {
      0      38
      1      38
      2      38
      3      38
      4      38
      5      38
      6      38
      7      38
      9      38
      b      38
      d      38
      e      38
      f      38
      10      38
      11      38
      12      39
      13      39
      14      39
      15      39
      16      39
      17      39
      18      39
      19      39
      1a      39
      1b      39
      1c      39
      1d      39
      1e      39
      1f      39
      20      39
      21      41
   }
}

Lines mapping:
4 <-> 4
5 <-> 5
9 <-> 9
10 <-> 10
13 <-> 13
14 <-> 14
17 <-> 17
19 <-> 18
20 <-> 21
23 <-> 19
24 <-> 21
27 <-> 24
28 <-> 24
29 <-> 25
30 <-> 26
33 <-> 29
34 <-> 30
37 <-> 33
38 <-> 34
40 <-> 36
43 <-> 39
44 <-> 40
46 <-> 42
Not mapped:
3
