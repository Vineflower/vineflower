package org.vineflower.kotlin.struct;

import org.jetbrains.annotations.Nullable;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kt.metadata.ProtoBuf;

public record KClass(ProtoBuf.Class proto, MetadataNameResolver resolver) implements KElement {
  public int flags() {
    return proto.getFlags();
  }
}
