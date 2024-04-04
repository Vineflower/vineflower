// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.attr;

import org.jetbrains.java.decompiler.api.ClassAttributeRegistry;
import org.jetbrains.java.decompiler.code.BytecodeVersion;
import org.jetbrains.java.decompiler.util.Key;
import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.IOException;

/*
  attribute_info {
    u2 attribute_name_index;
    u4 attribute_length;
    u1 info[attribute_length];
  }
*/
public class StructGeneralAttribute {
  public static final Key<StructCodeAttribute> ATTRIBUTE_CODE = Key.of("Code");
  public static final Key<StructInnerClassesAttribute> ATTRIBUTE_INNER_CLASSES = Key.of("InnerClasses");
  public static final Key<StructGenericSignatureAttribute> ATTRIBUTE_SIGNATURE = Key.of("Signature");
  public static final Key<StructAnnDefaultAttribute> ATTRIBUTE_ANNOTATION_DEFAULT = Key.of("AnnotationDefault");
  public static final Key<StructExceptionsAttribute> ATTRIBUTE_EXCEPTIONS = Key.of("Exceptions");
  public static final Key<StructEnclosingMethodAttribute> ATTRIBUTE_ENCLOSING_METHOD = Key.of("EnclosingMethod");
  public static final Key<StructAnnotationAttribute> ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS = Key.of("RuntimeVisibleAnnotations");
  public static final Key<StructAnnotationAttribute> ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS = Key.of("RuntimeInvisibleAnnotations");
  public static final Key<StructAnnotationParameterAttribute> ATTRIBUTE_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = Key.of("RuntimeVisibleParameterAnnotations");
  public static final Key<StructAnnotationParameterAttribute> ATTRIBUTE_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = Key.of("RuntimeInvisibleParameterAnnotations");
  public static final Key<StructTypeAnnotationAttribute> ATTRIBUTE_RUNTIME_VISIBLE_TYPE_ANNOTATIONS = Key.of("RuntimeVisibleTypeAnnotations");
  public static final Key<StructTypeAnnotationAttribute> ATTRIBUTE_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = Key.of("RuntimeInvisibleTypeAnnotations");
  public static final Key<StructLocalVariableTableAttribute> ATTRIBUTE_LOCAL_VARIABLE_TABLE = Key.of("LocalVariableTable");
  public static final Key<StructLocalVariableTypeTableAttribute> ATTRIBUTE_LOCAL_VARIABLE_TYPE_TABLE = Key.of("LocalVariableTypeTable");
  public static final Key<StructConstantValueAttribute> ATTRIBUTE_CONSTANT_VALUE = Key.of("ConstantValue");
  public static final Key<StructBootstrapMethodsAttribute> ATTRIBUTE_BOOTSTRAP_METHODS = Key.of("BootstrapMethods");
  public static final Key<StructGeneralAttribute> ATTRIBUTE_SYNTHETIC = Key.of("Synthetic");
  public static final Key<StructGeneralAttribute> ATTRIBUTE_DEPRECATED = Key.of("Deprecated");
  public static final Key<StructLineNumberTableAttribute> ATTRIBUTE_LINE_NUMBER_TABLE = Key.of("LineNumberTable");
  public static final Key<StructMethodParametersAttribute> ATTRIBUTE_METHOD_PARAMETERS = Key.of("MethodParameters");
  public static final Key<StructModuleAttribute> ATTRIBUTE_MODULE = Key.of("Module");
  public static final Key<StructRecordAttribute> ATTRIBUTE_RECORD = Key.of("Record");
  public static final Key<StructPermittedSubclassesAttribute> ATTRIBUTE_PERMITTED_SUBCLASSES = Key.of("PermittedSubclasses");
  public static final Key<StructSourceFileAttribute> ATTRIBUTE_SOURCE_FILE = Key.of("SourceFile");
  public static final Key<StructNestHostAttribute> ATTRIBUTE_NEST_HOST = Key.of("NestHost");
  // TODO: NestMembers

  public static StructGeneralAttribute createAttribute(String name) {
    for (Key<? extends StructGeneralAttribute> key : ClassAttributeRegistry.getRegistry().keySet()) {
      if (key.name.equals(name)) {
        return ClassAttributeRegistry.get(key);
      }
    }

    // Unknown attribute
    return null;
  }

  // Not placed in static intializer to avoid class loading issues
  public static void init() {
    ClassAttributeRegistry.register(ATTRIBUTE_CODE, StructCodeAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_INNER_CLASSES, StructInnerClassesAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_CONSTANT_VALUE, StructConstantValueAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_SIGNATURE, StructGenericSignatureAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_ANNOTATION_DEFAULT, StructAnnDefaultAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_EXCEPTIONS, StructExceptionsAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_ENCLOSING_METHOD, StructEnclosingMethodAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS, StructAnnotationAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS, StructAnnotationAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS, StructAnnotationParameterAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS, StructAnnotationParameterAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_RUNTIME_VISIBLE_TYPE_ANNOTATIONS, StructTypeAnnotationAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS, StructTypeAnnotationAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_LOCAL_VARIABLE_TABLE, StructLocalVariableTableAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_LOCAL_VARIABLE_TYPE_TABLE, StructLocalVariableTypeTableAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_BOOTSTRAP_METHODS, StructBootstrapMethodsAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_SYNTHETIC, StructGeneralAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_DEPRECATED, StructGeneralAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_LINE_NUMBER_TABLE, StructLineNumberTableAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_METHOD_PARAMETERS, StructMethodParametersAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_MODULE, StructModuleAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_RECORD, StructRecordAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_PERMITTED_SUBCLASSES, StructPermittedSubclassesAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_SOURCE_FILE, StructSourceFileAttribute::new);
    ClassAttributeRegistry.register(ATTRIBUTE_NEST_HOST, StructNestHostAttribute::new);
  }

  public void initContent(DataInputFullStream data, ConstantPool pool, BytecodeVersion version) throws IOException { }
}
