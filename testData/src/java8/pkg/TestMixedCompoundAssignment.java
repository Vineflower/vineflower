package pkg;

public class TestMixedCompoundAssignment {
    public int testSimpleIntFloat(int i, float j) {
        i += j;
        i -= j;
        i *= j;
        i /= j;
        
        return i;
    }

  public int testSimpleIntLong(int i, long j) {
    i += j;
    i -= j;
    i *= j;
    i /= j;
    i &= j;
    i |= j;
    i ^= j;
    i >>= j;
    i <<= j;
    i >>>= j;

    return i;
  }

  public double testSimpleDoubleLong(double i, long j) {
    i += j;
    i -= j;
    i *= j;
    i /= j;

    return i;
  }

  public int testNestedIntLongDouble(int i, long j, double k) {
    i += j += k;
    i -= j -= k;
    i *= j *= k;
    i /= j /= k;


    return i;
  }

  public long testNestedLongIntLong(long i, int j, long k) {
    i += j += k;
    i -= j -= k;
    i *= j *= k;
    i /= j /= k;
    i &= j &= k;
    i |= j |= k;
    i ^= j ^= k;
    i >>= j >>= k;
    i <<= j <<= k;
    i >>>= j >>>= k;

    return i;
  }


  public void testArrayIntDouble(int[] holder, int i, double inc) {
    for (int j = 0; j < i; j++) {
      holder[j] += inc;
    }
  }

  public void testArrayIntLong(int[] holder, int i, long inc) {
    for (int j = 0; j < i; j++) {
      holder[j] += inc;
    }
  }

  public void testArrayDoubleInt(double[] holder, int i, int inc) {
    for (int j = 0; j < i; j++) {
      holder[j] += inc;
    }
  }

  public void testNestedArrayByteFloatLongDouble(byte[] outer, float[] holder, long[] inner, int i, double inc) {
    for (int j = 0; j < i; j++) {
      outer[i + ~j] += holder[j] -= inner[j * 3 % i] += inc;
    }
  }
}
