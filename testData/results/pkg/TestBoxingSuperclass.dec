package pkg;

public class TestBoxingSuperclass {
   public void referenceImplementationClass() {
      TestBoxingSuperclass.Implementation impl = new TestBoxingSuperclass.Implementation();// 8
      impl.type(true);// 9
      impl.type(1);// 10
      impl.type(1.0F);// 11
      impl.testAmbiguity(1.0F);// 12
      impl.testAmbiguity(Float.valueOf(1.0F));// 13
   }// 14

   public void referenceSuperclassClass() {
      TestBoxingSuperclass.Parent impl = new TestBoxingSuperclass.Implementation();// 17
      impl.type(true);// 18
      impl.type(1);// 19
      impl.type(1.0F);// 20
      impl.testAmbiguity(1.0F);// 21
      impl.testAmbiguity(Float.valueOf(1.0F));// 22
   }// 23

   public void referenceImplementationInterface() {
      TestBoxingSuperclass.InterfaceImplementation impl = new TestBoxingSuperclass.InterfaceImplementation();// 26
      impl.type(true);// 27
      impl.type(1);// 28
      impl.type(1.0F);// 29
      impl.testAmbiguity(1.0F);// 30
      impl.testAmbiguity(Float.valueOf(1.0F));// 31
   }// 32

   public void referenceSuperclassInterface() {
      TestBoxingSuperclass.OtherTypes impl = new TestBoxingSuperclass.InterfaceImplementation();// 35
      impl.type(true);// 36
      impl.type(1);// 37
      impl.type(1.0F);// 38
      impl.testAmbiguity(1.0F);// 39
      impl.testAmbiguity(Float.valueOf(1.0F));// 40
   }// 41

   public class Implementation extends TestBoxingSuperclass.Parent {
   }

   public static class InterfaceImplementation implements TestBoxingSuperclass.OtherTypes {
   }

   interface OtherTypes {
      default void type(Boolean type) {
      }// 72

      default void type(Integer type) {
      }// 75

      default void type(Float type) {
      }// 78

      default void testAmbiguity(Float type) {
      }// 81

      default void testAmbiguity(float type) {
      }// 84
   }

   public abstract static class Parent {
      public void type(Boolean type) {
      }// 50

      public void type(Integer type) {
      }// 53

      public void type(Float type) {
      }// 56

      public void testAmbiguity(float type) {
      }// 59

      public void testAmbiguity(Float type) {
      }// 62
   }
}

class 'pkg/TestBoxingSuperclass' {
   method 'referenceImplementationClass ()V' {
      8      4
      9      5
      a      5
      b      5
      c      5
      d      5
      e      5
      f      5
      10      5
      11      6
      12      6
      13      6
      14      6
      15      6
      16      6
      17      6
      18      6
      19      7
      1a      7
      1b      7
      1c      7
      1d      7
      1e      7
      1f      7
      20      7
      21      8
      22      8
      23      8
      24      8
      25      8
      26      9
      27      9
      28      9
      29      9
      2a      9
      2b      9
      2c      9
      2d      9
      2e      10
   }

   method 'referenceSuperclassClass ()V' {
      8      13
      9      14
      a      14
      b      14
      c      14
      d      14
      e      14
      f      14
      10      14
      11      15
      12      15
      13      15
      14      15
      15      15
      16      15
      17      15
      18      15
      19      16
      1a      16
      1b      16
      1c      16
      1d      16
      1e      16
      1f      16
      20      16
      21      17
      22      17
      23      17
      24      17
      25      17
      26      18
      27      18
      28      18
      29      18
      2a      18
      2b      18
      2c      18
      2d      18
      2e      19
   }

   method 'referenceImplementationInterface ()V' {
      7      22
      8      23
      9      23
      a      23
      b      23
      c      23
      d      23
      e      23
      f      23
      10      24
      11      24
      12      24
      13      24
      14      24
      15      24
      16      24
      17      24
      18      25
      19      25
      1a      25
      1b      25
      1c      25
      1d      25
      1e      25
      1f      25
      20      26
      21      26
      22      26
      23      26
      24      26
      25      27
      26      27
      27      27
      28      27
      29      27
      2a      27
      2b      27
      2c      27
      2d      28
   }

   method 'referenceSuperclassInterface ()V' {
      7      31
      8      32
      9      32
      a      32
      b      32
      c      32
      d      32
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
      1c      34
      1d      34
      1e      34
      1f      34
      20      34
      21      34
      22      34
      23      34
      24      34
      25      34
      26      35
      27      35
      28      35
      29      35
      2a      35
      2b      35
      2c      35
      2d      36
      2e      36
      2f      36
      30      36
      31      36
      32      36
      33      36
      34      36
      35      36
      36      36
      37      37
   }
}

class 'pkg/TestBoxingSuperclass$OtherTypes' {
   method 'type (Ljava/lang/Boolean;)V' {
      0      47
   }

   method 'type (Ljava/lang/Integer;)V' {
      0      50
   }

   method 'type (Ljava/lang/Float;)V' {
      0      53
   }

   method 'testAmbiguity (Ljava/lang/Float;)V' {
      0      56
   }

   method 'testAmbiguity (F)V' {
      0      59
   }
}

class 'pkg/TestBoxingSuperclass$Parent' {
   method 'type (Ljava/lang/Boolean;)V' {
      0      64
   }

   method 'type (Ljava/lang/Integer;)V' {
      0      67
   }

   method 'type (Ljava/lang/Float;)V' {
      0      70
   }

   method 'testAmbiguity (F)V' {
      0      73
   }

   method 'testAmbiguity (Ljava/lang/Float;)V' {
      0      76
   }
}

Lines mapping:
8 <-> 5
9 <-> 6
10 <-> 7
11 <-> 8
12 <-> 9
13 <-> 10
14 <-> 11
17 <-> 14
18 <-> 15
19 <-> 16
20 <-> 17
21 <-> 18
22 <-> 19
23 <-> 20
26 <-> 23
27 <-> 24
28 <-> 25
29 <-> 26
30 <-> 27
31 <-> 28
32 <-> 29
35 <-> 32
36 <-> 33
37 <-> 34
38 <-> 35
39 <-> 36
40 <-> 37
41 <-> 38
50 <-> 65
53 <-> 68
56 <-> 71
59 <-> 74
62 <-> 77
72 <-> 48
75 <-> 51
78 <-> 54
81 <-> 57
84 <-> 60
