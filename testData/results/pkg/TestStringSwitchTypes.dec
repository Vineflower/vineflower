package pkg;

public class TestStringSwitchTypes {
   public int testSplit(String s) {
      int res = 0;// 5
      switch (s) {// 6
         case "a":
            res = 1;// 8
            break;// 9
         case "b":
            res = 2;// 11
            break;// 12
         case "c":
            res = 4;// 14
      }

      return res;// 17
   }

   public int testSplit2(String s) {
      return switch (s) {// 21
         case "a" -> 1;// 22
         case "b" -> 2;// 23
         case "c" -> 4;// 24
         default -> 0;// 25
      };
   }

   public String testInlineSplit(String s) {
      String res = "";// 30
      switch (s) {// 31
         case "a":
            res = "a";// 33
         default:
            return res;// 37
      }
   }

   public int testMerged(String s) {
      switch (s) {// 41
         case "1":
            return 1;// 46
         case "2":
            return 2;// 48
         default:
            System.out.println("Test");// 43
            return 0;// 44 51
      }
   }

   public int testSplitDup(String s) {
      switch (s) {// 55
         case "a":
            return 1;// 57
         case "b":
            return 1;// 59
         default:
            System.out.println("Test");// 61
            return 0;// 62
      }
   }

   public int testMergedDup(String s) {
      switch (s) {// 67
         case "a":
            return 1;// 72
         case "b":
            return 1;// 74
         default:
            System.out.println("Test");// 69
            return 0;// 70 77
      }
   }
}

class 'pkg/TestStringSwitchTypes' {
   method 'testSplit (Ljava/lang/String;)I' {
      0      4
      1      4
      2      5
      25      6
      26      6
      34      9
      35      9
      43      12
      44      12
      50      5
      51      5
      52      5
      53      5
      54      5
      55      5
      56      5
      57      5
      58      5
      59      5
      5a      5
      5b      5
      5c      5
      5d      5
      5e      5
      5f      5
      60      5
      61      5
      62      5
      63      5
      64      5
      65      5
      66      5
      67      5
      68      5
      69      5
      6a      5
      6b      5
      6c      7
      6d      7
      6e      8
      71      10
      72      10
      73      11
      76      13
      77      13
      78      16
      79      16
   }

   method 'testSplit2 (Ljava/lang/String;)I' {
      0      20
      25      21
      26      21
      33      22
      34      22
      41      23
      42      23
      4c      20
      4d      20
      4e      20
      4f      20
      50      20
      51      20
      52      20
      53      20
      54      20
      55      20
      56      20
      57      20
      58      20
      59      20
      5a      20
      5b      20
      5c      20
      5d      20
      5e      20
      5f      20
      60      20
      61      20
      62      20
      63      20
      64      20
      65      20
      66      20
      67      20
      68      21
      6c      22
      70      23
      74      24
      75      20
   }

   method 'testInlineSplit (Ljava/lang/String;)Ljava/lang/String;' {
      0      29
      1      29
      2      29
      3      30
      21      31
      22      31
      2e      30
      2f      30
      30      30
      31      30
      32      30
      33      30
      34      30
      35      30
      36      30
      37      30
      38      30
      39      30
      3a      30
      3b      30
      3c      30
      3d      30
      3e      30
      3f      30
      40      32
      41      32
      42      32
      43      34
      44      34
   }

   method 'testMerged (Ljava/lang/String;)I' {
      0      39
      25      40
      26      40
      33      42
      34      42
      3e      39
      3f      39
      40      39
      41      39
      42      39
      43      39
      44      39
      45      39
      46      39
      47      39
      48      39
      49      39
      4a      39
      4b      39
      4c      39
      4d      39
      4e      39
      4f      39
      50      39
      51      39
      52      39
      53      39
      54      39
      55      39
      56      39
      57      39
      58      45
      59      45
      5a      45
      5b      45
      5c      45
      5d      45
      5e      45
      5f      45
      60      46
      63      41
      64      41
      65      43
      66      43
      67      46
   }

   method 'testSplitDup (Ljava/lang/String;)I' {
      0      51
      25      52
      26      52
      33      54
      34      54
      3e      51
      3f      51
      40      51
      41      51
      42      51
      43      51
      44      51
      45      51
      46      51
      47      51
      48      51
      49      51
      4a      51
      4b      51
      4c      51
      4d      51
      4e      51
      4f      51
      50      51
      51      51
      52      51
      53      51
      54      51
      55      51
      56      51
      57      51
      58      53
      59      53
      5a      55
      5b      55
      5c      57
      5d      57
      5e      57
      5f      57
      60      57
      61      57
      62      57
      63      57
      64      58
      65      58
   }

   method 'testMergedDup (Ljava/lang/String;)I' {
      0      63
      25      64
      26      64
      33      66
      34      66
      3e      63
      3f      63
      40      63
      41      63
      42      63
      43      63
      44      63
      45      63
      46      63
      47      63
      48      63
      49      63
      4a      63
      4b      63
      4c      63
      4d      63
      4e      63
      4f      63
      50      63
      51      63
      52      63
      53      63
      54      63
      55      63
      56      63
      57      63
      58      69
      59      69
      5a      69
      5b      69
      5c      69
      5d      69
      5e      69
      5f      69
      60      70
      63      65
      64      65
      65      67
      66      67
      67      70
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
8 <-> 8
9 <-> 9
11 <-> 11
12 <-> 12
14 <-> 14
17 <-> 17
21 <-> 21
22 <-> 22
23 <-> 23
24 <-> 24
25 <-> 25
30 <-> 30
31 <-> 31
33 <-> 33
37 <-> 35
41 <-> 40
43 <-> 46
44 <-> 47
46 <-> 42
48 <-> 44
51 <-> 47
55 <-> 52
57 <-> 54
59 <-> 56
61 <-> 58
62 <-> 59
67 <-> 64
69 <-> 70
70 <-> 71
72 <-> 66
74 <-> 68
77 <-> 71
