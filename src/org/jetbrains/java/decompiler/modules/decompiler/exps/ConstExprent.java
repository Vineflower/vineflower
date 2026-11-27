// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.TypeFamily;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericType;
import org.jetbrains.java.decompiler.struct.match.MatchEngine;
import org.jetbrains.java.decompiler.struct.match.MatchNode;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.TextBuffer;
import org.jetbrains.java.decompiler.util.TextUtil;

import java.util.*;

public class ConstExprent extends Exprent {
  private static final Map<Integer, String> CHAR_ESCAPES = new HashMap<>();

  static {
    CHAR_ESCAPES.put(0x8, "\\b");   /* \u0008: backspace BS */
    CHAR_ESCAPES.put(0x9, "\\t");   /* \u0009: horizontal tab HT */
    CHAR_ESCAPES.put(0xA, "\\n");   /* \u000a: linefeed LF */
    CHAR_ESCAPES.put(0xC, "\\f");   /* \u000c: form feed FF */
    CHAR_ESCAPES.put(0xD, "\\r");   /* \u000d: carriage return CR */
    //CHAR_ESCAPES.put(0x22, "\\\""); /* \u0022: double quote " */
    CHAR_ESCAPES.put(0x27, "\\'"); /* \u0027: single quote ' */
    CHAR_ESCAPES.put(0x5C, "\\\\"); /* \u005c: backslash \ */
  }

  private VarType constType;
  private final Object value;
  private final boolean boolPermitted;
  private boolean wasCondy = false;
  private int radix = 10;
  private boolean hasSetRadix = false;
  private boolean constTypeForced = false;

  public ConstExprent(int val, boolean boolPermitted, BitSet bytecodeOffsets) {
    this(guessType(val, boolPermitted), val, boolPermitted, bytecodeOffsets);
  }

  public ConstExprent(VarType constType, Object value, BitSet bytecodeOffsets) {
    this(constType, value, false, bytecodeOffsets);
  }

  public ConstExprent(VarType constType, Object value, BitSet bytecodeOffsets, boolean wasCondy, int radix, boolean hasSetRadix, boolean constTypeForced) {
    this(constType, value, false, bytecodeOffsets);
    this.wasCondy = wasCondy;
    this.radix = radix;
    this.hasSetRadix = hasSetRadix;
    this.constTypeForced = constTypeForced;
  }

  protected ConstExprent(VarType constType, Object value, boolean boolPermitted, BitSet bytecodeOffsets) {
    super(Type.CONST);
    this.constType = constType;
    this.value = value;
    this.boolPermitted = boolPermitted;
    addBytecodeOffsets(bytecodeOffsets);
    ValidationHelper.assertTrue(constType != VarType.VARTYPE_VOID, "Must not make const type with type void");

    if (constType.equals(VarType.VARTYPE_CLASS) && value != null) {
      String stringVal = value.toString();
      List<VarType> args = Collections.singletonList(new VarType(stringVal, !stringVal.startsWith("[")));
      this.constType = new GenericType(constType.type, constType.arrayDim, constType.value, null, args, GenericType.WILDCARD_NO);
    }
  }

  private static VarType guessType(int val, boolean boolPermitted) {
    if (boolPermitted && (val == 0 || val == 1)) {
      return VarType.VARTYPE_BOOLEAN;
    }
    else if (0 <= val && val <= 127) {
      return VarType.VARTYPE_BYTECHAR;
    }
    else if (-128 <= val && val <= 127) {
      return VarType.VARTYPE_BYTE;
    }
    else if (0 <= val && val <= 32767) {
      return VarType.VARTYPE_SHORTCHAR;
    }
    else if (-32768 <= val && val <= 32767) {
      return VarType.VARTYPE_SHORT;
    }
    else if (0 <= val && val <= 0xFFFF) {
      return VarType.VARTYPE_CHAR;
    }
    else {
      return VarType.VARTYPE_INT;
    }
  }

  private static double gcd(double a, double b) {
    return b == 0 ? a : gcd(b, a%b);
  }

  @Override
  public Exprent copy() {
    return new ConstExprent(constType, value, bytecode, wasCondy, radix, hasSetRadix, constTypeForced);
  }

  @Override
  public VarType getExprType() {
    return constType;
  }

  @Override
  public int getExprentUse() {
    return Exprent.MULTIPLE_USES | Exprent.SIDE_EFFECTS_FREE;
  }

  @Override
  public List<Exprent> getAllExprents(List<Exprent> list) {
    return list;
  }

  @Override
  public TextBuffer toJava(int indent) {
    boolean ascii = DecompilerContext.getOption(IFernflowerPreferences.ASCII_STRING_CHARACTERS);

    TextBuffer buf = new TextBuffer();
    buf.addBytecodeMapping(bytecode);

    if (wasCondy) {
      buf.append("/* $VF: constant dynamic */ ");
    }

    if (constType.type != CodeType.NULL && value == null) {
      return buf.appendCastTypeName(constType);
    }

    VarType unboxed = VarType.UNBOXING_TYPES.getOrDefault(constType, constType);

    switch (unboxed.type) {
      case BOOLEAN:
        return buf.append(Boolean.toString((Integer)value != 0));

      case CHAR:
        Integer val = (Integer)value;
        String ret = CHAR_ESCAPES.get(val);
        if (ret == null) {
          char c = (char)val.intValue();
          if (isPrintableAscii(c) || !ascii && TextUtil.isPrintableUnicode(c)) {
            ret = String.valueOf(c);
          }
          else {
            ret = TextUtil.charToUnicodeLiteral(c);
          }
        }
        return buf.append(ret).enclose("'", "'");

      case BYTECHAR:
      case SHORTCHAR:
        // TODO: assert here, we should not be writing higher order types
      case BYTE:
      case SHORT:
      case INT:
        int intVal = (Integer)value;

        switch (radix) {
          case 2 -> buf.append("0b").append(Integer.toUnsignedString(intVal, 2));
          case 8 -> buf.append("0").append(Integer.toUnsignedString(intVal, 8));
          case 16 -> buf.append("0x").append(Integer.toUnsignedString(intVal, 16).toUpperCase(Locale.ROOT));
          default -> buf.append(Integer.toString(intVal));
        }
        return buf;

      case LONG:
        long longVal = (Long)value;

        switch (radix) {
          case 2 -> buf.append("0b").append(Long.toUnsignedString(longVal, 2));
          case 8 -> buf.append("0").append(Long.toUnsignedString(longVal, 8));
          case 16 -> buf.append("0x").append(Long.toUnsignedString(longVal, 16).toUpperCase(Locale.ROOT));
          default -> buf.append(Long.toString(longVal));
        }
        return buf.append('L');

      case FLOAT:
        float floatVal = (Float)value;
        // Check for special values that can't be used directly in code
        // (and we can't replace with the constant due to the user requesting not to)
        if (Float.isNaN(floatVal)) {
          return buf.append("0.0F / 0.0F");
        }
        else if (floatVal == Float.POSITIVE_INFINITY) {
          return buf.append("1.0F / 0.0F");
        }
        else if (floatVal == Float.NEGATIVE_INFINITY) {
          return buf.append("-1.0F / 0.0F");
        }

        if (radix == 16) {
          return buf.append(Float.toHexString(floatVal).toUpperCase(Locale.ROOT).replace('P', 'p')).append('F');
        } else {
          return buf.append(trimFloat(Float.toString(floatVal), floatVal)).append('F');
        }

      case DOUBLE:
        double doubleVal = (Double)value;

        if (Double.isNaN(doubleVal)) {
          return buf.append("0.0 / 0.0");
        }
        else if (doubleVal == Double.POSITIVE_INFINITY) {
          return buf.append("1.0 / 0.0");
        }
        else if (doubleVal == Double.NEGATIVE_INFINITY) {
          return buf.append("-1.0 / 0.0");
        }

        float floatRepresentation = (float) doubleVal;
        if (radix == 16) {
          if (floatRepresentation == doubleVal && Float.toHexString(floatRepresentation).length() < Double.toHexString(doubleVal).length()) {
            buf.append(Float.toHexString(floatRepresentation).toUpperCase(Locale.ROOT).replace('P', 'p')).append('F');
          } else {
            buf.append(Double.toHexString(doubleVal).toUpperCase(Locale.ROOT).replace('P', 'p'));
          }
        } else {
          // Try to convert the double representation of the value to the float representation, to output the cleanest version of the value.
          // This patch is based on work in ForgeFlower submitted by Pokechu22.
          if (floatRepresentation == doubleVal && trimFloat(Float.toString(floatRepresentation), floatRepresentation).length() < trimDouble(Double.toString(doubleVal), doubleVal).length()) {
            return buf.append(trimFloat(Float.toString(floatRepresentation), floatRepresentation)).append("F");
          } else {
            return buf.append(trimDouble(Double.toString(doubleVal), doubleVal));
          }
        }

      case NULL:
        return buf.append("null");

      case OBJECT:
        if (constType.equals(VarType.VARTYPE_STRING)) {
          return buf.append(convertStringToJava(value.toString(), ascii)).enclose("\"", "\"");
        } else if (constType.equals(VarType.VARTYPE_CLASS)) {
          String stringVal = value.toString();
          VarType type = new VarType(stringVal, !stringVal.startsWith("["));
          return buf.appendCastTypeName(type).append(".class");
        } else if (constType.equals(VarType.VARTYPE_METHODTYPE)) {
          if (DecompilerContext.getOption(IFernflowerPreferences.DECOMPILER_COMMENTS)) {
            buf.append("/* $VF: MethodType constant */ ");
          }
          DecompilerContext.getImportCollector().getShortName(constType.value.replaceAll("/", "."));
          buf.append("MethodType.fromMethodDescriptorString(\"" + value + "\", null)");
          return buf;
        }
    }

    // prevent gc without discarding
    buf.convertToStringAndAllowDataDiscard();
    throw new RuntimeException("invalid constant type: " + constType + " with value " + value);
  }

  // Different JVM implementations/version display Floats and Doubles with different number of trailing zeros.
  // This trims them all down to only the necessary amount.
  private static String trimFloat(String value, float start) {
    // Includes NaN and simple numbers
    if (value.length() <= 3) {
      return value;
    }

    String exp = "";
    int eIdx = value.indexOf('E');
    if (eIdx != -1) {
      exp = value.substring(eIdx);
      value = value.substring(0, eIdx);
    }

    // Cut off digits that don't affect the value
    String temp = value;
    int dotIdx = value.indexOf('.');
    do {
      value = temp;
      temp = value.substring(0, value.length() - 1);
    } while (!temp.isEmpty() && !"-".equals(temp) && Float.parseFloat(temp + exp) == start);

    if (dotIdx != -1 && value.indexOf('.') == -1) {
      value += ".0";
    } else if (dotIdx != -1) {
      String integer = value.substring(0, dotIdx);
      String decimal = value.substring(dotIdx + 1);

      String rounded = (Integer.parseInt(integer) + 1) + ".0" + exp;
      if (Float.parseFloat(rounded) == start)
        return rounded;

      long decimalVal = 1;
      for (int i = 0; i < decimal.length() - 1; i++) {
        decimalVal = (decimalVal - 1) * 10 + decimal.charAt(i) - '0' + 1;
        rounded = integer + '.' + decimalVal + exp;
        if (Float.parseFloat(rounded) == start)
          return rounded;
      }
    }

    return value + exp;
  }

  private static String trimDouble(String value, double start) {
    // Includes NaN and simple numbers
    if (value.length() <= 3) {
      return value;
    }

    String exp = "";
    int eIdx = value.indexOf('E');
    if (eIdx != -1) {
      exp = value.substring(eIdx);
      value = value.substring(0, eIdx);
    }

    // Cut off digits that don't affect the value
    String temp = value;
    int dotIdx = value.indexOf('.');
    do {
      value = temp;
      temp = value.substring(0, value.length() - 1);
    } while (!temp.isEmpty() && !"-".equals(temp) && Double.parseDouble(temp) == start);

    if (dotIdx != -1 && value.indexOf('.') == -1) {
      value += ".0";
    } else if (dotIdx != -1) {
      String integer = value.substring(0, dotIdx);
      String decimal = value.substring(dotIdx + 1);

      String rounded = (Long.parseLong(integer) + 1) + ".0" + exp;
      if (Double.parseDouble(rounded) == start)
        return rounded;

      long decimalVal = 1;
      for (int i = 0; i < decimal.length() - 1; i++) {
        decimalVal = (decimalVal - 1) * 10 + decimal.charAt(i) - '0' + 1;
        rounded = integer + '.' + decimalVal + exp;
        if (Double.parseDouble(rounded) == start)
          return rounded;
      }
    }

    return value + exp;
  }

  public boolean isNull() {
    return CodeType.NULL == constType.type;
  }

  public static String convertStringToJava(String value, boolean ascii) {
    char[] arr = value.toCharArray();
    StringBuilder buffer = new StringBuilder(arr.length);

    for (char c : arr) {
      switch (c) {
        case '\\': //  u005c: backslash \
          buffer.append("\\\\");
          break;
        case 0x8: // "\\\\b");  //  u0008: backspace BS
          buffer.append("\\b");
          break;
        case 0x9: //"\\\\t");  //  u0009: horizontal tab HT
          buffer.append("\\t");
          break;
        case 0xA: //"\\\\n");  //  u000a: linefeed LF
          buffer.append("\\n");
          break;
        case 0xC: //"\\\\f");  //  u000c: form feed FF
          buffer.append("\\f");
          break;
        case 0xD: //"\\\\r");  //  u000d: carriage return CR
          buffer.append("\\r");
          break;
        case 0x22: //"\\\\\""); // u0022: double quote "
          buffer.append("\\\"");
          break;
        //case 0x27: //"\\\\'");  // u0027: single quote '
        //  buffer.append("\\\'");
        //  break;
        default:
          if (isPrintableAscii(c) || !ascii && TextUtil.isPrintableUnicode(c)) {
            buffer.append(c);
          }
          else {
            buffer.append(TextUtil.charToUnicodeLiteral(c));
          }
      }
    }

    return buffer.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof ConstExprent)) return false;

    ConstExprent cn = (ConstExprent)o;
    return InterpreterUtil.equalObjects(constType, cn.getConstType()) &&
           InterpreterUtil.equalObjects(value, cn.getValue()) &&
           radix == cn.radix;
  }

  @Override
  public int hashCode() {
    int result = constType != null ? constType.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }

  public boolean hasBooleanValue() {
    switch (constType.type) {
      case BOOLEAN:
      case CHAR:
      case BYTE:
      case BYTECHAR:
      case SHORT:
      case SHORTCHAR:
      case INT:
        int value = (Integer)this.value;
        return value == 0 || (DecompilerContext.getOption(IFernflowerPreferences.BOOLEAN_TRUE_ONE) && value == 1);
    }

    return false;
  }

  public boolean hasValueOne() {
    switch (constType.type) {
      case BOOLEAN:
      case CHAR:
      case BYTE:
      case BYTECHAR:
      case SHORT:
      case SHORTCHAR:
      case INT:
        return ((Number)value).intValue() == 1;
      case LONG:
        return (Long) value == 1;
      case DOUBLE:
        return (Double) value == 1.0;
      case FLOAT:
        return (Float) value == 1.F;
    }

    return false;
  }

  public static ConstExprent getZeroConstant(CodeType type) {
    switch (type) {
      case INT:
        return new ConstExprent(VarType.VARTYPE_INT, 0, null);
      case LONG:
        return new ConstExprent(VarType.VARTYPE_LONG, 0L, null);
      case DOUBLE:
        return new ConstExprent(VarType.VARTYPE_DOUBLE, 0d, null);
      case FLOAT:
        return new ConstExprent(VarType.VARTYPE_FLOAT, 0f, null);
    }

    throw new RuntimeException("Invalid argument: " + type);
  }

  public VarType getConstType() {
    return constType;
  }

  public void setConstType(VarType constType) {
    this.constType = constType;
  }

  public void adjustConstType(VarType expectedType) {
    if (constTypeForced) {
      // we are already confident on the const type from some other source
      return;
    }

    // BYTECHAR and SHORTCHAR => CHAR in the CHAR context
    if ((expectedType.equals(VarType.VARTYPE_CHAR) || expectedType.equals(VarType.VARTYPE_CHARACTER)) &&
            (constType.equals(VarType.VARTYPE_BYTECHAR) || constType.equals(VarType.VARTYPE_SHORTCHAR))) {
      int intValue = getIntValue();
      if (isPrintableAscii(intValue) || CHAR_ESCAPES.containsKey(intValue)) {
        setConstType(VarType.VARTYPE_CHAR);
      }
    }
    // BYTE, BYTECHAR, SHORTCHAR, SHORT, CHAR => INT in the INT context
    else if ((expectedType.equals(VarType.VARTYPE_INT) || expectedType.equals(VarType.VARTYPE_INTEGER)) &&
            constType.typeFamily == TypeFamily.INTEGER) {
      setConstType(VarType.VARTYPE_INT);
    }
  }

  private static boolean isPrintableAscii(int c) {
    return c >= 32 && c < 127;
  }

  public Object getValue() {
    return value;
  }

  public int getIntValue() {
    return (Integer)value;
  }

  public boolean isBoolPermitted() {
    return boolPermitted;
  }

  @Override
  public void getBytecodeRange(BitSet values) {
    measureBytecode(values);
  }

  public ConstExprent setWasCondy(boolean wasCondy) {
    this.wasCondy = wasCondy;
    return this;
  }

  public int getRadix() {
    return radix;
  }

  public void setRadix(int radix) {
    this.radix = radix;
    this.hasSetRadix = true;
  }

  public boolean hasSetRadix() {
    return hasSetRadix;
  }

  public boolean isConstTypeForced() {
    return constTypeForced;
  }

  public void setConstTypeForced(boolean constTypeForced) {
    this.constTypeForced = constTypeForced;
  }

  @Override
  public String toString() {
    try (var v = DecompilerContext.getImportCollector().lock()) {
      return "const(" + toJava(0).convertToStringAndAllowDataDiscard() + ")";
    }
  }

  // *****************************************************************************
  // IMatchable implementation
  // *****************************************************************************

  @Override
  public boolean match(MatchNode matchNode, MatchEngine engine) {
    if (!super.match(matchNode, engine)) {
      return false;
    }

    return matchNode.iterateRules((key, value) -> {
      if (key == MatchProperties.EXPRENT_CONSTTYPE) {
        return value.value.equals(this.constType);
      } else if (key == MatchProperties.EXPRENT_CONSTVALUE) {
        return !value.isVariable() || engine.checkAndSetVariableValue(value.value.toString(), this.value);
      }

      return true;
    });
  }
}
