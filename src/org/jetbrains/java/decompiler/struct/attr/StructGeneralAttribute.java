// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.attr;

import org.jetbrains.java.decompiler.api.AttributeRegistry;
import org.jetbrains.java.decompiler.code.BytecodeVersion;
import org.jetbrains.java.decompiler.struct.Key;
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
  public static final Key<StructCodeAttribute> ATTRIBUTE_CODE = new Key<>("Code");
  public static final Key<StructInnerClassesAttribute> ATTRIBUTE_INNER_CLASSES = new Key<>("InnerClasses");
  public static final Key<StructGenericSignatureAttribute> ATTRIBUTE_SIGNATURE = new Key<>("Signature");
  public static final Key<StructAnnDefaultAttribute> ATTRIBUTE_ANNOTATION_DEFAULT = new Key<>("AnnotationDefault");
  public static final Key<StructExceptionsAttribute> ATTRIBUTE_EXCEPTIONS = new Key<>("Exceptions");
  public static final Key<StructEnclosingMethodAttribute> ATTRIBUTE_ENCLOSING_METHOD = new Key<>("EnclosingMethod");
  public static final Key<StructAnnotationAttribute> ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS = new Key<>("RuntimeVisibleAnnotations");
  public static final Key<StructAnnotationAttribute> ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS = new Key<>("RuntimeInvisibleAnnotations");
  public static final Key<StructAnnotationParameterAttribute> ATTRIBUTE_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = new Key<>("RuntimeVisibleParameterAnnotations");
  public static final Key<StructAnnotationParameterAttribute> ATTRIBUTE_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = new Key<>("RuntimeInvisibleParameterAnnotations");
  public static final Key<StructTypeAnnotationAttribute> ATTRIBUTE_RUNTIME_VISIBLE_TYPE_ANNOTATIONS = new Key<>("RuntimeVisibleTypeAnnotations");
  public static final Key<StructTypeAnnotationAttribute> ATTRIBUTE_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = new Key<>("RuntimeInvisibleTypeAnnotations");
  public static final Key<StructLocalVariableTableAttribute> ATTRIBUTE_LOCAL_VARIABLE_TABLE = new Key<>("LocalVariableTable");
  public static final Key<StructLocalVariableTypeTableAttribute> ATTRIBUTE_LOCAL_VARIABLE_TYPE_TABLE = new Key<>("LocalVariableTypeTable");
  public static final Key<StructConstantValueAttribute> ATTRIBUTE_CONSTANT_VALUE = new Key<>("ConstantValue");
  public static final Key<StructBootstrapMethodsAttribute> ATTRIBUTE_BOOTSTRAP_METHODS = new Key<>("BootstrapMethods");
  public static final Key<StructGeneralAttribute> ATTRIBUTE_SYNTHETIC = new Key<>("Synthetic");
  public static final Key<StructGeneralAttribute> ATTRIBUTE_DEPRECATED = new Key<>("Deprecated");
  public static final Key<StructLineNumberTableAttribute> ATTRIBUTE_LINE_NUMBER_TABLE = new Key<>("LineNumberTable");
  public static final Key<StructMethodParametersAttribute> ATTRIBUTE_METHOD_PARAMETERS = new Key<>("MethodParameters");
  public static final Key<StructModuleAttribute> ATTRIBUTE_MODULE = new Key<>("Module");
  public static final Key<StructRecordAttribute> ATTRIBUTE_RECORD = new Key<>("Record");
  public static final Key<StructPermittedSubclassesAttribute> ATTRIBUTE_PERMITTED_SUBCLASSES = new Key<>("PermittedSubclasses");
  public static final Key<StructSourceFileAttribute> ATTRIBUTE_SOURCE_FILE = new Key<>("SourceFile");

  public static StructGeneralAttribute createAttribute(String name) {
    for (Key<? extends StructGeneralAttribute> key : AttributeRegistry.getRegistry().keySet()) {
      if (key.name.equals(name)) {
        return AttributeRegistry.get(key);
      }
    }

    // Unknown attribute
    return null;
  }

  // Not placed in static intializer to avoid class loading issues
  public static void init() {
    AttributeRegistry.register(ATTRIBUTE_CODE, StructCodeAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_INNER_CLASSES, StructInnerClassesAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_CONSTANT_VALUE, StructConstantValueAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_SIGNATURE, StructGenericSignatureAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_ANNOTATION_DEFAULT, StructAnnDefaultAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_EXCEPTIONS, StructExceptionsAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_ENCLOSING_METHOD, StructEnclosingMethodAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS, StructAnnotationAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS, StructAnnotationAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS, StructAnnotationParameterAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS, StructAnnotationParameterAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_RUNTIME_VISIBLE_TYPE_ANNOTATIONS, StructTypeAnnotationAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_RUNTIME_INVISIBLE_TYPE_ANNOTATIONS, StructTypeAnnotationAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_LOCAL_VARIABLE_TABLE, StructLocalVariableTableAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_LOCAL_VARIABLE_TYPE_TABLE, StructLocalVariableTypeTableAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_BOOTSTRAP_METHODS, StructBootstrapMethodsAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_SYNTHETIC, StructGeneralAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_DEPRECATED, StructGeneralAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_LINE_NUMBER_TABLE, StructLineNumberTableAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_METHOD_PARAMETERS, StructMethodParametersAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_MODULE, StructModuleAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_RECORD, StructRecordAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_PERMITTED_SUBCLASSES, StructPermittedSubclassesAttribute::new);
    AttributeRegistry.register(ATTRIBUTE_SOURCE_FILE, StructSourceFileAttribute::new);
  }

  public void initContent(DataInputFullStream data, ConstantPool pool, BytecodeVersion version) throws IOException { }
}
