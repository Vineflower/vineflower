package org.vineflower.kotlin.struct;

/**
 * Any element that should not be shown in decompiled output, but which might
 * contain information relevant to decompilation.
 */
public enum KHiddenElement implements KElement {
  GENERIC,
  /** The single instance for a companion object. This is a static field in the associated class. */
  COMPANION_INSTANCE,
  /** A method or field that a companion object declared but which is implemented in or forwarded from the associated class. */
  COMPANION_ITEM,
  /** The single instance for an object. This is a static field in the object itself. */
  OBJECT_INSTANCE,

  /** 
   * A default method or constructor implementation.
   * Default methods contain at least two extra parameters. The last is typed {@link Object} and is always null,
   * and all other parameters are bitfields as ints.
   * Default constructors contain the same extra parameters, but the last is instead typed
   * {@code kotlin.jvm.internal.DefaultConstructorMarker}.
   */
  DEFAULT_IMPL
}
