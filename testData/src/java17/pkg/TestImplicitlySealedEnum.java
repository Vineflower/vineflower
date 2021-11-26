package pkg;

public enum TestImplicitlySealedEnum {
  A, B {
    @Override
    int getX() {
      return 2;
    }
  };

  int getX() {
    return 1;
  }
}
