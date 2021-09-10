package pkg;

public class TestAssignmentTernaryConstantSimplification {
  public void ternaryNotAnd(boolean bl, boolean bl2) {
    boolean bl3 = bl ? false : bl2;
    System.out.println(bl3);
  }

  public void ifOr(boolean bl, boolean bl2) {
    boolean bl3;
    if (bl) {
      bl3 = true;
    } else {
      bl3 = bl2;
    }
    System.out.println(bl3);
  }

  public void ternaryNotOr(boolean bl, boolean bl2) {
    boolean bl3 = bl ? bl2 : true;
    System.out.println(bl3);
  }

  public void ternaryAnd(boolean bl, boolean bl2) {
    boolean bl3 = bl ? bl2 : false;
    System.out.println(bl3);
  }

  public void redundantIf(boolean bl) {
    boolean bl3;
    if (bl) {
      bl3 = true;
    } else {
      bl3 = false;
    }
    System.out.println(bl3);
  }

  public void redundantTernary(boolean bl) {
    boolean bl3 = bl ? true : false;
    System.out.println(bl3);
  }

  public void nestedIf(boolean bl, boolean bl2) {
    boolean bl3;
    if (bl) {
      if (bl2) {
        bl3 = true;
      } else {
        bl3 = bl;
      }
    } else {
      bl3 = true;
    }
    System.out.println(bl3);
  }

  public void nestedTernary(boolean bl, boolean bl2) {
    boolean bl3 = bl ? (bl2 ? true : false) : true;
    System.out.println(bl3);
  }

  public void nestedIfs(boolean bl, boolean bl2, boolean bl3) {
    boolean bl4;
    if (bl) {
      if (bl2) {
        bl4 = false;
      } else {
        bl4 = bl3;
      }
    } else {
      if (bl3) {
        bl4 = bl3;
      } else {
        bl4 = bl2;
      }
    }
    System.out.println(bl4);
  }
}
