package pkg;

public class TestStaticBlockFinalField {

  final static String field;
  final static String dum;

  static {
    String something = String.valueOf(1);
    field = something.length() + "dum";
    dum = field.substring(1);
  }
}
