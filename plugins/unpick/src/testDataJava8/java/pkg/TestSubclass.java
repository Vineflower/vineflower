package pkg;

public class TestSubclass {
	void test(int x) {
	}

	static class Subclass extends TestSubclass {
		@Override
		void test(int x) {
			boolean b = x == 257;
		}
	}
}
