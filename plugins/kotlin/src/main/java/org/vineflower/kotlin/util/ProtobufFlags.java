package org.vineflower.kotlin.util;

import kotlinx.metadata.internal.metadata.ProtoBuf;
import kotlinx.metadata.internal.metadata.deserialization.Flags;

public interface ProtobufFlags {
  //TODO: hasNonStableParameterNames
  //TODO: Update everything to use Flags class

  int HAS_ANNOTATIONS = 0x0001;

  int VISIBILITY_MASK = 0x000E;
  int MODALITY_MASK = 0x0030;
  int KIND_MASK = 0x01C0;

  //region: Constructor
  int CTOR_SECONDARY = 0x0010;

  class Constructor {
    public final boolean hasAnnotations;
    public final ProtoBuf.Visibility visibility;
    public final boolean isSecondary;
//    public final boolean hasNonStableParameterNames;

    public Constructor(int flags) {
      hasAnnotations = (flags & HAS_ANNOTATIONS) != 0;
      visibility = ProtoBuf.Visibility.valueOf((flags & VISIBILITY_MASK) >> 1);
      isSecondary = (flags & CTOR_SECONDARY) != 0;
//      hasNonStableParameterNames = (flags & FUN_HAS_NON_STABLE_PARAMETER_NAMES) != 0;
    }
  }
  //endregion

  //region: Function
  int FUN_IS_OPERATOR = 0x0100;
  int FUN_IS_INFIX = 0x0200;
  int FUN_IS_INLINE = 0x0400;
  int FUN_IS_TAILREC = 0x0800;
  int FUN_IS_EXTERNAL = 0x1000;
  int FUN_IS_SUSPEND = 0x2000;
  int FUN_IS_EXPECT = 0x4000;

  class Function {
    public final boolean hasAnnotations;
    public final ProtoBuf.Visibility visibility;
    public final ProtoBuf.Modality modality;
    public final ProtoBuf.MemberKind kind;
    public final boolean isOperator;
    public final boolean isInfix;
    public final boolean isInline;
    public final boolean isTailrec;
    public final boolean isExternal;
    public final boolean isSuspend;
    public final boolean isExpect;
//    public final boolean hasNonStableParameterNames;

    public Function(int flags) {
      hasAnnotations = (flags & HAS_ANNOTATIONS) != 0;
      visibility = ProtoBuf.Visibility.valueOf((flags & VISIBILITY_MASK) >> 1);
      modality = ProtoBuf.Modality.valueOf((flags & MODALITY_MASK) >> 4);
      kind = ProtoBuf.MemberKind.valueOf((flags & KIND_MASK) >> 6);
      isOperator = (flags & FUN_IS_OPERATOR) != 0;
      isInfix = (flags & FUN_IS_INFIX) != 0;
      isInline = (flags & FUN_IS_INLINE) != 0;
      isTailrec = (flags & FUN_IS_TAILREC) != 0;
      isExternal = (flags & FUN_IS_EXTERNAL) != 0;
      isSuspend = (flags & FUN_IS_SUSPEND) != 0;
      isExpect = (flags & FUN_IS_EXPECT) != 0;
//      hasNonStableParameterNames = (flags & FUN_HAS_NON_STABLE_PARAMETER_NAMES) != 0;
    }
  }
  //endregion

  //region: Property
  int PROP_IS_VAR = 0x0100;
  int PROP_HAS_GETTER = 0x0200;
  int PROP_HAS_SETTER = 0x0400;
  int PROP_IS_CONST = 0x0800;
  int PROP_IS_LATEINIT = 0x1000;
  int PROP_HAS_CONSTANT = 0x2000;
  int PROP_IS_EXTERNAL = 0x4000;
  int PROP_IS_DELEGATED = 0x8000;
  int PROP_IS_EXPECT = 0x10000;

  class Property {
    public final boolean hasAnnotations;
    public final ProtoBuf.Visibility visibility;
    public final ProtoBuf.Modality modality;
    public final ProtoBuf.MemberKind kind;
    public final boolean isVar;
    public final boolean hasGetter;
    public final boolean hasSetter;
    public final boolean isConst;
    public final boolean isLateinit;
    public final boolean hasConstant;
    public final boolean isExternal;
    public final boolean isDelegated;
    public final boolean isExpect;

    public Property(int flags) {
      hasAnnotations = (flags & HAS_ANNOTATIONS) != 0;
      visibility = ProtoBuf.Visibility.valueOf((flags & VISIBILITY_MASK) >> 1);
      modality = ProtoBuf.Modality.valueOf((flags & MODALITY_MASK) >> 4);
      kind = ProtoBuf.MemberKind.valueOf((flags & KIND_MASK) >> 6);
      isVar = (flags & PROP_IS_VAR) != 0;
      hasGetter = (flags & PROP_HAS_GETTER) != 0;
      hasSetter = (flags & PROP_HAS_SETTER) != 0;
      isConst = (flags & PROP_IS_CONST) != 0;
      isLateinit = (flags & PROP_IS_LATEINIT) != 0;
      hasConstant = (flags & PROP_HAS_CONSTANT) != 0;
      isExternal = (flags & PROP_IS_EXTERNAL) != 0;
      isDelegated = (flags & PROP_IS_DELEGATED) != 0;
      isExpect = (flags & PROP_IS_EXPECT) != 0;
    }
  }
  //endregion

  //region: Property getter/setter
  int PROP_ACCESSOR_IS_NOT_DEFAULT = 0x0040;
  int PROP_ACCESSOR_IS_EXTERNAL = 0x0080;
  int PROP_ACCESSOR_IS_INLINE = 0x0100;

  class PropertyAccessor {
    public final boolean hasAnnotations;
    public final ProtoBuf.Visibility visibility;
    public final ProtoBuf.Modality modality;
    public final boolean isNotDefault;
    public final boolean isExternal;
    public final boolean isInline;

    public PropertyAccessor(int flags) {
      hasAnnotations = (flags & HAS_ANNOTATIONS) != 0;
      visibility = ProtoBuf.Visibility.valueOf((flags & VISIBILITY_MASK) >> 1);
      modality = ProtoBuf.Modality.valueOf((flags & MODALITY_MASK) >> 4);
      isNotDefault = (flags & PROP_ACCESSOR_IS_NOT_DEFAULT) != 0;
      isExternal = (flags & PROP_ACCESSOR_IS_EXTERNAL) != 0;
      isInline = (flags & PROP_ACCESSOR_IS_INLINE) != 0;
    }
  }
  //endregion

  //region: Value parameter
  int VAL_PARAM_DECLARES_DEFAULT = 0x0002;
  int VAL_PARAM_IS_CROSSINLINE = 0x0004;
  int VAL_PARAM_IS_NOINLINE = 0x0008;

  class ValueParameter {
    public final boolean hasAnnotations;
    public final boolean declaresDefault;
    public final boolean isCrossinline;
    public final boolean isNoinline;

    public ValueParameter(int flags) {
      hasAnnotations = (flags & HAS_ANNOTATIONS) != 0;
      declaresDefault = (flags & VAL_PARAM_DECLARES_DEFAULT) != 0;
      isCrossinline = (flags & VAL_PARAM_IS_CROSSINLINE) != 0;
      isNoinline = (flags & VAL_PARAM_IS_NOINLINE) != 0;
    }
  }
  //endregion

  //region: Typealias
  class TypeAlias {
    public final boolean hasAnnotations;
    public final ProtoBuf.Visibility visibility;

    public TypeAlias(int flags) {
      hasAnnotations = (flags & HAS_ANNOTATIONS) != 0;
      visibility = ProtoBuf.Visibility.valueOf((flags & VISIBILITY_MASK) >> 1);
    }
  }
  //endregion

  //region: Class
  int CLASS_IS_INNER = 0x0200;
  int CLASS_IS_DATA = 0x0400;
  int CLASS_IS_EXTERNAL = 0x0800;
  int CLASS_IS_EXPECT = 0x1000;
  int CLASS_IS_INLINE = 0x2000;
  int CLASS_IS_FUN = 0x4000;
  int CLASS_HAS_ENUM_ENTRIES = 0x8000;

  class Class {
    public final boolean hasAnnotations;
    public final ProtoBuf.Visibility visibility;
    public final ProtoBuf.Modality modality;
    public final ProtoBuf.Class.Kind kind;
    public final boolean isInner;
    public final boolean isData;
    public final boolean isExternal;
    public final boolean isExpect;
    public final boolean isInline;
    public final boolean isFun;
    public final boolean hasEnumEntries;

    public Class(int flags) {
      hasAnnotations = (flags & HAS_ANNOTATIONS) != 0;
      visibility = ProtoBuf.Visibility.valueOf((flags & VISIBILITY_MASK) >> 1);
      modality = ProtoBuf.Modality.valueOf((flags & MODALITY_MASK) >> 4);
      kind = ProtoBuf.Class.Kind.valueOf((flags & KIND_MASK) >> 6);
      isInner = (flags & CLASS_IS_INNER) != 0;
      isData = (flags & CLASS_IS_DATA) != 0;
      isExternal = (flags & CLASS_IS_EXTERNAL) != 0;
      isExpect = (flags & CLASS_IS_EXPECT) != 0;
      isInline = (flags & CLASS_IS_INLINE) != 0;
      isFun = (flags & CLASS_IS_FUN) != 0;
      hasEnumEntries = (flags & CLASS_HAS_ENUM_ENTRIES) != 0;
    }
  }
  //endregion

  //region: Contract
  class Expression {
    public final boolean isNegated;
    public final boolean isNullPredicate;

    public Expression(int flags) {
      isNegated = Flags.IS_NEGATED.get(flags);
      isNullPredicate = Flags.IS_NULL_CHECK_PREDICATE.get(flags);
    }
  }
  //endregion

  static String toString(ProtoBuf.Visibility visibility) {
    switch (visibility) {
      case PRIVATE:
      case PRIVATE_TO_THIS:
        return "private";
      case PROTECTED:
        return "protected";
      case INTERNAL:
        return "internal";
      default:
        return "public";
    }
  }

  static String toString(ProtoBuf.Modality modality) {
    switch (modality) {
      case FINAL:
        return "final";
      case OPEN:
        return "open";
      case ABSTRACT:
        return "abstract";
      case SEALED:
        return "sealed";
    }
    throw new IllegalStateException("Unknown modality: " + modality);
  }
}
