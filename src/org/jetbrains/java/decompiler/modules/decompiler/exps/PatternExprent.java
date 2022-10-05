package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public abstract class PatternExprent extends Exprent {
  
  protected PatternExprent(){
    super(Type.PATTERN);
  }
  
  public abstract List<VarExprent> getBindings();
  
  public static class TypePatternExprent extends PatternExprent {
    
    private final VarType type;
    protected final VarExprent var;
  
    public TypePatternExprent(VarType type, VarExprent var){
      this.type = type;
      this.var = var;
    }
  
    protected List<Exprent> getAllExprents(List<Exprent> list){
      return Collections.singletonList(var);
    }
  
    public Exprent copy(){
      return new TypePatternExprent(type, var);
    }
  
    public TextBuffer toJava(int indent){
      TextBuffer buffer = new TextBuffer();
      //buffer.addBytecodeMapping(bytecode); // TODO
      buffer.append(ExprProcessor.getCastTypeName(type));
      buffer.append(" ");
      buffer.append(var.getName());
      return buffer;
    }
  
    public void getBytecodeRange(BitSet values){
      measureBytecode(values, var);
      measureBytecode(values);
    }
  
    public List<VarExprent> getBindings(){
      return Collections.singletonList(var);
    }
  }
  
  public static class RecordPatternExprent extends PatternExprent {
    
    private final VarType recordType;
    private final List<PatternExprent> nestedPatterns;
    private final VarExprent name;
  
    public RecordPatternExprent(VarType type, List<PatternExprent> patterns, VarExprent name){
      recordType = type;
      nestedPatterns = patterns;
      this.name = name;
    }
  
    protected List<Exprent> getAllExprents(List<Exprent> list){
      List<Exprent> buffer = new ArrayList<>(nestedPatterns);
      if (name != null) {
        buffer.add(name);
      }
      return buffer;
    }
  
    public Exprent copy(){
      return new RecordPatternExprent(recordType, nestedPatterns, name);
    }
  
    public TextBuffer toJava(int indent){
      TextBuffer buffer = new TextBuffer();
      buffer.append(ExprProcessor.getCastTypeName(recordType));
      buffer.append("(");
      for (int i = 0; i < nestedPatterns.size(); i++) {
        if (i != 0) {
          buffer.append(", ");
        }
        buffer.append(nestedPatterns.get(i).toJava());
      }
      buffer.append(")");
      if (name != null) {
        buffer.append(" ");
        buffer.append(name.getName());
      }
      return buffer;
    }
  
    public void getBytecodeRange(BitSet values) {
      for (PatternExprent p : nestedPatterns) {
        measureBytecode(values, p);
      }
      if (name != null) {
        measureBytecode(values, name);
      }
      measureBytecode(values);
    }
  
    public List<VarExprent> getBindings() {
      List<VarExprent> bindings = new ArrayList<>(nestedPatterns.size() + 1);
      for (PatternExprent p : nestedPatterns) {
        bindings.addAll(p.getBindings());
      }
      if (name != null) {
        bindings.add(name);
      }
      return bindings;
    }
  }
}