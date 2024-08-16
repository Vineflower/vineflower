package org.jetbrains.java.decompiler.modules.decompiler.decompose;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;

import java.util.*;

public class DomBlocks {

  enum DomEdgeType {
    UNKNOWN,
    UNKNOWN_EXCEPTION,
    REGULAR,
    CONTINUE,
    BREAK,
    EXCEPTION,
    EXIT,
    ;

    public boolean isUnknown() {
      return this == UNKNOWN || this == UNKNOWN_EXCEPTION;
    }
  }

  static class DomEdge {
    private final @NotNull DomBlock source;
    private @NotNull DomBlock destination;
    private @Nullable DomBlock closure;
    private @NotNull DomEdgeType type;

    private DomEdge(
      @NotNull DomBlock source,
      @NotNull DomBlock destination,
      @Nullable DomBlock closure,
      @NotNull DomEdgeType type) {
      assert source != null;
      assert destination != null;
      assert type != null;

      this.source = source;
      this.destination = destination;
      this.closure = closure;
      this.type = type;
    }

    public DomBlock getSource() {
      return source;
    }

    public DomBlock getDestination() {
      return destination;
    }

    public @Nullable DomBlock getClosure() {
      return closure;
    }

    public DomEdgeType getType() {
      return type;
    }

    static DomEdge create(DomBlock source, DomBlock destination, @Nullable DomBlock closure, DomEdgeType type) {
      DomEdge edge = new DomEdge(source, destination, closure, type);
      source.addSuccessor(edge);
      destination.addPredecessor(edge);
      if (closure != null) {
        closure.addEnclosed(edge);
      }
      return edge;
    }

    static DomEdge create(DomBlock source, @NotNull DomBlock destination) {
      return create(source, destination, null, DomEdgeType.UNKNOWN);
    }

    DomEdge changeType(DomEdgeType type) {
      this.type = type;
      return this;
    }

    DomEdge changeDestination(DomBlock destination) {
      this.destination.removePredecessor(this);
      this.destination = destination;
      this.destination.addPredecessor(this);
      return this;
    }

    DomEdge changeClosure(@Nullable DomBlock closure) {
      if (this.closure != null) {
        this.closure.removeEnclosed(this);
      }
      this.closure = closure;
      if (this.closure != null) {
        this.closure.addEnclosed(this);
      }
      return this;
    }

    @Override
    public String toString() {
      return "DomEdge{" + source + " --" + type + "-> " + destination + "}";

    }
  }

  abstract static class DomBlock {
    // For debugging purposes
    private final int id = DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.DOM_BLOCK_COUNTER);
    private Set<DomEdge> successors = new LinkedHashSet<>();
    private Set<DomEdge> predecessors = new LinkedHashSet<>();
    private Set<DomEdge> enclosed = new LinkedHashSet<>();

    void addSuccessor(DomEdge edge) {
      this.successors.add(edge);
    }

    void addPredecessor(DomEdge edge) {
      this.predecessors.add(edge);
    }

    void removePredecessor(DomEdge edge) {
      this.predecessors.remove(edge);
    }

    void addEnclosed(DomEdge edge) {
      this.enclosed.add(edge);
    }

    void removeEnclosed(DomEdge edge) {
      this.enclosed.remove(edge);
    }

    public Collection<? extends DomEdge> getSuccessors() {
      return successors;
    }

    public Collection<? extends DomEdge> getPredecessors() {
      return predecessors;
    }

    public Collection<? extends DomEdge> getEnclosed() {
      return enclosed;
    }

    abstract public void getRecursive(Collection<? super DomBlock> doms);

    abstract public Collection<? extends DomBlock> getChildren();

    public List<DomEdge> getAllSuccessors() {
      List<DomEdge> lst = new ArrayList<>();
      lst.addAll(this.successors);
      lst.addAll(this.enclosed);
      return lst;
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName() + "(" + this.id + ")";
    }

    /* package private */ int getId() {
      return this.id;
    }
  }

  static class DomExit extends DomBlock {

    @Override
    public void getRecursive(Collection<? super DomBlock> doms) {
      doms.add(this);
    }

    @Override
    public Collection<? extends DomBlock> getChildren() {
      return List.of();
    }
  }

  static class DomBasicBlock extends DomBlock {
    public final BasicBlock block;

    DomBasicBlock(BasicBlock block) {
      this.block = block;
    }

    @Override
    public void getRecursive(Collection<? super DomBlock> doms) {
      doms.add(this);
    }

    @Override
    public Collection<? extends DomBlock> getChildren() {
      return Collections.emptyList();
    }
  }

  static class DomIfBlock extends DomBlock {
    public final DomBasicBlock head;
    public final DomEdge ifEdge;
    public final DomEdge elseEdge;

    DomIfBlock(DomBasicBlock head, DomEdge ifEdge, DomEdge elseEdge) {
      this.head = head;
      this.ifEdge = ifEdge;
      this.elseEdge = elseEdge;
    }

    @Override
    public void getRecursive(Collection<? super DomBlock> doms) {
      doms.add(this);
      this.head.getRecursive(doms);
    }

    @Override
    public Collection<? extends DomBlock> getChildren() {
      return List.of(this.head);
    }

    public DomEdge getIfEdge() {
      return this.ifEdge;
    }

    public DomEdge getElseEdge() {
      return this.elseEdge;
    }
  }

  static class DomLoopBlock extends DomBlock {
    public final DomBlock body;

    DomLoopBlock(DomBlock body) {
      this.body = body;
    }

    @Override
    public void getRecursive(Collection<? super DomBlock> doms) {
      doms.add(this);
      this.body.getRecursive(doms);
    }

    @Override
    public Collection<? extends DomBlock> getChildren() {
      return List.of(this.body);
    }
  }

  static class DomSequenceBlock extends DomBlock {
    public final List<? extends DomBlock> blocks;

    DomSequenceBlock(List<? extends DomBlock> blocks) {
      this.blocks = blocks;
    }

    @Override
    public void getRecursive(Collection<? super DomBlock> doms) {
      doms.add(this);
      for (DomBlock block : this.blocks) {
        block.getRecursive(doms);
      }
    }

    @Override
    public Collection<? extends DomBlock> getChildren() {
      return this.blocks;
    }
  }

  static class DomTryCatchBlock extends DomBlock {
    public final Set<BasicBlock> protectedRange;
    public final DomBlock tryBlock;
    public final LinkedHashMap<List<String>, DomEdge> handlers = new LinkedHashMap<>();
    public final LinkedHashMap<List<String>, DomCatchBlock> handlerBlocks = new LinkedHashMap<>();

    DomTryCatchBlock(Collection<BasicBlock> protectedRange, DomBlock tryBlock) {
      this.protectedRange = new HashSet<>(protectedRange);
      this.tryBlock = tryBlock;
    }

    @Override
    public void getRecursive(Collection<? super DomBlock> doms) {
      doms.add(this);
      this.tryBlock.getRecursive(doms);
      for (var edge : this.handlerBlocks.values()) {
        edge.getRecursive(doms);
      }
    }

    @Override
    public Collection<? extends DomBlock> getChildren() {
      List<DomBlock> lst = new ArrayList<>(this.handlerBlocks.size() + 1);
      lst.add(this.tryBlock);
      lst.addAll(this.handlerBlocks.values());
      return lst;
    }
  }

  static class DomCatchBlock extends DomBlock {
    public final List<String> exceptionTypes;

    DomCatchBlock(List<String> exceptionTypes) {
      ValidationHelper.notNull(exceptionTypes);
      this.exceptionTypes = exceptionTypes;
    }

    @Override
    public void getRecursive(Collection<? super DomBlock> doms) {
      doms.add(this);
    }

    @Override
    public Collection<? extends DomBlock> getChildren() {
      return List.of();
    }
  }
}
