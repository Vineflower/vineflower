package pkg;

public class TestUnknownConstants {
	public void testByte() {
		Constants.consumeByte((byte) 42);
	}

	public void testShort() {
		Constants.consumeShort((short) 42);
	}

	public void testChar() {
		Constants.consumeChar('ඞ');
	}

	public void testInt() {
		Constants.consumeInt(42);
	}

	public void testLong() {
		Constants.consumeLong(42);
	}

	public void testFloat() {
		Constants.consumeFloat(42);
	}

	public void testDouble() {
		Constants.consumeDouble(42);
	}

	public void testString() {
		Constants.consumeString("baz");
	}
}
