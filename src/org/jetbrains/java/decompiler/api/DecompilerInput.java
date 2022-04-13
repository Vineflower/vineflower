package org.jetbrains.java.decompiler.api;

import java.util.Set;

public interface DecompilerInput {
  ClassSet<?> getOwnClasses();
  Set<ClassSet<?>> getLibraries();
}
