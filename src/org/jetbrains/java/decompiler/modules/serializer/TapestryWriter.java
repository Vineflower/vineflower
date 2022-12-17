package org.jetbrains.java.decompiler.modules.serializer;

import org.jetbrains.java.decompiler.code.cfg.BasicBlock;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public final class TapestryWriter {
  // [] are replaced with the actual value
  // () are inlined comments
  // id [Type] P:[parent] F:[first] ...D:[Extra data]
  // id -> [dest] [EdgeType] C:[closure] L (flag for labeled, true if present) E (flag for explicit) I (canInline, inverted) P (phantomContinue)


  // Serializes the statement graph into a string delimited by newline (\n) characters.
  public static String serialize(RootStatement root) {
    StringBuilder sb = new StringBuilder();
    List<Statement> stats = new ArrayList<>();
    stats.add(root);
    findAllStats(stats, root);

    int maxId = stats.stream().map(s -> s.id).max(Integer::compare).orElse(0);
    // Make sure all spaces are aligned properly by padding
    int maxSpaces = charsInId(maxId);

    int maxNameId = 0;
    for (Statement stat : stats) {
      int nameId = stat.type.getPrettyId().length();
      if (nameId > maxNameId) {
        maxNameId = nameId;
      }
    }

    int maxEdgeId = 0;
    for (Statement stat : stats) {
      for (StatEdge edge : stat.getAllSuccessorEdges()) {
        int edgeId = edge.makeEdgeTypeString().length();
        if (edgeId > maxEdgeId) {
          maxEdgeId = edgeId;

          // 9 is the length of "BreakExit", the longest type string
          if (maxEdgeId == 9) {
            break;
          }
        }
      }

      if (maxEdgeId == 9) {
        break;
      }
    }

    for (Statement st : stats) {
      int spaces = charsInId(st.id) - 1;

      StringBuilder eb = new StringBuilder();

      eb.append(st.id)
        .append(" ".repeat(maxSpaces - spaces));

      String id = st.type.getPrettyId();
      eb.append(id);
      eb.append(" ".repeat(maxNameId - id.length()));

      if (st.getParent() != null) {
        eb.append(" P:").append(st.getParent().id);
        eb.append(" ".repeat(maxSpaces - charsInId(st.getParent().id)));
      }

      if (st.getFirst() != null) {
        eb.append(" F:").append(st.getFirst().id);
        eb.append(" ".repeat(maxSpaces - charsInId(st.getFirst().id)));
      }
      eb.append(" ");

      st.addToTapestry(s -> eb.append("D:").append(s).append(" "));

      sb.append(eb.toString().stripTrailing());

      sb.append("\n");
    }

    int maxDestSpacing = maxSpaces + 1 + 3 + maxSpaces;

    List<StatEdgeData> edges = stats.stream()
      .map(stat -> {
        List<StatEdge> es = stat.getAllSuccessorEdges();
        if (stat instanceof IfStatement) {
          IfStatement ifst = (IfStatement) stat;
          es.add(ifst.getIfEdge());
          es.add(ifst.getElseEdge());
        } else if (stat instanceof SwitchStatement) {
          SwitchStatement swst = (SwitchStatement) stat;
          es.add(swst.getDefaultEdge());
        }

        return es;
      })
      .flatMap(List::stream)
      .filter(Objects::nonNull)
      .distinct()
      .map(StatEdgeData::new)
      .collect(Collectors.toList());

    Map<StatEdge, StatEdgeData> edgeMap = edges.stream()
      .collect(Collectors.toMap(e -> e.edge, e -> e));

    for (Statement st : stats) {
      if (st instanceof IfStatement) {
        IfStatement ifStat = (IfStatement) st;
        edgeMap.get(ifStat.getIfEdge()).ifId = ifStat.id;
        edgeMap.get(ifStat.getIfEdge()).hasIfStat = ifStat.getIfstat() != null;

        if (ifStat.getElsestat() != null) {
          edgeMap.get(ifStat.getElseEdge()).elseId = ifStat.id;
        }
      }

      if (st instanceof SwitchStatement) {
        SwitchStatement switchStat = (SwitchStatement) st;
        edgeMap.get(switchStat.getDefaultEdge()).defaultId = switchStat.id;
      }
    }

    for (StatEdgeData ed : edges) {
      StatEdge e = ed.edge;

      StringBuilder eb = new StringBuilder();
      eb.append(e.getSource().id)
        .append(" ".repeat(maxSpaces - (charsInId(e.getSource().id) - 1)))
        .append("-> ")
        .append(e.getDestination().id);

      eb.append(" ".repeat(Math.max(1, maxDestSpacing - (eb.length() - 1))));
      String type = e.makeEdgeTypeString();
      eb.append(type);
      eb.append(" ".repeat(maxEdgeId - type.length()));

      if (e.closure != null) {
        eb.append(" C:").append(e.closure.id);
      } else {
        eb.append(" ".repeat(maxSpaces + 3));
      }

      if (e.labeled) {
        eb.append(" L");
      }

      if (e.explicit) {
        eb.append(" E");
      }

      // Can inline is inverted because it is true by default and only changed in a few places
      if (!e.canInline) {
        eb.append(" I");
      }

      if (e.phantomContinue) {
        eb.append(" P");
      }

      if (ed.ifId != -1) {
        if (ed.hasIfStat) {
          eb.append(" If:").append(ed.ifId);
        } else {
          eb.append(" IfN:").append(ed.ifId);
        }
      }

      if (ed.elseId != -1) {
        eb.append(" IfE:").append(ed.elseId);
      }

      if (ed.defaultId != -1) {
        eb.append(" D:").append(ed.defaultId);
      }

      sb.append(eb.toString().stripTrailing());

      sb.append("\n");
    }

    for (Statement st : stats) {
      if (st instanceof BasicBlockStatement) {
        if (st.getExprents() == null || st.getExprents().isEmpty()) {
          continue;
        }

        for (Exprent e : st.getExprents()) {
          serializeExpr(sb, maxSpaces, st, e);
        }
      }

      if (st instanceof IfStatement) {
        serializeExpr(sb, maxSpaces, st, ((IfStatement) st).getHeadexprent());
      }

      if (st instanceof DoStatement) {
        DoStatement loop = (DoStatement) st;

        switch (loop.getLooptype()) {
          case INFINITE:
            break;
          case WHILE:
          case DO_WHILE:
            serializeExpr(sb, maxSpaces, st, loop.getConditionExprent());
            break;
          case FOR:
            serializeExpr(sb, maxSpaces, st, loop.getInitExprent());
            serializeExpr(sb, maxSpaces, st, loop.getConditionExprent());
            serializeExpr(sb, maxSpaces, st, loop.getIncExprent());
            break;
          case FOR_EACH:
            serializeExpr(sb, maxSpaces, st, loop.getInitExprent());
            serializeExpr(sb, maxSpaces, st, loop.getIncExprent());
        }
      }
    }

    return sb.toString().stripTrailing();
  }

  private static void serializeExpr(StringBuilder sb, int maxSpaces, Statement st, Exprent e) {
    StringBuilder eb = new StringBuilder();

    eb.append(st.id);
    eb.append(" ".repeat(maxSpaces - (charsInId(st.id) - 1)));
    eb.append("< ");

    e.toTapestry(eb);

    sb.append(eb.toString().stripTrailing());

    sb.append("\n");
  }

  private static class StatEdgeData {
    private final StatEdge edge;
    private int ifId = -1;
    private int elseId = -1;
    private int defaultId = -1;
    private boolean hasIfStat = false;

    private StatEdgeData(StatEdge edge) {
      this.edge = edge;
    }
  }

  private static int charsInId(int st) {
    return Integer.toString(st).length();
  }

  public static void findAllStats(List<Statement> list, Statement root) {
    for (Statement stat : root.getStats()) {
      if (!list.contains(stat)) {
        list.add(stat);
      }

      if (stat instanceof IfStatement) {
        IfStatement ifs = (IfStatement) stat;

        if (ifs.getIfstat() != null && !list.contains(ifs.getIfstat())) {
          list.add(ifs.getIfstat());
        }

        if (ifs.getElsestat() != null && !list.contains(ifs.getElsestat())) {
          list.add(ifs.getElsestat());
        }
      }

      findAllStats(list, stat);
    }

    if (root instanceof RootStatement) {
      list.add(((RootStatement)root).getDummyExit());
    }
  }
}
