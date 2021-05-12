package pkg;

public class TestSplitColorComponents {
    public byte[] split(int[] colors, byte[] in) {
        for (int i = 0; i < colors.length; i++) {
            in[(i * 4) + 0] = (byte) ((colors[i] >> 24) & 0xFF);
            in[(i * 4) + 1] = (byte) ((colors[i] >> 16) & 0xFF);
            in[(i * 4) + 2] = (byte) ((colors[i] >> 8) & 0xFF);
            in[(i * 4) + 3] = (byte) ((colors[i] >> 0) & 0xFF);
        }

        return in;
    }
}
