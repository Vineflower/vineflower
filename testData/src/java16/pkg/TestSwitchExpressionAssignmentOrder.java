package pkg;

public class TestSwitchExpressionAssignmentOrder {
  public int test1(int i1) {
    int v = 5;
    return v + (v = test1(i1)) + switch (v) {
      case 0 -> 1;
      case 1 -> 2;
      default -> 0;
    };
  }

  public int test2(int i1) {
    int v = 5;
    return v + (v = i1) + switch (v) {
      case 0 -> 1;
      case 1 -> 2;
      default -> 0;
    };
  }

  public int test3(int i1) {
    int v = 5, o = 3;
    if (i1 > 0) {
      o = v + (v = i1) + switch (v) {
        case 0 -> 1;
        case 1 -> v = 2;
        default -> 0;
      };
    }
    return v - 3;
  }


  public int test4(int i1) {
    int v = 5, o;
    return (i1 += v) + (o = i1) + switch (v += i1) {
        case 0 -> o;
        case 1 -> v = 2;
        default -> 0;
      };
  }
}
