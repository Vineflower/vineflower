package pkg;

public class TestLocalScopeClash {
   void acceptInt(int i) {
   }// 6

   void acceptBoolean(boolean b) {
   }// 10

   void test(boolean a, boolean b) {
      int i = 32767;// 14
      this.acceptInt(++i);// 15 16
      i = ++i | 7;// 17 18
      i = 1 ^ i;// 19
      i *= 2;// 20
      i += 2;// 21
      this.acceptInt(i);// 22
      boolean ix = a;// 25
      ix &= ix & b;// 26
      ix ^= ix || b;// 27
      this.acceptBoolean(ix);// 28
   }// 30
}

class 'pkg/TestLocalScopeClash' {
   method 'acceptInt (I)V' {
      0      4
   }

   method 'acceptBoolean (Z)V' {
      0      7
   }

   method 'test (ZZ)V' {
      0      10
      1      10
      2      10
      3      10
      4      11
      6      11
      8      11
      a      11
      b      11
      c      11
      d      12
      f      12
      12      12
      13      12
      14      12
      15      12
      16      13
      17      13
      18      13
      19      13
      1b      14
      1d      14
      1f      15
      21      15
      22      16
      23      16
      24      16
      25      16
      26      16
      27      17
      28      17
      2a      18
      2b      18
      2c      18
      2e      18
      30      19
      31      19
      32      19
      33      19
      34      19
      35      19
      36      19
      37      19
      3e      19
      3f      20
      40      20
      41      20
      42      20
      43      20
      44      21
   }
}

Lines mapping:
6 <-> 5
10 <-> 8
14 <-> 11
15 <-> 12
16 <-> 12
17 <-> 13
18 <-> 13
19 <-> 14
20 <-> 15
21 <-> 16
22 <-> 17
25 <-> 18
26 <-> 19
27 <-> 20
28 <-> 21
30 <-> 22
