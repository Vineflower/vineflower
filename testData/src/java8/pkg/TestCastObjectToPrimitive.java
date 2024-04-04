package pkg;

public class TestCastObjectToPrimitive {

    static Object object1 = null;

    boolean castObject = (Boolean) object1;
    boolean negatedObject = !(Boolean) object1;
    boolean booleanXor = (Boolean) object1 ^ (Boolean) object1;
    boolean isObjectTrue = (Boolean) object1 == true;
    boolean isObjectFalse = (Boolean) object1 == false;
    boolean isObject6 = (Integer) object1 == 6;
    boolean booleanOr = (Boolean) object1 || (Boolean) object1;
    int integerXor = (Integer) object1 ^ 5;
    short shorXor = (short) ((Short) object1 ^ 58);
    long longXor = 8 ^ (Long) object1;
    boolean integerGe = (Integer) object1 <= 48;

    public void method() {
       if ((Boolean) object1) {
           System.out.println("Test");
       }
       while (((Boolean) object1) == false) {
           System.out.println("Test2");
       }
       while ((Boolean) object1) {
           System.out.println("Test3");
       }
    }

  public void compound1() {
    if (getObj() != null && (boolean) getObj()) {
      return;
    }
  }

  public void compound2() {
    if (getObj() != null && (Boolean) getObj()) {
      return;
    }
  }

  public void compound3() {
    if ((boolean) getObj()) {
      return;
    }
  }

  public Object getObj() {
    return new Object();
  }
}
