package org.jetbrains.java.decompiler.api.plugin;

import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.util.TextBuffer;

public interface StatementWriter {
  void writeClassHeader(StructClass cl, TextBuffer buffer, ImportCollector importCollector);

  void writeClass(ClassesProcessor.ClassNode node, TextBuffer buffer, int indent);

  void writeField(ClassWrapper wrapper, StructClass cl, StructField fd, TextBuffer buffer, int indent);

  boolean writeMethod(ClassesProcessor.ClassNode node, StructMethod mt, int methodIndex, TextBuffer buffer, int indent);
}
