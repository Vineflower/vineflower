package pkg;

public class TestRecordPattern3 {
  
  record Bundle<A>(A a, String b, String c, int i1, int i2, int i3, int i4, long l1, long l2, char c1, char c2, byte bb, double dd, boolean bool) {}
  
  void test(Bundle<?> bundle){
    if (bundle instanceof Bundle<?>(
      String a, var b, var c, var i1, var i2, var i3, var i4, var l1, var l2, var c1, var c2, var bb, var dd, var bool
      )){
      System.out.println(((a + b + c) + (i1 + i2 + i3 + i4 + l1 + l2 + c1 + c2 + bb + dd)) + bool);
    }
  }
}