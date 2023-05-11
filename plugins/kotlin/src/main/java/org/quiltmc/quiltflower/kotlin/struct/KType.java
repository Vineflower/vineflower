package org.quiltmc.quiltflower.kotlin.struct;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.struct.gen.VarType;

public class KType {
  public final VarType type;

  @Nullable
  public final String kotlinType;

  public final boolean isNullable;

  public KType(VarType type, @Nullable String kotlinType, boolean isNullable) {
    this.type = type;
    this.kotlinType = kotlinType;
    this.isNullable = isNullable;
  }
}
