/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pkg;

public class TestConstants {
  static final boolean T = true;
  static final boolean F = false;

  static final char C0 = '\n';
  static final char C1 = 'a';
  static final char C2 = 512;

  static final byte BMin = Byte.MIN_VALUE;
  static final byte BMax = Byte.MAX_VALUE;

  static final short SMin = Short.MIN_VALUE;
  static final short SMax = Short.MAX_VALUE;

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
  static final double D10 = 1.0E10;
  static final double DN10 = 1.0E-10;

  static @interface A {
    Class<?> value();
  }

  @A(byte.class) void m1() { }
  @A(char.class) void m2() { }
  @A(double.class) void m3() { }
  @A(float.class) void m4() { }
  @A(int.class) void m5() { }
  @A(long.class) void m6() { }
  @A(short.class) void m7() { }
  @A(boolean.class) void m8() { }
  @A(void.class) void m9() { }
  @A(java.util.Date.class) void m10() { }
}