package org.vineflower.kotlin;

import kotlinx.metadata.internal.metadata.ProtoBuf;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.util.Key;
import org.vineflower.kotlin.metadata.MetadataNameResolver;

public class KotlinDecompilationContext {
  public enum KotlinType {
    CLASS,
    FILE,
    SYNTHETIC_CLASS,
    MULTIFILE_CLASS,
  }
  
  public static final Key<KotlinType> CURRENT_TYPE = Key.of("KT_CURRENT_TYPE");
  public static final Key<ProtoBuf.Class> CURRENT_CLASS = Key.of("KT_CURRENT_CLASS");
  public static final Key<ProtoBuf.Package> FILE_PACKAGE = Key.of("KT_FILE_PACKAGE");
  public static final Key<ProtoBuf.Function> SYNTHETIC_CLASS = Key.of("KT_SYNTHETIC_CLASS");
  public static final Key<ProtoBuf.Package> MULTIFILE_PACKAGE = Key.of("KT_MULTIFILE_PACKAGE");

  public static final Key<MetadataNameResolver> NAME_RESOLVER = Key.of("KT_NAME_RESOLVER");

  public static ProtoBuf.Class getCurrentClass() {
    return getCurrentType() == KotlinType.CLASS ? DecompilerContext.getContextProperty(CURRENT_CLASS) : null;
  }

  public static ProtoBuf.Package getFilePackage() {
    return getCurrentType() == KotlinType.FILE ? DecompilerContext.getContextProperty(FILE_PACKAGE) : null;
  }

  public static ProtoBuf.Function getSyntheticClass() {
    return getCurrentType() == KotlinType.SYNTHETIC_CLASS ? DecompilerContext.getContextProperty(SYNTHETIC_CLASS) : null;
  }

  public static ProtoBuf.Package getMultifilePackage() {
    return getCurrentType() == KotlinType.MULTIFILE_CLASS ? DecompilerContext.getContextProperty(MULTIFILE_PACKAGE) : null;
  }

  public static KotlinType getCurrentType() {
    return DecompilerContext.getContextProperty(CURRENT_TYPE);
  }

  public static MetadataNameResolver getNameResolver() {
    return DecompilerContext.getContextProperty(NAME_RESOLVER);
  }
}
