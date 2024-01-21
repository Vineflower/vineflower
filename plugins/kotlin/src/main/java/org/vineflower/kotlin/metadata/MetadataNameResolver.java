package org.vineflower.kotlin.metadata;

import kotlinx.metadata.internal.metadata.jvm.JvmProtoBuf;

import java.util.*;

public class MetadataNameResolver {
  private final JvmProtoBuf.StringTableTypes types;
  private final String[] strings;
  private final List<JvmProtoBuf.StringTableTypes.Record> records;
  private static final Map<Integer, String> PREDEFINED = buildPredefined();

  public MetadataNameResolver(JvmProtoBuf.StringTableTypes types, String[] strings) {
    this.types = types;
    this.strings = strings;
    this.records = new ArrayList<>();

    for (JvmProtoBuf.StringTableTypes.Record record : types.getRecordList()) {
      for (int i = 0; i < record.getRange(); i++) {
        records.add(record);
      }
    }
  }

  public String resolve(int idx) {
    JvmProtoBuf.StringTableTypes.Record record = this.records.get(idx);

    String string;
    if (record.hasString()) {
      string = record.getString();
    } else if (record.hasPredefinedIndex() && PREDEFINED.containsKey(record.getPredefinedIndex())) {
      string = PREDEFINED.get(record.getPredefinedIndex());
    } else {
      string = this.strings[idx];
    }

    if (record.getSubstringIndexCount() >= 2) {
      List<Integer> l = record.getReplaceCharList();
      int begin = l.get(0);
      int end = l.get(1);

      if (0 <= begin && begin <= end && end <= string.length()) {
        string = string.substring(begin, end);
      }
    }

    if (record.getReplaceCharCount() >= 2) {
      List<Integer> l = record.getReplaceCharList();
      int from = l.get(0);
      int to = l.get(1);

      if (0 <= from && from <= to && to <= 65535) {
        string = string.replace((char) from, (char) to);
      }
    }

    var operation = record.getOperation() == null ? JvmProtoBuf.StringTableTypes.Record.Operation.NONE : record.getOperation();
    switch (operation) {
      case INTERNAL_TO_CLASS_ID:
        string = string.replace('$', '.');
        break;
      case DESC_TO_CLASS_ID:
        if (string.length() >= 2) {
          string = string.substring(1, string.length() - 1);
        }

        string = string.replace('$', '.');
        break;
    }

    return string;
  }
  
  private static Map<Integer, String> buildPredefined() {
    List<String> strings = List.of(
      "kotlin/Any",
      "kotlin/Nothing",
      "kotlin/Unit",
      "kotlin/Throwable",
      "kotlin/Number",

      "kotlin/Byte", "kotlin/Double", "kotlin/Float", "kotlin/Int",
      "kotlin/Long", "kotlin/Short", "kotlin/Boolean", "kotlin/Char",

      "kotlin/CharSequence",
      "kotlin/String",
      "kotlin/Comparable",
      "kotlin/Enum",

      "kotlin/Array",
      "kotlin/ByteArray", "kotlin/DoubleArray", "kotlin/FloatArray", "kotlin/IntArray",
      "kotlin/LongArray", "kotlin/ShortArray", "kotlin/BooleanArray", "kotlin/CharArray",

      "kotlin/Cloneable",
      "kotlin/Annotation",

      "kotlin/collections/Iterable", "kotlin/collections/MutableIterable",
      "kotlin/collections/Collection", "kotlin/collections/MutableCollection",
      "kotlin/collections/List", "kotlin/collections/MutableList",
      "kotlin/collections/Set", "kotlin/collections/MutableSet",
      "kotlin/collections/Map", "kotlin/collections/MutableMap",
      "kotlin/collections/Map.Entry", "kotlin/collections/MutableMap.MutableEntry",

      "kotlin/collections/Iterator", "kotlin/collections/MutableIterator",
      "kotlin/collections/ListIterator", "kotlin/collections/MutableListIterator"
    );

    Map<Integer, String> res = new HashMap<>();
    for (int i = 0; i < strings.size(); i++) {
      res.put(i, strings.get(i));
    }

    return res;
  }
}
