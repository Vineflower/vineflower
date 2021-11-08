package pkg;

public enum TestEnumArrayStaticInit {
    A(0), B(1), C(2);

    private static final TestEnumArrayStaticInit[] VALUES;

    private final int v;

    TestEnumArrayStaticInit(int v) {
      this.v = v;
    }

    static {
      TestEnumArrayStaticInit[] values = values();

      VALUES =  new TestEnumArrayStaticInit[TestEnumArrayStaticInit.C.v + 1];

      for (TestEnumArrayStaticInit e : values) {
        VALUES[e.v] = e;
      }
    }
}
