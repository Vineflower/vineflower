package pkg;

public class TestKnownByteFlagsParameter {
	public void test1() {
		Constants.consumeByte((byte) 0b0100);
	}

	public void test2() {
		Constants.consumeByte((byte) 0b1100);
	}

	public void test3() {
		Constants.consumeByte((byte) 0b1010);
	}

	public void test4() {
		Constants.consumeByte((byte) 0b0111);
	}
}
