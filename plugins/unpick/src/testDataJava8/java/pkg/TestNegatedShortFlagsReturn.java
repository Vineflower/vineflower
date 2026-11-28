package pkg;

public class TestNegatedShortFlagsReturn {
	public short test1() {
		return ~0b0100;
	}

	public short test2() {
		return ~0b1100;
	}

	public short test3() {
		return ~0b1010;
	}

	public short test4() {
		return ~0b0111;
	}
}
