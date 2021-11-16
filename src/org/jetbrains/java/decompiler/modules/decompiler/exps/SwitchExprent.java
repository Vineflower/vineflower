package org.jetbrains.java.decompiler.modules.decompiler.exps;

import org.jetbrains.java.decompiler.main.collectors.BytecodeMappingTracer;
import org.jetbrains.java.decompiler.modules.decompiler.ExprProcessor;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.TextBuffer;

import java.util.BitSet;
import java.util.List;

public class SwitchExprent extends Exprent {
  private final SwitchStatement backing;
  private final VarType type;

  public SwitchExprent(SwitchStatement backing, VarType type) {
    super(EXPRENT_SWITCH);
    this.backing = backing;
    this.type = type;
  }

  @Override
  public TextBuffer toJava(int indent, BytecodeMappingTracer tracer) {
    // Validity checks
    if (!this.backing.isPhantom()) {
      throw new IllegalStateException("Switch expression backing statement isn't phantom!");
    }

    TextBuffer buf = new TextBuffer();

    VarType switchType = this.backing.getHeadexprentList().get(0).getExprType();

    buf.append(this.backing.getHeadexprentList().get(0).toJava(indent, tracer)).append(" {").appendLineSeparator();
    tracer.incrementCurrentSourceLine();
    for (int i = 0; i < this.backing.getCaseStatements().size(); i++) {

      Statement stat = this.backing.getCaseStatements().get(i);
      List<StatEdge> edges = this.backing.getCaseEdges().get(i);
      List<Exprent> values = this.backing.getCaseValues().get(i);

      boolean hasDefault = false;
      // As switch expressions can be compiled to a tableswitch, any gaps will contain a jump to the default element.
      // Switch expressions cannot have a case point to the same statement as the default, so we check for default first and don't check for cases if it exists [TestConstructorSwitchExpression1]

      // TODO: exhaustive switch on enum has a synthetic default edge of throw new IncompatibleClassChangeException()
      for (StatEdge edge : edges) {
        if (edge == this.backing.getDefaultEdge()) {
          buf.appendIndent(indent + 1).append("default -> ");
          hasDefault = true;
          break;
        }
      }

      boolean hasEdge = false;
      if (!hasDefault) {
        for (int j = 0; j < edges.size(); j++) {
          Exprent value = values.get(j);
          if (value == null) { // TODO: how can this be null? Is it trying to inject a synthetic case value in switch-on-string processing? [TestSwitchDefaultBefore]
            continue;
          }

          if (!hasEdge) {
            buf.appendIndent(indent + 1).append("case ");
          } else {
            buf.append(", ");
          }

          if (value instanceof ConstExprent) {
            value = value.copy();
            ((ConstExprent)value).setConstType(switchType);
          }
          if (value instanceof FieldExprent && ((FieldExprent)value).isStatic()) { // enum values
            buf.append(((FieldExprent)value).getName());
          }
          else {
            buf.append(value.toJava(indent, tracer));
          }

          hasEdge = true;
        }
      }

      if (hasEdge) {
        buf.append(" -> ");
      }

      boolean simple = true;
      if (stat.type != Statement.TYPE_BASICBLOCK) {
        simple = false;
      }

      if (stat.getExprents() != null && stat.getExprents().size() != 1) {
        simple = false;
      }

      if (simple) {
        Exprent exprent = stat.getExprents().get(0);

        if (exprent.type == Exprent.EXPRENT_YIELD) {
          Exprent content = ((YieldExprent) exprent).getContent();

          if (content.type == Exprent.EXPRENT_CONST) {
            ((ConstExprent)content).setConstType(this.type);
          }

          buf.append(content.toJava(indent, tracer).append(";"));
        } else if (exprent.type == Exprent.EXPRENT_EXIT) {
          ExitExprent exit = (ExitExprent) exprent;

          if (exit.getExitType() == ExitExprent.EXIT_THROW) {
            buf.append(exit.toJava(indent, tracer).append(";"));
          } else {
            throw new IllegalStateException("Can't have return in switch expression");
          }
        }
      } else {
        buf.append("{");
        buf.appendLineSeparator();
        tracer.incrementCurrentSourceLine();
        TextBuffer statBuf = stat.toJava(indent + 2, tracer);
        buf.append(statBuf);
        buf.appendIndent(indent + 1).append("}");
      }

      buf.appendLineSeparator();
      tracer.incrementCurrentSourceLine();
    }

    buf.appendIndent(indent).append("}");

    return buf;
  }

  @Override
  public VarType getExprType() {
    return this.type;
  }

  @Override
  public Exprent copy() {
    return new SwitchExprent(this.backing, this.type);
  }

  @Override
  protected List<Exprent> getAllExprents(List<Exprent> list) {
    return list;
  }

  @Override
  public void getBytecodeRange(BitSet values) {

  }
}
