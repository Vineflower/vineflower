package pkg;

public class Constants {
	public static final byte BYTE_FLAG_BIT_0 = 1 << 0;
	public static final byte BYTE_FLAG_BIT_1 = 1 << 1;
	public static final byte BYTE_FLAG_BIT_2 = 1 << 2;
	public static final byte BYTE_FLAG_BIT_3 = 1 << 3;
	public static final byte BYTE_FLAG_0 = 0;
	public static final byte BYTE_FLAG_M1 = -1;

	public static final short SHORT_FLAG_BIT_0 = 1 << 0;
	public static final short SHORT_FLAG_BIT_1 = 1 << 1;
	public static final short SHORT_FLAG_BIT_2 = 1 << 2;
	public static final short SHORT_FLAG_BIT_3 = 1 << 3;
	public static final short SHORT_FLAG_0 = 0;
	public static final short SHORT_FLAG_M1 = -1;

	public static final int INT_FLAG_BIT_0 = 1 << 0;
	public static final int INT_FLAG_BIT_1 = 1 << 1;
	public static final int INT_FLAG_BIT_2 = 1 << 2;
	public static final int INT_FLAG_BIT_3 = 1 << 3;
	public static final int INT_FLAG_0 = 0;
	public static final int INT_FLAG_M1 = -1;

	public static final long LONG_FLAG_BIT_0 = 1 << 0;
	public static final long LONG_FLAG_BIT_1 = 1 << 1;
	public static final long LONG_FLAG_BIT_2 = 1 << 2;
	public static final long LONG_FLAG_BIT_3 = 1 << 3;
	public static final long LONG_FLAG_0 = 0;
	public static final long LONG_FLAG_M1 = -1;

	public static final byte BYTE_CONST_M1 = -1;
	public static final byte BYTE_CONST_0 = 0;
	public static final byte BYTE_CONST_1 = 1;
	public static final byte BYTE_CONST_2 = 2;
	public static final byte BYTE_CONST_3 = 3;
	public static final byte BYTE_CONST_4 = 4;
	public static final byte BYTE_CONST_5 = 5;
	public static final byte BYTE_CONST = 117;

	public static final short SHORT_CONST_M1 = -1;
	public static final short SHORT_CONST_0 = 0;
	public static final short SHORT_CONST_1 = 1;
	public static final short SHORT_CONST_2 = 2;
	public static final short SHORT_CONST_3 = 3;
	public static final short SHORT_CONST_4 = 4;
	public static final short SHORT_CONST_5 = 5;
	public static final short SHORT_CONST = 257;

	public static final char CHAR_CONST_0 = '\0';
	public static final char CHAR_CONST_1 = '\1';
	public static final char CHAR_CONST_2 = '\2';
	public static final char CHAR_CONST_3 = '\3';
	public static final char CHAR_CONST_4 = '\4';
	public static final char CHAR_CONST_5 = '\5';
	public static final char CHAR_CONST = '\257';

	public static final int INT_CONST_M1 = -1;
	public static final int INT_CONST_0 = 0;
	public static final int INT_CONST_1 = 1;
	public static final int INT_CONST_2 = 2;
	public static final int INT_CONST_3 = 3;
	public static final int INT_CONST_4 = 4;
	public static final int INT_CONST_5 = 5;
	public static final int INT_CONST = 257;

	public static final long LONG_CONST_0 = 0;
	public static final long LONG_CONST_1 = 1;
	public static final long LONG_CONST = 1234567890;

	public static final float FLOAT_CONST_0 = 0F;
	public static final float FLOAT_CONST_1 = 1F;
	public static final float FLOAT_CONST_2 = 2F;
	public static final float FLOAT_CONST = 5.3F;

	public static final double DOUBLE_CONST_0 = 0D;
	public static final double DOUBLE_CONST_1 = 1D;
	public static final double DOUBLE_CONST = 5.3D;

	public static final String STRING_CONST_FOO = "foo";
	public static final String STRING_CONST_BAR = "bar";
	public static final String STRING_CONST_NULL = null;

	public static final Class<?> CLASS_CONST_STRING = String.class;
	public static final Class<?> CLASS_CONST_INTEGER = Integer.class;
	public static final Class<?> CLASS_CONST_NULL = null;

	public static void consumeByte(byte b) {
	}

	public static void consumeShort(short s) {
	}

	public static void consumeChar(char c) {
	}

	public static void consumeInt(int i) {
	}

	public static void consumeLong(long l) {
	}

	public static void consumeFloat(float f) {
	}

	public static void consumeDouble(double d) {
	}

	public static void consumeBoolean(boolean b) {
	}

	public static void consumeString(String s) {
	}

	public static void consumeClass(Class<?> c) {
	}
}
