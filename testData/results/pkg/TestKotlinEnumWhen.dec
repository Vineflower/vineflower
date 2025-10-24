package pkg;

import kotlin.DeprecationLevel;
import kotlin.Metadata;
import kotlin.NoWhenBranchMatchedException;
import kotlin.enums.EnumEntries;
import org.jetbrains.annotations.NotNull;

@Metadata(
   mv = {2, 0, 0},
   k = 1,
   xi = 48,
   d1 = "\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0005\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\u0007\u001a\u00020\bJ\u0006\u0010\t\u001a\u00020\bJ\u0006\u0010\n\u001a\u00020\bJ\u0006\u0010\u000b\u001a\u00020\bJ\u0006\u0010\f\u001a\u00020\bj\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006¨\u0006\r",
   d2 = {
         "Lpkg/TestKotlinEnumWhen;",
         "",
         "<init>",
         "(Ljava/lang/String;I)V",
         "FIRST",
         "SECOND",
         "THIRD",
         "testStatement",
         "",
         "testExpression",
         "testAnotherEnum",
         "testConsecutive",
         "testConsecutiveMixed",
         "vineflower_testDataKotlin"
   }
)
public enum TestKotlinEnumWhen {
   FIRST,
   SECOND,
   THIRD;

   public final void testStatement() {
      switch (this) {// 7
         case FIRST:
            System.out.println("first!");// 8
            break;
         case SECOND:
            System.out.println("second!");// 9
            break;
         case THIRD:
            System.out.println("third!");// 10
            break;
         default:
            throw new NoWhenBranchMatchedException();
      }
   }// 12

   public final void testExpression() {
      String var1 = switch (this) {// 16
         case FIRST -> "first!";// 17
         case SECOND -> "second!";// 18
         case THIRD -> "third!";// 19
         default -> throw new NoWhenBranchMatchedException();
      };
      System.out.println(var1);// 15
   }// 22

   public final void testAnotherEnum() {
      DeprecationLevel level = testAnotherEnum$getLevel();// 28
      switch (level) {
         case WARNING:
            System.out.println("warning " + level);// 29
            break;
         case ERROR:
            System.out.println("error " + level);// 30
            break;
         case HIDDEN:
            System.out.println("hidden " + level);// 31
            break;
         default:
            throw new NoWhenBranchMatchedException();
      }
   }// 33

   public final void testConsecutive() {
      switch (this) {// 36
         case FIRST:
            System.out.println("first!");// 37
            break;
         case SECOND:
            System.out.println("second!");// 38
            break;
         case THIRD:
            System.out.println("third!");// 39
            break;
         default:
            throw new NoWhenBranchMatchedException();
      }

      switch (this) {// 42
         case FIRST:
            System.out.println("first, again!");// 43
            break;
         case SECOND:
            System.out.println("second, again!");// 44
            break;
         case THIRD:
            System.out.println("third, again!");// 45
            break;
         default:
            throw new NoWhenBranchMatchedException();
      }
   }// 47

   public final void testConsecutiveMixed() {
      DeprecationLevel level = testConsecutiveMixed$getLevel$0();// 53
      switch (level) {
         case WARNING:
            System.out.println("warning " + level);// 54
            break;
         case ERROR:
            System.out.println("error " + level);// 55
            break;
         case HIDDEN:
            System.out.println("hidden " + level);// 56
            break;
         default:
            throw new NoWhenBranchMatchedException();
      }

      switch (this) {// 59
         case FIRST:
            System.out.println("first!");// 60
            break;
         case SECOND:
            System.out.println("second!");// 61
            break;
         case THIRD:
            System.out.println("third!");// 62
            break;
         default:
            throw new NoWhenBranchMatchedException();
      }
   }// 64

   @NotNull
   public static EnumEntries<TestKotlinEnumWhen> getEntries() {
      return $ENTRIES;// 65
   }

   private static final DeprecationLevel testAnotherEnum$getLevel() {
      throw new Exception();// 26
   }

   private static final DeprecationLevel testConsecutiveMixed$getLevel$0() {
      throw new Exception();// 51
   }
}

class 'pkg/TestKotlinEnumWhen' {
   method 'testStatement ()V' {
      0      36
      8      36
      9      36
      a      36
      b      36
      c      36
      d      36
      e      36
      f      36
      10      36
      11      36
      12      36
      13      36
      14      36
      15      36
      16      36
      17      36
      18      36
      19      36
      1a      36
      1b      36
      1c      36
      1d      36
      1e      36
      1f      36
      20      36
      21      36
      22      36
      23      36
      24      38
      25      38
      26      38
      27      38
      28      38
      2a      38
      2b      38
      2c      38
      2d      39
      30      41
      31      41
      32      41
      33      41
      34      41
      36      41
      37      41
      38      41
      39      42
      3c      44
      3d      44
      3e      44
      3f      44
      40      44
      42      44
      43      44
      44      44
      45      45
      4f      47
      50      49
   }

   method 'testExpression ()V' {
      0      52
      8      52
      9      52
      a      52
      b      52
      c      52
      d      52
      e      52
      f      52
      10      52
      11      52
      12      52
      13      52
      14      52
      15      52
      16      52
      17      52
      18      52
      19      52
      1a      52
      1b      52
      1c      52
      1d      52
      1e      52
      1f      52
      20      52
      21      52
      22      52
      23      52
      24      53
      25      53
      29      54
      2a      54
      2e      55
      2f      55
      3a      56
      3b      52
      3c      58
      3d      58
      3e      58
      3f      58
      40      58
      41      58
      42      58
      43      59
   }

   method 'testAnotherEnum ()V' {
      0      62
      1      62
      2      62
      3      62
      7      63
      b      63
      c      63
      d      63
      e      63
      f      63
      10      63
      11      63
      12      63
      13      63
      14      63
      15      63
      16      63
      17      63
      18      63
      19      63
      1a      63
      1b      63
      1c      63
      1d      63
      1e      63
      1f      63
      20      63
      21      63
      22      63
      23      63
      24      63
      25      63
      26      63
      27      63
      28      65
      29      65
      2a      65
      2b      65
      2c      65
      2d      65
      2e      65
      2f      65
      30      65
      32      65
      33      65
      34      65
      35      66
      38      68
      39      68
      3a      68
      3b      68
      3c      68
      3d      68
      3e      68
      3f      68
      40      68
      42      68
      43      68
      44      68
      45      69
      48      71
      49      71
      4a      71
      4b      71
      4c      71
      4d      71
      4e      71
      4f      71
      50      71
      52      71
      53      71
      54      71
      55      72
      5f      74
      60      76
   }

   method 'testConsecutive ()V' {
      0      79
      8      79
      9      79
      a      79
      b      79
      c      79
      d      79
      e      79
      f      79
      10      79
      11      79
      12      79
      13      79
      14      79
      15      79
      16      79
      17      79
      18      79
      19      79
      1a      79
      1b      79
      1c      79
      1d      79
      1e      79
      1f      79
      20      79
      21      79
      22      79
      23      79
      24      81
      25      81
      26      81
      27      81
      28      81
      2a      81
      2b      81
      2c      81
      2d      82
      30      84
      31      84
      32      84
      33      84
      34      84
      36      84
      37      84
      38      84
      39      85
      3c      87
      3d      87
      3e      87
      3f      87
      40      87
      42      87
      43      87
      44      87
      45      88
      4f      90
      50      93
      58      93
      59      93
      5a      93
      5b      93
      5c      93
      5d      93
      5e      93
      5f      93
      60      93
      61      93
      62      93
      63      93
      64      93
      65      93
      66      93
      67      93
      68      93
      69      93
      6a      93
      6b      93
      6c      93
      6d      93
      6e      93
      6f      93
      70      93
      71      93
      72      93
      73      93
      74      95
      75      95
      76      95
      77      95
      78      95
      7a      95
      7b      95
      7c      95
      7d      96
      80      98
      81      98
      82      98
      83      98
      84      98
      86      98
      87      98
      88      98
      89      99
      8c      101
      8d      101
      8e      101
      8f      101
      90      101
      92      101
      93      101
      94      101
      95      102
      9f      104
      a0      106
   }

   method 'testConsecutiveMixed ()V' {
      0      109
      1      109
      2      109
      3      109
      7      110
      b      110
      c      110
      d      110
      e      110
      f      110
      10      110
      11      110
      12      110
      13      110
      14      110
      15      110
      16      110
      17      110
      18      110
      19      110
      1a      110
      1b      110
      1c      110
      1d      110
      1e      110
      1f      110
      20      110
      21      110
      22      110
      23      110
      24      110
      25      110
      26      110
      27      110
      28      112
      29      112
      2a      112
      2b      112
      2c      112
      2d      112
      2e      112
      2f      112
      30      112
      32      112
      33      112
      34      112
      35      113
      38      115
      39      115
      3a      115
      3b      115
      3c      115
      3d      115
      3e      115
      3f      115
      40      115
      42      115
      43      115
      44      115
      45      116
      48      118
      49      118
      4a      118
      4b      118
      4c      118
      4d      118
      4e      118
      4f      118
      50      118
      52      118
      53      118
      54      118
      55      119
      5f      121
      60      124
      68      124
      69      124
      6a      124
      6b      124
      6c      124
      6d      124
      6e      124
      6f      124
      70      124
      71      124
      72      124
      73      124
      74      124
      75      124
      76      124
      77      124
      78      124
      79      124
      7a      124
      7b      124
      7c      124
      7d      124
      7e      124
      7f      124
      80      124
      81      124
      82      124
      83      124
      84      126
      85      126
      86      126
      87      126
      88      126
      8a      126
      8b      126
      8c      126
      8d      127
      90      129
      91      129
      92      129
      93      129
      94      129
      96      129
      97      129
      98      129
      99      130
      9c      132
      9d      132
      9e      132
      9f      132
      a0      132
      a2      132
      a3      132
      a4      132
      a5      133
      af      135
      b0      137
   }

   method 'getEntries ()Lkotlin/enums/EnumEntries;' {
      0      141
      1      141
      2      141
      3      141
   }

   method 'testAnotherEnum$getLevel ()Lkotlin/DeprecationLevel;' {
      7      145
   }

   method 'testConsecutiveMixed$getLevel$0 ()Lkotlin/DeprecationLevel;' {
      7      149
   }
}

Lines mapping:
7 <-> 37
8 <-> 39
9 <-> 42
10 <-> 45
12 <-> 50
15 <-> 59
16 <-> 53
17 <-> 54
18 <-> 55
19 <-> 56
22 <-> 60
26 <-> 146
28 <-> 63
29 <-> 66
30 <-> 69
31 <-> 72
33 <-> 77
36 <-> 80
37 <-> 82
38 <-> 85
39 <-> 88
42 <-> 94
43 <-> 96
44 <-> 99
45 <-> 102
47 <-> 107
51 <-> 150
53 <-> 110
54 <-> 113
55 <-> 116
56 <-> 119
59 <-> 125
60 <-> 127
61 <-> 130
62 <-> 133
64 <-> 138
65 <-> 142
