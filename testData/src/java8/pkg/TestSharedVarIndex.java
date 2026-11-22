package pkg;

import java.util.List;

public class TestSharedVarIndex {
  public void StringArrayVsString(){
    {
      String[] a = new String[0];
    }

    String b = "lol";
  }

  public static int ObjectVsInt(Object object, int value) {
    {
      Object alias = object;
      alias.hashCode();
    }
    {
      int alias = 0;
      int result = alias + value;
      return result;
    }
  }
  public static void ObjectVsInt2(int value) {
    {
      Object alias = null;
    }
    {
      int alias = value;
      switch (alias) {
        case 1:
          return;
        default:
          return;
      }
    }
  }

  public void lambda(List<Integer> l) {
    {
      int i = 0;
      System.out.println(i);
    }
    l.forEach(i -> System.out.println(i + 1));
  }
}
