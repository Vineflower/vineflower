package pkg;

public class TestDuplicateSwitchLocals {
  void test(int foo) {
    switch (foo) {
      case 0:
        int x = 10;
        for (int i = 0; i < x; i++) System.out.println(i);
        break;
      case 1:
        int y = 11;
        for (int i = 0; i < y; i++) System.out.println(i);
        break;
      case 2: {
        int z = 2;
        System.out.println(z);
      }
      case 3: {
        int z = 3;
        System.out.println(z);
      }
    }
  }
}
