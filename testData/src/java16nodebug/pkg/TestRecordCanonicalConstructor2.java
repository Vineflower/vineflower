package pkg;

public record TestRecordCanonicalConstructor2(String name, Object meta) {
  public TestRecordCanonicalConstructor2(String name) {
    this(name, null);
    if (name == null) {
      throw new NullPointerException("name");
    }
  }

  public TestRecordCanonicalConstructor2(String name, Object meta) {
    if (name == null) {
      throw new NullPointerException("name");
    }
    this.name = name;
    this.meta = meta;
  }

  public TestRecordCanonicalConstructor2(String name, String meta) {
    this(name, (Object) meta.toLowerCase());
    if (name == null) {
      throw new NullPointerException("name");
    }
  }
}
