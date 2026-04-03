// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent.FunctionType;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.FieldDescriptor;
import org.jetbrains.java.decompiler.struct.gen.TypeFamily;
import org.jetbrains.java.decompiler.struct.gen.VarType;
import org.jetbrains.java.decompiler.util.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class SwitchHelper {
  public static boolean simplifySwitches(Statement stat, StructMethod mt, RootStatement root) {
    boolean ret = false;
    if (stat instanceof SwitchStatement) {
      ret = simplify((SwitchStatement)stat, mt, root);
    }

    for (int i = 0; i < stat.getStats().size(); i++) {
      ret |= simplifySwitches(stat.getStats().get(i), mt, root);
    }

    return ret;
  }

  private static boolean simplify(SwitchStatement switchStatement, StructMethod mt, RootStatement root) {
    if (simplifySwitchOnEnumJ21(switchStatement, root)) {
      return true;
    }

    SwitchHeadExprent switchHeadExprent = (SwitchHeadExprent)switchStatement.getHeadexprent();
    Exprent value = switchHeadExprent.getValue();
    ArrayExprent array = getEnumArrayExprent(value, root);
    if (array != null) {
      List<List<Exprent>> caseValues = switchStatement.getCaseValues();
      Map<Exprent, Exprent> mapping = new HashMap<>(caseValues.size());
      if (array.getArray() instanceof FieldExprent arrayField) {
        ClassesProcessor.ClassNode classNode = DecompilerContext.getClassProcessor().getMapRootClasses().get(arrayField.getClassname());
        if (classNode != null) {
          ClassWrapper classWrapper = classNode.getWrapper();
          if (classWrapper != null) {
            MethodWrapper wrapper = classWrapper.getMethodWrapper(CodeConstants.CLINIT_NAME, "()V");
            if (wrapper != null && wrapper.root != null) {
              // The enum array field's assignments if the field is built with a temporary local variable.
              // We need this to find the array field's values from the container class.
              List<AssignmentExprent> fieldAssignments = getAssignmentsOfWithinOneStatement(wrapper.root, arrayField);
              // If assigned more than once => not what we're looking for and discard the list
              if (fieldAssignments.size() > 1) {
                fieldAssignments.clear();
              }

              // Keep track of whether the assignment of the array field has already happened.
              // The same local variable might be used for multiple arrays (like with Kotlin, for example.)
              boolean[] fieldAssignmentEncountered = new boolean[] { false }; // single-element reference for lambdas

              wrapper.getOrBuildGraph().iterateExprents(exprent -> {
                if (exprent instanceof AssignmentExprent assignment) {
                  Exprent left = assignment.getLeft();
                  if (left instanceof ArrayExprent) {
                    Exprent assignmentArray = ((ArrayExprent) left).getArray();
                    // If the assignment target is a field, we have the assignment we want.
                    boolean targetsField = assignmentArray.equals(arrayField);

                    // If the target is a local variable, this gets more complicated.
                    // Kotlin (as mentioned above) creates its enum arrays by storing the array
                    // in a local first, so we need to check if the variable is later uniquely
                    // assigned to the enum array.
                    if (!targetsField && assignmentArray instanceof VarExprent && !fieldAssignmentEncountered[0]) {
                      for (AssignmentExprent fieldAssignment : fieldAssignments) {
                        if (fieldAssignment.getRight().equals(assignmentArray)) {
                          targetsField = true;
                          break;
                        }
                      }
                    }

                    if (targetsField && ((ArrayExprent) left).getIndex() instanceof InvocationExprent) {
                      mapping.put(assignment.getRight(), ((InvocationExprent) ((ArrayExprent) left).getIndex()).getInstance());
                    }
                  } else if (fieldAssignments.contains(exprent)) {
                    fieldAssignmentEncountered[0] = true;
                  }
                }
                return 0;
              });
            }
          }
        }
      } else { // Invocation
        InvocationExprent invocation = (InvocationExprent) array.getArray();
        ClassesProcessor.ClassNode classNode = DecompilerContext.getClassProcessor().getMapRootClasses().get(invocation.getClassname());
        if (classNode != null) {
          ClassWrapper classWrapper = classNode.getWrapper();
          if (classWrapper != null) {
            MethodWrapper wrapper = classWrapper.getMethodWrapper(invocation.getName(), "()[I");
            if (wrapper != null && wrapper.root != null) {
              wrapper.getOrBuildGraph().iterateExprents(exprent -> {
                if (exprent instanceof AssignmentExprent assignment) {
                  Exprent left = assignment.getLeft();
                  if (left instanceof ArrayExprent) {
                    mapping.put(assignment.getRight(), ((InvocationExprent) ((ArrayExprent) left).getIndex()).getInstance());
                  }
                }
                return 0;
              });
            }
          } else {
            // Need to wait til last minute processing
            return false;
          }
        }
      }

      boolean nullable = false;
      List<List<Exprent>> realCaseValues = new ArrayList<>(caseValues.size());
      for (List<Exprent> caseValue : caseValues) {
        List<Exprent> values = new ArrayList<>(caseValue.size());
        realCaseValues.add(values);
        cases:
        for (Exprent exprent : caseValue) {
          if (exprent == null) {
            values.add(null);
          }
          else {
            Exprent realConst = mapping.get(exprent);
            if (realConst == null) {
              if (exprent instanceof ConstExprent constLabel) {
                if (constLabel.getConstType().typeFamily == TypeFamily.INTEGER) {
                  int intLabel = constLabel.getIntValue();
                  // check for -1, used by nullable switches for the null branch
                  if (intLabel == -1) {
                    values.add(new ConstExprent(VarType.VARTYPE_NULL, null, null));
                    nullable = true;
                    continue;
                  }
                  // other values can show up in a `tableswitch`, such as in [-1, fall-through synthetic 0, 1, 2, ...]
                  // they must have a valid value later though
                  // TODO: more tests
                  for (Exprent key : mapping.keySet()) {
                    if (key instanceof ConstExprent
                      && ((ConstExprent) key).getConstType().typeFamily == TypeFamily.INTEGER
                      && ((ConstExprent) key).getIntValue() > intLabel) {
                      values.add(key.copy());
                      continue cases;
                    }
                  }
                }
              }
              root.addComment("$VF: Unable to simplify switch on enum", true);
              DecompilerContext.getLogger()
                .writeMessage("Unable to simplify switch on enum: " + exprent + " not found, available: " + mapping + " in method " + mt.getClassQualifiedName() + " " + mt.getName(),
                  IFernflowerLogger.Severity.ERROR);
              return false;
            }
            values.add(realConst.copy());
          }
        }
      }
      caseValues.clear();
      caseValues.addAll(realCaseValues);
      Exprent newExpr = ((InvocationExprent)array.getIndex()).getInstance().copy();
      switchHeadExprent.replaceExprent(value, newExpr);
      newExpr.addBytecodeOffsets(value.bytecode);

      // If we replaced the only use of the local var, the variable should be removed altogether.
      if (value instanceof VarExprent valueVar) {
        List<Pair<Statement, Exprent>> references = new ArrayList<>();
        findExprents(root, Exprent.class, valueVar::isVarReferenced, false, (stat, expr) -> references.add(Pair.of(stat, expr)));

        // If we only have one reference...
        if (references.size() == 1) {
          // ...and if it's just an assignment, remove it.
          Pair<Statement, Exprent> ref = references.get(0);
          if (ref.b instanceof AssignmentExprent && ((AssignmentExprent) ref.b).getLeft().equals(value)) {
            ref.a.getExprents().remove(ref.b);
          }
        }
      }

      // Java 17 preview uses a switch map and creates a synthetic variable if there is a null case
      BasicBlockStatement head = switchStatement.getBasichead();
      if (nullable
        && head.getExprents().size() > 0
        && head.getExprents().get(head.getExprents().size() - 1) instanceof AssignmentExprent assignment
        && assignment.getLeft() instanceof VarExprent tempVar
        && switchHeadExprent.getValue() instanceof VarExprent usedVar
        && tempVar.equalsVersions(usedVar)
        && !tempVar.isVarReferenced(root, usedVar)) {
        head.getExprents().remove(head.getExprents().size() - 1);
        switchHeadExprent.setValue(assignment.getRight());
      }

      return true;
    }

    return trySimplifyStringSwitch(switchStatement, value);
  }

  /**
   * Tries to simplify a string switch
   * @param switchStat the switch statement to simplify
   * @param switchHeadValue the value of the switch head exprent
   * @return true if simplified successfully, false otherwise
   */
  private static boolean trySimplifyStringSwitch(SwitchStatement switchStat, Exprent switchHeadValue) {
    // Get the type of switch by matching against each type
    Optional<StringSwitch> result = StringSwitch.match(switchStat);
    if (result.isEmpty()) {
      return false;
    }
    StringSwitch switchInfo = result.get();

    // Replace the target switch condition to the first.
    Exprent targetVal = ((SwitchHeadExprent) switchInfo.target().getHeadexprent()).getValue();
    switchInfo.target().getHeadexprent().replaceExprent(targetVal, ((InvocationExprent) switchHeadValue).getInstance());

    // If it's merged, we need to unwrap the statements inside each if (which are returns usually).
    // Especially in the case of hashcode collisions, we need to change the case values differently as well
    // Then we can return safely, because there's nothing else to do.
    if (switchInfo instanceof Merged) {
      if (!unwrapMergedSwitchCases(switchInfo)) {
        return false;
      }

      mergeDuplicateCaseStats(switchInfo);
      removeSyntheticDupVar(switchInfo);
      return true;
    }

    // Replace case values of target switch to the correct ones from first switch
    HashMap<Integer, List<Exprent>> caseMap = getStringSwitchCaseMap(switchInfo);
    List<List<Exprent>> realCaseValues = getStringSwitchRealCaseValues(switchInfo, caseMap);
    switchInfo.target().getCaseValues().clear();
    switchInfo.target().getCaseValues().addAll(realCaseValues);

    // Remove lingering variable that is now unused. This is the intermediate variable shared between both switches.
    BasicBlockStatement firstBasic = switchStat.getBasichead();
    List<Exprent> firstExprs = firstBasic.getExprents();
    if (firstExprs != null && !firstExprs.isEmpty()) {
      firstExprs.remove(firstExprs.size() - 1);
    }

    // Remove all connecting edges to the removed elements.
    firstBasic.getAllPredecessorEdges().forEach(firstBasic::removePredecessor);
    firstBasic.getAllSuccessorEdges().forEach(firstBasic::removeSuccessor);

    // Replace the target/replacement's basichead with the first's bc after replacing the switch,
    //   the basichead can sometimes be the target's, which means anything in the first basichead gets left behind.
    List<Exprent> targetExprs = switchInfo.target().getBasichead().getExprents();
    if (targetExprs != null && firstExprs != null) {
      targetExprs.addAll(firstExprs);
      firstExprs.clear();
    }

    // The switch statement being in the default block has a parent sequence we want to keep.
    Statement replacement = firstBasic;
    if (switchInfo instanceof InlineSplit) {
      replacement = switchInfo.target().getParent() instanceof SequenceStatement seqStat
        ? seqStat : switchInfo.target();
    }

    // Replace the entire first switch with the replacement
    replacement.getAllPredecessorEdges().forEach(replacement::removePredecessor);
    switchStat.replaceWith(replacement);

    if (switchInfo instanceof NullableSplit si) {
      // remove the containing `if` and not used edges
      Statement replaced = si.nullCheck().replaceWithEmpty();
      replaced.getLabelEdges().forEach(e -> {
        switchInfo.target().removePredecessor(e);
        e.removeClosure();
      });
    }

    // Remove phantom references from old switch statement.
    // Ignore the first statement as that has been extracted out of the switch.
    switchInfo.target().getAllPredecessorEdges()
      .stream()
      .filter(e -> switchStat.containsStatement(e.getSource()) && e.getSource() != firstBasic)
      .forEach(e -> e.getSource().removeSuccessor(e));

    removeSyntheticDupVar(switchInfo);

    return true;
  }

  private static boolean unwrapMergedSwitchCases(StringSwitch switchInfo) {
    int processedCasesIdx = 0;

    List<Statement> caseStats = switchInfo.first().getCaseStatements();
    int origCaseCount = caseStats.size();
    for (int i = 0; i < origCaseCount; i++) {
      Statement stat = caseStats.get(i);
      if (!(stat instanceof IfStatement rootIfStat)) {
        continue;
      }

      List<Exprent> exprs = rootIfStat.getIfstat().getExprents();
      if (exprs == null || exprs.size() != 1) {
        continue;
      }

      // If the only statement inside the if is a return
      if (!(exprs.get(0) instanceof ExitExprent exitExpr) || !exitExpr.getExitType().equals(ExitExprent.Type.RETURN)) {
        continue;
      }

      IfStatement currIf = rootIfStat;

      // Remove all case data for this case as we create new values/edges/stats later.
      switchInfo.first().getCaseEdges().get(processedCasesIdx).forEach(StatEdge::remove);
      switchInfo.first().getCaseEdges().remove(processedCasesIdx);
      switchInfo.first().getCaseValues().remove(processedCasesIdx);
      switchInfo.first().getCaseStatements().remove(processedCasesIdx);
      // First stat is always basichead
      switchInfo.first().getStats().remove(currIf);

      // Remove normal edge between case and default block if it exists
      if (currIf.hasSuccessor(StatEdge.TYPE_REGULAR)) {
        currIf.getSuccessorEdges(StatEdge.TYPE_REGULAR).get(0).remove();
      }

      // Process the if and optionally the if-else/else chain if it exists too
      while (currIf != null) {
        IfStatement elseStat = (IfStatement) currIf.getElsestat();

        // Remove break that connects from the if to the block after the switch if it exists
        if (currIf.hasSuccessor(StatEdge.TYPE_BREAK)) {
          currIf.getSuccessorEdges(StatEdge.TYPE_BREAK).get(0).remove();
        }

        // Disconnect any blocks connected to this if basichead (such as if stat, else stat, etc.)
        currIf.getBasichead().getAllSuccessorEdges().forEach(StatEdge::remove);

        // Create a new case for it
        ArrayList<Exprent> cases = new ArrayList<>();
        Exprent ifCond = currIf.getHeadexprent().getCondition();
        if (ifCond instanceof FunctionExprent condFunc) {
          for (Exprent oper : condFunc.getLstOperands()) {
            if (oper instanceof InvocationExprent condInvoc) {
              cases.add(condInvoc.getLstParameters().get(0));
            }
          }
        } else if (ifCond instanceof InvocationExprent condInvoc) {
          cases.add(condInvoc.getLstParameters().get(0));
        } else {
          return false;
        }

        switchInfo.first().addCase(processedCasesIdx, cases, currIf.getIfstat());

        processedCasesIdx++;
        currIf = elseStat;
      }
    }

    return processedCasesIdx > 0;
  }

  private static void mergeDuplicateCaseStats(StringSwitch switchInfo) {
    List<Statement> caseStats = switchInfo.first().getCaseStatements();
    for (int i = 0; i < caseStats.size(); i++) {
      Statement stat = caseStats.get(i);

      if (stat.getExprents() != null && stat.getExprents().size() != 1) {
        continue;
      }

      int j = i + 1;
      while (j < caseStats.size()) {
        Statement next = caseStats.get(j);
        // If they contain the same exact exprent, merge the next's case value into the current stat's case list
        if (next.getExprents() != null
          && !next.getExprents().isEmpty()
          && stat.getExprents().get(0).equals(next.getExprents().get(0))) {
          switchInfo.first().getCaseValues().get(i).addAll(switchInfo.first().getCaseValues().get(j));

          // Move case edge from jth to ith case
          StatEdge edge = switchInfo.first().getCaseEdges().get(j).get(0);
          edge.setDestination(switchInfo.first().getCaseStatements().get(i));
          List<StatEdge> newLst = new ArrayList<>(switchInfo.first().getCaseEdges().get(i));
          newLst.add(edge);
          switchInfo.first().getCaseEdges().set(i, newLst);

          switchInfo.first().removeCase(j);
        } else {
          j++;
        }
      }
    }
  }

  /**
   * Attempts to find the synthetic stack variable that can exist to be used by string switches.
   * @param switchInfo the switch info to use
   * @return a populated optional if found, otherwise an empty optional.
   */
  private static Optional<SyntheticDupVarResult> findSyntheticDupVar(StringSwitch switchInfo) {
    if (!(switchInfo.target().getHeadexprent() instanceof SwitchHeadExprent switchHead)) {
      return Optional.empty();
    }

    BasicBlockStatement head;
    if (switchInfo instanceof NullableSplit si) {
      head = si.nullCheck().getBasichead();
    } else {
      head = switchInfo.target().getBasichead();
    }

    List<Exprent> headExprs = head.getExprents();
    int dupVarIdx = switchInfo instanceof NullableSplit ? 2 : 1;
    if (headExprs == null || headExprs.size() < dupVarIdx) {
      return Optional.empty();
    }

    if (!(headExprs.get(headExprs.size() - dupVarIdx) instanceof AssignmentExprent assignment)
      || !(assignment.getLeft() instanceof VarExprent tmpVar)) {
      return Optional.empty();
    }
    Exprent realExpr = assignment.getRight();

    VarExprent headVar = (VarExprent) (switchHead.getValue() instanceof InvocationExprent val
      ? val.getInstance() : switchHead.getValue());

    // If the dup var is not in the switch head,
    //   and the dup var is referenced somewhere else other than the switch head...
    //   then check all case statements that are ifs that are equals and see if they all use the dup var
    if (!tmpVar.equalsVersions(headVar) && tmpVar.isVarReferenced(switchInfo.first().getParent(), headVar)) {
      boolean isDupVarUsedInAllCaseIfs = true;
      for (Statement stat : switchInfo.first().getCaseStatements()) {
        if (!(stat instanceof IfStatement ifStat)) {
          continue;
        }

        Exprent ifCond = ifStat.getHeadexprent().getCondition();
        if (ifCond instanceof FunctionExprent condFunc && condFunc.getFuncType() == FunctionType.NE) {
          ifCond = condFunc.getLstOperands().get(0);
        }

        if (!(ifCond instanceof InvocationExprent condInvoc)) {
          continue;
        }

        isDupVarUsedInAllCaseIfs = isDupVarUsedInAllCaseIfs && tmpVar.equalsVersions(condInvoc.getInstance());
      }

      // If the temp var is not in the switch head or the dup var is not used in the all the if conditions
      if (!tmpVar.equalsVersions(headVar) || !isDupVarUsedInAllCaseIfs) {
        return Optional.empty();
      }
    }

    return Optional.of(new SyntheticDupVarResult(switchHead, headExprs, dupVarIdx, tmpVar, realExpr));
  }

  /**
   * Removes all instances of the synthetic stack variable that is commonly found in string-switches.
   * @param switchInfo the switch info to use
   */
  private static void removeSyntheticDupVar(StringSwitch switchInfo) {
    Optional<SyntheticDupVarResult> _result = findSyntheticDupVar(switchInfo);
    if (_result.isEmpty()) {
      return;
    }
    SyntheticDupVarResult result = _result.get();

    // Replace synthetic var in switch head
    result.switchHead().replaceExprent(result.switchHead().getValue(), result.realVar());
    if (!(switchInfo instanceof NullableSplit)) {
      result.headExprs().remove(result.headExprs().size() - result.dupVarIdx());
    }
  }

  private record SyntheticDupVarResult(SwitchHeadExprent switchHead, List<Exprent> headExprs, int dupVarIdx,
                                       VarExprent tmpVar, Exprent realVar) {}

  /**
   * Checks that the given exprent is a return with a constant value.
   *
   * @param expr exprent to check
   * @return true if matched otherwise false
   */
  private static boolean isConstReturn(Exprent expr) {
    return expr instanceof ExitExprent exitExprent
      && exitExprent.getExitType() == ExitExprent.Type.RETURN
      && exitExprent.getValue() instanceof ConstExprent;
  }

  /**
   * Checks that the given exprent is a const assignment and that the assignment variable matches the var exprent.
   *
   * @param expr exprent to check
   * @param varExpr var exprent to check
   * @return true if matched otherwise false
   */
  private static boolean isConstAssignWithVar(Exprent expr, VarExprent varExpr) {
    return expr instanceof AssignmentExprent assignExpr
      && assignExpr.getRight() instanceof ConstExprent
      && assignExpr.getLeft().equals(varExpr);
  }

  /**
   * Gets a case map for the given string switch.
   *
   * @param switchInfo the switch info
   * @return the case map
   */
  private static HashMap<Integer, List<Exprent>> getStringSwitchCaseMap(StringSwitch switchInfo) {
    HashMap<Integer, List<Exprent>> caseMap = new HashMap<>();
    HashMap<Integer, List<Exprent>> caseRetMap = new HashMap<>();

    for (int i = 0; i < switchInfo.first().getCaseStatements().size(); ++i) {
      Statement currStat = switchInfo.first().getCaseStatements().get(i);
      while (currStat instanceof IfStatement ifStat) {
        Exprent condition = ifStat.getHeadexprent().getCondition();

        if (condition instanceof FunctionExprent condFunc && condFunc.getFuncType() == FunctionType.NE) {
          condition = condFunc.getLstOperands().get(0);
        }

        if (condition instanceof InvocationExprent condInvoc && condInvoc.getLstParameters().size() == 1) {
          List<Exprent> ifExprs = ifStat.getIfstat().getExprents();
          Exprent ifEqFirstExpr = ifExprs.get(0);
          Exprent realVal = condInvoc.getLstParameters().get(0);

          // Split switches are guaranteed to have a unique intermediary even with hashcode collision.
          // Merged switches, however, may contain multiple case labels/strings sharing 1 intermediary.
          //   This is commonly found where switches have explicit return values.
          if (ifEqFirstExpr instanceof AssignmentExprent assignExpr) {
            int intermediate = ((ConstExprent) assignExpr.getRight()).getIntValue();
            caseMap.computeIfAbsent(intermediate, ArrayList::new).add(realVal);
          } else if (ifEqFirstExpr instanceof ExitExprent) {
            List<Exprent> currCaseVal = switchInfo.first().getCaseValues().get(i);
            for (Exprent val : currCaseVal) {
              int hashCode = ((ConstExprent) val).getIntValue();
              caseMap.computeIfAbsent(hashCode, ArrayList::new).add(realVal);
              caseRetMap.computeIfAbsent(hashCode, ArrayList::new).add(val);
            }
          }
        }

        currStat = ifStat.getElsestat();
      }
    }

    if (switchInfo instanceof NullableSplit si) {
      int oldVal = ((ConstExprent) si.expr().getRight()).getIntValue();
      ConstExprent val = new ConstExprent(VarType.VARTYPE_NULL, null, null);
      caseMap.computeIfAbsent(oldVal, ArrayList::new).add(val);
    }

    return caseMap;
  }

  private static List<List<Exprent>> getStringSwitchRealCaseValues(StringSwitch switchInfo,
                                                                   HashMap<Integer, List<Exprent>> caseMap) {
    List<List<Exprent>> realCaseValues = new ArrayList<>();

    for (int i = 0; i < switchInfo.target().getCaseValues().size(); i++) {
      ArrayList<Exprent> realCaseVal = new ArrayList<>();

      List<Exprent> targetCaseVal = switchInfo.target().getCaseValues().get(i);
      List<StatEdge> targetCaseEdges = switchInfo.target().getCaseEdges().get(i);

      for (int j = 0; j < targetCaseVal.size(); j++) {
        Exprent targetVal = targetCaseVal.get(j);
        StatEdge targetEdge = targetCaseEdges.get(j);

        Integer intermediate = targetVal instanceof ConstExprent e ? e.getIntValue() : null;

        // If the real return val exists (in the case of merged switches usually) then create new cases for them.
        List<Exprent> realVal = caseMap.get(intermediate);
        // If the real case value is null, it means this case wasn't linked between
        //   both first and second switches and is thus an invalid case (can be safely removed).
        // As noted from Jasmine (adf75bb):
        //   If the second switch is a tableswitch, it can have a synthetic value that jumps to the default label
        //   to fill out the table, even if this value is unreachable from the preceding switch.
        //   See TestSwitchDefaultBefore for an example.
        if (realVal == null) {
          // Synthetic values jump to the same place always (default label).
          if (targetEdge != switchInfo.target().getDefaultEdge()
            && targetEdge.getDestination() == switchInfo.target().getDefaultEdge().getDestination()) {
            targetCaseEdges.remove(j);
            targetCaseVal.remove(j);
            j--;
          } else {
            // Default case requires a case val with only null.
            realCaseVal.add(null);
          }

          continue;
        }

        realCaseVal.addAll(realVal);
      }

      realCaseValues.add(realCaseVal);
    }

    return realCaseValues;
  }

  private static boolean simplifySwitchOnEnumJ21(SwitchStatement switchSt, RootStatement root) {
    SwitchHeadExprent head = (SwitchHeadExprent) switchSt.getHeadexprent();
    Exprent inner = head.getValue();

    Map<ConstExprent, String> mapping = new HashMap<>();
    List<List<Exprent>> values = switchSt.getCaseValues();
    for (List<Exprent> list : values) {
      for (Exprent v : list) {
        if (v == null) {
          continue; // Default case - fine
        }

        if (!(v instanceof ConstExprent)) {
          return false; // no const? can't do anything
        }

        if (v.getExprType().typeFamily != TypeFamily.INTEGER) {
          return false; // no integer, can't process
        }

        mapping.put((ConstExprent) v, null);
      }
    }

    // Best guess based on the ordinal
    if (inner instanceof InvocationExprent invInner && invInner.getName().equals("ordinal")) {
      StructClass classStruct = DecompilerContext.getStructContext().getClass(invInner.getClassname());
      if (classStruct == null) {
        root.addComment("$VF: Unable to simplify switch-on-enum, as the enum class was not able to be found.", true);
        return false;
      }

      // Check for enum
      if ((classStruct.getAccessFlags() & CodeConstants.ACC_ENUM) == CodeConstants.ACC_ENUM) {
        List<String> enumNames = new ArrayList<>();

        // Capture fields
        for (StructField fd : classStruct.getFields()) {
          if ((fd.getAccessFlags() & CodeConstants.ACC_ENUM) == CodeConstants.ACC_ENUM) {
            enumNames.add(fd.getName());
          }
        }

        for (ConstExprent e : new HashSet<>(mapping.keySet())) {
          for (List<Exprent> lst : values) {
            for (int i = 0; i < lst.size(); i++) {
              Exprent ex = lst.get(i);

              if (e == ex) {
                // now do the replacement
                int idx = e.getIntValue();
                String name = enumNames.get(idx);
                lst.set(i, new FieldExprent(name, invInner.getClassname(), true, null, FieldDescriptor.parseDescriptor("L" + invInner.getClassname() + ";"), null));
              }
            }
          }
        }

        // Success! now let's clean it up. Remove "default -> throw new MatchException", if it exists
        // Only do this for switch expression (phantom) switches. Otherwise we might accidentally obliterate
        // definite assignment for a variable.
        if (switchSt.isPhantom()) {
          for (int i = 0; i < values.size(); i++) {
            List<Exprent> list = values.get(i);

            if (list.size() == 1 && list.get(0) == null) { // default by itself
              Statement st = switchSt.getCaseStatements().get(i);
              if (IfPatternMatchProcessor.isStatementMatchThrow(st)) {
                // Replace it with an empty block
                st.replaceWithEmpty();
              }
            }
          }
        }

        // Now replace the 'var.ordinal()' with 'var'
        head.replaceExprent(inner, invInner.getInstance());

        return true;
      }
    }

    return false;
  }

  public static final int STATIC_FINAL_SYNTHETIC = CodeConstants.ACC_STATIC | CodeConstants.ACC_FINAL | CodeConstants.ACC_SYNTHETIC;
  /**
   * When Java introduced Enums they added the ability to use them in Switch statements.
   * This was done in a pure syntax sugar way using the old switch on int methods.
   * The compiler creates a synthetic class with a static int array field.
   * To support enums changing post compile, It initializes this field with a length of the current enum length.
   * And then for every referenced enum value it adds a mapping in the form of:
   *   try {
   *     field[Enum.VALUE.ordinal()] = 1;
   *   } catch (FieldNotFoundException e) {}
   * <p>
   * If a class has multiple switches on multiple enums, the compiler adds the init and try list to the BEGINNING of the static initalizer.
   * But they add the field to the END of the fields list.
   * <p>
   * Note: SOME compilers name the field starting with $SwitchMap, so if we do not have full context this can be a guess.
   * But Obfuscated/renamed code could cause issues
   */
  private static boolean isEnumArray(Exprent exprent) {
    if (exprent instanceof ArrayExprent arr) {
      Exprent tmp = arr.getArray();
      if (tmp instanceof FieldExprent field) {
        Exprent index = arr.getIndex();
        ClassesProcessor.ClassNode classNode = DecompilerContext.getClassProcessor().getMapRootClasses().get(field.getClassname());

        if (classNode == null || !"[I".equals(field.getDescriptor().descriptorString)) {
          // TODO: tighten up this check to avoid false positives
          return field.getName().startsWith("$SwitchMap") || //This is non-standard but we don't have any more information so..
            (index instanceof InvocationExprent && ((InvocationExprent) index).getName().equals("ordinal")) && field.isStatic();
        }

        StructField stField;
        if (classNode.getWrapper() == null) { // I have no idea why this happens, according to debug tests it doesn't even return null
          stField = classNode.classStruct.getField(field.getName(), field.getDescriptor().descriptorString);
        } else {
          stField = classNode.getWrapper().getClassStruct().getField(field.getName(), field.getDescriptor().descriptorString);
        }

        if ((stField.getAccessFlags() & STATIC_FINAL_SYNTHETIC) != STATIC_FINAL_SYNTHETIC) {
          return false;
        }

        boolean isSyntheticClass;
        if (classNode.getWrapper() == null) {
          isSyntheticClass = (classNode.classStruct.getAccessFlags() & CodeConstants.ACC_SYNTHETIC) == CodeConstants.ACC_SYNTHETIC;
        } else {
          isSyntheticClass = (classNode.getWrapper().getClassStruct().getAccessFlags() & CodeConstants.ACC_SYNTHETIC) == CodeConstants.ACC_SYNTHETIC;
        }

        if (isSyntheticClass) {
          return true; //TODO: Find a way to check the structure of the initializer?
          //Exprent init = classNode.getWrapper().getStaticFieldInitializers().getWithKey(InterpreterUtil.makeUniqueKey(field.getName(), field.getDescriptor().descriptorString));
          //Above is null because we haven't preocess the class yet?
        }
      } else if (tmp instanceof InvocationExprent inv) {
        if (inv.getName().startsWith("$SWITCH_TABLE$")) { // More nonstandard behavior. Seems like eclipse compiler stuff: https://bugs.eclipse.org/bugs/show_bug.cgi?id=544521 TODO: needs tests!
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Gets the enum array exprent (or null if not found) corresponding to
   * the switch head. If the switch head itself is an enum array, returns the head.
   * If it's a variable only assigned to an enum array, returns that array.
   */
  private static ArrayExprent getEnumArrayExprent(Exprent switchHead, RootStatement root) {
    Exprent candidate = switchHead;

    if (switchHead instanceof FunctionExprent func) {
      // Check for switches on a ternary expression like `a != null ? ...SwitchMap[a.ordinal()] : -1` (nullable switch)
      if (func.getFuncType() == FunctionType.TERNARY && func.getLstOperands().size() == 3) {
        List<Exprent> ops = func.getLstOperands();
        if (ops.get(0) instanceof FunctionExprent nn) {
          if (nn.getFuncType() == FunctionType.NE
            && nn.getLstOperands().get(0) instanceof VarExprent
            && nn.getLstOperands().get(1).getExprType().equals(VarType.VARTYPE_NULL)) {
            // TODO: consider if verifying the variable used is necessary
            // probably not, since the array is checked to be generated (?) so user written code shouldn't encounter bad resugaring
            if (ops.get(2) instanceof ConstExprent minusOne) {
              if (minusOne.getConstType().equals(VarType.VARTYPE_INT) && minusOne.getIntValue() == -1) {
                candidate = ops.get(1);
              }
            }
          }
        }
      }
    }

    if (switchHead instanceof VarExprent var) {
      // Check for switches with intermediary assignment of enum array index
      // This happens with Kotlin when expressions on enums.

      if (!"I".equals(var.getVarType().toString())) {
        // Enum array index must be int
        return null;
      }

      List<AssignmentExprent> assignments = getAssignmentsOfWithinOneStatement(root, var);

      if (!assignments.isEmpty()) {
        if (assignments.size() == 1) {
          AssignmentExprent assignment = assignments.get(0);
          candidate = assignment.getRight();
        } else {
          // more than 1 assignment to variable => can't be what we're looking for
          return null;
        }
      }
    }

    return isEnumArray(candidate) ? (ArrayExprent) candidate : null;
  }

  /**
   * Recursively searches for assignments of the target that happen within one statement.
   * This is done as a list because the intended outcomes of the "1 found" (unique) and "2+ found" (non-unique) cases
   * are different. (But we don't need to have all the assignments within the root stat
   * because the non-unique case is a failure.)
   */
  private static List<AssignmentExprent> getAssignmentsOfWithinOneStatement(Statement start, Exprent target) {
    List<AssignmentExprent> exprents = new ArrayList<>();
    findExprents(start, AssignmentExprent.class, assignment -> assignment.getLeft().equals(target), true, (stat, expr) -> exprents.add(expr));
    return exprents;
  }

  /**
   * Recursively searches one statement for matching exprents.
   *
   * @param start       the statement to search
   * @param exprClass   the wanted exprent type
   * @param predicate   a predicate for filtering the exprents
   * @param onlyOneStat if true, will return eagerly after the first matching statement
   * @param consumer    the consumer that receives the exprents and their parent statements
   */
  // TODO: move somewhere better
  @SuppressWarnings("unchecked")
  public static <T extends Exprent> void findExprents(Statement start, Class<? extends T> exprClass, Predicate<T> predicate, boolean onlyOneStat, BiConsumer<Statement, T> consumer) {
    Queue<Statement> statQueue = new ArrayDeque<>();
    statQueue.offer(start);

    while (!statQueue.isEmpty()) {
      Statement stat = statQueue.remove();
      statQueue.addAll(stat.getStats());

      if (stat.getExprents() != null) {
        boolean foundAny = false;

        for (Exprent expr : stat.getExprents()) {
          if (exprClass.isInstance(expr) && predicate.test((T) expr)) {
            consumer.accept(stat, (T) expr);
            foundAny = true;
          }
        }

        if (onlyOneStat && foundAny) {
          break;
        }
      }
    }
  }

  /**
   * Switch on string gets compiled into two sequential switch statements.
   * The first is a switch on the hashcode of the string with the case statement
   *   being the actual if equal to string literal check.
   * Hashcode collisions result in an else if chain.
   * The body of the if block sets the switch variable for the following switch.
   * <p>
   * The switch statement block has the case statements of the original switch
   *   and may also be inlined directly into the first switch's default block.
   * <p>
   * It is not guaranteed to exist, for example if the cases return out of the switch statement.
   * In this scenario, instead of an intermediate variable the result is returned directly.
   * <p>
   * See the test {@code TestStringSwitchTypes} for examples of output.
   */
  private sealed interface StringSwitch permits Split, InlineSplit, NullableSplit, Merged {
    SwitchStatement first();
    SwitchStatement target();

    static Optional<StringSwitch> match(SwitchStatement stat) {
      return Split.match(stat)
        .or(() -> InlineSplit.match(stat))
        .or(() -> NullableSplit.match(stat))
        .or(() -> Merged.match(stat));
    }

    static boolean isValid(StringSwitch sw) {
      // Is the first expression in the switch head a function call like `var0.hashCode()`?
      Exprent firstHeadVal = ((SwitchHeadExprent) sw.first().getHeadexprent()).getValue();
      if (!(firstHeadVal instanceof InvocationExprent firstHeadValInvoc)
        || !firstHeadValInvoc.getName().equals("hashCode")
        || !firstHeadValInvoc.getClassname().equals("java/lang/String")
        || !(sw.first().getCaseStatements().get(0) instanceof IfStatement)) {
        return false;
      }

      // Get the intermediate variable used in all string-switch types other than merged.
      VarExprent intermediate = null;
      if (!(sw instanceof Merged)) {
        if (!(((SwitchHeadExprent) sw.target().getHeadexprent()).getValue() instanceof VarExprent varExpr)) {
          return false;
        }
        intermediate = varExpr;

        if (sw instanceof NullableSplit s && !s.expr().getLeft().equals(intermediate)) {
          // wrong assignment across `if`
          return false;
        }
      }

      // Get the synthetic duplicate variable sometimes used in the switch head exprent or the case if statements
      Optional<SyntheticDupVarResult> possibleDupVar = findSyntheticDupVar(sw);

      // Validate all the case statements in the switch to make sure it matches the type.
      for (int i = 0; i < sw.first().getCaseStatements().size(); i++) {
        Statement currStat = sw.first().getCaseStatements().get(i);
        List<StatEdge> currEdge = sw.first().getCaseEdges().get(i);

        // Skip default case because we only determine a string-switch is valid based on all the cases with values.
        if (currEdge.contains(sw.first().getDefaultEdge())) {
          continue;
        }

        // Validate the case's if/else blocks

        while (currStat != null) {
          if (!(currStat instanceof IfStatement ifStat)) {
            return false;
          }

          Exprent ifCond = ifStat.getHeadexprent().getCondition();
          if (ifCond instanceof FunctionExprent condFunc
            && (condFunc.getFuncType() == FunctionType.NE || condFunc.getFuncType() == FunctionType.BOOL_NOT)) {
            ifCond = condFunc.getLstOperands().get(0);
          }

          if (ifCond instanceof FunctionExprent condFunc) {
            // If it's a func exprent and all operands do equals on the head exprent stack var
            for (Exprent oper : condFunc.getLstOperands()) {
              if (!(oper instanceof InvocationExprent condInvoc)
                || !condInvoc.getName().equals("equals")
                || !condInvoc.getInstance().equals(firstHeadValInvoc.getInstance())
                && (possibleDupVar.isPresent() && !condInvoc.getInstance().equals(possibleDupVar.get().tmpVar()))) {
                return false;
              }
            }
          } else if (!(ifCond instanceof InvocationExprent condInvoc)
            || !condInvoc.getName().equals("equals")
            || !condInvoc.getInstance().equals(firstHeadValInvoc.getInstance())
            && (possibleDupVar.isPresent() && !condInvoc.getInstance().equals(possibleDupVar.get().tmpVar()))) {
            // The if statement not containing an equals on the switch head exprent/duplicate stack var
            // with the case string means that this is not a string-switch.
            return false;
          }

          // TODO: If break checks might not be needed here anymore?
          boolean isIfBreak = ifStat.getIfstat() == null
            && ifStat.getElsestat() == null
            && ifStat.getIfEdge() != null
            && ifStat.getElseEdge() == null
            && ifStat.getBasichead().hasSuccessor(StatEdge.TYPE_REGULAR)
            && ifStat.getBasichead().getSuccessorEdges(StatEdge.TYPE_BREAK).size() == 1;

          if (!isIfBreak) {
            // Non if-break switches only have 1 statement inside the if (an assignment or return)
            List<Exprent> block = ifStat.getIfstat() != null ? ifStat.getIfstat().getExprents() : null;
            if (block == null || block.size() != 1) {
              return false;
            }

            // Single/merged string-switch always has a return statement
            if (sw instanceof Merged && !isConstReturn(block.get(0))) {
              return false;
            }

            // Split string-switch always has a variable assignment statement
            if (!(sw instanceof Merged) && !(isConstAssignWithVar(block.get(0), intermediate))) {
              return false;
            }
          } else {
            // If the break isn't pointing to the default block
            if (sw.first().getDefaultCase().isPresent()) {
              StatEdge defaultBreak = sw.first().getDefaultCase().get().getAllSuccessorEdges().get(0);
              if (ifStat.getIfEdge().getType() != StatEdge.TYPE_BREAK
                || !ifStat.getIfEdge().getDestination().equals(defaultBreak.getDestination())) {
                return false;
              }
            }
          }

          // All of our desired checks have passed, we know for sure that it's a valid string-switch. Yippee!
          // Validate the else blocks if they exist as well because multiple hash collision use if-else chains.
          currStat = ifStat.getElsestat();
        }
      }

      return true;
    }
  }

  /**
   * The standard type of string-switch structure. Contains two consecutive switch statements where the first
   *   switch yields an intermediate case variable that is used by the second switch.
   */
  record Split(SwitchStatement first, SwitchStatement target) implements StringSwitch {
    private static Optional<StringSwitch> match(SwitchStatement stat) {
      List<StatEdge> edges = stat.getSuccessorEdges(StatEdge.TYPE_REGULAR);
      if (edges.size() != 1 || !(edges.get(0).getDestination() instanceof SwitchStatement found)) {
        return Optional.empty();
      }

      Split matched = new Split(stat, found);
      return StringSwitch.isValid(matched) ? Optional.of(matched) : Optional.empty();
    }
  }

  /**
   * The same as {@link Split} except the second switch is inlined into the first switch's default case block.
   */
  record InlineSplit(SwitchStatement first, SwitchStatement target) implements StringSwitch {
    private static Optional<StringSwitch> match(SwitchStatement stat) {
      if (stat.getDefaultEdge() == null) {
        return Optional.empty();
      }

      SwitchStatement target;
      Statement defaultDest = stat.getDefaultEdge().getDestination();
      if (defaultDest instanceof SwitchStatement found) {
        target = found;
      } else if (defaultDest instanceof SequenceStatement && defaultDest.getFirst() instanceof SwitchStatement found) {
        target = found;
      } else {
        return Optional.empty();
      }

      InlineSplit matched = new InlineSplit(stat, target);
      return StringSwitch.isValid(matched) ? Optional.of(matched) : Optional.empty();
    }
  }

  /**
   * This is maybe suspected to be a Java 17 preview feature that isn't really seen anymore.
   */
  record NullableSplit(SwitchStatement first, SwitchStatement target, AssignmentExprent expr, IfStatement nullCheck)
    implements StringSwitch {
    private static Optional<StringSwitch> match(SwitchStatement stat) {
      // if we're the only thing in an if statement,
      if (!(stat.getParent() instanceof IfStatement parent) || stat.hasSuccessor(StatEdge.TYPE_REGULAR)) {
        return Optional.empty();
      }

      // and it's a null check with `else` branch,
      Exprent ifCond = parent.getHeadexprent().getCondition();
      if (parent.iftype != IfStatement.IFTYPE_IFELSE
        || !(ifCond instanceof FunctionExprent funcExpr)
        || funcExpr.getFuncType() != FunctionType.NE
        || funcExpr.getLstOperands().size() != 2) {
        return Optional.empty();
      }

      Exprent right = funcExpr.getLstOperands().get(1);
      if (!(right instanceof ConstExprent) || right.getExprType() != VarType.VARTYPE_NULL) {
        return Optional.empty();
      }

      // and the `else` only assigns a variable,
      // the else branch of the containing `if` will have an assignment exprent to get the null case from
      Statement elseStat = parent.getElsestat();
      if (!(elseStat instanceof BasicBlockStatement)
        || elseStat.getExprents() == null
        || elseStat.getExprents().size() != 1
        || !(elseStat.getExprents().get(0) instanceof AssignmentExprent nullAssignExpr)) {
        return Optional.empty();
      }

      List<StatEdge> edges = parent.getSuccessorEdges(StatEdge.TYPE_REGULAR);
      if (edges.size() != 1 || !(edges.get(0).getDestination() instanceof SwitchStatement found)) {
        return Optional.empty();
      }

      // then we're probably a nullable string-switch
      NullableSplit matched = new NullableSplit(stat, found, nullAssignExpr, parent);
      return StringSwitch.isValid(matched) ? Optional.of(matched) : Optional.empty();
    }
  }

  /**
   * Similar to {@link Split} but the first switch contains all the necessary information, so a second isn't needed.
   * This is usually the last-resort matched type, so checking for it is recommended.
   */
  record Merged(SwitchStatement first, SwitchStatement target) implements StringSwitch {
    private static Optional<StringSwitch> match(SwitchStatement stat) {
      Merged matched = new Merged(stat, stat);
      return StringSwitch.isValid(matched) ? Optional.of(matched) : Optional.empty();
    }
  }
}
