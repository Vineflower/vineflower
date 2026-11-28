package pkg;

public class TestNegatedIntFlagsParameter {
	public void test1() {
		Constants.consumeInt(~0b0100);
	}

	public void test2() {
		Constants.consumeInt(~0b1100);
	}

	public void test3() {
		Constants.consumeInt(~0b1010);
	}

	public void test4() {
		Constants.consumeInt(~0b0111);
	}
}
