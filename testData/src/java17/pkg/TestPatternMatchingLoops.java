package pkg;

import java.util.List;

public class TestPatternMatchingLoops {
  public interface I {
    List<TestPatternMatchingLoops> get();
  }

  public static class Holder implements I {
    public List<TestPatternMatchingLoops> list;

    @Override
    public List<TestPatternMatchingLoops> get() {
      return List.of();
    }
  }

  public void test(I i) {
    System.out.println(i);

    if (i == null) {
      return;
    }

    List<TestPatternMatchingLoops> list = i instanceof Holder holder ? holder.list : i.get();

    for (TestPatternMatchingLoops l : list) {
      if (l == null) {
        continue;
      }

      System.out.println(l);
    }
  }
}
