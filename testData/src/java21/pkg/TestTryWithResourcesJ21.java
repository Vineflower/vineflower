package pkg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class TestTryWithResourcesJ21 {
  sealed interface Sealed {
    record R1(int x) implements Sealed {}

    record R2(String s) implements Sealed {}
  }

  public void test1(File f) {
    try (Scanner s = create(f)) {
      Sealed i = get(s.nextLine());
      if (i instanceof Sealed.R1(int v)) {
        System.out.println(v);
      } else if (i instanceof Sealed.R2(String w)) {
        System.out.println(w);
      }
    } catch (FileNotFoundException e) {
      System.out.println(e);
    }
  }

  public Object test2(File f) {
    try (Scanner s = create(f)) {
      Sealed i = get(s.nextLine());
      if (i instanceof Sealed.R1(int v)) {
        return v;
      } else if (i instanceof Sealed.R2(String w)) {
        return w;
      }
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  public Object test3(File f) {
    try (Scanner s = create(f)) {
      Sealed i = get(s.nextLine());
      if (i instanceof Sealed.R1(int v)) {
        return v;
      }
      System.out.println("end");
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  public Object test4(File f) {
    try (Scanner s = create(f)) {
      Sealed i = get(s.nextLine());
      if (i instanceof Sealed.R1 r) {
        return r;
      }
      System.out.println("end");
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  public Object test5(File f) {
    try (Scanner s = create(f)) {
      Sealed i = get(s.nextLine());
      if (i instanceof Sealed.R1(int v)) {
        System.out.println(v);
      } else if (i instanceof Sealed.R2(String w)) {
        return w;
      }
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  public Object test6(File f) {
    try (Scanner s = create(f)) {
      Sealed i = get(s.nextLine());
      if (i instanceof Sealed.R1(int v)) {
        return v;
      } else if (i instanceof Sealed.R2(String w)) {
        System.out.println(w);
      }
    } catch (FileNotFoundException e) {
      return e;
    }

    return null;
  }

  private Sealed get(String s) {
    return null;
  }

  private Scanner create(File file) throws FileNotFoundException {
    return new Scanner(file);
  }
}
