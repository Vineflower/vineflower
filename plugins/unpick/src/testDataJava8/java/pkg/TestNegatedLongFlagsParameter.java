package pkg;

public class TestNegatedLongFlagsParameter {
	public void test1() {
		Constants.consumeLong(~0b0100L);
	}

	public void test2() {
		Constants.consumeLong(~0b1100L);
	}

	public void test3() {
		Constants.consumeLong(~0b1010L);
	}

	public void test4() {
		Constants.consumeLong(~0b0111L);
	}
}
