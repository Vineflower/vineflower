package org.jetbrains.java.decompiler.modules.serializer;

import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.util.DotExporter;

import java.util.*;
import java.util.stream.Collectors;

public final class TapestryReader {
  private static final Map<String, Statement.StatementType> TYPES = Arrays.stream(Statement.StatementType.values())
    .collect(Collectors.toMap(Statement.StatementType::getPrettyId, t -> t));
  public static RootStatement readTapestry(String data) {
    String[] split = data.split("\n");

    List<StatRec> statRecs = new ArrayList<>();
    Map<Integer, Statement> stats = new HashMap<>();
    List<EdgeRec> edges = new ArrayList<>();

    Map<Integer, List<Exprent>> exprMaps = new HashMap<>();
    for (String s : split) {
      s = s.replaceAll("\\s+", " ");

      String[] components = s.split(" ");

      if (components[1].equals("->")) {
        // Edge
        edges.add(readEdge(components));
      } else if (components[1].equals("<")) {
        // Expr

        int id = Integer.parseInt(components[0]);

        exprMaps.computeIfAbsent(id, k -> new ArrayList<>())
          .add(readExpr(components));

      } else {
        // Statement
        StatRec stat = readStatement(components);
        statRecs.add(stat);
      }
    }

    for (StatRec v : statRecs) {
      stats.put(v.id, makeFull(v));
    }

    for (StatRec rec : statRecs) {
      Statement thisSt = stats.get(rec.id);

      if (rec.parentId != -1) {
        Statement parSt = stats.get(rec.parentId);

        thisSt.setParent(parSt);
        parSt.getStats().putWithKey(thisSt, thisSt.id);
      }

      if (rec.firstId != -1) {
        Statement firstSt = stats.get(rec.firstId);

        thisSt.setFirst(firstSt);
        thisSt.getStats().putWithKey(firstSt, firstSt.id);
      }

      for (String d : rec.data) {
        thisSt.readFromTapestry(d);
      }
    }

    for (EdgeRec edge : edges) {
      Statement src = stats.get(edge.sourceId);
      Statement dest = stats.get(edge.destId);

      StatEdge statEdge = new StatEdge(edge.edgeType, src, dest);
      statEdge.labeled = edge.labeled;
      statEdge.explicit = edge.explicit;
      statEdge.canInline = edge.canInline;
      statEdge.phantomContinue = edge.phantomContinue;

      if (edge.closureId != -1) {
        statEdge.closure = stats.get(edge.closureId);
      }

      if (edge.ifId != -1) {
        ((IfStatement)stats.get(edge.ifId)).setIfEdge(statEdge);
        ((IfStatement)stats.get(edge.ifId)).setIfstat(statEdge.getDestination());
      }

      if (edge.elseId != -1) {
        ((IfStatement)stats.get(edge.elseId)).setIfEdge(statEdge);
        ((IfStatement)stats.get(edge.ifId)).setElsestat(statEdge.getDestination());
      }

      if (edge.defaultId != -1) {
        ((SwitchStatement)stats.get(edge.defaultId)).setDefaultEdge(statEdge);
      }

      src.addSuccessor(statEdge);
    }

    RootStatement root = null;
    DummyExitStatement dummyExitStatement = null;

    for (Statement v : stats.values()) {
      // Find key statements
      if (v instanceof RootStatement) {
        root = (RootStatement) v;
      }

      if (v instanceof DummyExitStatement) {
        dummyExitStatement = (DummyExitStatement) v;
      }

      // Set exprs
      List<Exprent> exprs = exprMaps.get(v.id);
      if (exprs != null) {
        if (v instanceof BasicBlockStatement) {
          v.getExprents().addAll(exprs);
        } else if (v instanceof IfStatement) {
          ((IfStatement)v).getHeadexprentList().set(0, exprs.get(0));
        } else if (v instanceof DoStatement) {
          DoStatement loop = (DoStatement) v;

          if (exprs.size() == 1) {
            loop.setConditionExprent(exprs.get(0));
          } else if (exprs.size() == 2) {
            loop.setInitExprent(exprs.get(0));
            loop.setIncExprent(exprs.get(1));
          } else if (exprs.size() == 3) {
            loop.setInitExprent(exprs.get(0));
            loop.setConditionExprent(exprs.get(1));
            loop.setIncExprent(exprs.get(2));
          }
        }
      }
    }

    root.setDummyExit(dummyExitStatement);
    DotExporter.toDotFile(root, "tapestry" + ++i);

    return root;
  }
  private static int i = 0;

  private static Statement makeFull(StatRec rec) {
    switch (rec.type) {
      case ROOT:
        return new RootStatement(rec.id);
      case SEQUENCE:
        return new SequenceStatement(rec.id);
      case DUMMY_EXIT:
        return new DummyExitStatement(rec.id);
      case GENERAL:
        throw new IllegalStateException("Can't deserialize general statements yet");
      case BASIC_BLOCK:
        return new BasicBlockStatement(rec.id);
      case IF:
        return new IfStatement(rec.id);
      case DO:
        return new DoStatement(rec.id);
      case SWITCH:
        throw new IllegalStateException("Can't deserialize switch statements yet");
      case SYNCHRONIZED:
        return new SynchronizedStatement(rec.id);
      case TRY_CATCH:
        return new CatchStatement(rec.id);
      case CATCH_ALL:
        return new CatchAllStatement(rec.id);
      default:
        throw new RuntimeException("Unknown type: " + rec.type);
    }
  }

  private static StatRec readStatement(String[] components) {
    int id = Integer.parseInt(components[0]);
    Statement.StatementType type = TYPES.get(components[1]);

    int parentId = -1;
    int firstId = -1;
    List<String> data = new ArrayList<>();
    for (int i = 2; i < components.length; i++) {
      String component = components[i];
      if (component.startsWith("P:")) {
        parentId = Integer.parseInt(component.substring(2));
      } else if (component.startsWith("F:")) {
        firstId = Integer.parseInt(component.substring(2));
      } else if (component.startsWith("D:")) {
        data.add(component.substring(2));
      }
    }

    return new StatRec(id, type, parentId, firstId, data);
  }

  private static EdgeRec readEdge(String[] components) {
    int sourceId = Integer.parseInt(components[0]);
    // components[1] is the arrow
    int destId = Integer.parseInt(components[2]);
    int type = StatEdge.fromEdgeTypeString(components[3]);

    EdgeRecBuilder builder = new EdgeRecBuilder();
    for (int i = 4; i < components.length; i++) {
      String component = components[i];
      if (component.startsWith("C:")) {
        builder.setClosureId(Integer.parseInt(component.substring(2)));
      } else if (component.startsWith("If:")) {
        builder.setIfId(Integer.parseInt(component.substring(3)));
      } else if (component.startsWith("IfE:")) {
        builder.setElseId(Integer.parseInt(component.substring(4)));
      } else if (component.startsWith("D:")) {
        builder.setDefaultId(Integer.parseInt(component.substring(2)));
      } else if (component.equals("L")) {
        builder.setLabeled(true);
      } else if (component.equals("E")) {
        builder.setExplicit(true);
      } else if (component.equals("I")) {
        builder.setCanInline(false);
      } else if (component.equals("P")) {
        builder.setPhantomContinue(true);
      }
    }

    return builder
      .setEdgeType(type)
      .setSourceId(sourceId)
      .setDestId(destId)
      .createEdgeRec();
  }

  private static Exprent readExpr(String[] components) {
    StringBuilder content = new StringBuilder();
    for (int i = 2; i < components.length; i++) {
      content.append(components[i]);

      if (i != components.length - 1) {
        content.append(" ");
      }
    }

    return ExprParser.parse(content.toString());
  }

  private static class StatRec {
    private final int id;
    private final Statement.StatementType type;
    private final int parentId;
    private final int firstId;
    private final List<String> data;

    private StatRec(int id, Statement.StatementType type, int parentId, int firstId, List<String> data) {
      this.id = id;
      this.type = type;
      this.parentId = parentId;
      this.firstId = firstId;
      this.data = data;
    }
  }

  static class EdgeRec {
    private final int edgeType;
    private final int sourceId;
    private final int destId;
    private final int closureId;

    private final boolean labeled;
    private final boolean explicit;
    private final boolean canInline;
    private final boolean phantomContinue;
    private final int ifId;
    private final int elseId;
    private final int defaultId;

    EdgeRec(int edgeType, int sourceId, int destId, int closureId, boolean labeled, boolean explicit, boolean canInline, boolean phantomContinue, int ifId, int elseId, int defaultId) {
      this.edgeType = edgeType;
      this.sourceId = sourceId;
      this.destId = destId;
      this.closureId = closureId;
      this.labeled = labeled;
      this.explicit = explicit;
      this.canInline = canInline;
      this.phantomContinue = phantomContinue;
      this.ifId = ifId;
      this.elseId = elseId;
      this.defaultId = defaultId;
    }
  }

  public static class EdgeRecBuilder {
      private int edgeType;
      private int sourceId;
      private int destId;
      private int closureId = -1;
      private boolean labeled;
      private boolean explicit;
      private boolean canInline = true;
      private boolean phantomContinue;
      private int ifId = -1;
      private int elseId = -1;
      private int defaultId = -1;

      public EdgeRecBuilder setEdgeType(int edgeType) {
          this.edgeType = edgeType;
          return this;
      }

      public EdgeRecBuilder setSourceId(int sourceId) {
          this.sourceId = sourceId;
          return this;
      }

      public EdgeRecBuilder setDestId(int destId) {
          this.destId = destId;
          return this;
      }

      public EdgeRecBuilder setClosureId(int closureId) {
          this.closureId = closureId;
          return this;
      }

      public EdgeRecBuilder setLabeled(boolean labeled) {
          this.labeled = labeled;
          return this;
      }

      public EdgeRecBuilder setExplicit(boolean explicit) {
          this.explicit = explicit;
          return this;
      }

      public EdgeRecBuilder setCanInline(boolean canInline) {
          this.canInline = canInline;
          return this;
      }

      public EdgeRecBuilder setPhantomContinue(boolean phantomContinue) {
          this.phantomContinue = phantomContinue;
          return this;
      }

      public EdgeRecBuilder setIfId(int ifId) {
          this.ifId = ifId;
          return this;
      }

      public EdgeRecBuilder setElseId(int elseId) {
          this.elseId = elseId;
          return this;
      }

      public EdgeRecBuilder setDefaultId(int defaultId) {
          this.defaultId = defaultId;
          return this;
      }

      public EdgeRec createEdgeRec() {
          return new EdgeRec(edgeType, sourceId, destId, closureId, labeled, explicit, canInline, phantomContinue, ifId, elseId, defaultId);
      }
  }
}
