package org.vineflower.kotlin.util;

import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.NewExprent;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.vineflower.kotlin.KotlinChooser;
import org.vineflower.kotlin.struct.*;
import org.vineflower.kt.metadata.ProtoBuf;
import org.vineflower.kt.metadata.deserialization.Flags;

import java.util.Optional;

public class KotlinObjectProcessors {
  public static void parseWithCompanion(ClassesProcessor.ClassNode node) {
    ClassWrapper wrapper = node.getWrapper();
    StructClass cl = node.classStruct;

    KElement ktData = cl.getAttribute(KElement.KEY);
    if (ktData instanceof KClass cls && cls.proto().hasCompanionObjectName()) {
      String name = cls.resolver().resolve(cls.proto().getCompanionObjectName());
      Optional<ClassesProcessor.ClassNode> companion = node.nested.stream()
        .filter(n -> n.simpleName.equals(name))
        .findAny();
      if (companion.isPresent()) {
        // Re-parse companion data to associate non-companion entries
        StructClass companionStruct = companion.get().classStruct;
        KotlinChooser.parseMetadataFor(companionStruct);
        KElement companionData = companionStruct.getAttribute(KElement.KEY);
        if (!(companionData instanceof KClass companionCls)) {
          throw new IllegalStateException("Companion object of " + cl.qualifiedName + " is not a class");
        }
        KFunction.parse(companionStruct, companionCls.resolver(), companionCls.proto().getTypeTable(), companionCls.proto().getFunctionList(), false, cl);
        KProperty.parse(companionStruct, companionCls.proto().getPropertyList(), companionCls.resolver(), cl);

        // Mark the companion field as hidden
        for (StructField fd : cl.getFields()) {
          if (fd.getDescriptor().equals("L" + companionStruct.qualifiedName + ";")) {
            String key = InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor());
            Exprent initializer = wrapper.getStaticFieldInitializers().getWithKey(key);
            if (initializer instanceof NewExprent) {
              // Likely companion object - no other construction should exist for a companion
              fd.getAttributes().put(KElement.KEY, KHiddenElement.COMPANION_INSTANCE);
              break;
            }
          }
        }
      }
    }
  }

  public static void markObjectInstance(ClassWrapper wrapper) {
    StructClass cl = wrapper.getClassStruct();
    KElement ktData = cl.getAttribute(KElement.KEY);

    if (!(ktData instanceof KClass cls)) {
      return;
    }

    if (Flags.CLASS_KIND.get(cls.flags()) != ProtoBuf.Class.Kind.OBJECT) {
      return;
    }

    for (StructField fd : cl.getFields()) {
      if (fd.getDescriptor().equals("L" + cl.qualifiedName + ";")) {
        String key = InterpreterUtil.makeUniqueKey(fd.getName(), fd.getDescriptor());
        Exprent initializer = wrapper.getStaticFieldInitializers().getWithKey(key);
        if (initializer instanceof NewExprent) {
          fd.getAttributes().put(KElement.KEY, KHiddenElement.OBJECT_INSTANCE);
          break;
        }
      }
    }
  }
}
