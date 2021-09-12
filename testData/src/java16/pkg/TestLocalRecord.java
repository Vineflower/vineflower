package pkg;

public class TestLocalRecord {
  public void test(int i) {
    record Color(int red, int green, int blue) {}
    Color color = new Color(((i >> 16) & 0xFF) / 255, ((i >> 8) & 0xFF) / 255, (i & 0xFF) / 255);
    System.out.println(color);
  }
}
