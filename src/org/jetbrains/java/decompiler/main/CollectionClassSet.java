package org.jetbrains.java.decompiler.main;

import org.jetbrains.java.decompiler.api.ClassSet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CollectionClassSet implements ClassSet<ClassSet.ClassSupplier> {
  public final Map<String, ClassSupplier> classes = new LinkedHashMap<>();

  @Override
  public Map<String, ClassSupplier> getClassNames() throws IOException {
    return classes;
  }

  @Override
  public byte[] getClass(ClassSupplier supplier) throws IOException {
    return supplier.get();
  }
}
