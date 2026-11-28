package pkg;

public class TestZeroFlags {
	public void testByte() {
		Constants.consumeByte((byte) 0);
	}

	public void testShort() {
		Constants.consumeShort((short) 0);
	}

	public void testInt() {
		Constants.consumeInt(0);
	}

	public void testLong() {
		Constants.consumeLong(0);
	}
}
