package pkg;

import java.util.ArrayList;
import java.util.List;

public class TestGenericsHierarchy<T extends Number> {
  public T field;

  public <V extends T> void test(V v) {
    List<V> list = new ArrayList<>();
    List<? extends T> list2 = new ArrayList<>();
    List<T> list3 = new ArrayList<>();

    if (v != null) {
      list2 = list;
    }

    V v1 = list.get(0);
    T v2 = list2.get(0);

    list3.add(list2.get(0));

    this.field = v;
    setField(v);
  }

//  public <V extends T> void test1(V v) {
//    List<V> list = new ArrayList<>();
//    List<T> list2 = new ArrayList<>();
//    List<? extends T> list3 = new ArrayList<>();

//    list2 = list;
//    list3 = list;

//    list2.add(list.get(0));
//  }

  public void setField(T field) {
    this.field = field;
  }
}
