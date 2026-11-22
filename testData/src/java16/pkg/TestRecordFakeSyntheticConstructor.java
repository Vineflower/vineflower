package pkg;

public record TestRecordFakeSyntheticConstructor(int x, int y) {
  public TestRecordFakeSyntheticConstructor(int x, int y) {
    this.x = 0;
    this.y = y;
  }
}
