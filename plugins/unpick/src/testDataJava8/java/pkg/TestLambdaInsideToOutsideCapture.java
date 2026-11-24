package pkg;

import java.util.function.IntConsumer;

public class TestLambdaInsideToOutsideCapture {
	private static void test() {
		int n = 1;
		IntConsumer ic = i -> Constants.consumeInt(n);
	}
}
