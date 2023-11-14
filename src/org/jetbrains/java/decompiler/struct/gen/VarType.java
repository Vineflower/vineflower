// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.gen;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

public class VarType {

  public static final VarType[] EMPTY_ARRAY = {};

  public static final VarType VARTYPE_UNKNOWN = new VarType(CodeType.UNKNOWN);
  public static final VarType VARTYPE_INT = new VarType(CodeType.INT);
  public static final VarType VARTYPE_FLOAT = new VarType(CodeType.FLOAT);
  public static final VarType VARTYPE_LONG = new VarType(CodeType.LONG);
  public static final VarType VARTYPE_DOUBLE = new VarType(CodeType.DOUBLE);
  public static final VarType VARTYPE_BYTE = new VarType(CodeType.BYTE);
  public static final VarType VARTYPE_CHAR = new VarType(CodeType.CHAR);
  public static final VarType VARTYPE_SHORT = new VarType(CodeType.SHORT);
  public static final VarType VARTYPE_BOOLEAN = new VarType(CodeType.BOOLEAN);
  public static final VarType VARTYPE_BYTECHAR = new VarType(CodeType.BYTECHAR);
  public static final VarType VARTYPE_SHORTCHAR = new VarType(CodeType.SHORTCHAR);

  public static final VarType VARTYPE_NULL = new VarType(CodeType.NULL, 0, null);
  public static final VarType VARTYPE_STRING = new VarType(CodeType.OBJECT, 0, "java/lang/String");
  public static final VarType VARTYPE_CLASS = new VarType(CodeType.OBJECT, 0, "java/lang/Class");
  public static final VarType VARTYPE_OBJECT = new VarType(CodeType.OBJECT, 0, "java/lang/Object");
  public static final VarType VARTYPE_INTEGER = new VarType(CodeType.OBJECT, 0, "java/lang/Integer");
  public static final VarType VARTYPE_CHARACTER = new VarType(CodeType.OBJECT, 0, "java/lang/Character");
  public static final VarType VARTYPE_BYTE_OBJ = new VarType(CodeType.OBJECT, 0, "java/lang/Byte");
  public static final VarType VARTYPE_SHORT_OBJ = new VarType(CodeType.OBJECT, 0, "java/lang/Short");
  public static final VarType VARTYPE_BOOLEAN_OBJ = new VarType(CodeType.OBJECT, 0, "java/lang/Boolean");
  public static final VarType VARTYPE_FLOAT_OBJ = new VarType(CodeType.OBJECT, 0, "java/lang/Float");
  public static final VarType VARTYPE_DOUBLE_OBJ = new VarType(CodeType.OBJECT, 0, "java/lang/Double");
  public static final VarType VARTYPE_LONG_OBJ = new VarType(CodeType.OBJECT, 0, "java/lang/Long");
  public static final VarType VARTYPE_VOID = new VarType(CodeType.VOID);

  public static final Map<VarType, VarType> UNBOXING_TYPES = new HashMap<>();

  static {
    UNBOXING_TYPES.put(VARTYPE_INTEGER, VARTYPE_INT);
    UNBOXING_TYPES.put(VARTYPE_CHARACTER, VARTYPE_CHAR);
    UNBOXING_TYPES.put(VARTYPE_BYTE_OBJ, VARTYPE_BYTE);
    UNBOXING_TYPES.put(VARTYPE_SHORT_OBJ, VARTYPE_SHORT);
    UNBOXING_TYPES.put(VARTYPE_BOOLEAN_OBJ, VARTYPE_BOOLEAN);
    UNBOXING_TYPES.put(VARTYPE_FLOAT_OBJ, VARTYPE_FLOAT);
    UNBOXING_TYPES.put(VARTYPE_DOUBLE_OBJ, VARTYPE_DOUBLE);
    UNBOXING_TYPES.put(VARTYPE_LONG_OBJ, VARTYPE_LONG);
  }

  public final @NotNull CodeType type;
  public final int arrayDim;
  public final @Nullable String value;
  public final @NotNull TypeFamily typeFamily;
  public final int stackSize;

  public VarType(CodeType type) {
    this(type, 0);
  }

  public VarType(CodeType type, int arrayDim) {
    this(type, arrayDim, getChar(type));
  }

  public VarType(CodeType type, int arrayDim, String value) {
    this(type, arrayDim, value, getFamily(type, arrayDim), getStackSize(type, arrayDim));
  }

  protected VarType(CodeType type, int arrayDim, String value, @NotNull TypeFamily typeFamily, int stackSize) {
    this.type = type;
    this.arrayDim = arrayDim;
    this.value = value;
    this.typeFamily = typeFamily;
    this.stackSize = stackSize;
  }

  public VarType(String signature) {
    this(signature, false);
  }

  public VarType(String signature, boolean clType) {
    CodeType type = CodeType.BYTE; // TODO: should be null!
    int arrayDim = 0;
    String value = null;

    loop:
    for (int i = 0; i < signature.length(); i++) {
      switch (signature.charAt(i)) {
        case '[':
          arrayDim++;
          break;

        case 'L':
          if (signature.charAt(signature.length() - 1) == ';') {
            type = CodeType.OBJECT;
            value = signature.substring(i + 1, signature.length() - 1);
            break loop;
          }

        default:
          value = signature.substring(i);
          if ((clType && i == 0) || value.length() > 1) {
            type = CodeType.OBJECT;
          }
          else {
            type = getType(value.charAt(0));
          }
          break loop;
      }
    }

    this.type = type;
    this.arrayDim = arrayDim;
    this.value = value;
    this.typeFamily = getFamily(type, arrayDim);
    this.stackSize = getStackSize(type, arrayDim);
  }

  public static String getChar(CodeType type) {
    switch (type) {
      case BYTE:
        return "B";
      case CHAR:
        return "C";
      case DOUBLE:
        return "D";
      case FLOAT:
        return "F";
      case INT:
        return "I";
      case LONG:
        return "J";
      case SHORT:
        return "S";
      case BOOLEAN:
        return "Z";
      case VOID:
        return "V";
      case GROUP2EMPTY:
        return "G";
      case NOTINITIALIZED:
        return "N";
      case ADDRESS:
        return "A";
      case BYTECHAR:
        return "X";
      case SHORTCHAR:
        return "Y";
      case UNKNOWN:
        return "U";
      case NULL:
      case OBJECT:
        return null;
      default:
        throw new RuntimeException("Invalid type");
    }
  }

  protected static int getStackSize(CodeType type, int arrayDim) {
    if (arrayDim > 0) {
      return 1;
    }

    switch (type) {
      case DOUBLE:
      case LONG:
        return 2;
      case VOID:
      case GROUP2EMPTY:
        return 0;
      default:
        return 1;
    }
  }

  protected static TypeFamily getFamily(CodeType type, int arrayDim) {
    if (arrayDim > 0) {
      return TypeFamily.OBJECT;
    }

    switch (type) {
      case BYTE:
      case BYTECHAR:
      case SHORTCHAR:
      case CHAR:
      case SHORT:
      case INT:
        return TypeFamily.INTEGER;
      case DOUBLE:
        return TypeFamily.DOUBLE;
      case FLOAT:
        return TypeFamily.FLOAT;
      case LONG:
        return TypeFamily.LONG;
      case BOOLEAN:
        return TypeFamily.BOOLEAN;
      case NULL:
      case OBJECT:
        return TypeFamily.OBJECT;
      default:
        return TypeFamily.UNKNOWN;
    }
  }

  public VarType decreaseArrayDim() {
    if (arrayDim > 0) {
      return new VarType(type, arrayDim - 1, value);
    }
    else {
      //throw new RuntimeException("array dimension equals 0!"); FIXME: investigate this case
      return this;
    }
  }

  public VarType resizeArrayDim(int newArrayDim) {
    return new VarType(type, newArrayDim, value, typeFamily, stackSize);
  }

  public boolean isSuperset(VarType val) {
    return this.equals(val) || this.isStrictSuperset(val) || this.equals(UNBOXING_TYPES.get(val));
  }

  public boolean isStrictSuperset(VarType val) {
    CodeType valType = val.type;

    if (valType == CodeType.UNKNOWN && type != CodeType.UNKNOWN) {
      return true;
    }

    if (val.arrayDim > 0) {
      return this.equals(VARTYPE_OBJECT);
    }
    else if (arrayDim > 0) {
      return (valType == CodeType.NULL);
    }

    boolean res = false;

    switch (type) {
      case INT:
        res = (valType == CodeType.SHORT || valType == CodeType.CHAR);
      case SHORT:
        res |= (valType == CodeType.BYTE);
      case CHAR:
        res |= (valType == CodeType.SHORTCHAR);
      case BYTE:
      case SHORTCHAR:
        res |= (valType == CodeType.BYTECHAR);
      case BYTECHAR:
        res |= (valType == CodeType.BOOLEAN);
        break;

      case OBJECT:
        if (valType == CodeType.NULL) {
          return true;
        } else if (this.equals(VARTYPE_OBJECT)) {
          return valType == CodeType.OBJECT && !val.equals(VARTYPE_OBJECT);
        }
    }

    return res;
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 37 * result + type.ordinal();
    result = 37 * result + arrayDim;
    result = 37 * result + (value == null ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof VarType)) {
      return false;
    }

    VarType vt = (VarType)o;
    return type == vt.type && arrayDim == vt.arrayDim && InterpreterUtil.equalObjects(value, vt.value);
  }

  @Override
  public String toString() {
    StringBuilder res = new StringBuilder();
    for (int i = 0; i < arrayDim; i++) {
      res.append('[');
    }
    if (type == CodeType.OBJECT) {
      res.append('L').append(value).append(';');
    }
    else {
      res.append(value == null ? "--null--" : value);
    }
    return res.toString();
  }

  // type1 and type2 must not be null
  // Result should be the intersection of both types
  public static @Nullable VarType getCommonMinType(VarType type1, VarType type2) {
    if (type1.isSuperset(type2)) {
      return type2;
    }
    else if (type2.isSuperset(type1)) {
      return type1;
    }
    else if (type1.typeFamily == type2.typeFamily) {
      switch (type1.typeFamily) {
        case INTEGER:
          if ((type1.type == CodeType.CHAR && type2.type == CodeType.SHORT)
              || (type1.type == CodeType.SHORT && type2.type == CodeType.CHAR)) {
            return VARTYPE_SHORTCHAR;
          }
          else {
            return VARTYPE_BYTECHAR;
          }
        case OBJECT:
          return VARTYPE_NULL;
      }
    }

    return null;
  }

  // type1 and type2 must not be null
  // Result should be the union of both types
  public static @Nullable VarType getCommonSupertype(VarType type1, VarType type2) {
    if (type1.isSuperset(type2)) {
      return type1;
    }
    else if (type2.isSuperset(type1)) {
      return type2;
    }
    else if (type1.typeFamily == type2.typeFamily) {
      switch (type1.typeFamily) {
        case INTEGER:
          if ((type1.type == CodeType.SHORTCHAR && type2.type == CodeType.BYTE)
              || (type1.type == CodeType.BYTE && type2.type == CodeType.SHORTCHAR)) {
            return VARTYPE_SHORT;
          }
          else {
            return VARTYPE_INT;
          }
        case OBJECT:
          return VARTYPE_OBJECT;
      }
    }

    return null;
  }

  public static VarType getMinTypeInFamily(TypeFamily family) {
    switch (family) {
      case BOOLEAN:
        return VARTYPE_BOOLEAN;
      case INTEGER:
        return VARTYPE_BYTECHAR;
      case OBJECT:
        return VARTYPE_NULL;
      case FLOAT:
        return VARTYPE_FLOAT;
      case LONG:
        return VARTYPE_LONG;
      case DOUBLE:
        return VARTYPE_DOUBLE;
      case UNKNOWN:
        return VARTYPE_UNKNOWN;
      default:
        throw new IllegalArgumentException("Invalid type family: " + family);
    }
  }

  public static CodeType getType(char c) {
    switch (c) {
      case 'B':
        return CodeType.BYTE;
      case 'C':
        return CodeType.CHAR;
      case 'D':
        return CodeType.DOUBLE;
      case 'F':
        return CodeType.FLOAT;
      case 'I':
        return CodeType.INT;
      case 'J':
        return CodeType.LONG;
      case 'S':
        return CodeType.SHORT;
      case 'Z':
        return CodeType.BOOLEAN;
      case 'V':
        return CodeType.VOID;
      case 'G':
        return CodeType.GROUP2EMPTY;
      case 'N':
        return CodeType.NOTINITIALIZED;
      case 'A':
        return CodeType.ADDRESS;
      case 'X':
        return CodeType.BYTECHAR;
      case 'Y':
        return CodeType.SHORTCHAR;
      case 'U':
        return CodeType.UNKNOWN;
      default:
        throw new IllegalArgumentException("Invalid type: " + c);
    }
  }

  public static boolean isPrimitive(VarType type) {
    return UNBOXING_TYPES.values().contains(type);
  }

  public boolean isGeneric() {
    return false;
  }

  public VarType remap(Map<VarType, VarType> map) {
    VarType key = arrayDim == 0 ? this : this.resizeArrayDim(0);
    if (map.containsKey(key)) {
      VarType ret = map.get(key);
      return arrayDim == 0 || ret == null ? ret : ret.resizeArrayDim(ret.arrayDim + arrayDim);
    }
    return this;
  }
}
