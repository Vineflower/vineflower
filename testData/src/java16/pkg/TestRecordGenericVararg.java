package pkg;

public record TestRecordGenericVararg<T>(T first, T... other) {
  @SafeVarargs
  public TestRecordGenericVararg {}
}