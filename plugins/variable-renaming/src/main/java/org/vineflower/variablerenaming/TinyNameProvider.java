package org.vineflower.variablerenaming;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.ImportCollector;
import org.jetbrains.java.decompiler.main.extern.IVariableNameProvider;
import org.jetbrains.java.decompiler.main.extern.IVariableNamingFactory;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.Pair;
import org.jetbrains.java.decompiler.util.TextUtil;

import java.util.*;

// Adapted from https://github.com/FabricMC/tiny-remapper/blob/master/src/main/java/net/fabricmc/tinyremapper/AsmClassRemapper.java
public class TinyNameProvider implements IVariableNameProvider {
  private final HashMap<Integer, String> parameters = new HashMap<>();

  private final StructMethod method;
  private final boolean renameParameters;
  private final Set<String> usedNames = new HashSet<>();

  public TinyNameProvider(boolean renameParameters, StructMethod method) {
    this.renameParameters = renameParameters;
    this.method = method;
  }

  @Override
  public Map<VarVersionPair, String> rename(Map<VarVersionPair, Pair<VarType, String>> entries) {
    int params = 0;
    if ((this.method.getAccessFlags() & CodeConstants.ACC_STATIC) != CodeConstants.ACC_STATIC) {
      params++;
    }

    MethodDescriptor md = MethodDescriptor.parseDescriptor(this.method.getDescriptor());
    for (VarType param : md.params) {
      params += param.stackSize;
    }

    List<VarVersionPair> keys = new ArrayList<>(entries.keySet());
    Collections.sort(keys, (o1, o2) -> (o1.var != o2.var) ? o1.var - o2.var : o1.version - o2.version);

    Map<VarVersionPair, String> result = new LinkedHashMap<>();
    for (VarVersionPair ver : keys) {
      String type = cleanType(entries.get(ver).b);

      if (ver.var >= params) {
        result.put(ver, getNewName(Pair.of(entries.get(ver).a, type)));
      } else if (renameParameters) {
        result.put(ver, this.parameters.computeIfAbsent(ver.var, k -> getNewName(Pair.of(entries.get(ver).a, type))));
      }
    }

    return result;
  }

  private String getNewName(Pair<VarType, String> pair) {
    VarType type = pair.a;
    usedNames.add(pair.b);

    boolean increment = true;
    String name;
    switch (type.type) {
      case BYTECHAR: case SHORTCHAR:
      case INT: name = "i"; break;
      case LONG: name = "l"; break;
      case BYTE: name = "b"; break;
      case SHORT: name = "s"; break;
      case CHAR: name = "c"; break;
      case FLOAT: name = "f"; break;
      case DOUBLE: name = "d"; break;
      case BOOLEAN:
        name = "bl";
        increment = false;
        break;
      case OBJECT:
      case GENVAR:
        name = pair.b;

        // Lowercase first letter
        if (Character.isUpperCase(name.charAt(0))) {
          name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }

        if (type.arrayDim > 0 && !name.endsWith("s")) {
          name += "s";
        }

        increment = false;
        break;
      default: return null;
    }

    if (increment) {
      // Must be of length 1
      int idxStart = name.charAt(0) - 'a';
      while (usedNames.contains(name)) {
        name = convertToName(idxStart++);
      }
    } else {
      // Increment via numbers
      String oname = name;
      int idx = 0;
      while (usedNames.contains(name)) {
        name = oname + (++idx);
      }
    }

    if (TextUtil.isKeyword(name, method.getBytecodeVersion(), method)) {
      name += "_";
    }

    usedNames.add(name);

    return name;
  }

  @Override
  public String renameParameter(int flags, VarType type, String name, int index) {
    String typeName;
    try (var lock = DecompilerContext.getImportCollector().lock()) {
      typeName = ExprProcessor.getCastTypeName(type);
    }
    if (!this.renameParameters) {
      return IVariableNameProvider.super.renameParameter(flags, type, name, index);
    }

    return this.parameters.computeIfAbsent(index, k -> getNewName(Pair.of(type, cleanType(typeName))));
  }

  private static String convertToName(int idx) {
    // Convert to base 26
    String str = Integer.toString(idx, 26);

    // Remap start of name from '0' to 'a'
    StringBuilder res = new StringBuilder();
    for (int i = str.length() - 1; i >= 0; i--) {
      char c = str.charAt(i);
      // If we're numerical, remap to lowercase ascii range
      if (c <= '9') {
        c = (char) ('a' + (c - '0'));
      } else {
        // If we're not, simply shift up 10 ascii characters
        c += 10;
      }

      // TODO: idx 26 starts at 'ba', when it should start at 'aa'
      res.insert(0, c);
    }
    return res.toString();
  }

  private String cleanType(String type) {
    if (type.indexOf('<') != -1) {
      type = type.substring(0, type.indexOf('<'));
    }

    if (type.indexOf('.') != -1) {
      type = type.substring(type.lastIndexOf('.') + 1);
    }

    if (type.indexOf('$') != -1) {
      type = type.substring(type.lastIndexOf('$') + 1);
    }

    type = type.replaceAll("\\[\\]", "");

    return type;
  }

  @Override
  public void addParentContext(IVariableNameProvider renamer) {
    TinyNameProvider prov = (TinyNameProvider) renamer;

    this.usedNames.addAll(prov.usedNames);
  }

  public static class TinyNameProviderFactory implements IVariableNamingFactory {

    @Override
    public IVariableNameProvider createFactory(StructMethod structMethod) {
      return new TinyNameProvider(DecompilerContext.getOption(VariableRenamingOptions.RENAME_PARAMETERS), structMethod);
    }
  }
}
