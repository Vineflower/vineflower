package pkg;

public class TestLambdaGroupedFunctionalInterfaceReturn {
	void test() {
		I itf = () -> 1;
	}

	@FunctionalInterface
	interface I {
		int run();
	}
}
