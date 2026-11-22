package org.vineflower.kotlin.struct;

import org.vineflower.kt.metadata.ProtoBuf;

import java.util.List;

public record KMultifileFacade(List<String> files) implements KElement {
}
