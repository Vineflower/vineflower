package org.vineflower.unpick;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.api.plugin.pass.ClassPass;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.ConstExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.jetbrains.java.decompiler.util.Key;
import org.jetbrains.java.decompiler.util.Pair;
import org.vineflower.unpick.parser.UnpickV3Reader;
import org.vineflower.unpick.parser.tree.DataType;
import org.vineflower.unpick.parser.tree.GroupFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnpickPass implements ClassPass {
  private static final Key<UnpickDefinitions> DEFINITIONS_KEY = Key.of("unpick-definitions");

  @Override
  public boolean run(ClassesProcessor.ClassNode classNode) {
    UnpickDefinitions defs = loadDefinitions();
    if (defs.isEmpty()) {
      return false;
    }

    Map<Integer, String> exprentGroups = new HashMap<>();
    Map<Pair<String, VarVersionPair>, String> varGroups = new HashMap<>();

    identifyTargets(defs, classNode, exprentGroups, varGroups);
    while (propagateGroups(defs, classNode, exprentGroups, varGroups)) {
      // keep propagating while changed
    }
    return substituteConstants(defs, classNode, exprentGroups);
  }

  private static void identifyTargets(
    UnpickDefinitions defs,
    ClassesProcessor.ClassNode classNode,
    Map<Integer, String> exprentGroups,
    Map<Pair<String, VarVersionPair>, String> varGroups
  ) {
    // TODO
  }

  private static boolean propagateGroups(
    UnpickDefinitions defs,
    ClassesProcessor.ClassNode classNode,
    Map<Integer, String> exprentGroups,
    Map<Pair<String, VarVersionPair>, String> varGroups
  ) {
    // TODO
    return false;
  }

  private static boolean substituteConstants(
    UnpickDefinitions defs,
    ClassesProcessor.ClassNode classNode,
    Map<Integer, String> exprentGroups
  ) {
    boolean res = false;

    for (MethodWrapper method : classNode.getWrapper().getMethods()) {
      res |= substituteConstants(defs, method, method.root, exprentGroups);
    }
    for (ClassesProcessor.ClassNode nested : classNode.nested) {
      res |= substituteConstants(defs, nested, exprentGroups);
    }

    return res;
  }

  private static boolean substituteConstants(
    UnpickDefinitions defs,
    MethodWrapper method,
    Statement stat,
    Map<Integer, String> exprentGroups
  ) {
    boolean res = false;

    if (stat.getExprents() == null) {
      for (Exprent exprent : stat.getStatExprents()) {
        res |= substituteConstants(defs, method, exprent, newExprent -> stat.replaceExprent(exprent, newExprent), exprentGroups);
      }

      for (Statement st : stat.getStats()) {
        res |= substituteConstants(defs, method, st, exprentGroups);
      }
    } else {
      for (Exprent exprent : stat.getExprents()) {
        res |= substituteConstants(defs, method, exprent, newExprent -> stat.replaceExprent(exprent, newExprent), exprentGroups);
      }
    }

    return res;
  }

  // TODO: handle casting to the right type when overload resolution is affected
  private static boolean substituteConstants(
    UnpickDefinitions defs,
    MethodWrapper method,
    Exprent exprent,
    Consumer<Exprent> replace,
    Map<Integer, String> exprentGroups
  ) {
    boolean res = false;

    for (Exprent subExprent : exprent.getAllExprents()) {
      res |= substituteConstants(defs, method, subExprent, newExprent -> exprent.replaceExprent(subExprent, newExprent), exprentGroups);
    }

    Object constantValue = computeConstantValue(exprent);
    DataType constantType = DataTypeUtils.getDataType(constantValue);

    String groupName = exprentGroups.get(exprent.id);
    List<UnpickDefinitions.GroupData> groupDatas = getGroupData(defs, groupName, getExprType(exprent));

    GroupFormat bestFormat = null;
    for (UnpickDefinitions.GroupData groupData : groupDatas) {
      for (UnpickDefinitions.ScopedData scopedData : getScopedData(groupData, method.methodStruct)) {
        if (bestFormat == null) {
          bestFormat = scopedData.format;
        }

        // TODO: flags
        UnpickDefinitions.ConstantReplacementData replacementData = scopedData.constantReplacementMap.get(constantValue);
        if (replacementData != null) {
          boolean compatibleTypes = replacementData.strict() ? groupData.dataType == constantType : DataTypeUtils.isAssignable(constantType, groupData.dataType);
          if (compatibleTypes) {
            ExpressionToExprent converter = new ExpressionToExprent(method, exprent.bytecode);
            replacementData.replacementExpr().accept(converter);
            replace.accept(converter.result);
            return true;
          }
        }
      }
    }

    if (bestFormat != null && exprent instanceof ConstExprent constExprent) {
      if (bestFormat == GroupFormat.CHAR) {
        if (constantType == DataType.INT && !constExprent.getConstType().equals(VarType.VARTYPE_CHAR)) {
          constExprent.setConstType(VarType.VARTYPE_CHAR);
          res = true;
        }
      } else {
        int radix = switch (bestFormat) {
          case BINARY -> 2;
          case OCTAL -> 8;
          case DECIMAL -> 10;
          case HEX -> 16;
          default -> throw new IllegalStateException("Unknown group format");
        };
        if (constExprent.getConstType().equals(VarType.VARTYPE_CHAR)) {
          constExprent.setConstType(VarType.VARTYPE_INT);
          res = true;
        }
        if (constExprent.getRadix() != radix) {
          constExprent.setRadix(radix);
          res = true;
        }
      }
    }

    return res;
  }

  // TODO: other types of exprent listed in the spec, including unary minus
  @Nullable
  private static Object computeConstantValue(Exprent exprent) {
    if (exprent instanceof ConstExprent constExprent) {
      if (constExprent.getConstType().equals(VarType.VARTYPE_CLASS)) {
        return new ClassLiteral(constExprent.getValue().toString());
      } else {
        return constExprent.getValue();
      }
    }

    return null;
  }

  private static VarType getExprType(Exprent exprent) {
    VarType exprType = exprent.getExprType();
    return VarType.UNBOXING_TYPES.getOrDefault(exprType, exprType);
  }

  private static List<UnpickDefinitions.GroupData> getGroupData(UnpickDefinitions defs, @Nullable String groupName, VarType varType) {
    if (groupName != null) {
      UnpickDefinitions.GroupData group = defs.groups.get(groupName);
      return group == null ? List.of() : List.of(group);
    }

    DataType dataType = DataTypeUtils.fromVarType(varType);
    if (dataType == null) {
      return List.of();
    }
    List<DataType> narrowedTypes = switch (dataType) {
      case LONG -> List.of(DataType.LONG, DataType.INT);
      case FLOAT -> List.of(DataType.FLOAT, DataType.LONG, DataType.INT);
      case DOUBLE -> List.of(DataType.DOUBLE, DataType.FLOAT, DataType.LONG, DataType.INT);
      case CHAR -> List.of(DataType.INT);
      default -> List.of(dataType);
    };

    List<UnpickDefinitions.GroupData> result = new ArrayList<>(narrowedTypes.size());
    for (DataType narrowedType : narrowedTypes) {
      UnpickDefinitions.GroupData defaultGroup = defs.defaultGroups.get(narrowedType);
      if (defaultGroup != null) {
        result.add(defaultGroup);
      }
    }
    return result;
  }

  // TODO: handle lambdas and anonymous classes
  private static List<UnpickDefinitions.ScopedData> getScopedData(UnpickDefinitions.GroupData groupData, StructMethod mt) {
    List<UnpickDefinitions.ScopedData> result = new ArrayList<>(4);

    UnpickDefinitions.ScopedData methodData = groupData.methodScopes.get(InterpreterUtil.makeUniqueKey(mt.getClassQualifiedName(), mt.getName(), mt.getDescriptor()));
    if (methodData != null) {
      result.add(methodData);
    }

    UnpickDefinitions.ScopedData classData = groupData.classScopes.get(mt.getClassQualifiedName());
    if (classData != null) {
      result.add(classData);
    }

    if (mt.getClassQualifiedName().contains("/")) {
      UnpickDefinitions.ScopedData packageData = groupData.packageScopes.get(mt.getClassQualifiedName().substring(0, mt.getClassQualifiedName().lastIndexOf('/')));
      if (packageData != null) {
        result.add(packageData);
      }
    }

    result.add(groupData.globalScope);
    return result;
  }

  // Loading in Plugin.initialize is too early, we need StructContext to be populated
  private static UnpickDefinitions loadDefinitions() {
    UnpickDefinitions defs = DecompilerContext.getContextProperty(DEFINITIONS_KEY);
    if (defs != null) {
      return defs;
    }

    defs = new UnpickDefinitions();
    DecompilerContext.setProperty(DEFINITIONS_KEY, defs);

    try {
      loadBuiltInDefinitions(defs);
      defs.canBeOverwritten = false;
      loadCustomDefinitions(defs);
    } catch (IOException e) {
      DecompilerContext.getLogger().writeMessage("Failed to load unpick definitions", e);
    }

    return defs;
  }

  private static void loadBuiltInDefinitions(UnpickDefinitions defs) throws IOException {
    if (!DecompilerContext.getOption(UnpickOptions.USE_BUILTIN_UNPICK_FILES)) {
      return;
    }

    try (InputStream jdkUnpick = UnpickPass.class.getResourceAsStream("/jdk.unpick")) {
      if (jdkUnpick == null) {
        throw new IOException("Could not find built-in unpick definitions");
      }

      new UnpickV3Reader(new InputStreamReader(jdkUnpick)).accept(defs);
    }
  }

  private static void loadCustomDefinitions(UnpickDefinitions defs) throws IOException {
    String unpickFiles = (String) DecompilerContext.getProperty(UnpickOptions.UNPICK_FILES);
    if (unpickFiles.isEmpty()) {
      return;
    }

    loadCustomDefinitions(Path.of(unpickFiles), defs);
  }

  private static void loadCustomDefinitions(Path path, UnpickDefinitions defs) throws IOException {
    if (Files.isDirectory(path)) {
      Files.walkFileTree(path, new SimpleFileVisitor<>() {
        @Override
        @NotNull
        public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
          loadCustomDefinitions(file, defs);
          return FileVisitResult.CONTINUE;
        }
      });
    } else {
      String fileName = path.getFileName().toString();
      if (fileName.endsWith(".unpick")) {
        try (UnpickV3Reader reader = new UnpickV3Reader(Files.newBufferedReader(path))) {
          reader.accept(defs);
        }
      } else if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
        try (ZipFile zip = new ZipFile(path.toFile())) {
          Enumeration<? extends ZipEntry> entries = zip.entries();
          while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".unpick")) {
              try (InputStream stream = zip.getInputStream(entry)) {
                new UnpickV3Reader(new InputStreamReader(stream)).accept(defs);
              }
            }
          }
        }
      }
    }
  }
}
