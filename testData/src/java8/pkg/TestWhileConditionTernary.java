package pkg;

public class TestWhileConditionTernary {
  public void test1() {
    if (blackBox()) {
      while (!blackBox() && (blackBox() ? blackBox3().equals("a") : blackBox2())) {
        System.out.println("text");
      }
    }
  }

  public boolean blackBox() {
    return true;
  }

  public boolean blackBox2() {
    return false;
  }

  public String blackBox3() {
    return "thing;";
  }
}
