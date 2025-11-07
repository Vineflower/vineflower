package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.consts.PooledConstant;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.CodeType;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.TypeFamily;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.Pair;

import java.util.*;
import java.util.stream.Stream;

public final class SwitchPatternMatchProcessor {
  public static boolean processPatternMatching(Statement root) {
    boolean ret = processPatternMatchingRec(root, root);

    if (ret) {
      SequenceHelper.condenseSequences(root);
    }

    return ret;
  }

  private static boolean processPatternMatchingRec(Statement stat, Statement root) {
    ValidationHelper.validateStatement((RootStatement) root);

    boolean ret = false;
    for (Statement st : new ArrayList<>(stat.getStats())) {
      ret |= processPatternMatchingRec(st, root);
      ValidationHelper.validateStatement((RootStatement) root);
    }

    if (stat instanceof SwitchStatement) {
      ret |= processStatement((SwitchStatement) stat, root);
      ValidationHelper.validateStatement((RootStatement) root);
    }

    return ret;
  }

  private static boolean processStatement(SwitchStatement stat, Statement root) {
    if (stat.isPhantom()) {
      return false;
    }

    SwitchHeadExprent head = (SwitchHeadExprent) stat.getHeadexprent();

    boolean switchPatternMatch = isSwitchPatternMatch(head);

    if (!switchPatternMatch) {
      return false;
    }

    // Found switch pattern match, start applying basic transformations
    // replace `SwitchBootstraps.typeSwitch<...>(o, idx)` with `o`
    // if `idx` is used in one place, there's no guards and we can quickly remove it
    // otherwise, we need to look at every usage and eliminate guards
    InvocationExprent value = (InvocationExprent) head.getValue();
    List<Exprent> origParams = value.getLstParameters();
    Exprent realSelector = origParams.get(0);
    boolean guarded = true;
    boolean isEnumSwitch = value.getName().equals("enumSwitch");
    boolean nullCase = false;
    List<Pair<Statement, Exprent>> references = new ArrayList<>();
    if (origParams.get(1) instanceof VarExprent) {
      VarExprent var = (VarExprent) origParams.get(1);
      SwitchHelper.findExprents(root, Exprent.class, var::isVarReferenced, false, (st, expr) -> references.add(Pair.of(st, expr)));
      // If we have one reference...
      if (references.size() == 1) {
        // ...and its just assignment...
        Pair<Statement, Exprent> ref = references.get(0);
        if (ref.b instanceof AssignmentExprent) {
          // ...remove the variable
          ref.a.getExprents().remove(ref.b);
          guarded = false;
        }
      }
    }

    Map<List<Exprent>, Exprent> guards = new HashMap<>(0);
    if (guarded) {
      guards = new HashMap<>(references.size());
      // remove the initial assignment to 0
      boolean canEliminate = true;
      Pair<Statement, Exprent> initialUse = references.get(0);
      if (initialUse.b instanceof AssignmentExprent && ((AssignmentExprent) initialUse.b).getRight() instanceof ConstExprent) {
        ConstExprent constExprent = (ConstExprent) ((AssignmentExprent) initialUse.b).getRight();
        if (constExprent.getConstType().typeFamily == TypeFamily.INTEGER && constExprent.getIntValue() == 0) {
          references.remove(0);
        } else {
          return false;
        }
      } else {
        return false;
      }
      // check every assignment of `idx`
      for (Pair<Statement, Exprent> reference : references) {
        canEliminate &= eliminateGuardRef(stat, guards, reference, true);
      }
      if (!canEliminate) {
        return false;
      }
      initialUse.a.getExprents().remove(initialUse.b);
      for (Pair<Statement, Exprent> reference : references) {
        eliminateGuardRef(stat, guards, reference, false);
      }
    }

    for (int i = 0; i < stat.getCaseStatements().size(); i++) {
      Statement caseStat = stat.getCaseStatements().get(i);

      List<Exprent> allCases = stat.getCaseValues().get(i);
      Exprent caseExpr = allCases.get(0);

      // null expression = default case, can't be shared with patterns
      if (caseExpr == null) {
        continue;
      }

      if (guards.containsKey(allCases)) {
        // add the guard to the same index as this case, padding the list with nulls as necessary
        while (stat.getCaseGuards().size() <= i) {
          stat.getCaseGuards().add(null);
        }
        stat.getCaseGuards().set(i, guards.get(allCases));
      }
      if (caseExpr instanceof ConstExprent) {
        int caseValue = ((ConstExprent) caseExpr).getIntValue();

        // -1 always means null
        if (caseValue == -1) {
          nullCase = true;
          allCases.remove(caseExpr);
          ConstExprent nullConst = new ConstExprent(VarType.VARTYPE_NULL, null, null);
          // null can be shared with a pattern or default; put it at the end, but before default, to make sure it doesn't get
          // absorbed by the default or overwritten by a pattern
          if (allCases.contains(null)) {
            allCases.add(allCases.indexOf(null), nullConst);
          } else {
            allCases.add(nullConst);
          }
        }
      }

      // find the pattern variable assignment
      if (caseStat instanceof SequenceStatement) {
        Statement oldStat = caseStat;
        caseStat = caseStat.getStats().get(0);
        // we can end up with a SequenceStatement with 1 statement from guard `if` elimination, eliminate the sequence entirely
        if (oldStat.getStats().size() == 1) {
          oldStat.replaceWith(caseStat);
        }
      }

      PatternExprent pattern;
      if (stat.getCaseGuards().size() > i && stat.getCaseGuards().get(i) instanceof PatternExprent) {
        pattern = (PatternExprent) stat.getCaseGuards().set(i, null);
      } else {
        pattern = identifySwitchRecordPatternMatch(stat, stat.getCaseStatements().get(i), false);
      }

      if (pattern != null) {
        List<Exprent> operands = new ArrayList<>();
        operands.add(realSelector);
        operands.add(new ConstExprent(pattern.getExprType(), null, null));
        operands.add(pattern);

        FunctionExprent function = new FunctionExprent(FunctionType.INSTANCEOF, operands, null);
        allCases.set(0, function);
      } else {

        // make instanceof from assignment
        BasicBlockStatement caseStatBlock = caseStat.getBasichead();
        if (caseStatBlock.getExprents().size() >= 1) {
          Exprent expr = caseStatBlock.getExprents().get(0);
          if (expr instanceof AssignmentExprent) {
            AssignmentExprent assign = (AssignmentExprent) expr;

            if (assign.getLeft() instanceof VarExprent) {
              VarExprent var = (VarExprent) assign.getLeft();

              if (isPatternMatchingCastAssignment(head, assign)) {
                List<Exprent> operands = new ArrayList<>();
                if (assign.getRight() instanceof VarExprent check) {
                  if (caseExpr instanceof ConstExprent constExpr
                      && value.getBootstrapArguments().get(constExpr.getIntValue() == -1 ? value.getBootstrapArguments().size() - 1 : constExpr.getIntValue()) instanceof PrimitiveConstant primitive
                      && primitive.type == CodeConstants.CONSTANT_Class) {
                    operands.add(check); // checking var
                    operands.add(new ConstExprent(VarType.VARTYPE_CLASS, primitive.value, null));
                    operands.add(var); // pattern match var
                    if (constExpr.getIntValue() == -1 && allCases.size() == 2) {
                      allCases.remove(1);
                    }
                  }
                } else if (assign.getRight() instanceof FunctionExprent cast) {
                  operands.add(cast.getLstOperands().get(0)); // checking var
                  operands.add(cast.getLstOperands().get(1)); // type
                  operands.add(var); // pattern match var
                }

                if (!operands.isEmpty()) {
                  FunctionExprent func = new FunctionExprent(FunctionExprent.FunctionType.INSTANCEOF, operands, null);

                  caseStatBlock.getExprents().remove(0);

                  // TODO: ssau representation
                  // any shared nulls will be at the end, and patterns & defaults can't be shared,
                  // so its safe to overwrite whatever's here
                  allCases.set(0, func);
                }
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < stat.getCaseValues().size(); i++) {
      if (stat.getCaseValues().get(i).contains(null)) {
        // Default case statements are required to be last
        stat.getCaseValues().add(stat.getCaseValues().remove(i));
        stat.getCaseStatements().add(stat.getCaseStatements().remove(i));
        stat.getCaseEdges().add(stat.getCaseEdges().remove(i));
        if (i < stat.getCaseGuards().size()) {
          if (stat.getCaseGuards().get(i) != null) {
            while (stat.getCaseGuards().size() < stat.getCaseStatements().size()) {
              stat.getCaseGuards().add(null);
            }
            stat.getCaseGuards().add(stat.getCaseGuards().remove(i));
          } else {
            stat.getCaseGuards().remove(i);
          }
        }
        break;
      }
    }

    // go through bootstrap arguments to ensure types are correct & add enum/integer/string constants
    for (int i = 0; i < value.getBootstrapArguments().size(); i++) {
      PooledConstant bsa = value.getBootstrapArguments().get(i);
      // replace the constant with the value of i, which may not be at index i
      int replaceIndex = i;
      for (List<Exprent> caseValueSet : stat.getCaseValues()) {
        if (caseValueSet.get(0) instanceof ConstExprent) {
          ConstExprent constExpr = (ConstExprent) caseValueSet.get(0);
          if (constExpr.getValue() instanceof Integer && (Integer) constExpr.getValue() == i) {
            replaceIndex = stat.getCaseValues().indexOf(caseValueSet);
          }
        }
      }
      // either an integer, String, or Class
      if (bsa instanceof PrimitiveConstant) {
        PrimitiveConstant p = (PrimitiveConstant) bsa;
        Exprent newValue = null;
        switch (p.type) {
          case CodeConstants.CONSTANT_Integer:
            newValue = new ConstExprent((Integer) p.value, false, null);
            break;
          case CodeConstants.CONSTANT_String:
            if (isEnumSwitch) {
              String typeName = realSelector.getExprType().value;
              newValue = new FieldExprent(p.value.toString(), typeName, true, null, FieldDescriptor.parseDescriptor("L" + typeName + ";"), null, false, false);
            } else {
              newValue = new ConstExprent(VarType.VARTYPE_STRING, p.value, null);
            }
            break;
          case CodeConstants.CONSTANT_Class:
            // may happen if the switch head is a supertype of the pattern
            if (stat.getCaseValues().get(replaceIndex).stream().allMatch(x -> x instanceof ConstExprent)) {
              VarType castType = new VarType(CodeType.OBJECT, 0, (String) p.value);
              List<Exprent> operands = new ArrayList<>();
              operands.add(realSelector); // checking var
              operands.add(new ConstExprent(castType, null, null)); // type
              operands.add(new VarExprent(DecompilerContext.getCounterContainer().getCounterAndIncrement(CounterContainer.VAR_COUNTER),
                castType,
                DecompilerContext.getVarProcessor()));
              newValue = new FunctionExprent(FunctionExprent.FunctionType.INSTANCEOF, operands, null);
            }
            break;
        }
        if (newValue != null) {
          int ix = i;
          Exprent nvx = newValue;
          // make sure we replace the right constant, null can be shared with anything
          stat.getCaseValues().get(replaceIndex).replaceAll(u ->
            u instanceof ConstExprent
            && u.getExprType().typeFamily == TypeFamily.INTEGER
            && ((ConstExprent) u).getIntValue() == ix
              ? nvx : u);
        }
      }
    }

    if (guarded && stat.getParent() instanceof DoStatement) {
      // remove the enclosing while(true) loop of a guarded switch
      stat.getParent().replaceWith(stat);
      // FIXME: this replacement code looks wrong,
      //  doesn't get any coverage in tests
      // update continue-loops into break-switches
      for (StatEdge edge : stat.getPredecessorEdges(StatEdge.TYPE_CONTINUE)) {
        edge.changeType(StatEdge.TYPE_BREAK);
      }
    }

    // Try to inline:
    // var stackVar = ...
    // Objects.requireNonNull(stackVar)
    // var var1 = stackVar
    // switch (var1) {
    //
    // to:
    // switch (...) {

    Exprent oldSelector = realSelector;
    // inline head
    List<Exprent> basicHead = stat.getBasichead().getExprents();
    if (basicHead.isEmpty()) {
      List<StatEdge> edges = stat.getPredecessorEdges(StatEdge.TYPE_REGULAR);
      if (edges.size() == 1 && edges.get(0).getSource() instanceof BasicBlockStatement block) {
        basicHead = block.getExprents();
      }
    }
    if (realSelector instanceof VarExprent var && basicHead != null && basicHead.size() >= 1) {
      if (basicHead.get(basicHead.size() - 1) instanceof AssignmentExprent assignment && assignment.getLeft() instanceof VarExprent assigned) {
        if (var.equals(assigned) && !var.isVarReferenced(root,
            Stream.concat(
                Stream.of(assigned),
                stat.getCaseValues().stream()
                    .flatMap(List::stream)
                    .filter(exp -> exp instanceof FunctionExprent func && func.getFuncType() == FunctionType.INSTANCEOF && func.getLstOperands().get(0) instanceof VarExprent checked && checked.equals(var))
                    .map(exp -> (VarExprent) ((FunctionExprent) exp).getLstOperands().get(0)))
                .toArray(VarExprent[]::new))) {
          realSelector = assignment.getRight();
          basicHead.remove(basicHead.size() - 1);
        }
      }
    }

    // Check for non null
    if (basicHead != null && basicHead.size() >= 1 && realSelector instanceof VarExprent var && !nullCase) {
      Exprent last = basicHead.get(basicHead.size() - 1);
      AssignmentExprent stackAssignment = null;
      if (last instanceof InvocationExprent inv && inv.isStatic() && inv.getClassname().equals("java/util/Objects") && inv.getName().equals("requireNonNull") && inv.getStringDescriptor().equals("(Ljava/lang/Object;)Ljava/lang/Object;")) {
        VarExprent requireNonNullStackVar = null;
        if (inv.getLstParameters().get(0) instanceof VarExprent varExprent) {
          requireNonNullStackVar = varExprent;
        }
        if (basicHead.size() >= 2 && var.isStack() && !nullCase && basicHead.get(basicHead.size() - 2) instanceof AssignmentExprent assignment && assignment.getLeft() instanceof VarExprent assigned && var.equals(assigned) && !var.isVarReferenced(root, assigned, requireNonNullStackVar)) {
          stackAssignment = assignment;
        }
        if (var.equals(inv.getLstParameters().get(0)) || (inv.getLstParameters().get(0).getExprentUse() & Exprent.MULTIPLE_USES) != 0 && inv.getLstParameters().get(0).equals(stackAssignment.getRight())) {
          basicHead.remove(basicHead.size() - 1);
          // Check for other assignment
          if (basicHead.size() >= 1 && var.isStack() && !nullCase) {
            last = basicHead.get(basicHead.size() - 1);
            if (stackAssignment != null) {
              realSelector = stackAssignment.getRight();
              basicHead.remove(basicHead.size() - 1);
            }
          }
        }
      }
    }

    if (oldSelector != realSelector) {
      Exprent finalSelector = realSelector;
      // Replace the original selector with the new selector in instanceof check in case values
      stat.getCaseValues().stream()
          .flatMap(List::stream)
          .filter(Objects::nonNull)
          .filter(exp -> exp instanceof FunctionExprent func && func.getFuncType() == FunctionType.INSTANCEOF && func.getLstOperands().get(0).equals(oldSelector))
          .forEach(exp -> ((FunctionExprent) exp).getLstOperands().set(0, finalSelector));
    }

    head.setValue(realSelector); // SwitchBootstraps.typeSwitch(o, var1) -> o

    return true;
  }

  private static boolean eliminateGuardRef(SwitchStatement stat, Map<List<Exprent>, Exprent> guards, Pair<Statement, Exprent> reference, boolean simulate) {
    // a guard takes the form of exactly
    // `if (!guardCond) { idx = __thisIdx + 1; break; }`
    // at the start of that branch
    // alternatively, it can be inverted as `if (guardCond) { /* regular case code... */ break; } idx = __thisIdx + 1;`
    if (reference.b instanceof AssignmentExprent) {
      Statement assignStat = reference.a;

      // If a record pattern contains an `instanceof` check then a guard is used so
      // attempt to eliminate it
      for (int i = 0; i < stat.getCaseStatements().size(); i++) {
        if (stat.getCaseStatements().get(i).containsStatement(assignStat)) {
          Statement patternStat = stat.getCaseStatements().get(i);
          PatternExprent pattern = identifySwitchRecordPatternMatch(stat, patternStat, simulate);
          if (pattern != null) {
            if (!simulate) {
              guards.put(stat.getCaseValues().get(i), pattern);
            }
            return true;
          }
          break;
        }
      }

      // Note: This can probably be checked earlier
      if (assignStat.getAllPredecessorEdges().size() > 1) {
        return false;
      }
      // check if the assignment follows the guard layout
      Statement parent = assignStat.getParent();
      // sometimes the assignment is after the `if` and it's condition is inverted [see TestSwitchPatternMatchingInstanceof1/2/3]
      boolean invert = true;
      if (parent instanceof SequenceStatement && parent.getStats().size() == 2 && parent.getStats().get(1) == assignStat) {
        parent = parent.getStats().get(0);
        invert = false;
      }
      // the assignment should be alone in a basic block, contained in an `if`, contained in a sequence, within the `switch`
      if (assignStat instanceof BasicBlockStatement
          && assignStat.getExprents().size() == 1
          && parent instanceof IfStatement
          && ((IfStatement) parent).iftype == IfStatement.IFTYPE_IF
          && ((IfStatement) parent).getIfstat() != null
          && parent.getParent() instanceof SequenceStatement
          && parent.getParent().getParent() == stat) {
        StatEdge continueEdge = assignStat.getSuccessorEdges(StatEdge.TYPE_CONTINUE).get(0);
        Statement next = continueEdge.getDestination();
        if (next == stat.getParent()) {
          IfStatement guardIf = (IfStatement) parent;
          // the condition of the `if` is the guard condition (usually inverted)
          Exprent guardExprent = guardIf.getHeadexprent().getCondition();
          // find which case branch we're in (to assign the guard to)
          List<Statement> caseStatements = stat.getCaseStatements();
          for (int i = 0; i < caseStatements.size(); i++) {
            if (caseStatements.get(i).containsStatement(reference.a)) {
              if (simulate) {
                // we're not actually removing the guard yet
                return true;
              }
              // the assignment of the pattern variable may be inside the `if`, take it out and add it to the next statement
              List<Exprent> guardExprs = guardIf.getStats().get(0).getExprents();
              // the assignment might also just not exist, if the switch head is a supertype of the pattern
              List<Exprent> carryExprs = guardExprs.size() > 0 ? Collections.singletonList(guardExprs.get(0)) : Collections.emptyList();

              // remove the continue edge
              continueEdge.remove();

              // eliminate the guard `if`
              guardIf.getParent().getStats().remove(0);

              Statement nextStat;


              if (invert) {
                // normally the guard condition is inverted, re-invert it here
                guardExprent = new FunctionExprent(FunctionExprent.FunctionType.BOOL_NOT, guardExprent, guardExprent.bytecode);
                nextStat = guardIf.getParent().getStats().get(0);
              } else {
                nextStat = guardIf.getIfstat();
                // remove assignment
                guardIf.getParent().getStats().remove(0);
                guardIf.getParent().getStats().add(nextStat);
              }

              guards.put(stat.getCaseValues().get(i), guardExprent);
              guardIf.getParent().setFirst(nextStat);
              for (StatEdge edge : nextStat.getAllPredecessorEdges()) {
                edge.remove();
              }
              // add the pattern variable assignment (or case statement for inverted cases) to next statement
              nextStat.getBasichead().getExprents().addAll(0, carryExprs);
              return true;
            }
          }
        }
      } else if (parent == stat) {
        // an `&& false` guard leaves us with nothing but an assignment and break
        // get the branch we're in
        List<Statement> caseStatements = stat.getCaseStatements();
        for (int i = 0; i < caseStatements.size(); i++) {
          if (caseStatements.get(i).containsStatement(reference.a)) {
            if (simulate) {
              return true;
            }
            guards.put(stat.getCaseValues().get(i), new ConstExprent(0, true, null));
            Statement replaced = reference.a.replaceWithEmpty();
            replaced.getFirstSuccessor().remove();
            Set<StatEdge> labelEdges = stat.getParent().getLabelEdges();

            // This block is technically unreachable, but most code doesn't
            // really handle that. So a break edge, mirroring the others
            // is added
            boolean multipleSuccessors = false;
            Statement target = null;
            for (StatEdge edge : labelEdges) {
              if (edge.getType() != StatEdge.TYPE_BREAK) {
                continue;
              }

              if (target == null) {
                target = edge.getDestination();
              } else if (edge.getDestination() != target) {
                // inconsistent break targets
                multipleSuccessors = true;
                break;
              }
            }

            if (target != null && !multipleSuccessors) {
              // all breaks go to the same place, so we also add a break to there to help other stages
              replaced.addSuccessor(new StatEdge(StatEdge.TYPE_BREAK, replaced, target, stat.getParent()));
            } else {
              // no break targets, or multiple targets to use
              // TODO: figure out how to handle this
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  private static PatternExprent identifySwitchRecordPatternMatch(SwitchStatement stat, Statement statement, boolean simulate) {
    BasicBlockStatement head = statement.getBasichead();
    if (head.getExprents() == null || head.getExprents().isEmpty()) {
      return null;
    }

    Exprent first = head.getExprents().get(0);

    if (!(first instanceof AssignmentExprent assignment)) {
      return null;
    }

    if (!(assignment.getLeft() instanceof VarExprent assigned)) {
      return null;
    }
    PatternExprent pattern = IfPatternMatchProcessor.identifyRecordPatternMatch(stat, statement, assigned, assigned.getExprType(), simulate);
    if (pattern != null && !simulate) {
      head.getExprents().remove(0);
    }
    return pattern;
  }

  private static boolean isSwitchPatternMatch(SwitchHeadExprent head) {
    Exprent value = head.getValue();

    if (value instanceof InvocationExprent) {
      InvocationExprent invoc = (InvocationExprent) value;

      // TODO: test for SwitchBootstraps properly
      return invoc.getInvocationType() == InvocationExprent.InvocationType.DYNAMIC
             && (invoc.getName().equals("typeSwitch") || invoc.getName().equals("enumSwitch"));
    }

    return false;
  }

  public static boolean hasPatternMatch(RootStatement root) {
    return root.mt.getBytecodeVersion().hasSwitchPatternMatch() && DecompilerContext.getOption(IFernflowerPreferences.PATTERN_MATCHING);
  }

  private static boolean isPatternMatchingCastAssignment(final SwitchHeadExprent switchHead, final AssignmentExprent assignment) {
    if (assignment.getRight() instanceof VarExprent switchHeadRef) {
      if (switchHead.containsVar(switchHeadRef.getVarVersionPair())) {
        return true;
      }
    } else if (assignment.getRight() instanceof final FunctionExprent functionExprent) {
      if (functionExprent.getFuncType() != FunctionType.CAST) return false;

      final List<Exprent> lstOperands = functionExprent.getLstOperands();
      // We expect the assignment to be a literal `n = (Type) m`.
      // Any other operands are not allowed in simple pattern matching.
      if (lstOperands.size() < 2) return false;
      if (!(lstOperands.get(0) instanceof final VarExprent switchHeadRef)) return false;
      if (!(lstOperands.get(1) instanceof final ConstExprent castTypeRef)) return false;
      if (!switchHead.containsVar(switchHeadRef.getVarVersionPair())) return false; // Not the switch head var ref
      return true;
    }
    return false;
  }
}
