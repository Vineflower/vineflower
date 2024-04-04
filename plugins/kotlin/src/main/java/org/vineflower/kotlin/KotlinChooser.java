package org.vineflower.kotlin;

import kotlinx.metadata.internal.metadata.ProtoBuf;
import kotlinx.metadata.internal.metadata.jvm.JvmProtoBuf;
import kotlinx.metadata.internal.protobuf.ExtensionRegistryLite;
import org.jetbrains.java.decompiler.api.plugin.LanguageChooser;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AnnotationExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.attr.StructAnnotationAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.util.Key;
import org.vineflower.kotlin.metadata.BitEncoding;
import org.vineflower.kotlin.metadata.MetadataNameResolver;

import java.io.ByteArrayInputStream;
import java.util.Objects;

public class KotlinChooser implements LanguageChooser {
  private static final Key<?>[] ANNOTATION_ATTRIBUTES = {
    StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS, StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS
  };

  private static final ExtensionRegistryLite EXTENSIONS = ExtensionRegistryLite.newInstance();
  static {
    JvmProtoBuf.registerAllExtensions(EXTENSIONS);
  }

  @Override
  public boolean isLanguage(StructClass cl) {
    if (!DecompilerContext.getOption(KotlinOptions.DECOMPILE_KOTLIN)) {
      return false;
    }

    // Try to find @Metadata()

    for (Key<?> key : ANNOTATION_ATTRIBUTES) {
      if (cl.hasAttribute(key)) {
        StructAnnotationAttribute attr = cl.getAttribute((Key<StructAnnotationAttribute>) key);
        for (AnnotationExprent anno : attr.getAnnotations()) {
          if (anno.getClassName().equals("kotlin/Metadata")) {
            setContextVariables(cl);
            return true;
          }
        }
      }
    }

    DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, null);
    return false;
  }

  public static void setContextVariables(StructClass cl) {
    AnnotationExprent anno = null;

    loop:
    for (Key<?> key : ANNOTATION_ATTRIBUTES) {
      if (cl.hasAttribute(key)) {
        StructAnnotationAttribute attr = cl.getAttribute((Key<StructAnnotationAttribute>) key);
        for (AnnotationExprent a : attr.getAnnotations()) {
          if (a.getClassName().equals("kotlin/Metadata")) {
            anno = a;
            break loop;
          }
        }
      }
    }

    try {
      int kIndex = anno.getParNames().indexOf("k");
      int d1Index = anno.getParNames().indexOf("d1");
      int d2Index = anno.getParNames().indexOf("d2");

      if (kIndex == -1) {
        DecompilerContext.getLogger().writeMessage("No k attribute in class metadata for class " + cl.qualifiedName, IFernflowerLogger.Severity.WARN);
        DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, null);
        return;
      }

      if (d1Index == -1) {
        DecompilerContext.getLogger().writeMessage("No d1 attribute in class metadata for class " + cl.qualifiedName, IFernflowerLogger.Severity.WARN);
        DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, null);
        return;
      }

      if (d2Index == -1) {
        DecompilerContext.getLogger().writeMessage("No d2 attribute in class metadata for class " + cl.qualifiedName, IFernflowerLogger.Severity.WARN);
        DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, null);
        return;
      }

      int k = (int) ((ConstExprent) anno.getParValues().get(kIndex)).getValue();
      Exprent d1 = anno.getParValues().get(d1Index);
      Exprent d2 = anno.getParValues().get(d2Index);

      String[] data1 = getDataFromExpr((NewExprent) d1);

      String[] data2 = getDataFromExpr((NewExprent) d2);

      byte[] buf = BitEncoding.decodeBytes(data1);

      ByteArrayInputStream input = new ByteArrayInputStream(buf);
      JvmProtoBuf.StringTableTypes types = JvmProtoBuf.StringTableTypes.parseDelimitedFrom(input, EXTENSIONS);

      DecompilerContext.setProperty(KotlinDecompilationContext.NAME_RESOLVER, new MetadataNameResolver(types, data2));

      if (k == 1) { // Class file
        ProtoBuf.Class pcl = ProtoBuf.Class.parseFrom(input, EXTENSIONS);

        DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, KotlinDecompilationContext.KotlinType.CLASS);
        DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_CLASS, pcl);

      } else if (k == 2) { // File facade
        ProtoBuf.Package pcl = ProtoBuf.Package.parseFrom(input, EXTENSIONS);
        DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, KotlinDecompilationContext.KotlinType.FILE);
        DecompilerContext.setProperty(KotlinDecompilationContext.FILE_PACKAGE, pcl);
      } else if (k == 3) { // Synthetic class
        ProtoBuf.Function func = ProtoBuf.Function.parseFrom(input, EXTENSIONS);
        DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, KotlinDecompilationContext.KotlinType.SYNTHETIC_CLASS);
        DecompilerContext.setProperty(KotlinDecompilationContext.SYNTHETIC_CLASS, func);
      } else if (k == 5) { // Multi-file facade
        ProtoBuf.Package pcl = ProtoBuf.Package.parseFrom(input, EXTENSIONS);
        DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, KotlinDecompilationContext.KotlinType.MULTIFILE_CLASS);
        DecompilerContext.setProperty(KotlinDecompilationContext.MULTIFILE_PACKAGE, pcl);
      } else {
        DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, null);
      }
    } catch (Exception e) {
      DecompilerContext.getLogger().writeMessage("Failed to parse metadata for class " + cl.qualifiedName, IFernflowerLogger.Severity.WARN, e);
      DecompilerContext.setProperty(KotlinDecompilationContext.CURRENT_TYPE, null);
    }
  }

  private static String[] getDataFromExpr(NewExprent d2) {
    return d2.getLstArrayElements()
      .stream()
      .map(ConstExprent.class::cast)
      .map(ConstExprent::getValue)
      .map(String.class::cast)
      .toArray(String[]::new);
  }
}
