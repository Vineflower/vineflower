package org.vineflower.kotlin.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.util.Key;
import org.vineflower.kt.metadata.ProtoBuf;
import org.vineflower.kotlin.struct.KConstructor;
import org.vineflower.kotlin.struct.KFunction;
import org.vineflower.kotlin.struct.KProperty;

import java.util.List;
import java.util.Map;

public class StructKotlinMetadataAttribute extends StructGeneralAttribute {
  public sealed interface Metadata {
    @NotNull StructClass classStruct();
  }

  public record Class(@NotNull StructClass classStruct, @NotNull ProtoBuf.Class proto) implements Metadata {}
  public record File(@NotNull StructClass classStruct, @NotNull ProtoBuf.Package proto) implements Metadata {}
  public record SyntheticClass(@NotNull StructClass classStruct, @NotNull ProtoBuf.Function proto) implements Metadata {}
  public record MultifileClass(@NotNull StructClass classStruct, @NotNull ProtoBuf.Package proto) implements Metadata {}

  public final @NotNull Metadata metadata;
  public final @Nullable MetadataNameResolver nameResolver;

  private KProperty.Data propertyData;
  private Map<StructMethod, KFunction> functions;
  private KConstructor.Data constructorData;

  public StructKotlinMetadataAttribute(@NotNull Metadata metadata, @Nullable MetadataNameResolver nameResolver) {
    this.metadata = metadata;
    this.nameResolver = nameResolver;
  }

  public @Nullable KProperty.Data getProperties() {
    if (propertyData == null) {
      if (nameResolver == null) {
        return null;
      }

      List<ProtoBuf.Property> protoProperties;
      if (metadata instanceof SyntheticClass) { // Check first for a quick return
        return null;
      } else if (metadata instanceof Class cls) {
        protoProperties = cls.proto().getPropertyList();
      } else if (metadata instanceof File file) {
        protoProperties = file.proto().getPropertyList();
      } else if (metadata instanceof MultifileClass multifileClass) {
        protoProperties = multifileClass.proto().getPropertyList();
      } else {
        throw new IllegalStateException("Impossible metadata value");
      }
      propertyData = KProperty.parse(metadata.classStruct(), protoProperties, nameResolver);
    }

    return propertyData;
  }

  public @Nullable Map<StructMethod, KFunction> getFunctions() {
    if (functions == null) {
      if (nameResolver == null) {
        return null;
      }

      List<ProtoBuf.Function> protoFunctions;
      if (metadata instanceof Class cls) {
        protoFunctions = cls.proto().getFunctionList();
      } else if (metadata instanceof File file) {
        protoFunctions = file.proto().getFunctionList();
      } else if (metadata instanceof MultifileClass multifileClass) {
        protoFunctions = multifileClass.proto().getFunctionList();
      } else if (metadata instanceof SyntheticClass syntheticClass) {
        protoFunctions = List.of(syntheticClass.proto());
      } else {
        throw new IllegalStateException("Impossible metadata value");
      }
      functions = KFunction.parse(metadata.classStruct(), this, nameResolver, protoFunctions);
    }

    return functions;
  }

  public @Nullable KConstructor.Data getConstructors() {
    if (constructorData == null) {
      if (nameResolver == null || !(metadata instanceof Class cls)) {
        return null;
      }

      constructorData = KConstructor.parse(metadata.classStruct(), cls.proto(), nameResolver);
      return constructorData;
    }

    return constructorData;
  }

  public static final Key<StructKotlinMetadataAttribute> KEY = Key.of("kotlin-metadata-attribute");
}
