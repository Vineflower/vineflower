package pkg;

import java.util.Random;

public class TestArrayInitializations {
   public int[] test1(Random r) {
      return new int[]{r.nextInt(10), r.nextInt(20)};// 7
   }

   public int[] test2(Random r) {
      return new int[]{r.nextInt(10), r.nextInt(20)};// 12 13 14
   }

   public int[] test3(Random r) {
      int[] ints = new int[]{0, r.nextInt(20)};// 18 19
      ints[0] = r.nextInt(10);// 20
      return ints;// 21
   }

   public Object[] test4(Random r) {
      Object[] objects = new Object[]{r.nextInt(10), r, null};// 25 26 27
      objects[2] = objects;// 28
      return objects;// 29
   }

   public Object[] test5(Random r) {
      Object[] objects = new Object[]{r.nextInt(10), r, null};// 33 34 35
      objects[2] = objects[0];// 36
      return objects;// 37
   }

   public Object[] test6(Random r) {
      Object[] o;
      Object[] objects = o = new Object[3];// 43
      objects[0] = r.nextInt(10);// 44
      objects[1] = r;// 45
      objects[2] = o;// 46
      return objects;// 47
   }

   public Object[] test7(Random r) {
      Object[] objects;
      Object[] o = objects = new Object[3];// 53
      objects[0] = r.nextInt(10);// 54
      objects[1] = r;// 55
      objects[2] = o;// 56
      return objects;// 57
   }

   public int[] test8(Random r) {
      return new int[]{10, 20};// 61
   }

   public int[] test9(Random r) {
      return new int[]{10, 20};// 66 67 68
   }

   public int[] test10(Random r) {
      return new int[]{10, r.nextInt(20)};// 73 74 75
   }

   public int[] test11(Random r) {
      return new int[]{r.nextInt(10), 20};// 80 81 82
   }

   public int[] test12(Random r) {
      return new int[]{r.nextInt(10), r.nextInt(20), 30};// 87 88 89 90
   }

   public int[] test13(Random r) {
      return new int[]{10, 20};// 95 96 97
   }

   public int[] test14(Random r) {
      return new int[]{10, r.nextInt(20)};// 102 103 104
   }

   public int[] test15(Random r) {
      return new int[]{r.nextInt(10), 20};// 109 110 111
   }

   public int[] test16(Random r) {
      int[] ints = new int[]{0, r.nextInt(20), 30};// 115 116 117
      ints[0] = r.nextInt(10);// 118
      return ints;// 119
   }
}

class 'pkg/TestArrayInitializations' {
   method 'test1 (Ljava/util/Random;)[I' {
      5      6
      6      6
      7      6
      8      6
      9      6
      a      6
      e      6
      f      6
      10      6
      11      6
      12      6
      13      6
      15      6
   }

   method 'test2 (Ljava/util/Random;)[I' {
      6      10
      7      10
      8      10
      9      10
      a      10
      b      10
      f      10
      10      10
      11      10
      12      10
      13      10
      14      10
      17      10
   }

   method 'test3 (Ljava/util/Random;)[I' {
      3      14
      6      14
      7      14
      8      14
      9      14
      a      14
      b      14
      d      15
      e      15
      f      15
      10      15
      11      15
      12      15
      13      15
      14      15
      15      15
      16      16
      17      16
   }

   method 'test4 (Ljava/util/Random;)[Ljava/lang/Object;' {
      4      20
      7      20
      8      20
      9      20
      a      20
      b      20
      c      20
      d      20
      e      20
      f      20
      13      20
      15      21
      16      21
      17      21
      18      21
      19      22
      1a      22
   }

   method 'test5 (Ljava/util/Random;)[Ljava/lang/Object;' {
      4      26
      7      26
      8      26
      9      26
      a      26
      b      26
      c      26
      d      26
      e      26
      f      26
      13      26
      15      27
      16      27
      17      27
      18      27
      19      27
      1a      27
      1b      28
      1c      28
   }

   method 'test6 (Ljava/util/Random;)[Ljava/lang/Object;' {
      0      33
      5      33
      6      33
      7      34
      8      34
      9      34
      a      34
      b      34
      c      34
      d      34
      e      34
      f      34
      10      34
      11      34
      12      34
      13      35
      14      35
      15      35
      16      35
      17      36
      18      36
      19      36
      1a      36
      1b      37
      1c      37
   }

   method 'test7 (Ljava/util/Random;)[Ljava/lang/Object;' {
      0      42
      5      42
      6      42
      7      43
      8      43
      9      43
      a      43
      b      43
      c      43
      d      43
      e      43
      f      43
      10      43
      11      43
      12      43
      13      44
      14      44
      15      44
      16      44
      17      45
      18      45
      19      45
      1a      45
      1b      46
      1c      46
   }

   method 'test8 (Ljava/util/Random;)[I' {
      5      50
      6      50
      a      50
      b      50
      d      50
   }

   method 'test9 (Ljava/util/Random;)[I' {
      6      54
      7      54
      b      54
      c      54
      f      54
   }

   method 'test10 (Ljava/util/Random;)[I' {
      6      58
      7      58
      b      58
      c      58
      d      58
      e      58
      f      58
      10      58
      13      58
   }

   method 'test11 (Ljava/util/Random;)[I' {
      6      62
      7      62
      8      62
      9      62
      a      62
      b      62
      f      62
      10      62
      13      62
   }

   method 'test12 (Ljava/util/Random;)[I' {
      6      66
      7      66
      8      66
      9      66
      a      66
      b      66
      f      66
      10      66
      14      66
      15      66
      16      66
      17      66
      18      66
      19      66
      1c      66
   }

   method 'test13 (Ljava/util/Random;)[I' {
      6      70
      7      70
      b      70
      c      70
      f      70
   }

   method 'test14 (Ljava/util/Random;)[I' {
      6      74
      7      74
      8      74
      9      74
      a      74
      b      74
      f      74
      10      74
      13      74
   }

   method 'test15 (Ljava/util/Random;)[I' {
      6      78
      7      78
      b      78
      c      78
      d      78
      e      78
      f      78
      10      78
      13      78
   }

   method 'test16 (Ljava/util/Random;)[I' {
      3      82
      6      82
      7      82
      8      82
      9      82
      a      82
      b      82
      f      82
      10      82
      12      83
      13      83
      14      83
      15      83
      16      83
      17      83
      18      83
      19      83
      1a      83
      1b      84
      1c      84
   }
}

Lines mapping:
7 <-> 7
12 <-> 11
13 <-> 11
14 <-> 11
18 <-> 15
19 <-> 15
20 <-> 16
21 <-> 17
25 <-> 21
26 <-> 21
27 <-> 21
28 <-> 22
29 <-> 23
33 <-> 27
34 <-> 27
35 <-> 27
36 <-> 28
37 <-> 29
43 <-> 34
44 <-> 35
45 <-> 36
46 <-> 37
47 <-> 38
53 <-> 43
54 <-> 44
55 <-> 45
56 <-> 46
57 <-> 47
61 <-> 51
66 <-> 55
67 <-> 55
68 <-> 55
73 <-> 59
74 <-> 59
75 <-> 59
80 <-> 63
81 <-> 63
82 <-> 63
87 <-> 67
88 <-> 67
89 <-> 67
90 <-> 67
95 <-> 71
96 <-> 71
97 <-> 71
102 <-> 75
103 <-> 75
104 <-> 75
109 <-> 79
110 <-> 79
111 <-> 79
115 <-> 83
116 <-> 83
117 <-> 83
118 <-> 84
119 <-> 85
Not mapped:
11
65
72
79
86
94
101
108
