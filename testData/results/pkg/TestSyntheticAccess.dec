package pkg;

class TestSyntheticAccess {
   private static int s;
   private int i;

   private class Assigner {
      void assignI(int newValue) {
         TestSyntheticAccess.this.i = newValue;// 32
      }// 33

      void assignS(int newValue) {
         TestSyntheticAccess.s = newValue;// 36
      }// 37
   }

   private class Incrementer {
      void orI() {
         TestSyntheticAccess.this.i = TestSyntheticAccess.this.i | 1;// 10
      }// 11

      void incrementI() {
         TestSyntheticAccess.this.i++;// 14
      }// 15

      void decrementI() {
         --TestSyntheticAccess.this.i;// 18
      }// 19

      void incrementS() {
         ++TestSyntheticAccess.s;// 22
      }// 23

      void decrementS() {
         TestSyntheticAccess.s--;// 26
      }// 27
   }
}

class 'pkg/TestSyntheticAccess$Assigner' {
   method 'assignI (I)V' {
      1      8
      2      8
      3      8
      5      8
      6      8
      7      8
      9      9
   }

   method 'assignS (I)V' {
      1      12
      2      12
      3      12
      5      13
   }
}

class 'pkg/TestSyntheticAccess$Incrementer' {
   method 'orI ()V' {
      1      18
      2      18
      3      18
      5      18
      6      18
      7      18
      8      18
      9      18
      a      18
      b      18
      d      18
      e      18
      f      18
      11      19
   }

   method 'incrementI ()V' {
      1      22
      2      22
      3      22
      4      22
      5      22
      6      22
      8      23
   }

   method 'decrementI ()V' {
      1      26
      2      26
      3      26
      4      26
      5      26
      6      26
      8      27
   }

   method 'incrementS ()V' {
      0      30
      1      30
      2      30
      4      31
   }

   method 'decrementS ()V' {
      0      34
      1      34
      2      34
      4      35
   }
}

Lines mapping:
10 <-> 19
11 <-> 20
14 <-> 23
15 <-> 24
18 <-> 27
19 <-> 28
22 <-> 31
23 <-> 32
26 <-> 35
27 <-> 36
32 <-> 9
33 <-> 10
36 <-> 13
37 <-> 14
