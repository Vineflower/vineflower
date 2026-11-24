package pkg;

public class TestKnownByteConstantsParameter {
	public void test1() {
		Constants.consumeByte((byte) -1);
	}

	public void test2() {
		Constants.consumeByte((byte) 0);
	}

	public void test3() {
		Constants.consumeByte((byte) 1);
	}

	public void test4() {
		Constants.consumeByte((byte) 2);
	}

	public void test5() {
		Constants.consumeByte((byte) 3);
	}

	public void test6() {
		Constants.consumeByte((byte) 4);
	}

	public void test7() {
		Constants.consumeByte((byte) 5);
	}

	public void test8() {
		Constants.consumeByte((byte) 117);
	}
}
