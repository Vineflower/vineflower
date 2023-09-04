package org.vineflower.kotlin.pass;

import org.jetbrains.java.decompiler.api.plugin.pass.Pass;
import org.jetbrains.java.decompiler.api.plugin.pass.PassContext;
import org.jetbrains.java.decompiler.modules.decompiler.MergeHelper;
import org.jetbrains.java.decompiler.modules.decompiler.SequenceHelper;
import org.jetbrains.java.decompiler.modules.decompiler.StatEdge;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.DoStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SequenceStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.vars.CheckTypesResult;
import org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.ArrayList;

public class KMergePass extends MergeHelper implements Pass {
  @Override
  public boolean run(PassContext ctx) {
    RootStatement root = ctx.getRoot();

    while (enhanceLoopsRec(root)) /**/;
    SequenceHelper.condenseSequences(root);

    return false;
  }

  private static boolean enhanceLoopsRec(Statement stat) {
    boolean res = false;

    for (Statement st : new ArrayList<>(stat.getStats())) {
      if (st.getExprents() == null) {
        res |= enhanceLoopsRec(st);
      }
    }

    if (stat instanceof DoStatement) {
      res |= enhanceLoop((DoStatement)stat);
    }

    return res;
  }

  private static boolean enhanceLoop(DoStatement stat) {
    DoStatement.Type oldloop = stat.getLooptype();

    switch (oldloop) {
      case INFINITE:

        // identify a while loop
        if (matchWhile(stat)) {
          if (!matchForEach(stat)) {
            matchFor(stat);
          }
        }
        else {
          // identify a do{}while loop
          //matchDoWhile(stat);
        }

        break;
      case WHILE:
        if (!matchForEach(stat)) {
          matchFor(stat);
        }
    }

    return (stat.getLooptype() != oldloop);
  }

  protected static boolean matchForEach(DoStatement stat) {
    AssignmentExprent firstDoExprent = null;
    AssignmentExprent[] initExprents = new AssignmentExprent[3];
    Statement firstData = null, preData = null, lastData = null;
    Exprent lastExprent = null;

    // search for an initializing exprent
    Statement current = stat;
    while (true) {
      Statement parent = current.getParent();
      if (parent == null) {
        break;
      }

      if (parent instanceof SequenceStatement) {
        if (current == parent.getFirst()) {
          current = parent;
        }
        else {
          preData = current.getNeighbours(StatEdge.TYPE_REGULAR, Statement.EdgeDirection.BACKWARD).get(0);
          preData = getLastDirectData(preData);
          if (preData != null && !preData.getExprents().isEmpty()) {
            int size = preData.getExprents().size();
            for (int x = 0; x < initExprents.length; x++) {
              if (size > x) {
                Exprent exprent = preData.getExprents().get(size - 1 - x);
                if (exprent instanceof AssignmentExprent) {
                  initExprents[x] = (AssignmentExprent)exprent;
                }
              }
            }
          }
          break;
        }
      }
      else {
        break;
      }
    }

    firstData = getFirstDirectData(stat.getFirst());
    if (firstData != null && firstData.getExprents().get(0) instanceof AssignmentExprent) {
      firstDoExprent = (AssignmentExprent)firstData.getExprents().get(0);
    }
    lastData = getLastDirectData(stat.getFirst());
    if (lastData != null && !lastData.getExprents().isEmpty()) {
      lastExprent = lastData.getExprents().get(lastData.getExprents().size() - 1);
    }

    if (stat.getLooptype() == DoStatement.Type.WHILE && initExprents[0] != null && firstDoExprent != null) {
      if (isIteratorCall(initExprents[0].getRight())) {

        InvocationExprent invc = (InvocationExprent)getUncast((initExprents[0]).getRight());
        if (invc.getClassname().contains("java/util/stream")) {
          return false;
        }

        if (!isHasNextCall(drillNots(stat.getConditionExprent())) ||
          !(firstDoExprent instanceof AssignmentExprent)) {
          return false;
        }

        AssignmentExprent ass = firstDoExprent;
        if ((!isNextCall(ass.getRight()) && !isNextUnboxing(ass.getRight())) || !(ass.getLeft() instanceof VarExprent)) {
          return false;
        }

        InvocationExprent next = (InvocationExprent)getUncast(ass.getRight());
        if (isNextUnboxing(next))
          next = (InvocationExprent)getUncast(next.getInstance());
        InvocationExprent hnext = (InvocationExprent)getUncast(drillNots(stat.getConditionExprent()));
        if (!(next.getInstance() instanceof VarExprent) ||
          !(hnext.getInstance() instanceof VarExprent) ||
          ((VarExprent)initExprents[0].getLeft()).isVarReferenced(stat, (VarExprent)next.getInstance(), (VarExprent)hnext.getInstance())) {
          return false;
        }

        // Casted foreach
        Exprent right = initExprents[0].getRight();
        if (right instanceof FunctionExprent) {
          FunctionExprent fRight = (FunctionExprent) right;
          if (fRight.getFuncType() == FunctionExprent.FunctionType.CAST) {
            right = fRight.getLstOperands().get(0);
          }

          if (right instanceof InvocationExprent) {
            return false;
          }
        }

        // Make sure this variable isn't used before
        if (isVarUsedBefore((VarExprent) ass.getLeft(), stat)) {
          return false;
        }

        InvocationExprent holder = (InvocationExprent)right;

        initExprents[0].getBytecodeRange(holder.getInstance().bytecode);
        holder.getBytecodeRange(holder.getInstance().bytecode);
        firstDoExprent.getBytecodeRange(ass.getLeft().bytecode);
        ass.getRight().getBytecodeRange(ass.getLeft().bytecode);
        if (stat.getIncExprent() != null) {
          stat.getIncExprent().getBytecodeRange(holder.getInstance().bytecode);
        }
        if (stat.getInitExprent() != null) {
          stat.getInitExprent().getBytecodeRange(ass.getLeft().bytecode);
        }

        stat.setLooptype(DoStatement.Type.FOR_EACH);
        stat.setInitExprent(ass.getLeft());
        stat.setIncExprent(holder.getInstance());
        preData.getExprents().remove(initExprents[0]);
        firstData.getExprents().remove(firstDoExprent);

        if (initExprents[1] != null && initExprents[1].getLeft() instanceof VarExprent &&
          holder.getInstance() instanceof VarExprent) {
          VarExprent copy = (VarExprent)initExprents[1].getLeft();
          VarExprent inc = (VarExprent)holder.getInstance();
          if (copy.getIndex() == inc.getIndex() && copy.getVersion() == inc.getVersion() &&
            !inc.isVarReferenced(stat.getTopParent(), copy) && !isNextCall(initExprents[1].getRight())) {
            preData.getExprents().remove(initExprents[1]);
            initExprents[1].getBytecodeRange(initExprents[1].getRight().bytecode);
            stat.getIncExprent().getBytecodeRange(initExprents[1].getRight().bytecode);
            stat.setIncExprent(initExprents[1].getRight());
          }
        }

        // Type of assignment- store in var for type calculation
        CheckTypesResult typeRes = ass.checkExprTypeBounds();
        if (typeRes != null && !typeRes.getLstMinTypeExprents().isEmpty()) {
          VarType boundType = typeRes.getLstMinTypeExprents().get(0).type;
          VarExprent var = (VarExprent) ass.getLeft();
          var.setBoundType(boundType);
        }

        return true;
      } else if (initExprents[1] != null) {
        if (!(firstDoExprent.getRight() instanceof ArrayExprent) || !(firstDoExprent.getLeft() instanceof VarExprent)) {
          return false;
        }

        if (!(lastExprent instanceof FunctionExprent)) {
          return false;
        }

        // Kotlin: Inverted indices 0 and 1 as kotlinc inverts the cases

        if (!(initExprents[1].getRight() instanceof ConstExprent) ||
          !(initExprents[0].getRight() instanceof FunctionExprent) ||
          !(stat.getConditionExprent() instanceof FunctionExprent)) {
          return false;
        }

        //FunctionExprent funcCond  = (FunctionExprent)drillNots(stat.getConditionExprent()); //TODO: Verify this is counter < copy.length
        FunctionExprent funcRight = (FunctionExprent)initExprents[0].getRight();
        FunctionExprent funcInc   = (FunctionExprent)lastExprent;
        ArrayExprent    arr       = (ArrayExprent)firstDoExprent.getRight();
        FunctionExprent.FunctionType incType = funcInc.getFuncType();

        if (funcRight.getFuncType() != FunctionExprent.FunctionType.ARRAY_LENGTH ||
          (incType != FunctionExprent.FunctionType.PPI && incType != FunctionExprent.FunctionType.IPP) ||
          !(arr.getIndex() instanceof VarExprent) ||
          !(arr.getArray() instanceof VarExprent)) {
          return false;
        }

        VarExprent index = (VarExprent)arr.getIndex();
        VarExprent array = (VarExprent)arr.getArray();
        Exprent countExpr = funcInc.getLstOperands().get(0);

        // Foreach over multi dimensional array initializers can cause this to not be a var exprent
        if (countExpr instanceof VarExprent) {
          VarExprent counter = (VarExprent) countExpr;

          if (counter.getIndex() != index.getIndex() ||
            counter.getVersion() != index.getVersion()) {
            return false;
          }

          if (counter.isVarReferenced(stat.getFirst(), index)) {
            return false;
          }
        }

        // Make sure this variable isn't used before
        if (isVarUsedBefore((VarExprent) firstDoExprent.getLeft(), stat)) {
          return false;
        }

        // Add bytecode offsets
        funcRight.getLstOperands().get(0).addBytecodeOffsets(initExprents[0].bytecode);
        funcRight.getLstOperands().get(0).addBytecodeOffsets(initExprents[1].bytecode);
        funcRight.getLstOperands().get(0).addBytecodeOffsets(lastExprent.bytecode);
        firstDoExprent.getLeft().addBytecodeOffsets(firstDoExprent.bytecode);
        firstDoExprent.getLeft().addBytecodeOffsets(initExprents[0].bytecode);

        stat.setLooptype(DoStatement.Type.FOR_EACH);
        stat.setInitExprent(firstDoExprent.getLeft());
        stat.setIncExprent(funcRight.getLstOperands().get(0));
        preData.getExprents().remove(initExprents[1]);
        preData.getExprents().remove(initExprents[0]);
        firstData.getExprents().remove(firstDoExprent);
        lastData.getExprents().remove(lastExprent);

        if (initExprents[2] != null && initExprents[2].getLeft() instanceof VarExprent) {
          VarExprent copy = (VarExprent)initExprents[2].getLeft();

          if (copy.getIndex() == array.getIndex() && copy.getVersion() == array.getVersion()) {
            preData.getExprents().remove(initExprents[2]);
            initExprents[2].getRight().addBytecodeOffsets(initExprents[2].bytecode);
            initExprents[2].getRight().addBytecodeOffsets(stat.getIncExprent().bytecode);
            stat.setIncExprent(initExprents[2].getRight());
          }
        }

        // Type of assignment- store in var for type calculation
        CheckTypesResult typeRes = firstDoExprent.checkExprTypeBounds();
        if (typeRes != null && !typeRes.getLstMinTypeExprents().isEmpty()) {
          VarType boundType = typeRes.getLstMinTypeExprents().get(0).type;
          VarExprent var = (VarExprent) firstDoExprent.getLeft();
          var.setBoundType(boundType);
        }

        return true;
      }
    }

    return false;
  }
}
