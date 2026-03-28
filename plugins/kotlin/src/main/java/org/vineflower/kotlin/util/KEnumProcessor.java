package org.vineflower.kotlin.util;

import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.EnumProcessor;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

public class KEnumProcessor {
  private static final String ENUM_ENTRIES = "Lkotlin/enums/EnumEntries;";

  public static void clearEnum(ClassWrapper wrapper) {
    EnumProcessor.clearEnum(wrapper);

    for (StructField fd : wrapper.getClassStruct().getFields()) {
      String descriptor = fd.getDescriptor();
      if (fd.isSynthetic() && descriptor.equals(ENUM_ENTRIES)) {
        wrapper.getHiddenMembers().add(InterpreterUtil.makeUniqueKey(fd.getName(), descriptor));
      }
    }
  }
}
