package org.vineflower.kotlin.struct;

import kotlinx.metadata.internal.metadata.ProtoBuf;
import org.vineflower.kotlin.metadata.MetadataNameResolver;

import java.util.List;
import java.util.stream.Collectors;

public class KTypeParameter {
  public final boolean reified;
  public final ProtoBuf.TypeParameter.Variance variance;
  public final List<KType> upperBounds;
  public final String name;
  public final int id;

  private KTypeParameter(
    boolean reified,
    ProtoBuf.TypeParameter.Variance variance,
    List<KType> upperBounds,
    String name,
    int id) {
    this.reified = reified;
    this.variance = variance;
    this.upperBounds = upperBounds;
    this.name = name;
    this.id = id;
  }

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
