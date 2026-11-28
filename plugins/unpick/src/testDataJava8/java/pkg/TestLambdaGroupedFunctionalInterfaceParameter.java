package pkg;

public class TestLambdaGroupedFunctionalInterfaceParameter {
	void test() {
		I itf = i -> System.out.println(i == 1);
	}

	@FunctionalInterface
	interface I {
		void run(int i);
	}
}
