package pkg;

@SharedName4
public class TestClashName extends ext.TestClashNameParent implements TestClashNameIface {
   int TestClashNameParent = 0;// 53
   int TestClashNameIface = 0;// 54
   int SharedName1 = 0;// 55
   int SharedName4 = 0;// 56
   int SharedName5 = 0;// 57
   int i = pkg.SharedName1.f;// 59
   int j = NonSharedName.f;// 60
   int k = SharedName2.f;// 61
   int l = pkg.SharedName3.f;// 62
   int m = pkg.SharedName1.getF();// 63
   int n = NonSharedName.getF();// 64
   SharedName1 p = null;// 65
   SharedName5<SharedName1> q = null;// 67

   @SharedName4
   public int m() {
      int SharedName2 = this.i;// 73
      pkg.SharedName1.f = this.j;// 74
      int x = SharedName2.f;// 75
      NonSharedName.f = this.k;// 76
      int y = NonSharedName.f;// 77
      return SharedName2 + x + y;// 78
   }

   @Override
   public void f() {
   }// 82
}

class 'pkg/TestClashName' {
   method '<init> ()V' {
      5      4
      a      5
      f      6
      14      7
      19      8
      1e      9
      1f      9
      20      9
      25      10
      26      10
      27      10
      2c      11
      2d      11
      2e      11
      33      12
      34      12
      35      12
      3a      13
      3b      13
      3c      13
      41      14
      42      14
      43      14
      48      15
      4d      16
   }

   method 'm ()I' {
      0      20
      1      20
      2      20
      3      20
      4      20
      5      21
      6      21
      7      21
      8      21
      9      21
      a      21
      b      21
      c      22
      d      22
      e      22
      f      22
      10      23
      11      23
      12      23
      13      23
      14      23
      15      23
      16      23
      17      24
      18      24
      19      24
      1a      24
      1b      25
      1c      25
      1d      25
      1e      25
      1f      25
      20      25
   }

   method 'f ()V' {
      0      30
   }
}

Lines mapping:
53 <-> 5
54 <-> 6
55 <-> 7
56 <-> 8
57 <-> 9
59 <-> 10
60 <-> 11
61 <-> 12
62 <-> 13
63 <-> 14
64 <-> 15
65 <-> 16
67 <-> 17
73 <-> 21
74 <-> 22
75 <-> 23
76 <-> 24
77 <-> 25
78 <-> 26
82 <-> 31
Not mapped:
52
