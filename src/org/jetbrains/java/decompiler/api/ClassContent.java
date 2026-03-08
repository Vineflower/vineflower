package org.jetbrains.java.decompiler.api;

import java.util.Map;

public interface ClassContent {
  String content();

  Map<Integer, Integer> lineMapping();
}
