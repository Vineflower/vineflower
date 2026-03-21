package pkg;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TestWhileIterator {
    public int testNested(List<Object> list, Set<Object> set) {
        Iterator<Object> it1 = list.iterator();
        Iterator<Object> it2 = set.iterator();
        int i = 0;

        while (it1.hasNext() && it2.hasNext()) {
            if (it1.next().equals(it2.next())) {
                i++;
            }
        }

        return i;
    }

    public Iterator<?> testDoubleUse1(Iterable<?> iterable) {
      System.out.println(iterable);
      Iterator<?> iterator = iterable.iterator();
      while (iterator.hasNext()) {
        Object next = iterator.next();
        if (next != null) {
          System.out.println(next);
          break;
        }
      }
      while (iterator.hasNext()) {
        Object next = iterator.next();
        if (next != null) {
          System.out.println(next);
        }
      }
      return iterator;
    }

  public Iterator<?> testDoubleUse2(Iterable<?> iterable) {
    System.out.println(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
        break;
      }
    }
    System.out.println(iterable);
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
      }
    }
    return iterator;
  }

  public Iterator<?> testDoubleUse3(Iterable<?> iterable) {
    System.out.println(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
        break;
      }
    }
    if (iterator != null) {
      while (iterator.hasNext()) {
        Object next = iterator.next();
        if (next != null) {
          System.out.println(next);
        }
      }
    }
    return iterator;
  }

  public Iterator<?> testDoubleUse4(Iterable<?> iterable) {
    System.out.println(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
        break;
      }
    }
    if (iterable != null) {
      while (iterator.hasNext()) {
        Object next = iterator.next();
        if (next != null) {
          System.out.println(next);
        }
      }
    }
    return iterator;
  }

  public Iterator<?> testDoubleUse5(Iterable<?> iterable) {
    System.out.println(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
        break;
      }
    }
    iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
      }
    }
    return iterator;
  }

  public void testDoubleUse6(Iterable<?> iterable) {
    System.out.println(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
        break;
      }
    }
    iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
      }
    }
  }

  public Iterator<?> testOtherUse1(Iterable<?> iterable) {
    System.out.println(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
        break;
      }
    }

    return iterator;
  }

  public Iterator<?> testOtherUse2(Iterable<?> iterable) {
    System.out.println(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
        break;
      }
    }

    return iterable.iterator();
  }

  public void testOtherUse3(Iterable<?> iterable) {
    System.out.println(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
        break;
      }
    }

    if (new Random().nextBoolean()) {
      System.out.println(iterator);
    }
  }

  public void testOtherUse4(Iterable<?> iterable) {
    System.out.println(iterable);
    Iterator<?> iterator = iterable.iterator();
    while (iterator.hasNext()) {
      Object next = iterator.next();
      if (next != null) {
        System.out.println(next);
        break;
      }
    }

    if (new Random().nextBoolean()) {
      System.out.println(iterable);
    }
  }
}
