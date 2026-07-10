package pkg;

import org.vineflower.marker.CatchAllException;

public class TestTryFinallyMarkerExceptionsInvalid {
  public Object testReturn() {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      return e; // return instead of throw
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
    return null;
  }


  public void testLogged(int i) {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      // eg logger injection
      System.out.println(e.getMessage());

      System.out.println("Finally");
      throw e;
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }


  public void testWeirdCatch(int i) {
    // In a try catch finally, the catch is actually inside the try block of the finally
    // here we don't. Such cases have been seen in non java code.
    try {
      System.out.println("Hello");
    } catch (NullPointerException npe) {
      System.out.println("NPE");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      throw e;
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }

  public void testWeirdCatch2(int i) {
    // In a try catch finally, the catch is actually inside the try block of the finally
    // here we don't. Such cases have been seen in non java code.
    try {
      System.out.println("Hello");
    } catch (NullPointerException npe) {
      System.out.println("Finally");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      throw e;
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }


  public void testWeirdCatch3(int i) {
    // In a try catch finally, the catch is actually inside the try block of the finally
    // here we don't. Such cases have been seen in non java code.
    try {
      System.out.println("Hello");
    } catch (NullPointerException npe) {
      System.out.println("Finally");
      System.out.println("NPE: " + npe.getMessage());
    } catch (CatchAllException e) {
      System.out.println("Finally");
      throw e;
    }
    {
      System.out.println("Finally");
    }

    System.out.println("Bye");
  }


  public void testBlockReuse(int i) {
    // while matching basic blocks, just because a "sample" block has been seen before
    // does not guarantee it matches with the block we are inspecting.
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      if (i > 0) {
        System.out.println("Cool 1");
      } else {
        System.out.println("Cool 2");
      }
      if (i < 0) ; // break up basic block
      throw e;
    }
    {
      System.out.println("Finally");
      if (i > 0) ;
      System.out.println("Cool 1");
      if (i < 0) ; // break up basic block
    }

    System.out.println("Bye");
  }

  public void testInjectWrongException(boolean b) {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally start");
      if (b) {
        e = new CatchAllException("yo");
      }
      System.out.println("Finally end");
      throw e;
    }
    {
      System.out.println("Finally start");
      if (b) {
        CatchAllException e = new CatchAllException("yo");
      }
      System.out.println("Finally end");
    }

    System.out.println("Bye");
  }

  public void testMultiImplicitExitsBad(int i) {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      if (i > 0) {
        System.out.println("Positive!");
        throw e;
      } else if (i == 0) {
        System.out.println("ZERO");
        throw e;
      } else {
        System.out.println("Negative!");
        throw e;
      }
    }
    {
      System.out.println("Finally");
      if (i > 0) {
        System.out.println("Positive!");
        System.out.println("Bye 1");
      } else if (i == 0) {
        System.out.println("ZERO");
        System.out.println("Bye 2");
      } else {
        System.out.println("Negative!");
        System.out.println("Bye 3");
      }
    }
  }


  public void testMultiImplicitExitsGood(int i) {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      if (i > 0) {
        System.out.println("Positive!");
        throw e;
      } else if (i == 0) {
        System.out.println("ZERO");
        throw e;
      } else {
        System.out.println("Negative!");
        throw e;
      }
    }
    {
      System.out.println("Finally");
      if (i > 0) {
        System.out.println("Positive!");
      } else if (i == 0) {
        System.out.println("ZERO");
      } else {
        System.out.println("Negative!");
      }
    }
    System.out.println("Bye");
  }

  public void testConfusion1(String a, String b) {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      System.out.println(a);
      throw e;
    }
    {
      System.out.println("Finally");
      System.out.println(b);
    }
    System.out.println("Bye");
  }

  public void testConfusion2() {
    String x, y, z;
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      x = "X";
      y = "Y";
      z = "Z";
      x += z;
      y += x;
      z += y;
      System.out.println(z);  // "ZYXZ"
      throw e;
    }
    {
      System.out.println("Finally");
      y = "X";  // x -> y
      z = "Y";  // y -> z
      x = "Z";  // z -> x
      z = y + z;  // x -> z
      x = z + x;  // y -> x
      y = x + y; // z -> y
      System.out.println(y);  // "XYZX"
    }
    System.out.println("Bye");
  }


  public void testMismatchedSwitch(int i) {
    try {
      System.out.println("Hello");
    } catch (CatchAllException e) {
      System.out.println("Finally");
      switch (i) {
        case 10:
          System.out.println("10");
        case 9:
          System.out.println("9");
        case 8:
          System.out.println("8");
        case 7:
          System.out.println("7");
        case 6:
          System.out.println("6");
        case 5:
          System.out.println("5");
        case 4:
          System.out.println("4");
        case 3:
          System.out.println("3");
        case 2:
          System.out.println("2");
        case 1:
          System.out.println("1");
        case 0:
          System.out.println("Lift off!");
      }
      throw e;
    }
    {
      System.out.println("Finally");
      switch (i) {
        case 1337_10:
          System.out.println("10");
        case 1337_09:
          System.out.println("9");
        case 1337_08:
          System.out.println("8");
        case 1337_07:
          System.out.println("7");
        case 1337_06:
          System.out.println("6");
        case 1337_05:
          System.out.println("5");
        case 1337_04:
          System.out.println("4");
        case 1337_03:
          System.out.println("3");
        case 1337_02:
          System.out.println("2");
        case 1337_01:
          System.out.println("1");
        case 1337_00:
          System.out.println("Lift off!");
      }
    }
    System.out.println("Bye");
  }

  public void testMismatchedOutputExit(int a, int b) {
    A:
    while (a > 0) {
      System.out.println("A: " + a);
      a--;
      B:
      while (b > 0) {
        a++;
        System.out.println("B: " + b);

        try {
          b--;
          System.out.println("Hello");
        } catch (CatchAllException e) {
          System.out.println("Finally");
          if (a > b) {
            a--;
            continue A;
          }
          System.out.println("Boop");
          throw e;
        }
        {
          System.out.println("Finally");
          if (a > b) {
            a--;
            continue B;
          }
          System.out.println("Boop");
        }
        System.out.println("Beep");
      }
      System.out.println("BLORB");
    }
    System.out.println("Bye");
  }


  public void testMismatchedOutputExit3(int a, int b, int c) {
    A:
    while (a > 0) {
      System.out.println("A: " + a);
      a--;
      B:
      while (b > 0) {
        a++;
        System.out.println("B: " + b);

        C:
        while (c > 0) {
          c -= b;

          C_post_finally:
          {
            C_finally:
            {
              try {
                b--;
                System.out.println("Hello");
                if (c < a) {
                  break C_finally;
                }
              } catch (CatchAllException e) {
                System.out.println("Finally");
                if (a > b) {
                  a--;
                  continue A;
                }
                System.out.println("Boop");
                throw e;
              }
              {
                System.out.println("Finally");
                if (a > b) {
                  a--;
                  continue B;
                }
                System.out.println("Boop");
              }
              break C_post_finally;
            }
            {
              System.out.println("Finally");
              if (a > b) {
                a--;
                continue C;
              }
              System.out.println("Boop");
              continue B;
            }
          }
          System.out.println("Lost");
        }
        System.out.println("Beep");
      }
      System.out.println("BLORB");
    }
    System.out.println("Bye");
  }


  public void testMismatchedOutputExitReturn(int a, int b) {
    A:
    while (a > 0) {
      System.out.println("A: " + a);
      a--;
      B:
      while (b > 0) {
        a++;
        System.out.println("B: " + b);

        try {
          b--;
          System.out.println("Hello");
        } catch (CatchAllException e) {
          System.out.println("Finally");
          if (a > b) {
            a--;
            continue A;
          }
          System.out.println("Boop");
          throw e;
        }
        {
          System.out.println("Finally");
          if (a > b) {
            a--;
            return;
          }
          System.out.println("Boop");
        }
        System.out.println("Beep");
      }
      System.out.println("BLORB");
    }
    System.out.println("Bye");
  }

  public void testMismatchedOutputExitReturnInner(int a, int b) {
    A:
    while (a > 0) {
      System.out.println("A: " + a);
      a--;
      B:
      while (b > 0) {
        a++;
        System.out.println("B: " + b);

        try {
          b--;
          System.out.println("Hello");
        } catch (CatchAllException e) {
          System.out.println("Finally");
          if (a > b) {
            a--;
            return;
          }
          System.out.println("Boop");
          throw e;
        }
        {
          System.out.println("Finally");
          if (a > b) {
            a--;
            continue B;
          }
          System.out.println("Boop");
        }
        System.out.println("Beep");
      }
      System.out.println("BLORB");
    }
    System.out.println("Bye");
  }
}
