package org.jetbrains.java.decompiler.api;

import org.jetbrains.java.decompiler.struct.StructClass;

public interface LanguageChooser {
  boolean isLanguage(StructClass cl);
}
