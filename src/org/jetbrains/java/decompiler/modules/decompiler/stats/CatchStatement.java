// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.stats;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.modules.decompiler.DecHelper;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

public class CatchStatement extends Statement {
  public static final class CatchEntry {
    private final List<String> excStrings;
    private VarExprent var;
    private Statement stat;

    public CatchEntry(List<String> excStrings, VarExprent var, Statement stat) {
      this.excStrings = excStrings;
      this.var = var;
      this.stat = stat;
    }

    public List<String> excStrings() {
      return excStrings;
    }

    public VarExprent var() {
      return var;
    }

    public Statement stat() {
      return stat;
    }

    public void setVar(VarExprent var) {
      this.var = var;
    }

    public void setStat(Statement stat) {
      this.stat = stat;
    }
  }

  private final List<CatchEntry> catchEntries = new ArrayList<>();
  private final List<Exprent> resources = new ArrayList<>();

  // *****************************************************************************
  // constructors
  // *****************************************************************************

  protected CatchStatement() {
    super(StatementType.TRY_CATCH);
  }

  protected CatchStatement(Statement head, @Nullable Statement next, Set<Statement> setHandlers) {
    this();

    first = head;
    stats.addWithKey(first, first.id);

    // TODO: make setHandlers ordered
    for (StatEdge edge : head.getSuccessorEdges(StatEdge.TYPE_EXCEPTION)) {
      Statement stat = edge.getDestination();

      if (setHandlers.contains(stat)) {
        stats.addWithKey(stat, stat.id);
        catchEntries.add(new CatchEntry(
          new ArrayList<>(edge.getExceptions()),
          new VarExprent(DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.VAR_COUNTER),
            new VarType(CodeType.OBJECT, 0, edge.getExceptions().get(0)),
            // FIXME: for now simply the first type. Should get the first common superclass when possible.
            DecompilerContext.getVarProcessor()),
          stat
        ));
      }
    }

    if (next != null) {
      post = next;
    }
  }

  // *****************************************************************************
  // public methods
  // *****************************************************************************

  public static @Nullable Statement isHead(Statement head) {
    if (head.getLastBasicType() != LastBasicType.GENERAL) {
      return null;
    }

    Set<Statement> setHandlers = DecHelper.getUniquePredExceptions(head);
    if (!setHandlers.isEmpty()) {
      int hnextcount = 0; // either no statements with connection to next, or more than 1

      Statement next = null;
      List<StatEdge> lstHeadSuccs = head.getSuccessorEdges(STATEDGE_DIRECT_ALL);
      if (!lstHeadSuccs.isEmpty() && lstHeadSuccs.get(0).getType() == StatEdge.TYPE_REGULAR) {
        next = lstHeadSuccs.get(0).getDestination();
        hnextcount = 2;
      }

      for (StatEdge edge : head.getSuccessorEdges(StatEdge.TYPE_EXCEPTION)) {
        Statement stat = edge.getDestination();

        boolean handlerok = true;

        if (edge.getExceptions() != null && setHandlers.contains(stat)) {
          if (stat.getLastBasicType() != LastBasicType.GENERAL) {
            handlerok = false;
          } else {
            List<StatEdge> lstStatSuccs = stat.getSuccessorEdges(STATEDGE_DIRECT_ALL);
            if (!lstStatSuccs.isEmpty() && lstStatSuccs.get(0).getType() == StatEdge.TYPE_REGULAR) {

              Statement statn = lstStatSuccs.get(0).getDestination();

              if (next == null) {
                next = statn;
              } else if (next != statn) {
                handlerok = false;
              }

              if (handlerok) {
                hnextcount++;
              }
            }
          }
        } else {
          handlerok = false;
        }

        if (!handlerok) {
          setHandlers.remove(stat);
        }
      }

      if (hnextcount != 1 && !setHandlers.isEmpty()) {
        List<Statement> lst = new ArrayList<>();
        lst.add(head);
        lst.addAll(setHandlers);

        for (Statement st : lst) {
          if (st.isMonitorEnter()) {
            return null;
          }
        }

        if (DecHelper.invalidHeadMerge(head)) {
          return null;
        }

        if (DecHelper.checkStatementExceptions(lst)) {
          return new CatchStatement(head, next, setHandlers);
        }
      }
    }
    return null;
  }

  @Override
  public TextBuffer toJava(int indent) {
    ValidationHelper.notNull(first);
    TextBuffer buf = new TextBuffer();

    buf.append(ExprProcessor.listToJava(varDefinitions, indent));

    if (isLabeled()) {
      buf.appendIndent(indent).append("label").append(this.id).append(":").appendLineSeparator();
    }

    if (resources.isEmpty()) {
      buf.appendIndent(indent).append("try {").appendLineSeparator();
    } else {
      buf.appendIndent(indent).append("try (");

      if (resources.size() > 1) {
        buf.appendLineSeparator();
        buf.append(ExprProcessor.listToJava(resources, indent + 1));
        buf.appendIndent(indent);
      } else {
        buf.append(resources.get(0).toJava(indent + 1));
      }
      buf.append(") {").appendLineSeparator();
    }

    buf.append(ExprProcessor.jmpWrapper(first, indent + 1, true));
    buf.appendIndent(indent).append("}");

    for (CatchEntry entry : this.catchEntries) {
      Statement stat = entry.stat();
      List<String> exception_types = entry.excStrings();
      VarExprent var = entry.var();

      // map first instruction storing the exception to the catch statement
      BasicBlock block = stat.getBasichead().getBlock();
      if (!block.getSeq().isEmpty() && block.getInstruction(0).opcode == CodeConstants.opc_astore) {
        Integer offset = block.getOldOffset(0);
        if (offset > -1) buf.addBytecodeMapping(offset);
      }

      buf.append(" catch (");

      for (int exc_index = 0; exc_index < exception_types.size(); ++exc_index) {
        String name = ExprProcessor.getCastTypeName(new VarType(CodeType.OBJECT, 0, exception_types.get(exc_index)));
        if (exception_types.size() > 1 && exc_index > 0) { // multi-catch, Java 7 style
          buf.append(" | ");
        }
        buf.append(name);
      }

      buf.append(" ");

      validateType(exception_types, var.getVarType());

      // Temporarily set variable as not a definition, since we just wrote the type above
      try (var v = var.new DefinitionLocker()) {
        buf.append(var.toJava(indent));
      }

      buf.append(") {").appendLineSeparator();
      buf.append(ExprProcessor.jmpWrapper(stat, indent + 1, false)).appendIndent(indent)
        .append("}");
    }
    buf.appendLineSeparator();

    return buf;
  }

  private void validateType(List<String> exTypes, VarType exVarType) {
    // TODO: join together all types, then check if exVarType instanceof that
    // Not correct!!
    if (ValidationHelper.VALIDATE) {
//      VarType type = new VarType(CodeType.OBJECT, 0, exTypes.get(exTypes.size() - 1));
//      ValidationHelper.validateTrue(type.higherEqualInLatticeThan(exVarType), "Invalid exception type " + exVarType + " " + type);
    }
  }

  @Override
  public List<Exprent> getStatExprents() {
    List<Exprent> lst = new ArrayList<>(resources);
    for (CatchEntry entry : this.catchEntries) {
      lst.add(entry.var());
    }
    return lst;
  }

  @Override
  public List<VarExprent> getImplicitlyDefinedVars() {
    List<VarExprent> vars = new ArrayList<>();

    for (CatchEntry entry : this.catchEntries) {
      vars.add(entry.var());
    }
    // resource vars must also be included
    for (Exprent exp : getResources()) {
      if (exp instanceof AssignmentExprent assignment) {
        vars.add((VarExprent) assignment.getLeft());
      }
    }


    return vars;
  }

  @Override
  public Statement getSimpleCopy() {
    CatchStatement cs = new CatchStatement();

    for (CatchEntry entry : this.catchEntries) {
      cs.catchEntries.add(new CatchEntry(
        new ArrayList<>(entry.excStrings()),
        new VarExprent(DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.VAR_COUNTER),
          new VarType(CodeType.OBJECT, 0, entry.excStrings().get(0)),
          DecompilerContext.getVarProcessor()),
        entry.stat() // temp, will be replaced
      ));
    }

    return cs;
  }

  @Override
  public void initSimpleCopy() {
    super.initSimpleCopy();

    for (int i = 0; i < this.catchEntries.size(); i++) {
      CatchEntry entry = this.catchEntries.get(i);
      entry.setStat(this.getStats().get(i + 1));
    }
  }

  public void getOffset(BitSet values) {
    super.getOffset(values);

    for (Exprent exp : this.getResources()) {
      exp.getBytecodeRange(values);
    }
  }

  public void removeCatchEntry(CatchEntry entry) {
    ValidationHelper.assertTrue(this.catchEntries.remove(entry), "Entry did not exist");
    this.stats.removeWithKey(entry.stat().id);
  }

  @Override
  public void replaceStatement(Statement oldStat, Statement newStat) {
    super.replaceStatement(oldStat, newStat);

    for (CatchEntry entry : this.catchEntries) {
      if (entry.stat() == oldStat) {
        entry.setStat(newStat);
        break;
      }
    }
  }

  // *****************************************************************************
  // getter and setter methods
  // *****************************************************************************

  @Override
  public Statement getFirst() {
    return ValidationHelper.notNull(super.getFirst());
  }

  public List<Exprent> getResources() {
    return resources;
  }

  public List<CatchEntry> getCatchEntries() {
    return this.catchEntries;
  }

  public Statement tryStat() {
    ValidationHelper.notNull(this.first);
    return this.first;
  }
}