package pkg;

public class TestRecordGenericSuperclass<G extends Number> {
  public G getNum() {
    return null;
  }

  public record Rec<G>(G num) {
    public G getNum() {
      return this.num;
    }
  }
}
