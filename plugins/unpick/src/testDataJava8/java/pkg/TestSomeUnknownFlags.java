package pkg;

public class TestSomeUnknownFlags {
	public void testByte() {
		Constants.consumeByte((byte) 0b10001);
	}

	public void testShort() {
		Constants.consumeShort((short) 0b10001);
	}

	public void testInt() {
		Constants.consumeInt(0b10001);
	}

	public void testLong() {
		Constants.consumeLong(0b10001);
	}
}
