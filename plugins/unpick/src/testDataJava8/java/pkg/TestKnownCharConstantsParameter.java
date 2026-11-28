package pkg;

public class TestKnownCharConstantsParameter {
	public void test1() {
		Constants.consumeChar('\0');
	}

	public void test2() {
		Constants.consumeChar('\1');
	}

	public void test3() {
		Constants.consumeChar('\2');
	}

	public void test4() {
		Constants.consumeChar('\3');
	}

	public void test5() {
		Constants.consumeChar('\4');
	}

	public void test6() {
		Constants.consumeChar('\5');
	}

	public void test7() {
		Constants.consumeChar('\257');
	}
}
