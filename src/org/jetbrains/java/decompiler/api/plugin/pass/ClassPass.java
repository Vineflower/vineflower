package org.jetbrains.java.decompiler.api.plugin.pass;

import org.jetbrains.java.decompiler.main.ClassesProcessor;

public interface ClassPass {
  boolean run(ClassesProcessor.ClassNode root);
}
