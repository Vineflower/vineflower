package pkg;

public record TestRecordCanonicalConstructor(String name, Object meta) {
  public TestRecordCanonicalConstructor {
    if (name == null) {
      throw new NullPointerException("name");
    }
  }
}
