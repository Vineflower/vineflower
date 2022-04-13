// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct;

import org.jetbrains.java.decompiler.api.ClassSet;
import org.jetbrains.java.decompiler.api.DecompilerInput;
import org.jetbrains.java.decompiler.api.Option;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.LegacyDecompilerInput;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger.Severity;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMain;
import org.jetbrains.java.decompiler.struct.gen.generics.GenericMethodDescriptor;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StructContext {
  private final IResultSaver saver;
  private final IDecompiledData decompiledData;
  private final Set<DecompilerInput> inputs = new LinkedHashSet<>();
  private final Map<String, ClassProvider> classes = new HashMap<>();
  private final Map<String, StructClass> ownClasses = new HashMap<>();
  private final Map<String, List<String>> abstractNames = new HashMap<>();

  public StructContext(IResultSaver saver, IDecompiledData decompiledData) {
    this.saver = saver;
    this.decompiledData = decompiledData;
  }

  public StructClass getClass(String name) {
    ClassProvider provider = classes.get(name);
    if (provider == null) {
      return null;
    }
    return provider.get();
  }

  public void reloadContext() throws IOException {
    classes.clear();
    ownClasses.clear();
    for (DecompilerInput input : inputs) {
      ClassSet<?> inputOwnClasses = input.getOwnClasses();
      if (inputOwnClasses != null) {
        addClasses(inputOwnClasses, true);
      }
      for (ClassSet<?> library : input.getLibraries()) {
        addClasses(library, false);
      }
    }
    for (Map.Entry<String, ClassProvider> e : classes.entrySet()) {
      if (e.getValue().own) ownClasses.put(e.getKey(), e.getValue().get());
    }
  }

  private void addClasses(ClassSet<?> set, boolean own) throws IOException {
    set.forEach((name, supplier) -> {
      ClassProvider provider = new ClassProvider(name, own, supplier);
      if (own) name = provider.get().qualifiedName;
      classes.put(name, provider);
    });
  }

  public void saveContext() {
    Set<String> createdFolders = new HashSet<>();
    for (Map.Entry<String, StructClass> cls : ownClasses.entrySet()) {
      StructClass cl = cls.getValue();
      String name = cls.getKey();
      name = name.substring(name.lastIndexOf('/') + 1);
      String entryName = decompiledData.getClassEntryName(cl, name);
      if (entryName != null) {
        String content = decompiledData.getClassContent(cl);
        if (content != null) {
          int[] mapping = null;
          if (DecompilerContext.getOption(Option.BYTECODE_SOURCE_MAPPING)) {
            mapping = DecompilerContext.getBytecodeSourceMapper().getOriginalLinesMapping();
          }
          String folder = entryName.substring(0, Math.max(entryName.lastIndexOf('/'), 0));
          if (createdFolders.add(folder)) {
            saver.saveFolder(folder);
          }
          // TODO: path
          saver.saveClassFile("", cl.qualifiedName, entryName, content, mapping);
        }
      }
    }
  }

  public void addInput(DecompilerInput input) {
    this.inputs.add(input);
  }

  public void removeInput(DecompilerInput input) {
    this.inputs.remove(input);
  }

  public void addSpace(File file, boolean isOwn) {
    for (DecompilerInput input : inputs) {
      if (input instanceof LegacyDecompilerInput) {
        ((LegacyDecompilerInput) input).addSpace(file, isOwn);
        return;
      }
    }
    throw new IllegalStateException("Cannot add new files to non-legacy decompiler input");
  }

  public void addData(String path, String cls, byte[] data, boolean isOwn) throws IOException {
    /*
    ContextUnit unit = units.get(path);
    if (unit == null) {
      unit = new ContextUnit(ContextUnit.TYPE_FOLDER, path, cls, isOwn, saver, decompiledData);
      units.put(path, unit);
    }

    addClass(unit, cls.substring(0, cls.length() - 6), path, cls, isOwn, () -> data);
    */
  }

  public Map<String, StructClass> getOwnClasses() {
    return ownClasses;
  }

  public boolean hasClass(String name) {
    return classes.containsKey(name);
  }

  public boolean instanceOf(String valclass, String refclass) {
    if (valclass.equals(refclass)) {
      return true;
    }

    StructClass cl = this.getClass(valclass);
    if (cl == null) {
      return false;
    }

    if (cl.superClass != null && this.instanceOf(cl.superClass.getString(), refclass)) {
      return true;
    }

    int[] interfaces = cl.getInterfaces();
    for (int i = 0; i < interfaces.length; i++) {
      String intfc = cl.getPool().getPrimitiveConstant(interfaces[i]).getString();

      if (this.instanceOf(intfc, refclass)) {
        return true;
      }
    }

    return false;
  }

  public StructClass getFirstCommonClass(String firstclass, String secondclass) {
    StructClass fcls = this.getClass(firstclass);
    StructClass scls = this.getClass(secondclass);

    if (fcls != null && scls != null) {
      List<StructClass> clsList = scls.getAllSuperClasses();
      while (fcls != null) {
        if (clsList.contains(fcls)) {
          return fcls;
        }

        fcls = fcls.superClass == null ? null : this.getClass(fcls.superClass.getString());
      }
    }

    return null;
  }

  public void loadAbstractMetadata(String string) {
    for (String line : string.split("\n")) {
      String[] pts = line.split(" ");
      if (pts.length < 4) //class method desc [args...]
        continue;
      GenericMethodDescriptor desc = GenericMain.parseMethodSignature(pts[2]);
      List<String> params = new ArrayList<>();
      for (int x = 0; x < pts.length - 3; x++) {
        for (int y = 0; y < desc.parameterTypes.get(x).stackSize; y++)
            params.add(pts[x+3]);
      }
      this.abstractNames.put(pts[0] + ' '+ pts[1] + ' ' + pts[2], params);
    }
  }

  public String renameAbstractParameter(String className, String methodName, String descriptor, int index, String _default) {
    List<String> params = this.abstractNames.get(className + ' ' + methodName + ' ' + descriptor);
    return params != null && index < params.size() ? params.get(index) : _default;
  }

  public static class ClassProvider {
    private final String name;
    private volatile ClassSet.ClassSupplier supplier;
    private final boolean own;
    private StructClass value;

    ClassProvider(String name, boolean own, ClassSet.ClassSupplier supplier) {
      this.name = name;
      this.own = own;
      this.supplier = supplier;
    }

    ClassProvider(StructClass value) {
      this.name = null;
      this.supplier = null;
      this.own = value.isOwn();
      this.value = value;
    }

    public StructClass get() {
      StructClass v = value;
      if (v != null) return v;
      synchronized (this) {
        if (supplier == null) return value;
        try {
          DecompilerContext.getLogger().writeMessage("  Loading Class: " + name, Severity.INFO);
          byte[] data = supplier.get();
          StructClass cl = StructClass.create(new DataInputFullStream(data), own);
          value = cl;
          supplier = null;
          return cl;
        } catch (IOException ex) {
          String message = "Corrupted class file: " + name;
          DecompilerContext.getLogger().writeMessage(message, ex);
          throw new RuntimeException(ex);
        }
      }
    }
  }
}
