package pkg;

public class TestChainedCFG {
  public static void test(int a, int b, int c, int d) {
    A: while(true) {
      System.out.println("Hello world");
      B: while(true) {
        if (a++ == 0) {
          continue A;
        }
        C: while(true) {
          if (b++ == 0) {
            continue B;
          }
          D: while(true) {
            if (c++ == 0) {
              continue C;
            }
            E: while(true) {
              if (d++ == 0) {
                continue D;
              }
              F: while(true) {
                if ( (d+=c) == 0) {
                  continue E;
                }
                G: while(true) {
                  if ( (c+=b) == 0) {
                    continue F;
                  }
                  H: while(true) {
                    if ( (b+=a) == 0) {
                      continue G;
                    }
                    I: while(true) {
                      if ( (a+=d) == 0) {
                        continue H;
                      }
                      continue A;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
