package org.quiltmc.quiltflower.kotlin;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;
import kotlin.reflect.jvm.internal.impl.metadata.jvm.JvmProtoBuf;
import kotlin.reflect.jvm.internal.impl.protobuf.ExtensionRegistryLite;
import org.jetbrains.java.decompiler.api.LanguageChooser;
import org.quiltmc.quiltflower.kotlin.metadata.BitEncoding;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.attr.StructAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.quiltmc.quiltflower.kotlin.metadata.MetadataNameResolver;

import java.io.ByteArrayInputStream;

public class KotlinChooser implements LanguageChooser {
  private static final StructGeneralAttribute.Key<?>[] ANNOTATION_ATTRIBUTES = {
    StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS, StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS
  };

  private static final ExtensionRegistryLite EXTENSIONS = ExtensionRegistryLite.newInstance();
  static {
    JvmProtoBuf.registerAllExtensions(EXTENSIONS);
  }

  @Override
  public boolean isLanguage(StructClass cl) {
    // Try to find @Metadata()

    for (StructGeneralAttribute.Key<?> k : ANNOTATION_ATTRIBUTES) {
      if (cl.hasAttribute(k)) {
        StructAnnotationAttribute attr = (StructAnnotationAttribute) cl.getAttribute(k);
        for (AnnotationExprent anno : attr.getAnnotations()) {
          if (anno.getClassName().equals("kotlin/Metadata")) {

            Exprent d1 = anno.getParValues().get(3);
            Exprent d2 = anno.getParValues().get(4);

            String[] data1 = getDataFromExpr((NewExprent) d1);


            try {
              String[] data2 = getDataFromExpr((NewExprent) d2);

              byte[] buf = BitEncoding.decodeBytes(data1);

              ByteArrayInputStream input = new ByteArrayInputStream(buf);
              JvmProtoBuf.StringTableTypes types = JvmProtoBuf.StringTableTypes.parseDelimitedFrom(input, EXTENSIONS);
              // TODO: use kind to figure out which to parse
              ProtoBuf.Class pcl = ProtoBuf.Class.parseFrom(input, EXTENSIONS);

              MetadataNameResolver resolver = new MetadataNameResolver(types, data2);
//              System.out.println(resolver.resolve(pcl.getFqName()));
//
//              for (ProtoBuf.Function func : pcl.getFunctionList()) {
//                System.out.println(resolver.resolve(func.getName()));
//              }
            } catch (Exception e) {
              e.printStackTrace();
            }

            return true;
          }
        }
      }
    }
    return false;
  }

  private String[] getDataFromExpr(NewExprent d2) {
    return d2.getLstArrayElements()
      .stream()
      .map(ConstExprent.class::cast)
      .map(ConstExprent::getValue)
      .map(String.class::cast)
      .toArray(String[]::new);
  }
}
