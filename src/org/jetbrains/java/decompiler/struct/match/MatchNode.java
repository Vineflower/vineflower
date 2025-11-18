// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.match;

import org.jetbrains.java.decompiler.struct.match.IMatchable.MatchProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class MatchNode {
  public static class RuleValue {
    public final int parameter;
    public final Object value;

    public RuleValue(int parameter, Object value) {
      this.parameter = parameter;
      this.value = value;
    }

    public boolean isVariable() {
      String strValue = value.toString();
      return (strValue.charAt(0) == '$' && strValue.charAt(strValue.length() - 1) == '$');
    }

    public String toString() {
      return value.toString();
    }
  }

  public static final int MATCHNODE_STATEMENT = 0;
  public static final int MATCHNODE_EXPRENT = 1;

  private final int type;
  private final Map<MatchProperties, List<RuleValue>> rules = new HashMap<>();
  private final List<MatchNode> children = new ArrayList<>();

  public MatchNode(int type) {
    this.type = type;
  }

  public void addChild(MatchNode child) {
    children.add(child);
  }

  public void addRule(MatchProperties property, RuleValue value) {
    rules.computeIfAbsent(property, k -> new ArrayList<>()).add(value);
  }

  public int getType() {
    return type;
  }

  public List<MatchNode> getChildren() {
    return children;
  }

  public boolean iterateRules(BiFunction<MatchProperties, RuleValue, Boolean> consumer) {
    // Make sure that the iterator succeeds for every rule in the map
    for (Map.Entry<MatchProperties, List<RuleValue>> e : rules.entrySet()) {
      for (RuleValue rule : e.getValue()) {
        if (!consumer.apply(e.getKey(), rule)) {
          return false;
        }
      }
    }

    return true;
  }

  public RuleValue getRawRule(MatchProperties property) {
    List<RuleValue> list = rules.get(property);
    return list != null ? list.get(0) : null;
  }

  public Object getRuleValue(MatchProperties property) {
    List<RuleValue> rule = rules.get(property);
    return rule == null ? null : rule.get(0).value;
  }
}