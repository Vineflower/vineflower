package org.vineflower.kotlin.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.util.Key;
import org.vineflower.kt.metadata.ProtoBuf;

public class StructKotlinMetadataAttribute extends StructGeneralAttribute {
  public sealed interface Metadata {}

  public record Class(@NotNull StructClass classStruct, @NotNull ProtoBuf.Class proto) implements Metadata {}
  public record File(@NotNull StructClass classStruct, @NotNull ProtoBuf.Package proto) implements Metadata {}
  public record SyntheticClass(@NotNull StructClass classStruct, @NotNull ProtoBuf.Function proto) implements Metadata {}
  public record MultifileClass(@NotNull StructClass classStruct, @NotNull ProtoBuf.Package proto) implements Metadata {}

  public final @NotNull Metadata metadata;
  public final @Nullable MetadataNameResolver nameResolver;

  public StructKotlinMetadataAttribute(@NotNull Metadata metadata, @Nullable MetadataNameResolver nameResolver) {
    this.metadata = metadata;
    this.nameResolver = nameResolver;
  }

  public static final Key<StructKotlinMetadataAttribute> KEY = Key.of("kotlin-metadata-attribute");
}
