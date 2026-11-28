package pkg;

public class TestKnownLongFlagsParameter {
	public void test1() {
		Constants.consumeLong(0b0100);
	}

	public void test2() {
		Constants.consumeLong(0b1100);
	}

	public void test3() {
		Constants.consumeLong(0b1010);
	}

	public void test4() {
		Constants.consumeLong(0b0111);
	}
}
