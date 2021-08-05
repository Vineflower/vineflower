package pkg;

public class TestTernaryConstantSimplification {
  public boolean ternaryNotAnd(boolean bl, boolean bl2) {
    return bl ? false : bl2;
  }

  public boolean ifOr(boolean bl, boolean bl2) {
    if (bl) {
      return true;
    } else {
      return bl2;
    }
  }

  public boolean ternaryNotOr(boolean bl, boolean bl2) {
    return bl ? bl2 : true;
  }

  public boolean ternaryAnd(boolean bl, boolean bl2) {
    return bl ? bl2 : false;
  }

  public boolean redundantIf(boolean bl) {
    if (bl) {
      return true;
    } else {
      return false;
    }
  }

  public boolean redundantTernary(boolean bl) {
    return bl ? true : false;
  }

  public boolean nestedIf(boolean bl, boolean bl2) {
    if (bl) {
      if (bl2) {
        return true;
      } else {
        return bl;
      }
    } else {
      return true;
    }
  }

  public boolean nestedTernary(boolean bl, boolean bl2) {
    return bl ? (bl2 ? true : false) : true;
  }

  public boolean nestedIfs(boolean bl, boolean bl2, boolean bl3) {
    if (bl) {
      if (bl2) {
        return false;
      } else {
        return bl3;
      }
    } else {
      if (bl3) {
        return bl3;
      } else {
        return bl2;
      }
    }
  }
}
