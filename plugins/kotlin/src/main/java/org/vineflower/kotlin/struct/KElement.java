package org.vineflower.kotlin.struct;

import org.jetbrains.java.decompiler.util.Key;

public sealed interface KElement
  permits KClass, KConstructor, KFile, KFunction, KFunctionReference, KHiddenElement, KMultifileFacade, KProperty {
  Key<KElement> KEY = Key.of("KElement");
}
