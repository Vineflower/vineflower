package pkg;

public class TestAssignmentTernaryConstantSimplification {
   public void ternaryNotAnd(boolean bl, boolean bl2) {
      boolean bl3 = !bl && bl2;// 5
      System.out.println(bl3);// 6
   }// 7

   public void ifOr(boolean bl, boolean bl2) {
      boolean bl3;
      if (bl) {// 11
         bl3 = true;// 12
      } else {
         bl3 = bl2;// 14
      }

      System.out.println(bl3);// 16
   }// 17

   public void ternaryNotOr(boolean bl, boolean bl2) {
      boolean bl3 = !bl || bl2;// 20
      System.out.println(bl3);// 21
   }// 22

   public void ternaryAnd(boolean bl, boolean bl2) {
      boolean bl3 = bl && bl2;// 25
      System.out.println(bl3);// 26
   }// 27

   public void redundantIf(boolean bl) {
      boolean bl3;
      if (bl) {// 31
         bl3 = true;// 32
      } else {
         bl3 = false;// 34
      }

      System.out.println(bl3);// 36
   }// 37

   public void redundantTernary(boolean bl) {
      boolean bl3 = bl;// 40
      System.out.println(bl3);// 41
   }// 42

   public void nestedIf(boolean bl, boolean bl2) {
      boolean bl3;
      if (bl) {// 46
         if (bl2) {// 47
            bl3 = true;// 48
         } else {
            bl3 = bl;// 50
         }
      } else {
         bl3 = true;// 53
      }

      System.out.println(bl3);// 55
   }// 56

   public void nestedTernary(boolean bl, boolean bl2) {
      boolean bl3 = !bl || bl2;// 59
      System.out.println(bl3);// 60
   }// 61

   public void nestedIfs(boolean bl, boolean bl2, boolean bl3) {
      boolean bl4;
      if (bl) {// 65
         if (bl2) {// 66
            bl4 = false;// 67
         } else {
            bl4 = bl3;// 69
         }
      } else if (bl3) {// 72
         bl4 = bl3;// 73
      } else {
         bl4 = bl2;// 75
      }

      System.out.println(bl4);// 78
   }// 79
}

class 'pkg/TestAssignmentTernaryConstantSimplification' {
   method 'ternaryNotAnd (ZZ)V' {
      0      4
      1      4
      2      4
      3      4
      8      4
      9      4
      a      5
      b      5
      c      5
      d      5
      e      5
      f      5
      10      5
      11      6
   }

   method 'ifOr (ZZ)V' {
      0      10
      1      10
      2      10
      3      10
      4      11
      5      11
      9      13
      a      13
      b      16
      c      16
      d      16
      e      16
      f      16
      10      16
      11      16
      12      17
   }

   method 'ternaryNotOr (ZZ)V' {
      0      20
      1      20
      2      20
      3      20
      4      20
      9      20
      a      21
      b      21
      c      21
      d      21
      e      21
      f      21
      10      21
      11      22
   }

   method 'ternaryAnd (ZZ)V' {
      0      25
      1      25
      2      25
      3      25
      4      25
      9      25
      a      26
      b      26
      c      26
      d      26
      e      26
      f      26
      10      26
      11      27
   }

   method 'redundantIf (Z)V' {
      0      31
      1      31
      2      31
      3      31
      4      32
      5      32
      9      34
      a      34
      b      37
      c      37
      d      37
      e      37
      f      37
      10      37
      11      37
      12      38
   }

   method 'redundantTernary (Z)V' {
      0      41
      1      41
      2      41
      3      41
      9      41
      a      42
      b      42
      c      42
      d      42
      e      42
      f      42
      10      42
      11      43
   }

   method 'nestedIf (ZZ)V' {
      0      47
      1      47
      2      47
      3      47
      4      48
      5      48
      6      48
      7      48
      8      49
      9      49
      d      51
      e      51
      12      54
      13      54
      14      57
      15      57
      16      57
      17      57
      18      57
      19      57
      1a      57
      1b      58
   }

   method 'nestedTernary (ZZ)V' {
      0      61
      1      61
      2      61
      3      61
      4      61
      5      61
      6      61
      7      61
      11      61
      12      62
      13      62
      14      62
      15      62
      16      62
      17      62
      18      62
      19      63
   }

   method 'nestedIfs (ZZZ)V' {
      0      67
      1      67
      2      67
      3      67
      4      68
      5      68
      6      68
      7      68
      8      69
      9      69
      a      69
      e      71
      f      71
      10      71
      14      73
      15      73
      16      73
      17      73
      18      74
      19      74
      1a      74
      1e      76
      1f      76
      20      76
      21      79
      22      79
      23      79
      24      79
      25      79
      26      79
      27      79
      28      79
      29      80
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
7 <-> 7
11 <-> 11
12 <-> 12
14 <-> 14
16 <-> 17
17 <-> 18
20 <-> 21
21 <-> 22
22 <-> 23
25 <-> 26
26 <-> 27
27 <-> 28
31 <-> 32
32 <-> 33
34 <-> 35
36 <-> 38
37 <-> 39
40 <-> 42
41 <-> 43
42 <-> 44
46 <-> 48
47 <-> 49
48 <-> 50
50 <-> 52
53 <-> 55
55 <-> 58
56 <-> 59
59 <-> 62
60 <-> 63
61 <-> 64
65 <-> 68
66 <-> 69
67 <-> 70
69 <-> 72
72 <-> 74
73 <-> 75
75 <-> 77
78 <-> 80
79 <-> 81
