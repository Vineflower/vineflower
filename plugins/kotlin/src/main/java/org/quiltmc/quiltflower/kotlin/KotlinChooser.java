package org.quiltmc.quiltflower.kotlin;

import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import org.jetbrains.java.decompiler.api.LanguageChooser;
import org.jetbrains.java.decompiler.api.metadata.BitEncoding;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.attr.StructAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.util.TextUtil;
import org.jetbrains.kotlin.metadata.ProtoBuf;
import org.jetbrains.kotlin.metadata.jvm.JvmProtoBuf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

            Exprent ex = anno.getParValues().get(3);
            NewExprent newEx = (NewExprent) ex;
            // TODO: all elements
            String value = (String) ((ConstExprent) newEx.getLstArrayElements().get(0)).getValue();
//            System.out.println(ConstExprent.convertStringToJava(value, false));

            try {
              byte[] buf = BitEncoding.decodeBytes(new String[]{value});
//              System.out.println(Arrays.toString(buf));
              // FIXME: instantly crashes!
//              ProtoBuf.Class pcl = ProtoBuf.Class.parseFrom(new ByteArrayInputStream(buf), EXTENSIONS);
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
}
