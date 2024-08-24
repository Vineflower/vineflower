package pkg;

public enum TestInlineSwitchExpression7 {
   A,
   B,
   C;

   private int get() {
      return this.ordinal();// 9
   }

   public int test(TestInlineSwitchExpression7 in) {
      return switch (in) {// 13
         case A -> {
            int a = this.get();// 15
            yield a + 1;// 16
         }
         case B -> {
            int b = this.get();// 19
            yield b + 2;// 20
         }
         case C -> {
            int c = this.get();// 23
            yield c + 3;// 24
         }
      };
   }
}

class 'pkg/TestInlineSwitchExpression7' {
   method 'get ()I' {
      0      8
      1      8
      2      8
      3      8
      4      8
   }

   method 'test (Lpkg/TestInlineSwitchExpression7;)I' {
      3      12
      7      12
      8      12
      9      12
      a      12
      b      12
      c      12
      d      12
      e      12
      f      12
      10      12
      11      12
      12      12
      13      12
      14      12
      15      12
      16      12
      17      12
      18      12
      19      12
      1a      12
      1b      12
      1c      12
      1d      12
      1e      12
      1f      12
      20      12
      21      12
      22      12
      23      12
      24      14
      25      14
      26      14
      27      14
      28      14
      29      15
      2a      15
      2b      15
      2f      18
      30      18
      31      18
      32      18
      33      18
      34      19
      35      19
      36      19
      3a      22
      3b      22
      3c      22
      3d      22
      3e      22
      3f      23
      40      23
      41      23
      4d      12
   }
}

Lines mapping:
9 <-> 9
13 <-> 13
15 <-> 15
16 <-> 16
19 <-> 19
20 <-> 20
23 <-> 23
24 <-> 24
