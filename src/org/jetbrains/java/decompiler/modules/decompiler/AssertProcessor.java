// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.MethodProperties;
import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.util.ArrayList;
import java.util.List;

public final class AssertProcessor {

  private static final VarType CLASS_ASSERTION_ERROR = new VarType(CodeType.OBJECT, 0, "java/lang/AssertionError");

  public static boolean buildAssertions(RootStatement root) {
    if (!DecompilerContext.getOption(IFernflowerPreferences.DECOMPILE_ASSERTIONS)) {
      return false;
    }

    ClassWrapper wrapper = getHoldingClass();
    MethodProperties prop = wrapper.getMethodProperties("<clinit>", "()V");

    if (prop != null && prop.assertField != null) {
      String key = InterpreterUtil.makeUniqueKey(prop.assertField.getName(), prop.assertField.getDescriptor());

      boolean res = false;
      // For interface static initializers, remove empty ifs
      if (root.mt.getName().equals("<clinit>")) {
        res |= cleanInterfaceStaticInitializer(root, wrapper.getClassStruct().qualifiedName, key);
      }

      res |= replaceAssertions(root, wrapper.getClassStruct().qualifiedName, key);

      if (res) {
        SequenceHelper.condenseSequences(root);
        return true;
      }
    }

    return false;
  }

  public static ClassWrapper getHoldingClass() {
    ClassWrapper wrapper = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS_WRAPPER);

    if (wrapper.getClassStruct().hasModifier(CodeConstants.ACC_INTERFACE)) {
      ClassNode node = DecompilerContext.getContextProperty(DecompilerContext.CURRENT_CLASS_NODE);
      while (node.type != ClassNode.Type.ROOT) {
        node = node.parent;
      }

      for (ClassNode nd : node.nested) {
        if (nd.getWrapper() != null && nd.getWrapper().getClassStruct().isSynthetic()) {
          MethodProperties props = nd.getWrapper().getMethodProperties("<clinit>", "()V");

          if (props != null && props.assertField != null) {
            return nd.getWrapper();
          }
        }
      }
    }
    return wrapper;
  }

  private static boolean cleanInterfaceStaticInitializer(Statement stat, String className, String key) {
    boolean res = false;
    for (Statement st : new ArrayList<>(stat.getStats())) {
      res |= cleanInterfaceStaticInitializer(st, className, key);
    }

    if (stat instanceof IfStatement ifSt && ifSt.getIfstat() == null) {
      Exprent condition = ifSt.getHeadexprent().getCondition();
      // check for "<field> != false"
      if (condition instanceof FunctionExprent func && func.getFuncType() == FunctionType.NE && func.getLstOperands().get(0) instanceof FieldExprent field
        && className.equals(field.getClassname())
        && key.equals(InterpreterUtil.makeUniqueKey(field.getName(), field.getDescriptor().descriptorString))
        && func.getLstOperands().get(1) instanceof ConstExprent con && con.getIntValue() == 0) {
        stat.replaceWithEmpty();
        res = true;
      }
    }

    return res;
  }

  private static boolean replaceAssertions(Statement statement, String classname, String key) {

    boolean res = false;

    for (Statement st : new ArrayList<>(statement.getStats())) {
      res |= replaceAssertions(st, classname, key);
    }

    boolean replaced = true;
    while (replaced) {
      replaced = false;

      for (Statement st : new ArrayList<>(statement.getStats())) {
        // The parent will be set to null if no further processing is needed.
        if (st instanceof IfStatement && st.getParent() != null) {
          if (replaceAssertion(statement, (IfStatement)st, classname, key)) {
            replaced = true;
            break;
          }
        }
      }

      res |= replaced;
    }

    return res;
  }

  private static boolean replaceAssertion(Statement parent, IfStatement stat, String classname, String key) {

    boolean throwInIf = true;
    Statement ifstat = stat.getIfstat();
    // Since we're early on in processing, the bodies of the if statement may not be inlined yet, so we need to follow break edges.
    if (ifstat == null && stat.getIfEdge().getType() == StatEdge.TYPE_BREAK) {
      ifstat = stat.getIfEdge().getDestination();
    }
    InvocationExprent throwError = isAssertionError(ifstat);

    if (throwError == null) {
      //check else:
      Statement elsestat = stat.getElsestat();
      throwError = isAssertionError(elsestat);

      if (throwError == null) {
          return false;
      } else {
          throwInIf = false;
          ifstat = elsestat;
      }
    }

    AssertResult res = getAssertionExprent(stat, stat.getHeadexprent().getCondition().copy(), classname, key, throwInIf);
    if (!res.valid) {
      return false;
    }

    List<Exprent> lstParams = new ArrayList<>();

    Exprent ascond = null, retcond = null;
    if (res.exprent != null) {
      ascond = res.negate ? new FunctionExprent(FunctionType.BOOL_NOT, res.exprent, throwError.bytecode) : res.exprent;
      retcond = ascond;
    }


    lstParams.add(retcond == null ? ascond : retcond);
    if (!throwError.getLstParameters().isEmpty()) {
      lstParams.add(throwError.getLstParameters().get(0));
    }

    AssertExprent asexpr = new AssertExprent(lstParams);

    Statement newstat = BasicBlockStatement.create();
    Statement block = newstat;
    List<Exprent> exprs = new ArrayList<>();
    exprs.add(asexpr);
    newstat.setExprents(exprs);

    Statement first = stat.getFirst();

    if (stat.iftype == IfStatement.IFTYPE_IFELSE ||
      (!res.assignment && first.getExprents() != null && !first.getExprents().isEmpty())) {

      first.removeSuccessor(stat.getIfEdge());
      first.removeSuccessor(stat.getElseEdge());

      List<Statement> lstStatements = new ArrayList<>();
      if (first.getExprents() != null && !first.getExprents().isEmpty()) {
        lstStatements.add(first);
      }

      lstStatements.add(newstat);
      if (stat.iftype == IfStatement.IFTYPE_IFELSE) {
        if (throwInIf) {
          lstStatements.add(stat.getElsestat());
        } else {
          lstStatements.add(stat.getIfstat());
        }
      }

      SequenceStatement sequence = new SequenceStatement(lstStatements);
      sequence.setAllParent();

      for (int i = 0; i < sequence.getStats().size() - 1; i++) {
        sequence.getStats().get(i).addSuccessor(new StatEdge(StatEdge.TYPE_REGULAR,
                                                             sequence.getStats().get(i), sequence.getStats().get(i + 1)));
      }

      if (stat.iftype == IfStatement.IFTYPE_IFELSE || !throwInIf) {
        Statement stmts;
        if (throwInIf) {
          stmts = stat.getElsestat();
        }
        else {
          stmts = stat.getIfstat();
        }

        List<StatEdge> lstSuccs = stmts.getAllSuccessorEdges();
        if (!lstSuccs.isEmpty()) {
          StatEdge endedge = lstSuccs.get(0);
          if (endedge.closure == stat) {
            sequence.addLabeledEdge(endedge);
          }
        }
      }

      newstat = sequence;
    }

    // Remove the break->exit edges created by the throw in the if body
    for (StatEdge edge : ifstat.getAllSuccessorEdges()) {
      edge.remove();
    }

    // Move all break edges on the if statement onto the new block (rather than any constructed sequence)
    for (StatEdge edge : stat.getAllSuccessorEdges()) {
      if (edge.getType() == StatEdge.TYPE_BREAK) {
        edge.changeSource(block);
      }
    }

    newstat.getVarDefinitions().addAll(stat.getVarDefinitions());

    // For assignment asserts, do some more processing to properly replace the two ifs with the single assert.
    if (res.assignment) {
      for (StatEdge edge : stat.getAllSuccessorEdges()) {
        edge.remove();
      }

      stat.setParent(null);

      parent.replaceWith(newstat);
    } else {
      parent.replaceStatement(stat, newstat);
    }

    if (res.replaceParent) {
      if (!parent.getParent().getBasichead().getExprents().isEmpty()) {
        Statement pParent = parent.getParent();
        parent = new SequenceStatement(List.of(parent.getParent().getBasichead(), parent));
        parent.setParent(pParent);
      }
      parent.getParent().replaceWith(parent);
    }

    return true;
  }

  private static InvocationExprent isAssertionError(Statement stat) {
    if (stat == null || stat.getExprents() == null || stat.getExprents().size() != 1) {
      return null;
    }

    Exprent expr = stat.getExprents().get(0);

    if (expr instanceof ExitExprent) {
      ExitExprent exexpr = (ExitExprent)expr;
      if (exexpr.getExitType() == ExitExprent.Type.THROW && exexpr.getValue() instanceof NewExprent) {
        NewExprent nexpr = (NewExprent)exexpr.getValue();
        if (CLASS_ASSERTION_ERROR.equals(nexpr.getNewType()) && nexpr.getConstructor() != null) {
          return nexpr.getConstructor();
        }
      }
    }

    return null;
  }

  private static class AssertResult {
    private final Exprent exprent;
    private final boolean valid;
    private boolean replaceParent;
    private boolean negate = true;
    private boolean assignment = false;

    private AssertResult(Exprent exprent, boolean valid) {
      this.exprent = exprent;
      this.valid = valid;
    }
  }

  private static AssertResult getAssertionExprent(Statement stat, Exprent exprent, String classname, String key, boolean throwInIf) {
    if (exprent instanceof FunctionExprent fexpr) {
      if (!throwInIf) {
        // Check for single wrapped bool not
        if (fexpr.getFuncType() == FunctionType.BOOL_NOT) {
          exprent = fexpr.getLstOperands().get(0);
        }
      } else {
        // Check for double wrapped bool not
        if (fexpr.getFuncType() == FunctionType.BOOL_NOT) {
          if (fexpr.getLstOperands().get(0) instanceof FunctionExprent func && func.getFuncType() == FunctionType.BOOL_NOT) {
            if (func.getLstOperands().get(0) instanceof FunctionExprent f) {
              exprent = f;
            }
          }
        }
      }

      fexpr = (FunctionExprent)exprent;
      if (fexpr.getFuncType() == FunctionType.BOOLEAN_AND) {

        for (int i = 0; i < 2; i++) {
          Exprent param = fexpr.getLstOperands().get(i);

          if (isAssertionField(param, classname, key)) {
            return new AssertResult(fexpr.getLstOperands().get(1 - i), true);
          }
        }

        for (int i = 0; i < 2; i++) {
          Exprent param = fexpr.getLstOperands().get(i);

          AssertResult res = getAssertionExprent(stat, param, classname, key, throwInIf);
          if (res.valid) {
            if (param != res.exprent) {
              fexpr.getLstOperands().set(i, res.exprent);
            }

            return new AssertResult(fexpr, true);
          }
        }
      } else if (isAssertionField(fexpr, classname, key)) {
        // assert false;
        return new AssertResult(null, true);
      } else if (fexpr.getFuncType() == FunctionType.EQ) {
        // Switch expression assert

        Statement parent = parentSkippingSequences(stat);
        if (parent instanceof IfStatement) {
          // No head exprents, inline
          if (stat.getBasichead().getExprents().isEmpty()) {

            if (isAssertionField(((IfStatement) parent).getHeadexprent().getCondition(), classname, key)) {
              // Bool not will be propagated out
              AssertResult res = new AssertResult(fexpr, true);
              res.replaceParent = true;
              return res;
            }
          } else if (stat.getBasichead().getExprents().size() == 1) {
            // Check for "assert assertsEnabled = true;" pattern
            if (stat.getBasichead().getExprents().get(0) instanceof AssignmentExprent assign) {
              Exprent right = assign.getRight();
              if (fexpr.getLstOperands().get(0).equals(right) && fexpr.getLstOperands().get(1) instanceof ConstExprent con && con.getIntValue() == 0) {
                AssertResult res = new AssertResult(assign, true);
                res.negate = false;
                res.assignment = true;
                return res;
              }
            }
          }
        }
      }
    }

    // Nothing found?
    return new AssertResult(exprent, false);
  }

  private static Statement parentSkippingSequences(Statement stat) {
    stat = stat.getParent();

    while (stat instanceof SequenceStatement) {
      stat = stat.getParent();
    }

    return stat;
  }

  private static boolean isAssertionField(Exprent exprent, String classname, String key) {
    if (exprent instanceof FunctionExprent fparam) {
      // Check for double-wrapped bool not
      if (fparam.getFuncType() == FunctionType.BOOL_NOT) {
        if (fparam.getLstOperands().get(0) instanceof FunctionExprent func && func.getFuncType() == FunctionType.BOOL_NOT) {
          if (func.getLstOperands().get(0) instanceof FunctionExprent f) {
            fparam = f;
          }
        }
      }

      // The pattern is "<assert field> == false"
      if (fparam.getFuncType() == FunctionType.EQ &&
        fparam.getLstOperands().get(0) instanceof FieldExprent fdparam &&
        fparam.getLstOperands().get(1) instanceof ConstExprent con && con.getIntValue() == 0) {
        return classname.equals(fdparam.getClassname()) &&
          key.equals(InterpreterUtil.makeUniqueKey(fdparam.getName(), fdparam.getDescriptor().descriptorString));
      }
    }

    return false;
  }
}
