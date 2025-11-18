package pkg;

public class TestLabeledBlockInConstructor {
  public TestLabeledBlockInConstructor() {
    boolean result;
    block: {
      if (Math.random() < 0.5) {
        System.out.println(1); // print statement to prevent simplification into ||
        if (Math.random() < 0.5) {
          result = false;
          break block;
        }
      }
      result = true;
    }
    System.out.println(result);
  }
}
