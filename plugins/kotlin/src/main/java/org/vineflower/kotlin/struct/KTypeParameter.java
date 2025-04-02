package org.vineflower.kotlin.struct;

import kotlin.metadata.internal.metadata.ProtoBuf;
import org.vineflower.kotlin.metadata.MetadataNameResolver;

import java.util.List;
import java.util.stream.Collectors;

public record KTypeParameter(
  boolean reified,
  ProtoBuf.TypeParameter.Variance variance,
  List<KType> upperBounds,
  String name,
  int id
) {
  public static KTypeParameter from(ProtoBuf.TypeParameter proto, MetadataNameResolver resolver) {
    return new KTypeParameter(
      proto.getReified(),
      proto.getVariance(),
      proto.getUpperBoundList().stream().map(type -> KType.from(type, resolver)).collect(Collectors.toList()),
      resolver.resolve(proto.getName()),
      proto.getId()
    );
  }
}
