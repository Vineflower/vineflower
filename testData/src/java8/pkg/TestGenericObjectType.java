package pkg;

import java.util.ArrayList;
import java.util.List;

public class TestGenericObjectType<T> {
  public static final List<Object> LIST = new ArrayList<>();
  public static final List<Number> LIST_NUM = new ArrayList<>();

  public static <T> List<T> getListStatic() {
    return (List<T>) LIST;
  }

  public static <T extends Number> List<T> getListStatic1() {
    return (List<T>) LIST_NUM;
  }

  public List<T> getList() {
    return (List<T>) LIST;
  }
}
