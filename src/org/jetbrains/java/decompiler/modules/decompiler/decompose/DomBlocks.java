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
    private @NotNull DomBlock source;
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

    DomEdge changeSource(DomBlock source) {
      this.source.removeSuccessor(this);
      this.source = source;
      this.source.addSuccessor(this);
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

    public void delete() {
      if (this.closure != null) {
        this.closure.removeEnclosed(this);
      }
      this.source.removeSuccessor(this);
      this.destination.removePredecessor(this);
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

    void removeSuccessor(DomEdge edge) {
      this.successors.remove(edge);
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

  abstract static class RawDomBlock extends DomBlock {
    abstract public List<? extends DomEdge> inlinableEdges();

    abstract public DomBlock resolve(Map<? super DomEdge, ? extends DomBlock> blocks);
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
    public final @Nullable BasicBlock block;

    DomBasicBlock(@Nullable BasicBlock block) {
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

  static class RawDomSwitchBlock extends RawDomBlock {
    public final DomBasicBlock head;
    public final DomEdge defaultEdge;
    public final List<DomEdge> edges;
    public final int[] values;

    RawDomSwitchBlock(DomBasicBlock head, DomEdge defaultEdge, List<DomEdge> edges, int[] values) {
      this.head = head;
      this.defaultEdge = defaultEdge;
      this.edges = edges;
      this.values = values;
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


    @Override
    public List<? extends DomEdge> inlinableEdges() {
      List<DomEdge> res = new ArrayList<>(this.edges.size() + 1);
      res.addAll(this.edges);
      res.add(this.defaultEdge);
      return res;
    }

    private static DomEdge resolveEdge(DomEdge edge, Map<? super DomEdge, ? extends DomBlock> blocks) {
      DomBlock dest = blocks.get(edge);
      if (dest == null) {
        // Block couldn't be inlined, let's make a fake basic block
        dest = new DomBasicBlock(null);

        DomEdge newEdge = DomEdge.create(edge.getSource(), dest, edge.getClosure(), DomEdgeType.REGULAR);

        edge.changeSource(dest);
        return newEdge;
      }

      edge.changeDestination(dest).changeType(DomEdgeType.REGULAR);
      return edge;
    }

    @Override
    public DomSwitchBlock resolve(Map<? super DomEdge, ? extends DomBlock> blocks) {
      DomEdge newDefaultEdge = resolveEdge(this.defaultEdge, blocks);
      DomBlock newDefaultBlock = newDefaultEdge.getDestination();

      List<DomEdge> newEdges = new ArrayList<>(this.edges.size());
      List<DomBlock> newBlocks = new ArrayList<>(this.edges.size());
      for (DomEdge edge : this.edges) {
        DomEdge newEdge = resolveEdge(edge, blocks);
        newEdges.add(newEdge);
        newBlocks.add(newEdge.getDestination());
      }

      DomSwitchBlock newSwitch = new DomSwitchBlock(
        this.head,
        newDefaultEdge,
        newDefaultBlock,
        newEdges,
        newBlocks,
        this.values
      );

      for (DomEdge domEdge : new ArrayList<>(this.getPredecessors())) {
        domEdge.changeDestination(newSwitch);
      }

      for (DomEdge domEdge : new ArrayList<>(this.getSuccessors())) {
        domEdge.changeSource(newSwitch);
      }

      for (DomEdge domEdge : new ArrayList<>(this.getEnclosed())) {
        domEdge.changeClosure(newSwitch);
      }

      return newSwitch;
    }
  }

  static class DomSwitchBlock extends DomBlock {
    public final DomBasicBlock head;
    public final DomEdge defaultEdge;
    public final DomBlock defaultBlock;
    public final List<DomEdge> edges;
    public final List<DomBlock> blocks;
    public final int[] values;

    DomSwitchBlock(
      DomBasicBlock head,
      DomEdge defaultEdge,
      DomBlock defaultBlock,
      List<DomEdge> edges,
      List<DomBlock> blocks,
      int[] values
    ) {
      this.head = head;
      this.defaultEdge = defaultEdge;
      this.defaultBlock = defaultBlock;
      this.edges = edges;
      this.blocks = blocks;
      this.values = values;
    }

    @Override
    public void getRecursive(Collection<? super DomBlock> doms) {
      doms.add(this);
      this.head.getRecursive(doms);
      this.defaultBlock.getRecursive(doms);
      for (DomBlock block : this.blocks) {
        block.getRecursive(doms);
      }
    }

    @Override
    public Collection<? extends DomBlock> getChildren() {
      List<DomBlock> lst = new ArrayList<>(this.blocks.size() + 2);
      lst.add(this.head);
      lst.addAll(this.blocks);
      lst.add(this.defaultBlock);
      return lst;
    }

    public DomEdge getDefaultEdge() {
      return this.defaultEdge;
    }

    public List<DomEdge> getEdges() {
      return this.edges;
    }

    public int[] getValues() {
      return this.values;
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

  static class RawDomTryCatchBlock extends RawDomBlock {
    public final Set<BasicBlock> protectedRange;
    public final DomBlock tryBlock;
    public final LinkedHashMap<List<String>, DomEdge> handlers = new LinkedHashMap<>();
    public final LinkedHashMap<List<String>, RawDomCatchBlock> handlerBlocks = new LinkedHashMap<>();

    RawDomTryCatchBlock(Collection<BasicBlock> protectedRange, DomBlock tryBlock) {
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

    @Override
    public List<? extends DomEdge> inlinableEdges() {
      return new ArrayList<>(handlers.values());
    }

    @Override
    public DomBlock resolve(Map<? super DomEdge, ? extends DomBlock> blocks) {
      LinkedHashMap<List<String>, DomEdge> handlers = new LinkedHashMap<>();
      LinkedHashMap<List<String>, DomBlock> handlerBlocks = new LinkedHashMap<>();

      for (var entry : this.handlers.entrySet()) {
        var oldEdge = entry.getValue();
        // TODO: assert that there is always a single pred and it is an exception edge
        var predEdge = oldEdge.getSource().getPredecessors().iterator().next();
        if (blocks.containsKey(entry.getValue())) {
          var targetBlock = blocks.get(entry.getValue());
          predEdge.changeDestination(targetBlock);
          oldEdge.delete();
          handlers.put(entry.getKey(), predEdge);
          handlerBlocks.put(entry.getKey(), targetBlock);
        } else {
          var targetBlock = new DomBasicBlock(null);
          predEdge.changeDestination(targetBlock);
          oldEdge.changeSource(targetBlock);
          handlers.put(entry.getKey(), predEdge);
          handlerBlocks.put(entry.getKey(), targetBlock);
        }
      }

      var res = new DomTryCatchBlock(this.protectedRange, this.tryBlock, handlers, handlerBlocks);
      for (DomEdge edge : new ArrayList<>(this.getEnclosed())) {
        edge.changeClosure(res);
      }
      for (DomEdge edge : new ArrayList<>(this.getPredecessors())) {
        edge.changeDestination(res);
      }
      return res;
    }
  }


  static class DomTryCatchBlock extends DomBlock {
    public final Set<BasicBlock> protectedRange;
    public final DomBlock tryBlock;
    public final LinkedHashMap<List<String>, DomEdge> handlers;
    public final LinkedHashMap<List<String>, DomBlock> handlerBlocks;

    DomTryCatchBlock(
      Collection<BasicBlock> protectedRange,
      DomBlock tryBlock,
      LinkedHashMap<List<String>, DomEdge> handlers,
      LinkedHashMap<List<String>, DomBlock> handlerBlocks
    ) {
      this.protectedRange = new HashSet<>(protectedRange);
      this.tryBlock = tryBlock;
      this.handlers = handlers;
      this.handlerBlocks = handlerBlocks;
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

  static class RawDomCatchBlock extends DomBlock {
    public final List<String> exceptionTypes;

    RawDomCatchBlock(List<String> exceptionTypes) {
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
