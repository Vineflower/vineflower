package pkg;

public class TestWildcardFieldParameter {
	public void test1() {
		Constants.consumeString("foo");
	}

	public void test2() {
		Constants.consumeString("bar");
	}

	public void test3() {
		Constants.consumeString(null);
	}
}
