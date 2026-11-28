package pkg;

public class TestNegatedIntFlagsReturn {
	public int test1() {
		return ~0b0100;
	}

	public int test2() {
		return ~0b1100;
	}

	public int test3() {
		return ~0b1010;
	}

	public int test4() {
		return ~0b0111;
	}
}
