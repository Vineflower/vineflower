package pkg;

public class TestSwitchDefaultCaseReturn {
   public static String test(String s) {
      if (s.hashCode() == 0) {// 5
         return switch (s) {// 6
            case "", " " -> "";// 7
            default -> s;// 8
         };
      } else {
         int i = s.indexOf(97);// 12
         boolean bl = false;// 13

         while (true) {
            switch (s) {// 16
               case "":
               case " ":
                  return "";// 19
            }

            if (!bl && i != 0) {// 21
               return "";// 22
            }

            if (bl) {// 23
               return "";// 24
            }

            if (s.indexOf(65 + i) == -1) {// 27
               bl = true;// 28
            }
         }
      }
   }
}

class 'pkg/TestSwitchDefaultCaseReturn' {
   method 'test (Ljava/lang/String;)Ljava/lang/String;' {
      0      4
      1      4
      2      4
      3      4
      4      4
      5      4
      6      4
      7      5
      29      6
      2a      6
      37      6
      38      6
      42      5
      43      5
      44      5
      45      5
      46      5
      47      5
      48      5
      49      5
      4a      5
      4b      5
      4c      5
      4d      5
      4e      5
      4f      5
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
      5c      6
      5d      6
      61      7
      62      5
      63      10
      64      10
      65      10
      66      10
      67      10
      68      10
      69      10
      6a      11
      6b      11
      6c      14
      91      15
      92      15
      a0      16
      a1      16
      ad      14
      ae      14
      af      14
      b0      14
      b1      14
      b2      14
      b3      14
      b4      14
      b5      14
      b6      14
      b7      14
      b8      14
      b9      14
      ba      14
      bb      14
      bc      14
      bd      14
      be      14
      bf      14
      c0      14
      c1      14
      c2      14
      c3      14
      c4      14
      c5      14
      c6      14
      c7      14
      c8      17
      c9      17
      ca      17
      cb      20
      cc      20
      cd      20
      ce      20
      cf      20
      d0      20
      d1      20
      d2      20
      d3      21
      d4      21
      d5      21
      d6      24
      d7      24
      d8      24
      d9      24
      da      25
      db      25
      dc      25
      dd      28
      de      28
      df      28
      e0      28
      e1      28
      e2      28
      e3      28
      e4      28
      e5      28
      e6      28
      e7      28
      e8      28
      e9      29
      ea      29
   }
}

Lines mapping:
5 <-> 5
6 <-> 6
7 <-> 7
8 <-> 8
12 <-> 11
13 <-> 12
16 <-> 15
19 <-> 18
21 <-> 21
22 <-> 22
23 <-> 25
24 <-> 26
27 <-> 29
28 <-> 30
Not mapped:
30
