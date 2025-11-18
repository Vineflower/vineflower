package org.jetbrains.java.decompiler.api;

import org.jetbrains.java.decompiler.util.Key;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ClassAttributeRegistry {
  private static final Map<Key<? extends StructGeneralAttribute>, Supplier<? extends StructGeneralAttribute>> REGISTRY = new HashMap<>();

  public static <T extends StructGeneralAttribute> void register(Key<T> key, Supplier<T> supplier) {
    REGISTRY.put(key, supplier);
  }

  public static <T extends StructGeneralAttribute> T get(Key<T> key) {
    return (T) REGISTRY.get(key).get();
  }

  public static Map<Key<? extends StructGeneralAttribute>, Supplier<? extends StructGeneralAttribute>> getRegistry() {
    return REGISTRY;
  }
}
