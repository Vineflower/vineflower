package pkg;

public class TestGroupConflict {
  private static final float FLOAT_PI = (float) Math.PI;
  private static final int CLASS_SCOPE = 1;
  private static final int METHOD_SCOPE = 1;

  float testConflictWithVineflower() {
    return FLOAT_PI;
  }

  int testMoreSpecificScopeWins(int i) {
    return i + METHOD_SCOPE;
  }

  int testMoreSpecificScopeWinsControl(int i) {
    return i + CLASS_SCOPE;
  }
}
