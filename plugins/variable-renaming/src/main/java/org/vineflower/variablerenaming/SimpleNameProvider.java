package org.vineflower.variablerenaming;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IVariableNameProvider;
import org.jetbrains.java.decompiler.main.extern.IVariableNamingFactory;
import org.jetbrains.java.decompiler.modules.decompiler.vars.VarVersionPair;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;
import org.jetbrains.java.decompiler.struct.gen.VarType;

public class SimpleNameProvider implements IVariableNameProvider {
  private final boolean renameParameters;
  private final StructMethod method;
  private final Map<String, Integer> usedNames = new HashMap<>();
  private final Map<Integer, String> parameters = new HashMap<>();

  public SimpleNameProvider(boolean renameParameters, StructMethod method) {
    this.renameParameters = renameParameters;
    this.method = method;
  }

  @Override
  public Map<VarVersionPair, String> renameVariables(Map<VarVersionPair, VariableNamingData> entries) {
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
      String origName = entries.get(ver).lvt().getName();
      if (origName == null) {
        origName = "lv";
      }
      final String origNameFinal = origName;
      if (ver.var >= params) {
        this.method.getLocalVariableAttr().getMapNames();
        result.put(ver, getNewName(origNameFinal));
      } else if (renameParameters) {
        result.put(ver, this.parameters.computeIfAbsent(ver.var, k -> getNewName(origNameFinal)));
      }
    }

    return result;
  }

  private String getNewName(String name) {
    int timesUsed = this.usedNames.compute(name, (k, v) -> v == null ? 1 : v + 1);
    if (timesUsed == 1) {
      return name;
    }
    return name + (timesUsed - 2);
  }

  @Override
  public String renameParameter(int flags, VarType type, String name, int index) {
    if (!this.renameParameters) {
      return IVariableNameProvider.super.renameParameter(flags, type, name, index);
    }
    return this.parameters.computeIfAbsent(index, k -> getNewName(name));
  }

  @Override
  public void addParentContext(IVariableNameProvider renamer) {
    if (renamer instanceof SimpleNameProvider s) {
      s.usedNames.forEach((k, v) -> this.usedNames.merge(k, v, Integer::sum));
    }
  }

  public static class SimpleNameProviderFactory implements IVariableNamingFactory {

    @Override
    public IVariableNameProvider createFactory(StructMethod structMethod) {
      return new SimpleNameProvider(
        DecompilerContext.getOption(VariableRenamingOptions.RENAME_PARAMETERS), structMethod);
    }
  }
}
