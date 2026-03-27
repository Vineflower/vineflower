package org.vineflower.kotlin;

import org.vineflower.kotlin.struct.*;
import org.vineflower.kt.metadata.ProtoBuf;
import org.vineflower.kt.metadata.deserialization.Flags;
import org.vineflower.kt.metadata.jvm.JvmProtoBuf;
import org.vineflower.kt.protobuf.ExtensionRegistryLite;
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
import java.util.Arrays;
import java.util.List;

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
    boolean decompEnabled = DecompilerContext.getOption(KotlinOptions.DECOMPILE_KOTLIN);
    if (!decompEnabled && !DecompilerContext.getOption(KotlinOptions.ALWAYS_EXPORT_METADATA)) {
      return false;
    }

    // Try to find @Metadata()
    for (Key<?> key : ANNOTATION_ATTRIBUTES) {
      if (cl.hasAttribute(key)) {
        StructAnnotationAttribute attr = cl.getAttribute((Key<StructAnnotationAttribute>) key);
        for (AnnotationExprent anno : attr.getAnnotations()) {
          if (anno.getClassName().equals("kotlin/Metadata")) {
            parseMetadataFor(cl);
            return decompEnabled;
          }
        }
      }
    }

    return false;
  }

  public static void parseMetadataFor(StructClass cl) {
    if (cl.getAttribute(KElement.KEY) != null) {
      return;
    }

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
        DecompilerContext.getLogger().writeMessage("No k attribute (type) in class metadata for class " + cl.qualifiedName + ", cannot continue Kotlin parsing", IFernflowerLogger.Severity.WARN);
        return;
      }

      int k = (int) ((ConstExprent) anno.getParValues().get(kIndex)).getValue();

      if (d1Index == -1) {
        boolean functionRef = cl.superClass.getString().equals("kotlin/jvm/internal/FunctionReferenceImpl");
        if (functionRef) {
          cl.getAttributes().put(KElement.KEY, new KFunctionReference());
        }

        String resolution = functionRef ? "assuming function reference" : "cannot continue Kotlin parsing";
        DecompilerContext.getLogger().writeMessage("No d1 attribute (data) in class metadata for class " + cl.qualifiedName + ", " + resolution, IFernflowerLogger.Severity.WARN);
        return;
      }

      if (d2Index == -1) {
        DecompilerContext.getLogger().writeMessage("No d2 attribute (strings) in class metadata for class " + cl.qualifiedName, IFernflowerLogger.Severity.WARN);
      }

      Exprent d1 = anno.getParValues().get(d1Index);
      Exprent d2 = d2Index != -1 ? anno.getParValues().get(d2Index) : null;

      String[] data1 = getDataFromExpr((NewExprent) d1);

      byte[] buf = BitEncoding.decodeBytes(data1);

      ByteArrayInputStream input = new ByteArrayInputStream(buf);

      MetadataNameResolver resolver;
      if (d2 != null) {
        String[] data2 = getDataFromExpr((NewExprent) d2);
        JvmProtoBuf.StringTableTypes types = JvmProtoBuf.StringTableTypes.parseDelimitedFrom(input, EXTENSIONS);
        resolver = new MetadataNameResolver(types, data2);
      } else {
        resolver = null;
      }

      if (k == 1) { // Class file
        ProtoBuf.Class pcl = ProtoBuf.Class.parseFrom(input, EXTENSIONS);
        KProperty.parse(cl, pcl.getPropertyList(), resolver, null);
        KFunction.parse(cl, resolver, pcl.getTypeTable(), pcl.getFunctionList(), false, null);
        KConstructor.parse(cl, pcl, resolver);
        cl.getAttributes().put(KElement.KEY, new KClass(pcl, resolver));
      } else if (k == 2) { // File facade
        ProtoBuf.Package pcl = ProtoBuf.Package.parseFrom(input, EXTENSIONS);
        KProperty.parse(cl, pcl.getPropertyList(), resolver, null);
        KFunction.parse(cl, resolver, pcl.getTypeTable(), pcl.getFunctionList(), false, null);
        cl.getAttributes().put(KElement.KEY, new KFile(pcl, resolver, null));
      } else if (k == 3) { // Synthetic class
        ProtoBuf.Function func = ProtoBuf.Function.parseFrom(input, EXTENSIONS);
        KFunction.parse(cl, resolver, func.getTypeTable(), List.of(func), true, null);
        if (!cl.hasAttribute(KElement.KEY)) {
          cl.getAttributes().put(KElement.KEY, KFunction.FAILED_LAMBDA);
        }
      } else if (k == 4) { // Multi-file facade
        cl.getAttributes().put(KElement.KEY, new KMultifileFacade(Arrays.asList(data1)));
      } else if (k == 5) { // Multi-file part
        ProtoBuf.Package pcl = ProtoBuf.Package.parseFrom(input, EXTENSIONS);
        KProperty.parse(cl, pcl.getPropertyList(), resolver, null);
        KFunction.parse(cl, resolver, pcl.getTypeTable(), pcl.getFunctionList(), false, null);

        String multifileName;
        int xsIndex = anno.getParNames().indexOf("xs");
        if (xsIndex != -1) {
          multifileName = (String) ((ConstExprent) anno.getParValues().get(xsIndex)).getValue();
        } else {
          DecompilerContext.getLogger().writeMessage("No xs attribute (extra string; multifile part name) in class metadata for class " + cl.qualifiedName, IFernflowerLogger.Severity.WARN);
          multifileName = cl.qualifiedName.split("__")[0];
        }

        cl.getAttributes().put(KElement.KEY, new KFile(pcl, resolver, multifileName));
      }
    } catch (Exception e) {
      DecompilerContext.getLogger().writeMessage("Failed to parse metadata for class " + cl.qualifiedName, IFernflowerLogger.Severity.WARN, e);
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
