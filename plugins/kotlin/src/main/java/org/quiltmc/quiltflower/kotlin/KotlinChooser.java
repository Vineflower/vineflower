package org.quiltmc.quiltflower.kotlin;

import kotlin.reflect.jvm.internal.impl.metadata.ProtoBuf;
import kotlin.reflect.jvm.internal.impl.metadata.jvm.JvmProtoBuf;
import kotlin.reflect.jvm.internal.impl.protobuf.ExtensionRegistryLite;
import org.jetbrains.java.decompiler.api.LanguageChooser;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.util.Key;
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
  private static final Key<?>[] ANNOTATION_ATTRIBUTES = {
    StructGeneralAttribute.ATTRIBUTE_RUNTIME_VISIBLE_ANNOTATIONS, StructGeneralAttribute.ATTRIBUTE_RUNTIME_INVISIBLE_ANNOTATIONS
  };

  private static final ExtensionRegistryLite EXTENSIONS = ExtensionRegistryLite.newInstance();
  static {
    JvmProtoBuf.registerAllExtensions(EXTENSIONS);
  }

  @Override
  public boolean isLanguage(StructClass cl) {
    // Try to find @Metadata()

    for (Key<?> key : ANNOTATION_ATTRIBUTES) {
      if (cl.hasAttribute(key)) {
        StructAnnotationAttribute attr = cl.getAttribute(key);
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
        StructAnnotationAttribute attr = cl.getAttribute(key);
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
      int k = (int) ((ConstExprent) anno.getParValues().get(kIndex)).getValue();
  
      int d1Index = anno.getParNames().indexOf("d1");
      Exprent d1 = anno.getParValues().get(d1Index);
  
      int d2Index = anno.getParNames().indexOf("d2");
      Exprent d2 = anno.getParValues().get(d2Index);

      String[] data1 = getDataFromExpr((NewExprent) d1);

      String[] data2 = getDataFromExpr((NewExprent) d2);

      byte[] buf = BitEncoding.decodeBytes(data1);

      ByteArrayInputStream input = new ByteArrayInputStream(buf);
      JvmProtoBuf.StringTableTypes types = JvmProtoBuf.StringTableTypes.parseDelimitedFrom(input, EXTENSIONS);

      MetadataNameResolver resolver = new MetadataNameResolver(types, data2);
      DecompilerContext.setProperty(KotlinDecompilationContext.NAME_RESOLVER, resolver);

      if (k == 1) { // Class file
        ProtoBuf.Class pcl = ProtoBuf.Class.parseFrom(input, EXTENSIONS);

//                for (ProtoBuf.Function func : pcl.getFunctionList()) {
//                  System.out.println(func.getFlags());
//                }
//
//                System.out.println(resolver.resolve(pcl.getFqName()));
//
//                for (ProtoBuf.Function func : pcl.getFunctionList()) {
//                  System.out.println(resolver.resolve(func.getName()));
//                }

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
