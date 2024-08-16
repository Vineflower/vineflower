package org.jetbrains.java.decompiler.modules.decompiler.decompose;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.modules.decompiler.decompose.DomBlocks.*;
import org.jetbrains.java.decompiler.struct.StructMethod;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.*;

import static org.jetbrains.java.decompiler.util.DotExporter.DOTS_FOLDER;
import static org.jetbrains.java.decompiler.util.DotExporter.getFile;

class DomTracer2 {
  private static final boolean COLLECT_STRINGS = false;
  private static final boolean COLLECT_DOTS = true;

  private static final boolean EXTENDED_MODE = false;
  private static final boolean STATEMENT_LR_MODE = false;
  private static final boolean SAME_RANK_MODE = true;

  private final String filePrefix;
  private final StructMethod structMethod;
  private String string = "";
  private int counter = 0;

  DomTracer2(String filePrefix, StructMethod structMethod) {
    this.filePrefix = filePrefix;
    this.structMethod = structMethod;
  }

  public void add(Collection<DomBlock> gen, String s, @Nullable String topLevel, @Nullable Map<DomBlock, String> props) {
    if (COLLECT_DOTS) {
      HashMap<DomBlock, String> map = props == null ? new HashMap<>() : new HashMap<>(props);
      if (topLevel != null) {
        if (!topLevel.contains("label=\"")) {
          topLevel = topLevel + ";label=\"" + s + "\"";
        }
      } else {
        topLevel = "label=\"" + s + "\"";
      }
      toDotFile(gen, this.structMethod, this.filePrefix, "g" + this.counter, map, topLevel);

      if (COLLECT_STRINGS) {
        this.string += "(g" + this.counter + ") ";
      }
    }

    if (COLLECT_STRINGS) {
      this.string += ("[" + gen + "] " + s + "\n");
    }

    this.counter++;
  }

  void error(Collection<DomBlock> stat, String s, Map<DomBlock, String> props) {
    this.add(stat, s, null, props);
  }

  void error(Collection<DomBlock> stat, String s, Collection<DomBlock> props) {
    Map<DomBlock, String> map = new HashMap<>();
    for (DomBlock domBlock : props) {
      map.put(domBlock, "fillcolor=coral1,style=filled");
    }
    this.add(stat, s, null, map);
  }

  void error(Collection<DomBlock> stat, String s) {
    this.add(stat, s, /* "fillcolor=coral1,style=filled" */ null, null);
  }

  void warn(Collection<DomBlock> stat, String s) {
    this.add(stat, s, /* "fillcolor=tan1,style=filled" */ null, null);
  }

  void info(Collection<DomBlock> stat, String s, Map<DomBlock, String> props) {
    this.add(stat, s, /* "fillcolor=lightblue,style=filled" */ null, props);
  }

  void info(Collection<DomBlock> stat, String s) {
    HashMap<DomBlock, String> map = new HashMap<>();
    for (DomBlock domBlock : stat) {
      map.put(domBlock, "fillcolor=lightblue,style=filled");
    }
    this.add(stat, s, null, map);
  }

  void success(List<DomBlock> stat, String s) {
    this.add(stat, s, /* "fillcolor=lawngreen, style=filled" */ null, null);
  }

  void successCreated(Collection<DomBlock> stat, String s, DomBlock newStat) {
    if (!COLLECT_DOTS) {
      this.add(List.of(newStat), s, "fillcolor=lawngreen,style=filled", null);
      return;
    }

    List<DomBlock> all = new ArrayList<>(stat);
    all.add(newStat);
    Map<DomBlock, String> props = new HashMap<>();
    for (var old : stat) {
      props.put(old, "fillcolor=orange,style=filled");
    }
    props.put(newStat, "fillcolor=lawngreen,style=filled");
//    for (DomBlock block : newStat.getRecursive()) {
//      props.put(block, "fillcolor=pink,style=filled");
//    }
    this.add(all, s, null, props);
  }

  @Override
  public String toString() {
    return this.string;
  }


  public static void toDotFile(Collection<DomBlock> doms, StructMethod mt, String subdirectory, String suffix, Map<DomBlock, String> extraProps, @Nullable String topLevel) {
    try {
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(getFile(DOTS_FOLDER, mt, subdirectory, suffix)));
//      try (var lock = DecompilerContext.getImportCollector().lock()) {
      out.write(statToDot(doms, suffix, extraProps, topLevel).getBytes());
//      }
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static String statToDot(Collection<DomBlock> doms, String name, Map<DomBlock, String> extraProps, @Nullable String topLevel) {
    StringBuilder builder = new StringBuilder();

    Set<DomBlock> visitedNodes = new HashSet<>();
    Set<DomBlock> exits = new HashSet<>();
    Set<DomBlock> referenced = new HashSet<>();

    builder.append("digraph ").append(name).append(" {\r\n");
    if (topLevel != null) {
      builder.append("  ").append(topLevel).append(";\r\n");
    }

    if (STATEMENT_LR_MODE) {
      builder.append("  rankdir = LR;\r\n");
    }

    LinkedHashSet<DomBlock> blocks = new LinkedHashSet<>();

    for (DomBlock domBlock : doms) {
      domBlock.getRecursive(blocks);
    }

    // Pre process
    Map<DomEdge, String> extraData = new HashMap<>();
    Set<DomEdge> extraDataSeen = new HashSet<>();

    for (DomBlock domBlock : blocks) {
      if (domBlock instanceof DomIfBlock ifBlock) {
        if (ifBlock.getIfEdge() != null) {
          extraData.put(ifBlock.getIfEdge(), "If Edge");
        }

        if (ifBlock.getElseEdge() != null) {
          extraData.put(ifBlock.getElseEdge(), "Else Edge");
        }
      }
//      else if (domBlock instanceof DomTryCatchBlock tryCatchBlock) {
//        for (var entry : tryCatchBlock.handlers.entrySet()) {
//          extraData.put(entry.getValue(), "Handler: " + String.join(", ", entry.getKey()));
//        }
//      }
      if (SAME_RANK_MODE && domBlock.getChildren().size() > 1 && domBlock instanceof DomSequenceBlock) {
        builder.append(" subgraph { rank = same; ");
        for (var s : domBlock.getChildren()) {
//          if (st instanceof SwitchStatement) {
//            if (s == st.getFirst()) {
//              continue;
//            }
//          }

          builder.append(s.getId()).append("; ");
        }
        builder.append("}\r\n");
      }
    }

    for (DomBlock domBlock : blocks) {
      String sourceId = domBlock.getId() + ""; // + (domBlock.getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty()?"":"000000");

      boolean edges = false;
      for (DomEdge edge : domBlock.getSuccessors()) {
        String destId = edge.getDestination().getId() + ""; //(edge.getDestination().getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty()?"":"000000");

        String edgeType = getEdgeType(edge);
        String meta = getEdgeMeta(edge);

        // Add extra edge data
        // TODO do same for predecessors?
        if (extraData.containsKey(edge)) {
          String data = extraData.get(edge);
          edgeType = edgeType == null ? data : edgeType + " (" + data + ")";
          extraDataSeen.add(edge);
        }

        if (edge.getClosure() != null) {
          edgeType = edgeType == null ? "Closure: " + edge.getClosure().getId() : edgeType + " (Closure: " + edge.getClosure().getId() + ")";
        }

        if (edge.getType() == DomEdgeType.CONTINUE) {
          // make the arrow go backwards, but the visually flip it again so that it looks like a forward arrow
          // This is to avoid continue edges messing up the layout
          builder.append(destId).append("->").append(sourceId);
        } else {
          builder.append(sourceId).append("->").append(destId);
        }
        builder
          .append(edgeType != null ? "[label=\"" + edgeType + "\", " + meta + "]" : "[" + meta + "]").append(";\n");

        if (EXTENDED_MODE && edge.getClosure() != null) {
          String clsId = edge.getClosure().getId() + ""; //(edge.getDestination().getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty()?"":"000000");
          builder.append(sourceId).append("->").append(clsId).append(" [arrowhead=diamond,label=\"Closure\"];\r\n");
        }

        if (/* edge.getType() == StatEdge.TYPE_FINALLYEXIT || */ edge.getType() == DomEdgeType.BREAK) {
          exits.add(edge.getDestination());
        }

        referenced.add(edge.getDestination());

        edges = true;
      }

      for (DomEdge edge : domBlock.getEnclosed()) {
        if (edge.getType() == DomEdgeType.CONTINUE) {
          continue;
        }

        String destId = edge.getDestination().getId() + ""; //(edge.getDestination().getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty()?"":"000000");

        builder.append(sourceId).append("->").append(destId).append(" [color=blue,style=dotted];\r\n");
      }

      if (EXTENDED_MODE) {
        for (var labelEdge : domBlock.getPredecessors()) {
          String src = labelEdge.getSource().getId() + ""; //(labelEdge.getSource().getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty() ? "" : "000000");
          String destId = labelEdge.getDestination().getId() + ""; //(labelEdge.getDestination().getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty() ? "" : "000000");
          String data = "";

          builder.append(src).append("->").append(destId).append(" [color=orange,label=\"Label Edge (").append(data).append(") (Contained by ").append(domBlock.getId()).append(")\"];\r\n");
        }
      }

      // Neighbor set is redundant

      if (EXTENDED_MODE) {
        for (var edge : domBlock.getPredecessors()) {
          String destId = edge.getSource().getId() + ""; // (edge.getSource().getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty() ? "" : "000000");

          String edgeType = getEdgeType(edge);

          builder.append(sourceId).append("->").append(destId).append("[color=blue").append(edgeType != null ? ",fontcolor=blue,label=\"" + edgeType + "\"" : "").append("];\r\n");

          referenced.add(edge.getSource());

          edges = true;
        }
      }

      // Graph tree
      for (var s : domBlock.getChildren()) {
        String destId = s.getId() + ""; //(s.getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty() ? "" : "000000");

        String label = "";
        builder.append(sourceId + " -> " + destId + " [weight=2, arrowhead=vee,color=red" + (!label.equals("") ? ",fontcolor=red,label=\"" + label + "\"" : "") + "];\r\n");
        referenced.add(s);
      }


      visitedNodes.add(domBlock);

      String extra = extraProps == null || !extraProps.containsKey(domBlock) ? "" : "," + extraProps.get(domBlock);

      String node = sourceId + " [shape=box,label=\"" + domBlock.getId() + " (" + getStatType(domBlock) + ")\\n" /* + toJava(domBlock)*/ + "\"" + /* (domBlock == stat ? ",color=red" : "") + */ extra + "];\n";
      builder.append(node);
    }

    // Exits
//    if (exit != null) {
//      builder.append(exit.id + " [color=green,label=\"" + exit.id + " (Canonical Return)\"];\n");
//      referenced.remove(exit.id);
//    }

//    for (Integer exit : exits) {
//      if (!visitedNodes.contains(exit)) {
//        buffer.append(exit + " [color=green,label=\"" + exit + " (Canonical Return)\"];\r\n");
//      }
//
//      referenced.remove(exit);
//    }

    referenced.removeAll(visitedNodes);

    // Unresolved statement references
    for (var ref : referenced) {
      if (ref instanceof DomExit) {
        builder.append(ref.getId() + " [color=red,label=\"" + ref.getId() + " EXIT\"];\r\n");
        continue;
      }
      builder.append(ref.getId() + " [color=red,label=\"" + ref.getId() + " (External reference: " + getStatType(ref) + ")\"];\r\n");
    }

    for (var labelEdge : extraData.keySet()) {
      if (extraDataSeen.contains(labelEdge)) {
        continue;
      }

      String src = labelEdge.getSource().getId() + ""; // (labelEdge.getSource().getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty() ? "" : "000000");
      String destId = labelEdge.getDestination().getId() + ""; // (labelEdge.getDestination().getSuccessorEdges(StatEdge.TYPE_EXCEPTION).isEmpty() ? "" : "000000");
      String label = "Floating extra edge: (" + extraData.get(labelEdge) + ")";

      builder.append(src + " -> " + destId + " [arrowhead=vee,color=red,fontcolor=red,label=\"" + label + "\"];\r\n");
    }

//    if (subgraph.size() > 0) {
//      buffer.append("subgraph cluster_non_parented {\r\n\tlabel=\"Isolated statements\";\r\n");
//
//      for (String s : subgraph) {
//        buffer.append("\t"+s);
//      }
//
//      buffer.append("\t}\r\n");
//    }

    builder.append("}");

    return builder.toString();
  }


  private static String getEdgeType(DomEdge edge) {
    return switch (edge.getType()) {
      case REGULAR -> null;
      case EXCEPTION -> "Exception";
      case BREAK -> "Break";
      case CONTINUE -> "Continue";
//      case DomEdgeType.FINALLYEXIT: return "Finally Exit";
//      default -> "Unknown Edge (composite?)";
      case UNKNOWN -> "RAW";
      case UNKNOWN_EXCEPTION -> "RAW (Exception)";
      case EXIT -> "RAW (Exit)";
    };
  }


  private static String getEdgeMeta(DomEdge edge) {
    return switch (edge.getType()) {
      case REGULAR -> "weight=1, color=black";
      case EXCEPTION -> "weight=1, color=orange, style=dashed";
      case BREAK -> "weight=0.4, color=blue";
      case CONTINUE -> "weight=0.3, color=green, dir=back";
//      case FINALLYEXIT -> return "weight=1, color=orange, style=dotted";
//      default: return "weight=1, color=purple";
      case UNKNOWN -> "weight=1, color=gray";
      case UNKNOWN_EXCEPTION -> "weight=1, color=gray, style=dashed";
      case EXIT -> "weight=1, color=red, style=dotted";
    };
  }


  private static String getStatType(DomBlock st) {
    if (st instanceof DomIfBlock) {
      return "If";
    } else if (st instanceof DomSequenceBlock) {
      return "Sequence";
    } else if (st instanceof DomLoopBlock) {
      return "Loop";
    } else if (st instanceof DomBasicBlock basicBlock) {
      return "Basic Block #" + basicBlock.block.getId();
    } else if (st instanceof DomCatchBlock catchBlock) {
      return "Catch " + String.join(", ", catchBlock.exceptionTypes);
    } else if (st instanceof DomTryCatchBlock) {
      return "Try Catch";
    } else {
      return "Unknown";
    }
  }

  public boolean isDotOn() {
    if (COLLECT_DOTS) {
      return true;
    } else {
      counter++; // to make sure that the counter matches in both cases
      return false;
    }
  }
}
