package pkg;

public class TestNonStaticConstants {
	public final int foo = 3;
	public final String bar = "Hello, World!";
	public final Class<?> baz = String.class;
	public final String nullConstant = null;

	public void test() {
		Constants.consumeInt(3);
		Constants.consumeString("Hello, World!");
		Constants.consumeClass(String.class);
		Constants.consumeString(null);
	}

	public void testOther(TestNonStaticConstants other) {
		Constants.consumeInt(other.foo);
		Constants.consumeString(new TestNonStaticConstants().bar);
	}

	public class TestInner {
		public void test() {
			Constants.consumeInt(3);
		}

		public class TestInnerInner {
			public void test() {
				Constants.consumeInt(3);
				System.out.println(TestNonStaticConstants.this); // use the outer class so it's preserved
			}
		}
	}
}
