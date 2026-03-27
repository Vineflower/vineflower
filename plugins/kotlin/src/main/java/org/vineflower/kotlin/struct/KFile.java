package org.vineflower.kotlin.struct;

import org.jetbrains.annotations.Nullable;
import org.vineflower.kotlin.metadata.MetadataNameResolver;
import org.vineflower.kt.metadata.ProtoBuf;

public record KFile(ProtoBuf.Package proto, @Nullable MetadataNameResolver resolver, @Nullable String multifileName) implements KElement {
}
