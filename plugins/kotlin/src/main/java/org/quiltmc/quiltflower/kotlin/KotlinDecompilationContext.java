package org.quiltmc.quiltflower.kotlin;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;

public class KotlinDecompilationContext {
  public enum KotlinType {
    CLASS,
    FILE,
    SYNTHETIC_CLASS,
    MULTIFILE_CLASS,
  }

  static ProtoBuf.Class currentClass = null;
  static ProtoBuf.Package filePackage = null;
  static ProtoBuf.Function syntheticClass = null;
  static ProtoBuf.Package multifilePackage = null;

  static KotlinType currentType = null;

  public static ProtoBuf.Class getCurrentClass() {
    return currentType == KotlinType.CLASS ? currentClass : null;
  }

  public static ProtoBuf.Package getFilePackage() {
    return currentType == KotlinType.FILE ? filePackage : null;
  }

  public static ProtoBuf.Function getSyntheticClass() {
    return currentType == KotlinType.SYNTHETIC_CLASS ? syntheticClass : null;
  }

  public static ProtoBuf.Package getMultifilePackage() {
    return currentType == KotlinType.MULTIFILE_CLASS ? multifilePackage : null;
  }
}
