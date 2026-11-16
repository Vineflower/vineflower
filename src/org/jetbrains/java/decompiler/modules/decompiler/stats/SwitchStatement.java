// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler.stats;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.java.decompiler.code.SwitchInstruction;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.modules.decompiler.DecHelper;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.ValidationHelper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.StartEndPair;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.*;
import java.util.stream.Collectors;

public class SwitchStatement extends Statement {
  public static final class SwitchCase {
    private Statement statement;
    private List<StatEdge> edges;
    private List<@Nullable Exprent> values;
    private @Nullable Exprent guard;

    public SwitchCase(
      Statement statement,
      List<StatEdge> edges,
      List<@Nullable Exprent> values,
      @Nullable Exprent guard
    ) {
      this.statement = statement;
      this.edges = edges;
      this.values = values;
      this.guard = guard;
    }

    public Statement statement() {
      return statement;
    }

    public List<StatEdge> edges() {
      return edges;
    }

    public List<@Nullable Exprent> values() {
      return values;
    }

    public @Nullable Exprent guard() {
      return guard;
    }

    public void setStatement(Statement statement) {
      this.statement = statement;
    }

    public void setEdges(List<StatEdge> edges) {
      this.edges = edges;
    }

    public void setValues(List<@Nullable Exprent> values) {
      this.values = values;
    }

    public void setGuard(@Nullable Exprent guard) {
      this.guard = guard;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) return true;
      if (obj == null || obj.getClass() != this.getClass()) return false;
      var that = (SwitchCase) obj;
      return Objects.equals(this.statement, that.statement) &&
        Objects.equals(this.edges, that.edges) &&
        Objects.equals(this.values, that.values) &&
        Objects.equals(this.guard, that.guard);
    }

    @Override
    public int hashCode() {
      return Objects.hash(statement, edges, values, guard);
    }

    @Override
    public String toString() {
      return "SwitchCase[" +
        "statement=" + statement + ", " +
        "edges=" + edges + ", " +
        "values=" + values + ", " +
        "guard=" + guard + ']';
    }


    }

  // *****************************************************************************
  // private fields
  // *****************************************************************************

  private List<SwitchCase> cases = new ArrayList<>();

  private final Set<Statement> scopedCaseStatements = new HashSet<>();

  private @Nullable StatEdge defaultEdge;
  private boolean defaultIsImplicit = false;

  private final List<@Nullable Exprent> headexprent = new ArrayList<>(1);

  // *****************************************************************************
  // constructors
  // *****************************************************************************

  protected SwitchStatement() {
    super(StatementType.SWITCH);

    headexprent.add(null);
  }

  protected SwitchStatement(Statement head, @Nullable Statement poststat) {

    this();

    first = head;
    stats.addWithKey(head, head.id);

    // find post node
    Set<Statement> lstNodes = new HashSet<>(head.getNeighbours(StatEdge.TYPE_REGULAR, EdgeDirection.FORWARD));

    // cluster nodes
    if (poststat != null) {
      post = poststat;
      lstNodes.remove(post);
    }

    defaultEdge = head.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL).get(0);

    //We need to use set above in case we have multiple edges to the same node. But HashSets iterator is not ordered, so sort
    List<Statement> sorted = new ArrayList<>(lstNodes);
    sorted.sort(Comparator.comparingInt(o -> o.id));
    for (Statement st : sorted) {
      stats.addWithKey(st, st.id);
    }
  }

  // *****************************************************************************
  // public methods
  // *****************************************************************************

  public static @Nullable Statement isHead(Statement head) {

    if (head instanceof BasicBlockStatement && head.getLastBasicType() == LastBasicType.SWITCH) {

      List<Statement> lst = new ArrayList<>();
      if (DecHelper.isChoiceStatement(head, lst)) {
        Statement post = lst.remove(0);

        for (Statement st : lst) {
          if (st.isMonitorEnter()) {
            return null;
          }
        }

        if (DecHelper.checkStatementExceptions(lst)) {
          return new SwitchStatement(head, post);
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
    buf.append(first.toJava(indent));

    boolean showPhantom = DecompilerContext.getOption(IFernflowerPreferences.SHOW_HIDDEN_STATEMENTS);

    // Is phantom and we don't want to show- just return what we have so far
    if (this.isPhantom() && !showPhantom) {
      return buf;
    }

    if (isLabeled()) {
      buf.appendIndent(indent).append("label").append(this.id).append(":").appendLineSeparator();
    }

    buf.appendIndent(indent);
    if (this.isPhantom()) {
      buf.append("/*");
    }

    buf.append(getHeadexprent().toJava(indent)).append(" {").appendLineSeparator();

    VarType switch_type = getHeadexprent().getExprType();

    for (SwitchCase entry : cases) {
      Statement stat = entry.statement();
      List<StatEdge> edges = entry.edges();
      List<@Nullable Exprent> values = entry.values();
      Exprent guard = entry.guard();

      for (int j = 0; j < edges.size(); j++) {
        if (edges.get(j) == defaultEdge) {
          buf.appendIndent(indent + 1).append("default:");
          if (this.scopedCaseStatements.contains(stat) && j == edges.size() - 1) {
            buf.append(" {");
          }

          buf.appendLineSeparator();
        } else {
          Exprent value = values.get(j);

          buf.appendIndent(indent + 1).append("case ");

          if (value instanceof ConstExprent && !value.getExprType().equals(VarType.VARTYPE_NULL)) {
            value = value.copy();
            ((ConstExprent)value).setConstType(switch_type);
            buf.append(value.toJava(indent));
          } else if (value instanceof FieldExprent field && field.isStatic() && (!getTopParent().mt.getBytecodeVersion().hasSwitchPatternMatch() || switch_type.equals(field.getExprType()))) { // enum values
            buf.appendField(field.getName(), false, field.getClassname(), field.getName(), field.getDescriptor());
          } else if (value instanceof FieldExprent field) {
            buf.append(field.toJava(indent, false));
          } else if (value instanceof FunctionExprent && ((FunctionExprent) value).getFuncType() == FunctionType.INSTANCEOF) {
            // Pattern matching variables

            Pattern pattern = (Pattern) value.getAllExprents().get(2);
            for (VarExprent var : pattern.getPatternVars()) {
              var.setWritingPattern();
            }

            buf.append(value.getAllExprents().get(2).toJava(indent));
          } else {
            buf.append(value.toJava(indent));
          }

          if (guard != null) {
            buf.append(" when ").append(guard.toJava());
          }

          buf.append(":");
          if (this.scopedCaseStatements.contains(stat) && j == edges.size() - 1) {
            buf.append(" {");
          }
          buf.appendLineSeparator();
        }
      }

      buf.append(ExprProcessor.jmpWrapper(stat, indent + 2, false));

      if (this.scopedCaseStatements.contains(stat)) {
        buf.appendIndent(indent + 1);
        buf.append("}");
        buf.appendLineSeparator();
      }
    }

    buf.appendIndent(indent).append("}").appendLineSeparator();

    if (this.isPhantom()) {
      buf.append("*/");
    }

    return buf;
  }

  @Override
  public void initExprents() {
    SwitchHeadExprent swexpr = (SwitchHeadExprent)getFirst().getExprents().remove(getFirst().getExprents().size() - 1);
    swexpr.setCaseValues(this.getCaseValues());

    headexprent.set(0, swexpr);
  }

  @Override
  public List<Exprent> getStatExprents() {
    List<Exprent> lst = new ArrayList<>(headexprent);

    for (SwitchCase entry : this.cases) {
      // make sure guards can be simplified by other helpers
      if (entry.guard() != null) {
        lst.add(entry.guard());
      }

      for (Exprent value : entry.values()) {
        if (value != null) {
          lst.add(value);
        }
      }
    }

    return lst;
  }

  // Returns true if this switch is a pattern matching switch.
  public boolean isPattern() {
    for (SwitchCase entry : this.cases) {
      if (entry.guard() != null) {
        // If there's a guard then it is for sure pattern matching
        return true;
      }
      for (Exprent e : entry.values()) {
        // If we have instanceofs in our case values, we're a pattern matching switch
        if (e instanceof FunctionExprent && ((FunctionExprent)e).getFuncType() == FunctionType.INSTANCEOF) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public List<VarExprent> getImplicitlyDefinedVars() {
    List<VarExprent> vars = new ArrayList<>();

    List<@Nullable Exprent> caseList = this.cases.stream()
      .map(SwitchCase::values)
      .flatMap(List::stream) // List<List<Exprent>> -> List<Exprent>
      .collect(Collectors.toList());
    // guards can also contain pattern variables
    caseList.addAll(this.cases.stream().map(SwitchCase::guard).toList());
    // guards may also contain nested variables, like `a instanceof B b && b == ...`
    caseList = caseList.stream()
      .filter(Objects::nonNull)
      .flatMap(x -> x.getAllExprents(true, true).stream())
      .collect(Collectors.toList());

    for (Exprent caseContent : caseList) {
      if (caseContent == null) {
        continue;
      }

      if (caseContent instanceof FunctionExprent func) {

        // Pattern match variable is implicitly defined
        if (func.getFuncType() == FunctionType.INSTANCEOF && func.getLstOperands().size() > 2) {
          vars.addAll(((Pattern) func.getLstOperands().get(2)).getPatternVars());
        }
      }
    }

    return vars;
  }

  @Override
  public void replaceExprent(Exprent oldexpr, Exprent newexpr) {
    if (headexprent.get(0) == oldexpr) {
      headexprent.set(0, newexpr);
    } else {
      for (SwitchCase entry : this.cases) {
        if (entry.guard() == oldexpr) {
          entry.setGuard(newexpr);
        }
      }
    }
  }

  @Override
  public void replaceStatement(Statement oldstat, Statement newstat) {

    boolean changedAny = false;
    for (SwitchCase entry : this.cases) {
      if (entry.statement() == oldstat) {
        changedAny = true;
        entry.setStatement(newstat);
      }
    }

    ValidationHelper.assertTrue(changedAny, "Replaced statement in switch without changing any case statements!");

    super.replaceStatement(oldstat, newstat);
  }

  @Override
  public Statement getSimpleCopy() {
    return new SwitchStatement();
  }

  @Override
  public void initSimpleCopy() {
    first = stats.get(0);
    defaultEdge = first.getSuccessorEdges(Statement.STATEDGE_DIRECT_ALL).get(0);

    sortEdgesAndNodes();
  }

  @Override
  public StartEndPair getStartEndRange() {
    StartEndPair[] sepairs = new StartEndPair[cases.size() + 1];
    int i = 0;
    sepairs[i++] = super.getStartEndRange();
    for (SwitchCase entry : cases) {
      sepairs[i++] = entry.statement.getStartEndRange();
    }
    return StartEndPair.join(sepairs);
  }

  // *****************************************************************************
  // private methods
  // *****************************************************************************

  public void sortEdgesAndNodes() {
    ValidationHelper.notNull(first);

    // skip for pattern switches
    if (this.cases.stream().map(SwitchCase::values).flatMap(Collection::stream).anyMatch(u -> !(u instanceof ConstExprent) || ((ConstExprent) u).isNull())
      || this.cases.stream().map(SwitchCase::guard).anyMatch(Objects::nonNull)) {
      return;
    }

    HashMap<StatEdge, Integer> mapEdgeIndex = new HashMap<>();

    List<StatEdge> lstFirstSuccs = first.getSuccessorEdges(STATEDGE_DIRECT_ALL);
    for (int i = 0; i < lstFirstSuccs.size(); i++) {
      mapEdgeIndex.put(lstFirstSuccs.get(i), i == 0 ? lstFirstSuccs.size() : i);
    }

    // case values
    BasicBlockStatement bbstat = (BasicBlockStatement)first;
    int[] values = ((SwitchInstruction)bbstat.getBlock().getLastInstruction()).getValues();

    List<@Nullable Statement> nodes = new ArrayList<>(stats.size() - 1);
    List<List<Integer>> edges = new ArrayList<>(stats.size() - 1);

    // collect regular edges
    for (int i = 1; i < stats.size(); i++) {

      Statement stat = stats.get(i);

      List<Integer> lst = new ArrayList<>();
      for (StatEdge edge : stat.getPredecessorEdges(StatEdge.TYPE_REGULAR)) {
        if (edge.getSource() == first) {
          lst.add(mapEdgeIndex.get(edge));
        }
      }
      Collections.sort(lst);

      nodes.add(stat);
      edges.add(lst);
    }

    // collect exit edges
    List<StatEdge> lstExitEdges = first.getSuccessorEdges(StatEdge.TYPE_BREAK | StatEdge.TYPE_CONTINUE);
    while (!lstExitEdges.isEmpty()) {
      StatEdge edge = lstExitEdges.get(0);

      List<Integer> lst = new ArrayList<>();
      for (int i = lstExitEdges.size() - 1; i >= 0; i--) {
        StatEdge edgeTemp = lstExitEdges.get(i);
        if (edgeTemp.getDestination() == edge.getDestination() && edgeTemp.getType() == edge.getType()) {
          lst.add(mapEdgeIndex.get(edgeTemp));
          lstExitEdges.remove(i);
        }
      }
      Collections.sort(lst);

      nodes.add(null);
      edges.add(lst);
    }

    // sort edges (bubblesort)
    for (int i = 0; i < edges.size() - 1; i++) {
      for (int j = edges.size() - 1; j > i; j--) {
        if (edges.get(j - 1).get(0) > edges.get(j).get(0)) {
          edges.set(j, edges.set(j - 1, edges.get(j)));
          nodes.set(j, nodes.set(j - 1, nodes.get(j)));
        }
      }
    }

    // sort statement cliques
    for (int index = 0; index < nodes.size(); index++) {
      Statement stat = nodes.get(index);

      if (stat != null) {
        HashSet<Statement> setPreds = new HashSet<>(stat.getNeighbours(StatEdge.TYPE_REGULAR, EdgeDirection.BACKWARD));
        setPreds.remove(first);

        if (!setPreds.isEmpty()) {
          Statement pred =
            setPreds.iterator().next(); // assumption: at most one predecessor node besides the head. May not hold true for obfuscated code.
          for (int j = 0; j < nodes.size(); j++) {
            if (j != (index - 1) && nodes.get(j) == pred) {
              nodes.add(j + 1, stat);
              edges.add(j + 1, edges.get(index));

              if (j > index) {
                nodes.remove(index);
                edges.remove(index);
                index--;
              }
              else {
                nodes.remove(index + 1);
                edges.remove(index + 1);
              }
              break;
            }
          }
        }
      }
    }

    // translate indices back into edges
    List<List<StatEdge>> lstEdges = new ArrayList<>(edges.size());
    List<List<@Nullable Exprent>> lstValues = new ArrayList<>(edges.size());

    for (List<Integer> lst : edges) {
      List<StatEdge> lste = new ArrayList<>(lst.size());
      List<@Nullable Exprent> lstv = new ArrayList<>(lst.size());

      List<StatEdge> lstSuccs = first.getSuccessorEdges(STATEDGE_DIRECT_ALL);
      for (Integer in : lst) {
        int index = in == lstSuccs.size() ? 0 : in;

        lste.add(lstSuccs.get(index));
        lstv.add(index == 0 ? null : new ConstExprent(values[index - 1], false, null));
      }
      lstEdges.add(lste);
      lstValues.add(lstv);
    }

    // replace null statements with dummy basic blocks
    for (int i = 0; i < nodes.size(); i++) {
      if (nodes.get(i) == null) {
        BasicBlockStatement bstat = BasicBlockStatement.create();

        StatEdge sample_edge = lstEdges.get(i).get(0);

        bstat.addSuccessor(new StatEdge(sample_edge.getType(), bstat, sample_edge.getDestination(), sample_edge.closure));

        for (StatEdge edge : lstEdges.get(i)) {

          edge.getSource().changeEdgeType(EdgeDirection.FORWARD, edge, StatEdge.TYPE_REGULAR);
          edge.closure.getLabelEdges().remove(edge);

          edge.getDestination().removePredecessor(edge);
          edge.getSource().changeEdgeNode(EdgeDirection.FORWARD, edge, bstat);
          bstat.addPredecessor(edge);
        }

        nodes.set(i, bstat);
        stats.addWithKey(bstat, bstat.id);
        bstat.setParent(this);
      }
    }


    List<SwitchCase> cases = new ArrayList<>(nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      Statement node = nodes.get(i);
      List<StatEdge> statEdges = lstEdges.get(i);
      List<@Nullable Exprent> caseValues = lstValues.get(i);

      ValidationHelper.notNull(node);

      cases.add(new SwitchCase(node, statEdges, caseValues, null));
    }

    this.cases = cases;
  }

  @Override
  public boolean hasBasicSuccEdge() {
    // FIXME: default switch

    return false;
  }

  @Override
  protected void onNodeCollapse() {
    // special case switch, sorting edges and nodes
    sortEdgesAndNodes();
  }

  public List<@Nullable Exprent> getHeadexprentList() {
    return headexprent;
  }

  public Exprent getHeadexprent() {
    return ValidationHelper.notNull(headexprent.get(0));
  }

  public List<SwitchCase> getCases() {
    return this.cases;
  }

  @Deprecated
  @Unmodifiable
  public List<List<StatEdge>> getCaseEdges() {
    return cases.stream().map(SwitchCase::edges).toList();
  }

  @Deprecated
  @Unmodifiable
  public List<Statement> getCaseStatements() {
    return cases.stream().map(SwitchCase::statement).toList();
  }

  public @Nullable StatEdge getDefaultEdge() {
    return defaultEdge;
  }

  @Deprecated
  @Unmodifiable
  public List<List<@Nullable Exprent>> getCaseValues() {
    return cases.stream().map(SwitchCase::values).toList();
  }

  @Deprecated
  @Unmodifiable
  public List<@Nullable Exprent> getCaseGuards() {
    return cases.stream().map(SwitchCase::guard).toList();
  }

  @Override
  public BasicBlockStatement getFirst() {
    return (BasicBlockStatement) ValidationHelper.notNull(first);
  }

  public boolean isDefaultIsImplicit() {
    return defaultIsImplicit;
  }

  public void setDefaultIsImplicit(boolean defaultIsImplicit) {
    this.defaultIsImplicit = defaultIsImplicit;
  }

  public void scopeCaseStatement(Statement stat) {
    if (!this.getCaseStatements().contains(stat)) {
      throw new IllegalStateException("Tried to scope a case statement that isn't in the switch!");
    }

    this.scopedCaseStatements.add(stat);
  }

  public void setDefaultEdge(@Nullable StatEdge edge) {
    this.defaultEdge = edge;
  }

  public Optional<Statement> getDefaultCase() {
    StatEdge defaultEdge = this.getDefaultEdge();
    return defaultEdge != null ? Optional.of(defaultEdge.getDestination()) : Optional.empty();
  }

  /**
   * Replaces the default case if it exists. Adds a new default case to the switch if it doesn't exist.
   * @param stat the case statement to be linked to
   */
  public void setDefaultCase(Statement stat) {
    Optional<Statement> defaultCase = this.getDefaultCase();
    if (defaultCase.isPresent()) {
      defaultCase.get().replaceWith(stat);
      return;
    }

    this.addCaseInternal(0, Collections.singletonList(null), stat);
  }

  /**
   * Adds a new case to the switch.
   * @param values the list of non-null exprents to use as case values
   * @param stat the case statement to be linked to
   */
  public void addCase(List<Exprent> values, Statement stat) {
    int caseCount = this.getCaseStatements().size();
    this.addCaseInternal(this.getDefaultEdge() != null ? caseCount - 1 : caseCount, values, stat);
  }

  /**
   * Adds a new case to the switch.
   * @param index the index of where the case should be added
   * @param values the list of exprents to use as case values
   * @param stat the case statement to be linked to
   */
  public void addCase(int index, List<Exprent> values, Statement stat) {
    this.addCaseInternal(index, values, stat);
  }

  @Deprecated()
  private void addCaseInternal(int index, List<@Nullable Exprent> values, Statement stat) {
    // Basichead is always the first stat
    this.getStats().addWithKeyAndIndex(1 + index, stat, stat.id);

    List<StatEdge> edges;

    // If it's a default case, or a regular case
    if (values.size() == 1 && values.get(0) == null) {
      StatEdge edge = new StatEdge(StatEdge.TYPE_REGULAR, this.getBasichead(), stat);
      edges = List.of(edge);
      this.setDefaultEdge(edge);
    } else {
      ArrayList<StatEdge> newCaseEdgeLst = new ArrayList<>();
      for (int i = 0; i < values.size(); i++) {
        StatEdge edge = new StatEdge(StatEdge.TYPE_REGULAR, this.getBasichead(), stat);
        newCaseEdgeLst.add(i, edge);

        this.getBasichead().addSuccessor(edge);
      }

      edges = newCaseEdgeLst;
    }

    this.cases.add(index, new SwitchCase(stat, edges, values, null));

    stat.setParent(this);
  }

  /**
   * Removes a case from the switch statement. This includes the case statement, values, and edges.
   * You should NOT manually handle stats/values/edges if you are calling this method.
   * @param stat the case statement to remove
   */
  public void removeCase(Statement stat) {
    for (int i = 0; i < cases.size(); i++) {
      if (cases.get(i).statement() == stat){
        this.removeCase(i);
        return;
      }
    }

    throw new IllegalStateException("Tried to remove a case statement that isn't in the switch!");
  }

  /**
   * Removes a case from the switch statement. This includes the case statement, values, and edges.
   * You should NOT manually handle stats/values/edges if you are calling this method.
   * @param index the index of the case to remove
   */
  public void removeCase(int index) {
    SwitchCase entry = this.cases.remove(index);
    Statement stat = entry.statement();

    stat.getAllPredecessorEdges().forEach(StatEdge::remove);
    stat.getAllSuccessorEdges().forEach(StatEdge::remove);

    this.getStats().removeWithKey(stat.id);
  }
}
