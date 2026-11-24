package pkg;

public class TestKnownShortConstantsParameter {
	public void test1() {
		Constants.consumeShort((short) -1);
	}

	public void test2() {
		Constants.consumeShort((short) 0);
	}

	public void test3() {
		Constants.consumeShort((short) 1);
	}

	public void test4() {
		Constants.consumeShort((short) 2);
	}

	public void test5() {
		Constants.consumeShort((short) 3);
	}

	public void test6() {
		Constants.consumeShort((short) 4);
	}

	public void test7() {
		Constants.consumeShort((short) 5);
	}

	public void test8() {
		Constants.consumeShort((short) 257);
	}
}
