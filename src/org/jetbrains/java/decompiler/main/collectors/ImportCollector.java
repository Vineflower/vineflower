// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main.collectors;

import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructContext;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.attr.StructGeneralAttribute;
import org.jetbrains.java.decompiler.struct.attr.StructInnerClassesAttribute;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.*;
import java.util.stream.Collectors;

public class ImportCollector {
  private static final String JAVA_LANG_PACKAGE = "java.lang";

  protected final Map<String, String> mapSimpleNames = new HashMap<>();
  protected final Set<String> setNotImportedNames = new HashSet<>();
  // set of field names in this class and all its predecessors.
  protected final Set<String> setFieldNames = new HashSet<>();
  protected final Map<String, Map<String, String>> mapInnerClassNames = new HashMap<>();
  protected final String currentPackageSlash;
  protected final String currentPackagePoint;
  protected boolean writeLocked = false;

  public ImportCollector(ClassNode root) {
    String clName = root.classStruct.qualifiedName;
    int index = clName.lastIndexOf('/');
    if (index >= 0) {
      String packageName = clName.substring(0, index);
      currentPackageSlash = packageName + '/';
      currentPackagePoint = packageName.replace('/', '.');
    }
    else {
      currentPackageSlash = "";
      currentPackagePoint = "";
    }

    Set<StructClass> processedClasses = new HashSet<>();
    StructContext ctx = DecompilerContext.getStructContext();
    StructClass currentClass = root.classStruct;
    while (currentClass != null) {
      processedClasses.add(currentClass);
      // all field names for the current class ..
      for (StructField f : currentClass.getFields()) {
        setFieldNames.add(f.getName());
      }

      // .. and traverse through parent.
      currentClass = currentClass.superClass != null ? ctx.getClass(currentClass.superClass.getString()) : null;

      // Class already processed, skipping.

      // This may be sign of circularity in the class hierarchy but in most cases this mean that same interface
      // are listed as implemented several times in the class hierarchy.
      if (currentClass != null && processedClasses.contains(currentClass)) {
        currentClass = null;
      }
    }

    collectConflictingShortNames(root, new HashMap<>());
  }
  
  public ImportCollector(ImportCollector other) {
    this.mapSimpleNames.putAll(other.mapSimpleNames);
    this.setNotImportedNames.addAll(other.setNotImportedNames);
    this.setFieldNames.addAll(other.setFieldNames);
    this.mapInnerClassNames.putAll(other.mapInnerClassNames);
    this.currentPackageSlash = other.currentPackageSlash;
    this.currentPackagePoint = other.currentPackagePoint;
    this.writeLocked = other.writeLocked;
  }

  /**
   * Check whether the package-less name ClassName is shaded by variable in a context of
   * the decompiled class
   * @param classToName - pkg.name.ClassName - class to find shortname for
   * @return ClassName if the name is not shaded by local field, pkg.name.ClassName otherwise
   */
  public String getShortNameInClassContext(String classToName) {
    String shortName = getShortName(classToName);
    if (shortName == null) {
      return "<unknownclass>";
    }
    
    if (setFieldNames.contains(shortName)) {
      return classToName;
    }
    else {
      return shortName;
    }
  }

  public String getShortName(String fullName) {
    return getShortName(fullName, true);
  }

  public String getShortName(String fullName, boolean imported) {
    ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(fullName.replace('.', '/')); //todo[r.sh] anonymous classes?

    String result = null;
    if (node != null && node.classStruct.isOwn()) {
      result = node.simpleName;

      while (node.parent != null && node.parent.simpleName != null && node.type == ClassNode.Type.MEMBER) {
        //noinspection StringConcatenationInLoop
        result = node.parent.simpleName + '.' + result;
        node = node.parent;
      }

      if (node.type == ClassNode.Type.ROOT) {
        fullName = node.classStruct.qualifiedName;
        fullName = fullName.replace('/', '.');
      } else {
        if (result == null && node.type == ClassNode.Type.ANONYMOUS) {
          result = ExprProcessor.UNREPRESENTABLE_TYPE_STRING;
        }
        return result;
      }
    }
    else {
      fullName = fullName.replace('$', '.');
    }

    String shortName = fullName;
    String packageName = "";

    int lastDot = fullName.lastIndexOf('.');
    if (lastDot >= 0) {
      shortName = fullName.substring(lastDot + 1);
      packageName = fullName.substring(0, lastDot);
    }

    StructContext context = DecompilerContext.getStructContext();

    // check for another class which could 'shadow' this one. Three cases:
    // 1) class with the same short name in the current package
    // 2) class with the same short name in the default package
    // 3) inner class with the same short name in the current class, a super class, or an implemented interface
    boolean existsDefaultClass =
      (context.getClass(currentPackageSlash + shortName) != null && !packageName.equals(currentPackagePoint)) || // current package
      (context.getClass(shortName) != null && !currentPackagePoint.isEmpty());

    ClassNode currCls = (ClassNode)DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS_NODE);
    String mapKey = currCls == null ? "" : currCls.classStruct.qualifiedName;
    Map<String, String> innerClassNames = mapInnerClassNames.getOrDefault(mapKey, new HashMap<>());
    if (!existsDefaultClass && innerClassNames.containsKey(shortName) && !innerClassNames.get(shortName).equals(fullName)) {
      // if the class being accessed is also an inner class
      // attempt to import the outer class and reference OuterClass.InnerClass
      if (context.getClass(packageName.replace('.', '/') + "$" + shortName) != null) {
        lastDot = fullName.lastIndexOf(".", lastDot - 1);
        if (lastDot >= 0) {
          result = fullName.substring(lastDot + 1);
          shortName = packageName.substring(lastDot + 1);
          packageName = packageName.substring(0, lastDot);

          if (innerClassNames.containsKey(shortName)  && !innerClassNames.get(shortName).equals(packageName + '.' + shortName)) {
            existsDefaultClass = true;
            result = null;
          }
        }
      }
      else {
        existsDefaultClass = true;
      }
    }

    if (existsDefaultClass ||
        (mapSimpleNames.containsKey(shortName) && !packageName.equals(mapSimpleNames.get(shortName)))) {
      //  don't return full name because if the class is a inner class, full name refers to the parent full name, not the child full name
      return result == null ? fullName : ((!packageName.isEmpty() ? (packageName + ".") : "") + result);
    }
    else if (!mapSimpleNames.containsKey(shortName)) {
      if (!this.writeLocked) {
        mapSimpleNames.put(shortName, packageName);
        if (!imported) {
          setNotImportedNames.add(shortName);
        }
      }
    }

    return result == null ? shortName : result;
  }

  public void writeImports(TextBuffer buffer, boolean addSeparator) {
    if (DecompilerContext.getOption(IFernflowerPreferences.REMOVE_IMPORTS)) {
      return;
    }

    List<String> imports = packImports();
    for (String line : imports) {
      buffer.append("import ").append(line).append(';').appendLineSeparator();
    }
    if (addSeparator && !imports.isEmpty()) {
      buffer.appendLineSeparator();
    }
  }

  protected List<String> packImports() {
    return mapSimpleNames.entrySet().stream()
      .filter(this::keepImport)
      .sorted(Map.Entry.<String, String>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
      .map(ent -> ent.getValue() + "." + ent.getKey())
      .collect(Collectors.toList());
  }

  /**
   * Check whether to keep the given entry in the import list.
   *
   * @param ent the entry in the map containing the class name and its corresponding package name
   * @return true if the entry should be kept for importing, false otherwise
   */
  protected boolean keepImport(Map.Entry<String, String> ent) {
    return !setNotImportedNames.contains(ent.getKey()) &&
           !ent.getValue().isEmpty() &&
           !JAVA_LANG_PACKAGE.equals(ent.getValue()) &&
           !ent.getValue().equals(currentPackagePoint);
  }

  private void collectConflictingShortNames(ClassNode root, Map<String, String> rootNames) {
    Map<String, String> names = new HashMap<>(rootNames);
    getSuperClassInnerClasses(root, names);
    mapInnerClassNames.put(root.classStruct.qualifiedName, names);

    for (ClassNode nested : root.nested) {
      collectConflictingShortNames(nested, names);
    }
  }

  private void getSuperClassInnerClasses(ClassNode node, Map<String, String> names) {
    StructContext ctx = DecompilerContext.getStructContext();
    Set<StructClass> processedClasses = new HashSet<>();
    LinkedList<String> queue = new LinkedList<>();
    StructClass currentClass = node.classStruct;
    while (currentClass != null) {
      processedClasses.add(currentClass);
      if (currentClass.superClass != null) {
        queue.add(currentClass.superClass.getString());
      }

      Collections.addAll(queue, currentClass.getInterfaceNames());

      // .. all inner classes for the current class ..
      StructInnerClassesAttribute attribute = currentClass.getAttribute(StructGeneralAttribute.ATTRIBUTE_INNER_CLASSES);
      if (attribute != null) {
        for (StructInnerClassesAttribute.Entry entry : attribute.getEntries()) {
          if (entry.enclosingName != null && entry.enclosingName.equals(currentClass.qualifiedName)) {
            names.put(entry.simpleName, entry.innerName.replace('/', '.').replace('$', '.'));
          }
        }
      }

      // .. and traverse through parent.
      do {
        currentClass = queue.isEmpty() ? null : ctx.getClass(queue.removeFirst());

        if (currentClass != null && processedClasses.contains(currentClass)) {
          // Class already processed, skipping.

          // This may be sign of circularity in the class hierarchy but in most cases this mean that same interface
          // are listed as implemented several times in the class hierarchy.
          currentClass = null;
        }
      } while (currentClass == null && !queue.isEmpty());
    }
  }

  private void setWriteLocked(boolean writeLocked) {
    this.writeLocked = writeLocked;
  }

  public Lock lock() {
    return new Lock();
  }

  public class Lock implements AutoCloseable {
    private final boolean target;
    private Lock() {
      this.target = writeLocked;
      setWriteLocked(true);
    }

    @Override
    public void close() {
      setWriteLocked(target);
    }
  }
}