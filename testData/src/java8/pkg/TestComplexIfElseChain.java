package pkg;

import java.util.Random;

public class TestComplexIfElseChain {
  public void test() {
    Random randy = new Random();
    int result = randy.nextInt(11);
    if (result == 0 || result == 1) {
      System.out.println("a");
    } else if (result == 2 || result == 3) {
      System.out.println("b");
    } else if (result == 4 || result == 5) {
      System.out.println("c");
    } else if (result == 6 || result == 7) {
      System.out.println("d");
    } else if (result == 8 || result == 9) {
      System.out.println("e");
    }
  }

  public void testInLoop() {
    Random randy = new Random();
    int result = randy.nextInt(11);
    for (int i = 0; i < 10; i++) {
      if (result == 0 || result == 1) {
        System.out.println("a");
      } else if (result == 2 || result == 3) {
        System.out.println("b");
      } else if (result == 4 || result == 5) {
        System.out.println("c");
      } else if (result == 6 || result == 7) {
        System.out.println("d");
      } else if (result == 8 || result == 9) {
        System.out.println("e");
      }
    }
  }

  public void testInLoop1() {
    Random randy = new Random();

    while (true) {
      int result = randy.nextInt(11);

      if (result == 0 || result == 1) {
        System.out.println("a");
      } else if (result == 2 || result == 3) {
        System.out.println("b");
      } else if (result == 4 || result == 5) {
        System.out.println("c");
      } else if (result == 6 || result == 7) {
        System.out.println("d");
      } else if (result == 8 || result == 9) {
        System.out.println("e");
        break;
      }
    }
  }

  public void testInLoop2() {
    Random randy = new Random();

    while (true) {
      int result = randy.nextInt(11);

      if (result == 0 || result == 1) {
        System.out.println("a");
      } else if (result == 2 || result == 3) {
        System.out.println("b");
      } else if (result == 4 || result == 5) {
        System.out.println("c");
        break;
      } else if (result == 6 || result == 7) {
        System.out.println("d");
        break;
      } else if (result == 8 || result == 9) {
        System.out.println("e");
        break;
      }
    }
  }

  public void testInLoop3() {
    Random randy = new Random();

    while (true) {
      int result = randy.nextInt(11);

      if (result == 0 || result == 1) {
        System.out.println("a");
      } else if (result == 2 || result == 3) {
        System.out.println("b");
      } else if (result == 4 || result == 5) {
        break;
      } else if (result == 6 || result == 7) {
        break;
      } else if (result == 8 || result == 9) {
        break;
      }
    }
  }

  public void testInLoop4(int i) {
    Random randy = new Random();

    do {
      i++;
      int result = randy.nextInt(11);

      if (result == 0 || result == 1) {
        System.out.println("a");
      } else if (result == 2 || result == 3) {
        System.out.println("b");
      } else if (result == 4 || result == 5) {
        System.out.println("c");
      } else if (result == 6 || result == 7) {
        System.out.println("d");
      } else if (result == 8 || result == 9) {
        System.out.println("e");
      }
    } while (i > 3);
  }

  public void testSwitch(int i) {
    Random randy = new Random();

    int result = randy.nextInt(11);
    switch (i) {
      case 0:
        System.out.println(1);
        break;
      case 1:
        if (result == 0 || result == 1) {
          System.out.println("a");
        } else if (result == 2 || result == 3) {
          System.out.println("b");
        } else if (result == 4 || result == 5) {
          System.out.println("c");
        } else if (result == 6 || result == 7) {
          System.out.println("d");
        } else if (result == 8 || result == 9) {
          System.out.println("e");
        }
        break;
      case 2:
        System.out.println(2);
    }

    System.out.println("hi");
  }

  public void testSwitch1(int i) {
    Random randy = new Random();

    int result = randy.nextInt(11);
    switch (i) {
      case 0:
        System.out.println(1);
        break;
      case 1:
        if (result == 0 || result == 1) {
          System.out.println("a");
        } else if (result == 2 || result == 3) {
          System.out.println("b");
        } else if (result == 4 || result == 5) {
          System.out.println("c");
          break;
        } else if (result == 6 || result == 7) {
          System.out.println("d");
          break;
        } else if (result == 8 || result == 9) {
          System.out.println("e");
          break;
        }
        System.out.println(3);
        break;
      case 2:
        System.out.println(2);
    }

    System.out.println("hi");
  }

  public void testFinally() {
    Random randy = new Random();

    int result = randy.nextInt(11);

    try {
      System.out.println("try");
    } finally {
      if (result == 0 || result == 1) {
        System.out.println("a");
      } else if (result == 2 || result == 3) {
        System.out.println("b");
      } else if (result == 4 || result == 5) {
        System.out.println("c");
      } else if (result == 6 || result == 7) {
        System.out.println("d");
      } else if (result == 8 || result == 9) {
        System.out.println("e");
      }
    }
  }

  public void testFinally1() {
    Random randy = new Random();

    int result = randy.nextInt(11);

    try {
      System.out.println("try");
    } finally {
      if (result == 0 || result == 1) {
        System.out.println("a");
      } else if (result == 2 || result == 3) {
        System.out.println("b");
      } else if (result == 4 || result == 5) {
        System.out.println("c");
      } else if (result == 6 || result == 7) {
        System.out.println("d");
      } else if (result == 8 || result == 9) {
        System.out.println("e");
      }
    }

    if (result == 0 || result == 1) {
      System.out.println("a");
    } else if (result == 2 || result == 3) {
      System.out.println("b");
    } else if (result == 4 || result == 5) {
      System.out.println("c");
    } else if (result == 6 || result == 7) {
      System.out.println("d");
    } else if (result == 8 || result == 9) {
      System.out.println("e");
    }
  }

  public void testFinally2() {
    Random randy = new Random();

    int result = randy.nextInt(11);

    try {
      System.out.println("try");
    } finally {
      if (result == 0 || result == 1) {
        try {
          System.out.println("a");
        } finally {
          System.out.println("a1");
        }
      } else if (result == 2 || result == 3) {
        try {
          System.out.println("b");
        } finally {
          System.out.println("b1");
        }
      } else if (result == 4 || result == 5) {
        try {
          System.out.println("c");
        } finally {
          System.out.println("c1");
        }
      } else if (result == 6 || result == 7) {
        try {
          System.out.println("d");
        } finally {
          System.out.println("d1");
        }
      } else if (result == 8 || result == 9) {
        try {
          System.out.println("e");
        } finally {
          System.out.println("e1");
        }
      }
    }
  }
}
