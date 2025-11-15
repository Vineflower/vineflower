package org.vineflower.kotlin.metadata;

import kotlin.metadata.internal.metadata.ProtoBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.util.Key;

public class StructKotlinMetadataAttribute extends StructGeneralAttribute {
  public sealed interface Metadata {}

  public record Class(@NotNull ProtoBuf.Class proto) implements Metadata {}
  public record File(@NotNull ProtoBuf.Package proto) implements Metadata {}
  public record SyntheticClass(@NotNull ProtoBuf.Function proto) implements Metadata {}
  public record MultifileClass(@NotNull ProtoBuf.Package proto) implements Metadata {}

  public final @NotNull Metadata metadata;
  public final @Nullable MetadataNameResolver nameResolver;

  public StructKotlinMetadataAttribute(@NotNull Metadata metadata, @Nullable MetadataNameResolver nameResolver) {
    this.metadata = metadata;
    this.nameResolver = nameResolver;
  }

  public static final Key<StructKotlinMetadataAttribute> KEY = Key.of("kotlin-metadata-attribute");
}
