package org.quiltmc.quiltflower.kotlin;

import org.jetbrains.java.decompiler.api.LanguageChooser;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.attr.StructAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;

public class KotlinChooser implements LanguageChooser {
  private static final StructGeneralAttribute.Key<?>[] ANNOTATION_ATTRIBUTES = {
    StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS, StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS
  };

  @Override
  public boolean isLanguage(StructClass cl) {
    // Try to find @Metadata()

    for (StructGeneralAttribute.Key<?> k : ANNOTATION_ATTRIBUTES) {
      if (cl.hasAttribute(k)) {
        StructAnnotationAttribute attr = (StructAnnotationAttribute) cl.getAttribute(k);
        for (AnnotationExprent anno : attr.getAnnotations()) {
          if (anno.getClassName().equals("kotlin/Metadata")) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
