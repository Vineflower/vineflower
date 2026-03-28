package pkg

import java.util.function.Consumer
import kotlin.jvm.functions.Function0
import kotlin.jvm.functions.Function1
import kotlin.jvm.functions.Function2
import kotlin.jvm.internal.Lambda
import kotlin.jvm.internal.SourceDebugExtension
import kotlin.jvm.internal.Ref.IntRef
import kotlin.reflect.KFunction

@SourceDebugExtension(["SMAP\nTestAnonymousEverything.kt\nKotlin\n*S Kotlin\n*F\n+ 1 TestAnonymousEverything.kt\npkg/TestAnonymousEverything\n+ 2 fake.kt\nkotlin/jvm/internal/FakeKt\n*L\n1#1,120:1\n1#2:121\n*E\n"])
public class TestAnonymousEverything {
   public fun testPlainLambda() {
      { x: Int, y: Int ->
         x + y// 7
      }(1, 2)// 8
   }// 9

   public fun testSerializableLambda() {
      @Metadata(
         mv = {2, 0, 0},
         k = 3,
         xi = 48,
         d1 = "\u0000\n\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00012\u0006\u0010\u0003\u001a\u00020\u0001H\n¢\u0006\u0004\b\u0004\u0010\u0005",
         d2 = {"<anonymous>", "", "x", "y", "invoke", "(II)Ljava/lang/Integer;"}
      )
      final class NamelessClass_2 extends Lambda implements Function2<Integer, Integer, Integer> {
         public static final NamelessClass_2 INSTANCE = NamelessClass_2();

         NamelessClass_2() {
            super(2)
         }

         public final Integer invoke(int x, int y) {
            x + y
         }
      }

      ({ x, y ->// 13
         x + y
      } as Function2)(1, 2)// 12
   }// 14

   public fun testAnonymousObject() {
      object {// 17 22
         public fun foo(x: Int, y: Int): Int {
            return x + y// 19
         }
      }.foo(1, 2)
   }// 23

   public fun testFunctionAnonymousObject() {
      object : (Int, Int) -> Int {// 26 31
         public open operator fun invoke(x: Int, y: Int): Int {
            return x + y// 28
         }
      }.invoke(1, 2)
   }// 32

   public fun testAnonymousFunction() {
      { x: Int, y: Int ->
         x + y// 35
      }(1, 2)// 36
   }// 37

   public fun add(x: Int, y: Int): Int {
      return x + y// 39
   }

   public fun testFunctionReference() {
      ((this::add as KFunction) as Function2)(1, 2)// 42 43
   }// 44

   public fun testConstructorReference() {
      ((::Any as KFunction) as Function0)()// 47 48
   }// 49

   public fun testNoTypeReference() {
      (CollectionsKt::listOf as Function1)(1)// 52 53
   }// 54

   public fun testMultipleSubtypes() {
      @Metadata(
         mv = {2, 0, 0},
         k = 1,
         xi = 48,
         d1 = "\u0000\u0015\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005*\u0001\u0000\b\n\u0018\u00002\u00020\u00012\b\u0012\u0004\u0012\u00020\u00030\u0002J\b\u0010\u0004\u001a\u00020\u0003H\u0016J\u0015\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0003H\u0016¢\u0006\u0002\u0010\u0007¨\u0006\b",
         d2 = {
               "pkg/TestAnonymousEverything$testMultipleSubtypes$obj$1",
               "Ljava/lang/Runnable;",
               "Ljava/util/function/Consumer;",
               "",
               "run",
               "accept",
               "t",
               "(Lkotlin/Unit;)V",
               "vineflower-kotlin_testDataKotlin"
         }
      )
      final class NamelessClass_1 implements Runnable, Consumer<Unit> {
         public void run() {
            System.out.println("Runnable.run")
         }// 58

         public void accept(Unit t) {
            System.out.println("Consumer.accept")
         }// 59
      }

      val obj: NamelessClass_1 = object : Runnable, Consumer<Unit> {// 57
         public open fun run() {
            System.out.println("Runnable.run")
         }

         public open fun accept(t: Unit) {
            System.out.println("Consumer.accept")
         }
      }
      obj.run()// 62
      obj.accept(Unit.INSTANCE)// 63
   }// 64

   public fun testNestedLambdas() {
      { x: Int ->
         ({ y: Int ->
            `$x` + y// 68
         }(1) as java.lang.Number).intValue()// 69
      }(1)// 71
   }// 72

   public fun testCapturedLocalVariables() {
      val x: IntRef = IntRef()// 75
      x.element = 1
      { 
         `$x`.element++// 76
      }()// 77
      { 
         `$y`// 80
      }()// 81
   }// 82

   public fun testEarlyReturn() {
      val lambda: Function1 = lambda_6@{ x: Int ->// 85
         if (x == 0) {// 86
            return@lambda_6 42// 87
         } else if (x == -2147483648) {// 90
            return@lambda_6 2147483647// 91
         } else {
            return@lambda_6 if (x < 0) 0 else x + 1// 94 95 98
         }
      }
      lambda(1)// 100
      lambda(-1)// 101
   }// 102

   public fun testThrows() {
      val lambda: Function1 = { x: Int ->// 105
         if (x < 0) {// 106
            throw IllegalArgumentException("x must be >= 0")// 107
         } else if (x == 2147483647) {
            throw IllegalArgumentException("x must not be Int.MAX_VALUE".toString())// 110
         } else {
            x + 1// 112
         }
      }
   }// 114

   public fun testIgnoredParams() {
      { var0: Int, var1: Any ->
         Unit.INSTANCE// 117
      }(1, 2)// 118
   }// 119
}

class 'pkg/TestAnonymousEverything' {
   method 'testPlainLambda$lambda$0 (II)I' {
      0      15
      1      15
      2      15
   }

   method 'testPlainLambda ()V' {
      7      16
      8      16
      9      16
      a      16
      b      16
      c      16
      d      16
      e      16
      f      16
      10      16
      11      16
      12      16
      13      16
      15      17
   }

   method 'testSerializableLambda ()V' {
      0      41
      1      41
      2      41
      3      39
      4      39
      5      39
      7      39
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
      16      42
   }

   method 'testAnonymousObject ()V' {
      0      45
      1      45
      2      45
      8      45
      9      49
      a      49
      b      49
      c      49
      d      49
      f      50
   }

   method 'testFunctionAnonymousObject ()V' {
      0      53
      1      53
      2      53
      8      53
      9      57
      a      57
      b      57
      c      57
      d      57
      f      58
   }

   method 'testAnonymousFunction$lambda$1 (II)I' {
      0      62
      1      62
      2      62
   }

   method 'testAnonymousFunction ()V' {
      7      63
      8      63
      9      63
      a      63
      b      63
      c      63
      d      63
      e      63
      f      63
      10      63
      11      63
      12      63
      13      63
      15      64
   }

   method 'add (II)I' {
      0      67
      1      67
      2      67
      3      67
   }

   method 'testFunctionReference ()V' {
      0      71
      1      71
      2      71
      4      71
      8      71
      9      71
      a      71
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
      1e      72
   }

   method 'testConstructorReference ()V' {
      0      75
      1      75
      2      75
      3      75
      4      75
      5      75
      7      75
      8      75
      9      75
      a      75
      b      75
      c      75
      d      75
      e      75
      f      75
      11      76
   }

   method 'testNoTypeReference ()V' {
      0      79
      1      79
      2      79
      3      79
      4      79
      5      79
      7      79
      8      79
      9      79
      a      79
      b      79
      c      79
      d      79
      e      79
      f      79
      10      79
      12      80
   }

   method 'testMultipleSubtypes ()V' {
      0      110
      1      110
      2      110
      7      110
      8      119
      9      119
      a      119
      b      119
      c      120
      d      120
      e      120
      f      120
      10      120
      11      120
      12      120
      13      121
   }

   method 'testNestedLambdas$lambda$3 (I)I' {
      8      127
      9      127
      a      127
      b      127
      c      127
      d      127
      e      127
      f      127
      10      127
      11      125
      12      125
      13      125
      14      127
      15      127
      16      127
   }

   method 'testNestedLambdas$lambda$3$lambda$2 (II)I' {
      0      126
      1      126
      2      126
   }

   method 'testNestedLambdas ()V' {
      7      128
      8      128
      9      128
      a      128
      b      128
      c      128
      d      128
      e      128
      f      128
      11      129
   }

   method 'testCapturedLocalVariables ()V' {
      7      132
      8      133
      9      133
      a      133
      b      133
      c      133
      15      136
      16      136
      17      136
      18      136
      19      136
      27      139
      28      139
      29      139
      2a      139
      2b      139
      2d      140
   }

   method 'testCapturedLocalVariables$lambda$4 (Lkotlin/jvm/internal/Ref$IntRef;)I' {
      6      135
      7      135
      8      135
      9      135
      e      135
   }

   method 'testCapturedLocalVariables$lambda$5 (I)I' {
      0      138
   }

   method 'testEarlyReturn ()V' {
      5      143
      6      152
      7      152
      8      152
      9      152
      a      152
      b      152
      c      152
      d      152
      e      152
      f      152
      11      153
      12      153
      13      153
      14      153
      15      153
      16      153
      17      153
      18      153
      19      153
      1a      153
      1c      154
   }

   method 'testEarlyReturn$lambda$6 (I)I' {
      0      144
      1      144
      2      144
      3      144
      4      145
      5      145
      7      146
      8      146
      9      146
      a      146
      b      146
      c      146
      d      147
      e      147
      10      149
      11      149
      12      149
      13      149
      14      149
      16      149
      17      149
      18      149
   }

   method 'testThrows ()V' {
      5      157
      6      166
   }

   method 'testThrows$lambda$8 (I)I' {
      0      158
      1      158
      2      158
      3      158
      8      159
      9      159
      d      159
      e      160
      f      160
      10      160
      11      160
      12      160
      13      160
      19      160
      1a      160
      1b      160
      1e      161
      1f      161
      25      161
      26      161
      27      161
      28      161
      2c      161
      2d      163
      2e      163
      2f      163
   }

   method 'testIgnoredParams$lambda$9 (ILjava/lang/Object;)Lkotlin/Unit;' {
      6      170
      7      170
      8      170
   }

   method 'testIgnoredParams ()V' {
      7      171
      8      171
      9      171
      a      171
      b      171
      c      171
      d      171
      e      171
      f      171
      10      171
      11      171
      12      171
      13      171
      15      172
   }
}

class 'pkg/TestAnonymousEverything$testSerializableLambda$lambda$1' {
   method '<init> ()V' {
      1      31
      2      31
      3      31
      4      31
      5      32
   }

   method 'invoke (II)Ljava/lang/Integer;' {
      0      35
      1      35
      2      35
      3      35
      4      35
      5      35
   }
}

class 'pkg/TestAnonymousEverything$testAnonymousObject$obj$1' {
   method 'foo (II)I' {
      0      47
      1      47
      2      47
      3      47
   }
}

class 'pkg/TestAnonymousEverything$testFunctionAnonymousObject$obj$1' {
   method 'invoke (II)Ljava/lang/Integer;' {
      0      55
      1      55
      2      55
      3      55
      4      55
      5      55
      6      55
   }
}

class 'pkg/TestAnonymousEverything$testMultipleSubtypes$obj$1' {
   method 'run ()V' {
      0      102
      1      102
      2      102
      3      102
      4      102
      6      102
      7      102
      8      102
      9      103
   }

   method 'accept (Lkotlin/Unit;)V' {
      6      106
      7      106
      8      106
      9      106
      a      106
      c      106
      d      106
      e      106
      f      107
   }
}

Lines mapping:
7 <-> 16
8 <-> 17
9 <-> 18
12 <-> 42
13 <-> 40
14 <-> 43
17 <-> 46
19 <-> 48
22 <-> 46
23 <-> 51
26 <-> 54
28 <-> 56
31 <-> 54
32 <-> 59
35 <-> 63
36 <-> 64
37 <-> 65
39 <-> 68
42 <-> 72
43 <-> 72
44 <-> 73
47 <-> 76
48 <-> 76
49 <-> 77
52 <-> 80
53 <-> 80
54 <-> 81
57 <-> 111
58 <-> 104
59 <-> 108
62 <-> 120
63 <-> 121
64 <-> 122
68 <-> 127
69 <-> 128
71 <-> 129
72 <-> 130
75 <-> 133
76 <-> 136
77 <-> 137
80 <-> 139
81 <-> 140
82 <-> 141
85 <-> 144
86 <-> 145
87 <-> 146
90 <-> 147
91 <-> 148
94 <-> 150
95 <-> 150
98 <-> 150
100 <-> 153
101 <-> 154
102 <-> 155
105 <-> 158
106 <-> 159
107 <-> 160
110 <-> 162
112 <-> 164
114 <-> 167
117 <-> 171
118 <-> 172
119 <-> 173
Not mapped:
67
79
121
