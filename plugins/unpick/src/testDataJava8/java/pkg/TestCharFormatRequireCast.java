package pkg;

public class TestCharFormatRequireCast {
  public void testChar() {
    consumeChar(':');
    consumeChar((int) ':');
  }

  public void testInt() {
    consumeInt(42);
    consumeInt((char) 42);
  }

  private void consumeChar(int i) {
  }

  private void consumeChar(char c) {
  }

  private void consumeInt(int i) {
  }

  private void consumeInt(char c) {
  }
}
