package pkg;

public class TestKnownLongConstantsParameter {
	public void test2() {
		Constants.consumeLong(0);
	}

	public void test3() {
		Constants.consumeLong(1);
	}

	public void test8() {
		Constants.consumeLong(1234567890);
	}
}
