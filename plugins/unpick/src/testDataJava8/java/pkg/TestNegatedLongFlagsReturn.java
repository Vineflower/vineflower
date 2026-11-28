package pkg;

public class TestNegatedLongFlagsReturn {
	public long test1() {
		return ~0b0100L;
	}

	public long test2() {
		return ~0b1100L;
	}

	public long test3() {
		return ~0b1010L;
	}

	public long test4() {
		return ~0b0111L;
	}
}
