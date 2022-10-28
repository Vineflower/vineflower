package pkg;

public class TestRecordPattern4 {
  
  record Many(Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i) {}
  
  void test(Object m){
    if (m instanceof Many(
        String a, String b, String c, String d, String e, String f, String g, String h, String i
      ) mm) {
      System.out.println(a + b + c + d + e + f + g + h + i + mm.hashCode());
    }
  }
  
  void test2(Object m) {
    if (m instanceof Many(
        var a, String b, var c, var d, String e, var f, var g, String h, Many(
          var a2, var b2, var c2, var d2, var e2, var f2, var g2, String h2, Many i2
      ) i) mm) {
      System.out.println(b + c + e + h + h2 + i2 + i.hashCode() + mm.hashCode());
    }
  }
}