package org.jetbrains.java.decompiler.code;

import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;

public class MethodProperties {
  public final StructMethod mt;
  public boolean isSyntheticReferenceLambda = false;
  public StructField assertField = null;

  public MethodProperties(StructMethod mt) {
    this.mt = mt;
  }
}
