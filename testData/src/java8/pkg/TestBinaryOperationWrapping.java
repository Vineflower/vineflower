package pkg;

public class TestBinaryOperationWrapping {
  public void testStringConcatenation(String longVariableName) {
    System.out.println(
      "This is a very very very very very very very very very very very very very very very long string" +
        longVariableName + longVariableName + longVariableName + longVariableName + longVariableName
    );
  }

  public void testBooleanOperation(boolean a, boolean b, boolean c, boolean d, boolean e, boolean f, boolean g, boolean h, boolean i, boolean j, boolean k, boolean l, boolean m, boolean n, boolean o, boolean p, boolean q, boolean r, boolean s, boolean t, boolean u, boolean v, boolean w, boolean x, boolean y, boolean z) {
    System.out.println(a && b || c && d || e && f || g && h || i && j || k && l || m && n || o && p || q && r || s && t || u && v || w && x || y && z);
  }
}
