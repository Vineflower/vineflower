package pkg;

public class TestTargetAnnotations {
	@TestAnnotation
	int field;

	boolean testField() {
		return field == 1;
	}

	@TestAnnotation
	int testReturn() {
		return 1;
	}

	boolean testParam(@TestAnnotation int i) {
		return i == 1;
	}

	boolean testReturnRef() {
		return testReturn() == 1;
	}

	void testParamRef() {
		testParam(1);
	}
}
