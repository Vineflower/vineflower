package pkg;

@TestInsideAnnotations.Ann(testStr = Constants.STRING_CONST_BAR)
public class TestInsideAnnotations {
  @Ann(testStr = Constants.STRING_CONST_BAR)
  int x;

  @Ann(testStr = Constants.STRING_CONST_BAR)
  void x(@Ann(testStr = Constants.STRING_CONST_BAR) int y) {
  }

  @interface Ann {
    String testStr() default Constants.STRING_CONST_FOO;
    Ann2 testAnn() default @Ann2(testInt = Constants.INT_CONST_0);
  }

  @interface Ann2 {
    int testInt();
  }
}
