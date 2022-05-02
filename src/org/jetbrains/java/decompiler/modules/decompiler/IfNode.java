package org.jetbrains.java.decompiler.modules.decompiler;

// Models an if statement, child if statements and their successors.
// Does not model grandchild if statements or successors of successors.
// Does **not** model if statements with else branches.
// Therefore the maximum amount of information that the root node can contain is:
// root [IfStatement] {
//   ifstat1 [IfStatement] {
//      ifstat2 [Any Statement]
//   }
//   elsestat2 [Any Statement]
// }
// elsestat1 [If Statement] {
//   ifstat3 [Any Statement]
// }
// elsestat3 [Any Statement]
// Note that an "elsestat" in this context is simply the stat we jump to when the condition is false, even if it's
// reachable from the if body (i.e. there should never need to be an "else" keyword in the source code).

import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;

import static org.jetbrains.java.decompiler.modules.decompiler.IfNode.EdgeType.*;

// UPDATE:
// to handle ternaries better, the root node now allows for the elsestat1 to be inside an else. It will then have an
// EdgeType.ELSE edge type. This shouldn't interfere with any other handling as they all check this edge type.
class IfNode {
  // The stat that this node refers to. Root node will be an if stat, child nodes can be any stat.
  final Statement value;
  IfNode succ0;
  EdgeType succ0Type;
  IfNode succ1;
  EdgeType succ1Type;

  IfNode(Statement value) {
    ValidationHelper.notNull(value);

    this.value = value;
  }

  void set0(IfNode ifNode, EdgeType indirect) {
    ValidationHelper.notNull(ifNode);
    ValidationHelper.notNull(indirect);

    this.succ0 = ifNode;
    this.succ0Type = indirect;
  }

  void set1(IfNode ifNode, EdgeType indirect) {
    ValidationHelper.notNull(ifNode);
    ValidationHelper.notNull(indirect);

    this.succ1 = ifNode;
    this.succ1Type = indirect;
  }

  enum EdgeType {
    DIRECT, // direct edge (regular)
    INDIRECT, // indirect edge (continue, break, implicit)
    ELSE // special case for the elseStat of the root node
  }

  static IfNode build(IfStatement stat, boolean stsingle) {
    IfNode res = new IfNode(stat);

    // if branch
    if (stat.getIfstat() == null) {
      res.set0(new IfNode(stat.getIfEdge().getDestination()), INDIRECT);
    } else {
      res.set0(buildSubIfNode(stat.getIfstat()), DIRECT);
    }

    // else branch
    if (stat.iftype == IfStatement.IFTYPE_IFELSE) {
      res.set1(buildSubIfNode(stat.getElsestat()), ELSE);
    } else {
      StatEdge edge = stat.getFirstSuccessor();
      if (stsingle || edge.getType() != StatEdge.TYPE_REGULAR) {
        res.set1(new IfNode(edge.getDestination()), INDIRECT);
      } else {
        res.set1(buildSubIfNode(edge.getDestination()), DIRECT);
      }
    }


    return res;
  }

  // will produce one of the following:
  // node [IfStatement] {
  //   succ0 [Any Statement] or [Goto]
  // }
  // succ1 [Goto]
  // or
  // node [Non-IfStatement] // or an if-else statement
  // succ0 [Goto] // or null if there isn't a successor
  private static IfNode buildSubIfNode(Statement statement) {
    IfNode ifnode = new IfNode(statement);
    if (statement.type == Statement.TYPE_IF && ((IfStatement) statement).iftype == IfStatement.IFTYPE_IF) {
      IfStatement ifStatement = (IfStatement) statement;
      ifnode.set0(new IfNode(ifStatement.getIfEdge().getDestination()), ifStatement.getIfstat() == null ? INDIRECT : DIRECT);
      ifnode.set1(new IfNode(ifStatement.getFirstSuccessor().getDestination()), INDIRECT);
      // note that the successor is always indirect, cause if it were direct, the 'if' should have been wrapped in a sequence
    } else if (statement.hasAnySuccessor()) {
      ifnode.set0(new IfNode(statement.getFirstSuccessor().getDestination()), INDIRECT);
    }
    return ifnode;
  }
}