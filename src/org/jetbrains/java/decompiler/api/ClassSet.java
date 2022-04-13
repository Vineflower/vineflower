package org.jetbrains.java.decompiler.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.jar.Manifest;

public interface ClassSet<T> {
  Map<String, T> getClassNames() throws IOException;
  byte[] getClass(T key) throws IOException;
  default Map<String, T> getOtherFiles() throws IOException {
    return Collections.emptyMap();
  }
  default byte[] getOtherFile(T key) throws IOException {
    return getClass(key);
  }
  default Manifest getManifest() throws IOException {
    return null;
  }

  default void forEach(BiConsumer<String, ClassSupplier> callback) throws IOException {
    for (Map.Entry<String, T> e : getClassNames().entrySet()) {
      try {
        callback.accept(e.getKey(), () -> getClass(e.getValue()));
      } catch (UncheckedIOException ex) {
        throw ex.getCause();
      }
    }
  }

  interface ClassSupplier {
    byte[] get() throws IOException;
  }
}
