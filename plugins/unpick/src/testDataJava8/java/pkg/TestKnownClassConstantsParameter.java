package pkg;

public class TestKnownClassConstantsParameter {
	public void test1() {
		Constants.consumeClass(String.class);
	}

	public void test2() {
		Constants.consumeClass(Integer.class);
	}

	public void test3() {
		Constants.consumeClass(null);
	}
}
