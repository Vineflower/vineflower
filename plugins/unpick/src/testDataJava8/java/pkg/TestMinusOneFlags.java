package pkg;

public class TestMinusOneFlags {
	public void testByte() {
		Constants.consumeByte((byte) -1);
	}

	public void testShort() {
		Constants.consumeShort((short) -1);
	}

	public void testInt() {
		Constants.consumeInt(-1);
	}

	public void testLong() {
		Constants.consumeLong(-1);
	}
}
