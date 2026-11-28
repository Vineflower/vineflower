package pkg;

class TestConstantUninlining {
  static final int IMin = Integer.MIN_VALUE;
  static final int IMax = Integer.MAX_VALUE;

  static final long LMin = Long.MIN_VALUE;
  static final long LMax = Long.MAX_VALUE;

  static final float FNan = Float.NaN;
  static final float FNeg = Float.NEGATIVE_INFINITY;
  static final float FPos = Float.POSITIVE_INFINITY;
  static final float FMin = Float.MIN_VALUE;
  static final float FMax = Float.MAX_VALUE;

  static final double DNan = Double.NaN;
  static final double DNeg = Double.NEGATIVE_INFINITY;
  static final double DPos = Double.POSITIVE_INFINITY;
  static final double DMin = Double.MIN_VALUE;
  static final double DMax = Double.MAX_VALUE;

  static final float FPI = (float) Math.PI;
  static final float FPIHalf = (float) Math.PI / 2.0f;
  static final float FPITwentieth = (float) Math.PI / 20.0f;
  static final float FPIThreeHalves = (float) Math.PI * 3.0f / 2.0f;
  static final float FPITwoThirds = (float) Math.PI * 2.0f / 3.0f;
  static final float FPITwoNinths = (float) Math.PI * 2.0f / 9.0f;
  static final float FPIOver180 = (float) Math.PI / 180.0f;

  static final float FE = (float) Math.E;
  static final float FENeg = (float) -Math.E;

  static final double DPI = Math.PI;
  static final double DPIHalf = Math.PI / 2.0;
  static final double DPITwentieth = Math.PI / 20.0;
  static final double DPIThreeHalves = Math.PI * 3.0 / 2.0;
  static final double DPITwoThirds = Math.PI * 2.0 / 3.0;
  static final double DPITwoNinths = Math.PI * 2.0 / 9.0;
  static final double DPIOver180 = Math.PI / 180.0;

  static final double DE = Math.E;
  static final double DENeg = -Math.E;
}