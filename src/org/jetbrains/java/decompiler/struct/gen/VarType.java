// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.gen;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericType;
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

  protected VarType(CodeType type, int arrayDim, String value, TypeFamily typeFamily, int stackSize) {
    ValidationHelper.assertTrue(type != null && typeFamily != null, "Type and type family must not be null");
    ValidationHelper.assertTrue(type == CodeType.NULL || value != null, "Must not be null for non null type");
    this.type = type;
    this.arrayDim = arrayDim;
    this.value = value == null ? null : value.intern();
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
    this.value = value == null ? null : value.intern();
    this.typeFamily = getFamily(type, arrayDim);
    this.stackSize = getStackSize(type, arrayDim);
  }

  public static String getChar(CodeType type) {
    return switch (type) {
      case BYTE -> "B";
      case CHAR -> "C";
      case DOUBLE -> "D";
      case FLOAT -> "F";
      case INT -> "I";
      case LONG -> "J";
      case SHORT -> "S";
      case BOOLEAN -> "Z";
      case VOID -> "V";
      case GROUP2EMPTY -> "G";
      case NOTINITIALIZED -> "N";
      case ADDRESS -> "A";
      case BYTECHAR -> "X";
      case SHORTCHAR -> "Y";
      case UNKNOWN -> "U";
      case NULL, OBJECT -> null;
      default -> throw new RuntimeException("Invalid type");
    };
  }

  protected static int getStackSize(CodeType type, int arrayDim) {
    if (arrayDim > 0) {
      return 1;
    }

    return switch (type) {
      case DOUBLE, LONG -> 2;
      case VOID, GROUP2EMPTY -> 0;
      default -> 1;
    };
  }

  protected static TypeFamily getFamily(CodeType type, int arrayDim) {
    if (arrayDim > 0) {
      return TypeFamily.OBJECT;
    }

    return switch (type) {
      case BYTE, BYTECHAR, SHORTCHAR,
           CHAR, SHORT, INT -> TypeFamily.INTEGER;
      case DOUBLE -> TypeFamily.DOUBLE;
      case FLOAT -> TypeFamily.FLOAT;
      case LONG -> TypeFamily.LONG;
      case BOOLEAN -> TypeFamily.BOOLEAN;
      case NULL, OBJECT, GENVAR -> TypeFamily.OBJECT;
      default -> TypeFamily.UNKNOWN;
    };
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
    ValidationHelper.assertTrue(newArrayDim >= 0, "Can't have an array of negative size!");
    return new VarType(type, newArrayDim, value, typeFamily, stackSize);
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

    if (!(o instanceof VarType vt)) {
      return false;
    }

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

  public static CodeType getType(char c) {
    return switch (c) {
      case 'B' -> CodeType.BYTE;
      case 'C' -> CodeType.CHAR;
      case 'D' -> CodeType.DOUBLE;
      case 'F' -> CodeType.FLOAT;
      case 'I' -> CodeType.INT;
      case 'J' -> CodeType.LONG;
      case 'S' -> CodeType.SHORT;
      case 'Z' -> CodeType.BOOLEAN;
      case 'V' -> CodeType.VOID;
      case 'G' -> CodeType.GROUP2EMPTY;
      case 'N' -> CodeType.NOTINITIALIZED;
      case 'A' -> CodeType.ADDRESS;
      case 'X' -> CodeType.BYTECHAR;
      case 'Y' -> CodeType.SHORTCHAR;
      case 'U' -> CodeType.UNKNOWN;
      default -> throw new IllegalArgumentException("Invalid type: " + c);
    };
  }

  public static boolean isPrimitive(VarType type) {
    return UNBOXING_TYPES.values().contains(type);
  }

  public boolean isGeneric() {
    return false;
  }

  public @Nullable VarType remap(Map<VarType, VarType> map) {
    VarType key = arrayDim == 0 ? this : this.resizeArrayDim(0);
    if (map.containsKey(key)) {
      VarType ret = map.get(key);
      return arrayDim == 0 || ret == null ? ret : ret.resizeArrayDim(ret.arrayDim + arrayDim);
    }
    return this;
  }

  // ==========================================================================
  //                            THE VARTYPE LATTICE
  // ==========================================================================
  //
  // VarType instances are organized in a lattice based on their type family.
  // Each family has its own lattice, each organized a bit differently, but all
  // are defined by MEET and JOIN. Of the 7 type families, the lattice of 4 of
  // them are trivial. For BOOLEAN, FLOAT, DOUBLE, and LONG, each element makes
  // up its own lattice such that:
  //                                  TOP
  //                                   |
  //                                  Type
  //                                   |
  //                                  BOT
  //
  // This makes MEET and JOIN trivial to define. Out of the rest, the integer
  // and object lattices are much more interesting. For the integer lattice, we
  // need to consider sub-integer types and how they play into the definition.
  // The integer lattice is defined as such:
  //                                  TOP
  //                                   |
  //                                  int
  //                                 /   \
  //                                /     \
  //                               /       \
  //                              /         \
  //                           char        short
  //                             \         /  |
  //                              \       /  byte
  //                               \     /    |
  //                               shortchar  |
  //                                   |     /
  //                                   |    /
  //                                bytechar
  //                                   |
  //                                  BOT
  //
  // The complication with this lattice is that there are in essence two
  // domains, integer and char. During the decompilation process there may
  // not be enough context to decide if a numerical definition is part of the
  // character or integer domain. To reconcile this, usages are calculated
  // and a sharper type is determined. The next interesting lattice is the
  // Object lattice family. It is defined as such:
  //
  //                                  TOP
  //                                   |
  //                                 Object
  //                                  /|\
  //                                 / | \
  //                                /  |  \
  //                             Implementors...
  //                                \  |  /
  //                                 \ | /
  //                                  \|/
  //                                   |
  //                                  null
  //                                   |
  //                                  BOT
  //
  // "Implementors" are any class that extends Object, so it can be regular
  // classes, all arrays, enums, records, et cetera. Arrays match the Object
  // lattice. If the size of the arrays are the same, then the array types
  // have the same relation as their base types.
  //
  // The final type family is UNKNOWN, the family of bottom types. This poses
  // a conundrum for the lattice, as it would only consist of the bottom.
  // Therefore it does not make sense to consider this as a lattice of its own
  // right, and just treat it as a special case.

  // Rule of thumb: A type HIGHER in the lattice will need a cast to go to a type LOWER in the lattice.


  public boolean higherEqualInLatticeThan(VarType val) {
    return this.equals(val) || this.higherInLatticeThan(val) || this.equals(UNBOXING_TYPES.get(val));
  }

  // Is 'this' higher in the lattice when compared to 'other'?
  public boolean higherInLatticeThan(VarType other) {
    // If other is BOT, and we are not BOT, we must necessarily be higher in the lattice
    if (other.type == CodeType.UNKNOWN && type != CodeType.UNKNOWN) {
      return true;
    }

    if (this.arrayDim > 0 && this.arrayDim == other.arrayDim) {
      // Testing against two arrays

      // Arrays are covariant, check their bases
      return this.resizeArrayDim(0).higherInLatticeThan(other.resizeArrayDim(0));
    } else if (other.arrayDim > 0) {
      // If the other is an array, only Object is higher in the lattice than the other
      return this.equals(VARTYPE_OBJECT);
    } else if (arrayDim > 0) {
      // If the other is not an array but we are, the only way we can be higher in the lattice is if the other is null (close to bottom)
      return (other.type == CodeType.NULL);
    }

    boolean res = false;

    // c.f. the int family lattice
    switch (type) {
      case INT:
        res = (other.type == CodeType.SHORT || other.type == CodeType.CHAR);
      case SHORT:
        res |= (other.type == CodeType.BYTE);
      case CHAR:
        res |= (other.type == CodeType.SHORTCHAR);
      case BYTE:
      case SHORTCHAR:
        res |= (other.type == CodeType.BYTECHAR);
      case BYTECHAR:
        // Special case boolean: it's represented as an integer, so the domains cross sometimes
        res |= (other.type == CodeType.BOOLEAN);
        break;

      case OBJECT:
        // BOT -> null -> Impl... -> Object -> TOP
        
        // if other is null, then, as an object, we must be higher in the lattice
        if (other.type == CodeType.NULL) {
          return true;
        } else if (this.equals(VARTYPE_OBJECT)) {
          // if we are object, then we are higher than everything other than object and TOP
          return !other.equals(VARTYPE_OBJECT);
        }

        // Check base if neither 'this' and 'other' are generic, or if the generic types satisfy the condition.

        boolean checkBase = true;
        if (isGeneric() && other.isGeneric()) {
          checkBase = shouldCheckGenericBase((GenericType) this, (GenericType) other);
        }

        // Given that B extends A, B is lower in the lattice when compared to A.
        if (checkBase && other.type == CodeType.OBJECT && !other.value.equals(value)) {
          if (DecompilerContext.getStructContext().instanceOf(other.value, value)) {
            return true;
          }
        }
    }

    return res;
  }

  // For generic types 't' and 'other', should we check the base type to determine if t <: other?
  // E.g. List<Type> <: Collection<Type>
  private static boolean shouldCheckGenericBase(GenericType t, GenericType other) {
    if (t.argumentsEqual(other)) {
      return t.getWildcard() == other.getWildcard();
    } else {
      // Arguments not equal. Check them, one by one.
      if (t.getArguments().size() == other.getArguments().size()) {
        for (int i = 0; i < t.getArguments().size(); i++) {
          VarType a1 = t.getArguments().get(i);
          VarType a2 = other.getArguments().get(i);

          // List<? extends Type> <: Collection<Type>
          // List<? super Type> <: Collection<Type>
          if (a2 != null && a1 instanceof GenericType ga1 && ga1.getWildcard() != GenericType.WILDCARD_NO) {
            if (a1.value.equals(a2.value) && !a2.isGeneric()) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  // higherEqualInLattice BUT we also check for assignability relations
  public boolean higherCrossFamilyThan(VarType other, boolean equal) {
    // Check higher (equal) within the same lattice
    if (equal) {
      if (this.higherEqualInLatticeThan(other)) {
        return true;
      }
    } else {
      if (this.higherInLatticeThan(other)) {
        return true;
      }
    }

    // c.f. https://docs.oracle.com/javase/specs/jls/se21/html/jls-4.html#jls-4.10.1
    boolean res = false;
    switch (this.type) {
      case DOUBLE: // float, long, and integer can be assigned to double
        res = other.typeFamily == TypeFamily.FLOAT;
      case FLOAT: // long and int
        res |= other.typeFamily == TypeFamily.LONG;
      case LONG: // just int
        res |= other.typeFamily == TypeFamily.INTEGER;
    }

    return res;
  }

  // Returns the "minimal" type out of the two provided (the MEET of both types)
  // Types should fall in lattice
  public static @Nullable VarType meet(@NotNull VarType type1, @NotNull VarType type2) {
    if (type1.higherEqualInLatticeThan(type2)) {
      return type2;
    } else if (type2.higherEqualInLatticeThan(type1)) {
      return type1;
    } else if (type1.typeFamily == type2.typeFamily) {
      // Special casing
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
          // Consider a hierarchy where:
          // - B extends A
          // - C extends A
          // meet(B, C) must be null
          // TODO: no it doesn't! it can be a union type, that way it preserves information!
          return VARTYPE_NULL;
      }
    }

    return null;
  }

  // Returns the "maximal" type out of the two provided (the JOIN of both types)
  // Types should rise in the lattice
  public static @Nullable VarType join(@NotNull VarType type1, @NotNull VarType type2) {
    if (type1.higherEqualInLatticeThan(type2)) {
      return type1;
    } else if (type2.higherEqualInLatticeThan(type1)) {
      return type2;
    } else if (type1.typeFamily == type2.typeFamily) {
      // Special casing
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
          // If either is null, join is the other
          if (type1.type == CodeType.NULL) {
            return type2;
          } else if (type2.type == CodeType.NULL) {
            return type1;
          }

          // TODO: can make an intersection type?
          StructClass cl = DecompilerContext.getStructContext().findCommonAncestor(type1.value, type2.value);
          if (cl != null) {
            return new VarType(cl.qualifiedName, true);
          }

          return VARTYPE_OBJECT;
      }
    }

    return null;
  }

  public static VarType findFamilyBottom(TypeFamily family) {
    return switch (family) {
      case BOOLEAN -> VARTYPE_BOOLEAN;
      case INTEGER -> VARTYPE_BYTECHAR;
      case OBJECT -> VARTYPE_NULL;
      case FLOAT -> VARTYPE_FLOAT;
      case LONG -> VARTYPE_LONG;
      case DOUBLE -> VARTYPE_DOUBLE;
      case UNKNOWN -> VARTYPE_UNKNOWN;
    };
  }
}
