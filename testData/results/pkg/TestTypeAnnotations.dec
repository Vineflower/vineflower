package pkg;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;

public class TestTypeAnnotations {
   public @TestTypeAnnotations.Anno TestTypeAnnotations.Inner a;
   public TestTypeAnnotations.@TestTypeAnnotations.Anno Inner b;
   public TestTypeAnnotations.@TestTypeAnnotations.Anno InnerStatic c;
   public @TestTypeAnnotations.Anno TestTypeAnnotations[] d;
   public TestTypeAnnotations @TestTypeAnnotations.Anno [] e;
   public TestTypeAnnotations.@TestTypeAnnotations.Anno InnerStatic.Inner2 f;
   public TestTypeAnnotations.InnerStatic.@TestTypeAnnotations.Anno Inner2 g;
   public TestTypeAnnotations.InnerStatic.@TestTypeAnnotations.Anno Inner2Static g_1;
   public TestTypeAnnotations.@TestTypeAnnotations.Anno Inner[] h;
   public TestTypeAnnotations.Inner @TestTypeAnnotations.Anno [] i;
   public @TestTypeAnnotations.Anno TestTypeAnnotations[][] j;
   public TestTypeAnnotations.Inner[] @TestTypeAnnotations.Anno [] k;
   public TestTypeAnnotations.Inner @TestTypeAnnotations.Anno [][] k_1;
   public TestTypeAnnotations.InnerStatic.Inner2 @TestTypeAnnotations.Anno [] l;
   public TestTypeAnnotations.InnerStatic.@TestTypeAnnotations.Anno Inner2[] m;
   public TestTypeAnnotations.@TestTypeAnnotations.Anno G1<TestTypeAnnotations> n;
   public TestTypeAnnotations.G1<@TestTypeAnnotations.Anno TestTypeAnnotations> o;
   public TestTypeAnnotations.@TestTypeAnnotations.Anno G1<TestTypeAnnotations>[] p;
   public TestTypeAnnotations.G1<@TestTypeAnnotations.Anno TestTypeAnnotations[]> q;
   public TestTypeAnnotations.G1<TestTypeAnnotations @TestTypeAnnotations.Anno []> r;
   public TestTypeAnnotations.G1<@TestTypeAnnotations.Anno ? extends TestTypeAnnotations> s;
   public TestTypeAnnotations.G1<? extends @TestTypeAnnotations.Anno TestTypeAnnotations> t;
   public TestTypeAnnotations.G1<@TestTypeAnnotations.Anno ?> u;
   public @TestTypeAnnotations.Anno MethodHandle v;
   public TestTypeAnnotations.G2<@TestTypeAnnotations.Anno String, Integer> w;
   public TestTypeAnnotations.G2<String, @TestTypeAnnotations.Anno Integer> x;

   public @TestTypeAnnotations.Anno TestTypeAnnotations.Inner a() {
      return null;// 97
   }

   public TestTypeAnnotations.@TestTypeAnnotations.Anno Inner b() {
      return null;// 101
   }

   public TestTypeAnnotations.@TestTypeAnnotations.Anno InnerStatic c() {
      return null;// 105
   }

   public @TestTypeAnnotations.Anno TestTypeAnnotations[] d() {
      return null;// 109
   }

   public TestTypeAnnotations @TestTypeAnnotations.Anno [] e() {
      return null;// 113
   }

   public byte @TestTypeAnnotations.Anno [] ary(byte @TestTypeAnnotations.Anno [] ary) {
      return ary;// 117
   }

   public void varargs(@TestTypeAnnotations.Anno Object... args) {
   }// 122

   public void varargs(int v, @TestTypeAnnotations.Anno Object... args) {
   }// 126

   public void varargs2(Object @TestTypeAnnotations.Anno ... args) {
   }// 130

   public void varargs2(int v, Object @TestTypeAnnotations.Anno ... args) {
   }// 134

   public void varargs3a(Object[] @TestTypeAnnotations.Anno ... args) {
   }// 138

   public void varargs3b(Object @TestTypeAnnotations.Anno []... args) {
   }// 142

   @Target(ElementType.TYPE_USE)
   public @interface Anno {
   }

   public static class G1<T> {
   }

   public static class G2<K, V> {
   }

   public class Inner {
      public TestTypeAnnotations.Inner.@TestTypeAnnotations.Anno InnerRec aa;
      public TestTypeAnnotations.Inner.@TestTypeAnnotations.Anno InnerInnerStatic ab;
      public @TestTypeAnnotations.Anno TestTypeAnnotations.Inner.InnerInner ac;
      public TestTypeAnnotations.@TestTypeAnnotations.Anno Inner.InnerInner ad;
      public TestTypeAnnotations.Inner.@TestTypeAnnotations.Anno InnerInner ae;

      public Inner(@TestTypeAnnotations.Anno String s, Object o, @TestTypeAnnotations.Anno String s2) {
      }// 22

      public class InnerInner {
      }

      public static class InnerInnerStatic {
         public static TestTypeAnnotations.Inner.@TestTypeAnnotations.Anno InnerRec.A ca;
         public static TestTypeAnnotations.Inner.InnerRec.@TestTypeAnnotations.Anno A cb;
         public static TestTypeAnnotations.Inner.InnerInnerStatic.@TestTypeAnnotations.Anno B cc;
         public static TestTypeAnnotations.Inner.@TestTypeAnnotations.Anno InnerInnerStatic.B cd;

         public class B {
         }
      }

      public record InnerRec() {
         public static TestTypeAnnotations.Inner.@TestTypeAnnotations.Anno InnerRec.A ba;
         public static TestTypeAnnotations.Inner.InnerRec.@TestTypeAnnotations.Anno A bb;

         public class A {
         }
      }
   }

   public static class InnerStatic {
      public class Inner2 {
      }

      public static class Inner2Static {
      }
   }

   public enum TestEnum {
      A("a"),// 145
      B("b"),// 146
      C("c");// 147

      TestEnum(@TestTypeAnnotations.Anno String name) {
         this(name, name.charAt(0), 0, null);// 150
      }// 151

      TestEnum(@TestTypeAnnotations.Anno String name, char c, int i, @TestTypeAnnotations.Anno Object o) {
         System.out.println(name + " " + c + i + " " + o);// 154
      }// 155
   }
}

class 'pkg/TestTypeAnnotations' {
   method 'a ()Lpkg/TestTypeAnnotations$Inner;' {
      0      35
      1      35
   }

   method 'b ()Lpkg/TestTypeAnnotations$Inner;' {
      0      39
      1      39
   }

   method 'c ()Lpkg/TestTypeAnnotations$InnerStatic;' {
      0      43
      1      43
   }

   method 'd ()[Lpkg/TestTypeAnnotations;' {
      0      47
      1      47
   }

   method 'e ()[Lpkg/TestTypeAnnotations;' {
      0      51
      1      51
   }

   method 'ary ([B)[B' {
      0      55
      1      55
   }

   method 'varargs ([Ljava/lang/Object;)V' {
      0      59
   }

   method 'varargs (I[Ljava/lang/Object;)V' {
      0      62
   }

   method 'varargs2 ([Ljava/lang/Object;)V' {
      0      65
   }

   method 'varargs2 (I[Ljava/lang/Object;)V' {
      0      68
   }

   method 'varargs3a ([[Ljava/lang/Object;)V' {
      0      71
   }

   method 'varargs3b ([[Ljava/lang/Object;)V' {
      0      74
   }
}

class 'pkg/TestTypeAnnotations$Inner' {
   method '<init> (Lpkg/TestTypeAnnotations;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;)V' {
      4      94
   }
}

class 'pkg/TestTypeAnnotations$TestEnum' {
   method '<clinit> ()V' {
      7      127
      8      127
      16      128
      17      128
      25      129
      26      129
   }

   method '<init> (Ljava/lang/String;ILjava/lang/String;)V' {
      3      132
      4      132
      5      132
      6      132
      7      132
      8      132
      9      132
      a      132
      b      132
      c      132
      d      132
      e      133
   }

   method '<init> (Ljava/lang/String;ILjava/lang/String;CILjava/lang/Object;)V' {
      6      136
      7      136
      8      136
      9      136
      a      136
      b      136
      c      136
      d      136
      e      136
      f      136
      13      136
      14      136
      15      136
      16      136
      17      136
      18      136
      19      136
      1a      136
      1b      137
   }
}

Lines mapping:
22 <-> 95
97 <-> 36
101 <-> 40
105 <-> 44
109 <-> 48
113 <-> 52
117 <-> 56
122 <-> 60
126 <-> 63
130 <-> 66
134 <-> 69
138 <-> 72
142 <-> 75
145 <-> 128
146 <-> 129
147 <-> 130
150 <-> 133
151 <-> 134
154 <-> 137
155 <-> 138
Not mapped:
20
144
153
