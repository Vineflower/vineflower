package pkg;

import ext.TestEnum2;
import java.util.function.Function;

public class TestSwitchOnEnumJ21 {
   public int test1(TestSwitchOnEnumJ21.TestEnum a) {
      return switch (a) {// 7
         case A -> 1;// 8
         case B -> 2;// 9
         case C -> 3;// 10
      };
   }

   public int test2(TestEnum2 a) {
      return switch (a) {// 15
         case A -> 1;// 16
         case B -> 2;// 17
         case C -> 3;// 18
      };
   }

   public int test3(TestSwitchOnEnumJ21.TestEnum a) {
      return switch (a) {// 23
         case null -> 4;// 27
         case A -> 1;// 24
         case B -> 2;// 25
         case C -> 3;// 26
      };
   }

   public int test4(TestEnum2 a) {
      return switch (a) {// 32
         case null -> 4;// 36
         case A -> 1;// 33
         case B -> 2;// 34
         case C -> 3;// 35
      };
   }

   public int test5(TestSwitchOnEnumJ21.TestEnum a, boolean b) {
      return switch (a) {// 41
         case A -> 1;// 42
         case B -> 2;// 43
         case C -> {
            if (b) {// 45
               boolean c = true;// 46
               yield 3;// 47
            } else {
               boolean d = true;// 49
               yield 4;// 50
            }
         }
      };
   }

   public int testDefault(TestSwitchOnEnumJ21.TestEnum a) {
      return switch (a) {// 57
         case A -> 1;// 58
         default -> 5;// 59
      };
   }

   public int testDefault2(TestEnum2 a) {
      return switch (a) {// 64
         case A -> 1;// 65
         default -> 5;// 66
      };
   }

   public void testStatement(TestSwitchOnEnumJ21.TestEnum a) {
      switch (a) {// 71
         case A:
            System.out.println("A");// 73
            break;// 74
         case B:
            System.out.println("B");// 76
            break;// 77
         case C:
            System.out.println("C");// 79
      }
   }// 81

   public void testStatementDefault(TestSwitchOnEnumJ21.TestEnum a) {
      switch (a) {// 84
         case A:
            System.out.println("A");// 86
            break;// 87
         default:
            System.out.println("C");// 89
      }
   }// 91

   public void testComplexPattern(TestSwitchOnEnumJ21.A param1) {
      // $VF: Couldn't be decompiled
      // Bytecode:
      // 00: aload 1
      // 01: astore 2
      // 02: bipush 0
      // 03: istore 3
      // 04: aload 2
      // 05: iload 3
      // 06: invokedynamic typeSwitch (Ljava/lang/Object;I)I bsm=java/lang/runtime/SwitchBootstraps.typeSwitch (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite; args=[ null.invoke Ljava/lang/Enum$EnumDesc;, pkg/TestSwitchOnEnumJ21$TestEnum, pkg/TestSwitchOnEnumJ21$A ]
      // 0b: tableswitch 100 -1 2 89 29 40 57
      // 28: getstatic java/lang/System.out Ljava/io/PrintStream;
      // 2b: ldc "b"
      // 2d: invokevirtual java/io/PrintStream.println (Ljava/lang/String;)V
      // 30: goto 7a
      // 33: aload 2
      // 34: checkcast pkg/TestSwitchOnEnumJ21$TestEnum
      // 37: astore 4
      // 39: getstatic java/lang/System.out Ljava/io/PrintStream;
      // 3c: ldc "enum"
      // 3e: invokevirtual java/io/PrintStream.println (Ljava/lang/String;)V
      // 41: goto 7a
      // 44: aload 2
      // 45: astore 5
      // 47: aload 5
      // 49: instanceof pkg/TestSwitchOnEnumJ21$C
      // 4c: ifne 54
      // 4f: bipush 3
      // 50: istore 3
      // 51: goto 04
      // 54: getstatic java/lang/System.out Ljava/io/PrintStream;
      // 57: aload 5
      // 59: invokeinterface pkg/TestSwitchOnEnumJ21$A.getClass ()Ljava/lang/Class; 1
      // 5e: invokevirtual java/io/PrintStream.println (Ljava/lang/Object;)V
      // 61: goto 7a
      // 64: getstatic java/lang/System.out Ljava/io/PrintStream;
      // 67: ldc "null"
      // 69: invokevirtual java/io/PrintStream.println (Ljava/lang/String;)V
      // 6c: goto 7a
      // 6f: getstatic java/lang/System.out Ljava/io/PrintStream;
      // 72: ldc "default"
      // 74: invokevirtual java/io/PrintStream.println (Ljava/lang/String;)V
      // 77: goto 7a
      // 7a: return
   }

   public <T> T testEnumSwitchWithCastYields(TestSwitchOnEnumJ21.TestEnum a, Function<Object, T> wrapper) {
      return (T)(switch (a) {// 104
         case null -> (Object)wrapper.apply("null");// 107
         case A -> (Object)wrapper.apply("A");// 105
         case B -> (Object)wrapper.apply(10);// 106
         default -> null;// 108
      });
   }

   interface A {
   }

   public static class C implements TestSwitchOnEnumJ21.A {
   }

   enum TestEnum implements TestSwitchOnEnumJ21.A {
      A,
      B,
      C;
   }
}

class 'pkg/TestSwitchOnEnumJ21' {
   method 'test1 (Lpkg/TestSwitchOnEnumJ21$TestEnum;)I' {
      0      7
      4      7
      5      7
      6      7
      7      7
      8      7
      9      7
      a      7
      b      7
      c      7
      d      7
      e      7
      f      7
      10      7
      11      7
      12      7
      13      7
      14      7
      15      7
      16      7
      17      7
      18      7
      19      7
      1a      7
      1b      7
      1c      7
      1d      7
      1e      7
      1f      7
      2a      8
      2e      9
      32      10
      33      7
   }

   method 'test2 (Lext/TestEnum2;)I' {
      3      15
      7      15
      8      15
      9      15
      a      15
      b      15
      c      15
      d      15
      e      15
      f      15
      10      15
      11      15
      12      15
      13      15
      14      15
      15      15
      16      15
      17      15
      18      15
      19      15
      1a      15
      1b      15
      1c      15
      1d      15
      1e      15
      1f      15
      20      15
      21      15
      22      15
      23      15
      2e      16
      32      17
      36      18
      37      15
   }

   method 'test3 (Lpkg/TestSwitchOnEnumJ21$TestEnum;)I' {
      0      23
      b      23
      c      23
      d      23
      e      23
      f      23
      10      23
      11      23
      12      23
      13      23
      14      23
      15      23
      16      23
      17      23
      18      23
      19      23
      1a      23
      1b      23
      1c      23
      1d      23
      1e      23
      1f      23
      20      23
      21      23
      22      23
      23      23
      24      23
      25      23
      26      23
      27      23
      32      25
      36      26
      3a      27
      3e      24
      3f      23
   }

   method 'test4 (Lext/TestEnum2;)I' {
      0      32
      b      32
      c      32
      d      32
      e      32
      f      32
      10      32
      11      32
      12      32
      13      32
      14      32
      15      32
      16      32
      17      32
      18      32
      19      32
      1a      32
      1b      32
      1c      32
      1d      32
      1e      32
      1f      32
      20      32
      21      32
      22      32
      23      32
      24      32
      25      32
      26      32
      27      32
      32      34
      36      35
      3a      36
      3e      33
      3f      32
   }

   method 'test5 (Lpkg/TestSwitchOnEnumJ21$TestEnum;Z)I' {
      0      41
      4      41
      5      41
      6      41
      7      41
      8      41
      9      41
      a      41
      b      41
      c      41
      d      41
      e      41
      f      41
      10      41
      11      41
      12      41
      13      41
      14      41
      15      41
      16      41
      17      41
      18      41
      19      41
      1a      41
      1b      41
      1c      41
      1d      41
      1e      41
      1f      41
      2a      42
      2e      43
      32      45
      33      45
      34      45
      35      45
      36      46
      37      46
      38      47
      3c      49
      3d      49
      3e      50
      42      41
   }

   method 'testDefault (Lpkg/TestSwitchOnEnumJ21$TestEnum;)I' {
      0      57
      4      57
      5      57
      6      57
      7      57
      8      57
      9      57
      a      57
      b      57
      c      57
      d      57
      e      57
      f      57
      10      57
      11      57
      12      57
      13      57
      14      57
      15      57
      16      57
      17      57
      18      58
      1c      59
      1d      57
   }

   method 'testDefault2 (Lext/TestEnum2;)I' {
      3      64
      7      64
      8      64
      9      64
      a      64
      b      64
      c      64
      d      64
      e      64
      f      64
      10      64
      11      64
      12      64
      13      64
      14      64
      15      64
      16      64
      17      64
      18      64
      19      64
      1a      64
      1b      64
      1c      65
      20      66
      21      64
   }

   method 'testStatement (Lpkg/TestSwitchOnEnumJ21$TestEnum;)V' {
      0      71
      4      71
      5      71
      6      71
      7      71
      8      71
      9      71
      a      71
      b      71
      c      71
      d      71
      e      71
      f      71
      10      71
      11      71
      12      71
      13      71
      14      71
      15      71
      16      71
      17      71
      18      71
      19      71
      1a      71
      1b      71
      1c      71
      1d      71
      1e      71
      1f      71
      20      73
      21      73
      22      73
      23      73
      24      73
      25      73
      26      73
      27      73
      28      74
      2b      76
      2c      76
      2d      76
      2e      76
      2f      76
      30      76
      31      76
      32      76
      33      77
      36      79
      37      79
      38      79
      39      79
      3a      79
      3b      79
      3c      79
      3d      79
      3e      81
   }

   method 'testStatementDefault (Lpkg/TestSwitchOnEnumJ21$TestEnum;)V' {
      0      84
      4      84
      5      84
      6      84
      7      84
      8      84
      9      84
      a      84
      b      84
      c      84
      d      84
      e      84
      f      84
      10      84
      11      84
      12      84
      13      84
      14      84
      15      84
      16      84
      17      84
      18      86
      19      86
      1a      86
      1b      86
      1c      86
      1d      86
      1e      86
      1f      86
      20      87
      23      89
      24      89
      25      89
      26      89
      27      89
      28      89
      29      89
      2a      89
      2b      91
   }

   method 'testEnumSwitchWithCastYields (Lpkg/TestSwitchOnEnumJ21$TestEnum;Ljava/util/function/Function;)Ljava/lang/Object;' {
      0      140
      d      140
      e      140
      f      140
      10      140
      11      140
      12      140
      13      140
      14      140
      15      140
      16      140
      17      140
      18      140
      19      140
      1a      140
      1b      140
      1c      140
      1d      140
      1e      140
      1f      140
      20      140
      21      140
      22      140
      23      140
      24      140
      25      140
      26      140
      27      140
      28      142
      29      142
      2a      142
      2b      142
      2c      142
      2d      142
      2e      142
      2f      142
      30      142
      31      142
      32      142
      36      143
      37      143
      38      143
      39      143
      3a      143
      3b      143
      3c      143
      3d      143
      3e      143
      3f      143
      40      143
      41      143
      42      143
      43      143
      47      141
      48      141
      49      141
      4a      141
      4b      141
      4c      141
      4d      141
      4e      141
      4f      141
      50      141
      51      141
      55      144
      56      140
   }
}

Lines mapping:
7 <-> 8
8 <-> 9
9 <-> 10
10 <-> 11
15 <-> 16
16 <-> 17
17 <-> 18
18 <-> 19
23 <-> 24
24 <-> 26
25 <-> 27
26 <-> 28
27 <-> 25
32 <-> 33
33 <-> 35
34 <-> 36
35 <-> 37
36 <-> 34
41 <-> 42
42 <-> 43
43 <-> 44
45 <-> 46
46 <-> 47
47 <-> 48
49 <-> 50
50 <-> 51
57 <-> 58
58 <-> 59
59 <-> 60
64 <-> 65
65 <-> 66
66 <-> 67
71 <-> 72
73 <-> 74
74 <-> 75
76 <-> 77
77 <-> 78
79 <-> 80
81 <-> 82
84 <-> 85
86 <-> 87
87 <-> 88
89 <-> 90
91 <-> 92
104 <-> 141
105 <-> 143
106 <-> 144
107 <-> 142
108 <-> 145
