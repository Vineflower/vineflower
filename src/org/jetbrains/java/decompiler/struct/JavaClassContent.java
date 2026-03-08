package org.jetbrains.java.decompiler.struct;

import org.jetbrains.java.decompiler.api.ClassContent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JavaClassContent implements ClassContent {
  public String content;
  public final Map<Integer, Integer> lineMapping = new LinkedHashMap<>();

  @Override
  public String content() {
    return content;
  }

  @Override
  public Map<Integer, Integer> lineMapping() {
    return lineMapping;
  }
}
