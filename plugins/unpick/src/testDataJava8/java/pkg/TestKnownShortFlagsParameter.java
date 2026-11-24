package pkg;

public class TestKnownShortFlagsParameter {
	public void test1() {
		Constants.consumeShort((short) 0b0100);
	}

	public void test2() {
		Constants.consumeShort((short) 0b1100);
	}

	public void test3() {
		Constants.consumeShort((short) 0b1010);
	}

	public void test4() {
		Constants.consumeShort((short) 0b0111);
	}
}
