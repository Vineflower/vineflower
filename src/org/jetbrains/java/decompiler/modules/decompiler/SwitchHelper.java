// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.rels.ClassWrapper;
import org.jetbrains.java.decompiler.main.rels.MethodWrapper;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.SwitchStatement;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SwitchHelper {
  public static boolean simplifySwitches(Statement stat, StructMethod mt) {
    boolean ret = false;
    if (stat.type == Statement.TYPE_SWITCH) {
      ret = simplify((SwitchStatement)stat, mt);
    }

    for (int i = 0; i < stat.getStats().size(); ++i) {
      ret |= simplifySwitches(stat.getStats().get(i), mt);
    }

    return ret;
  }

  private static boolean simplify(SwitchStatement switchStatement, StructMethod mt) {
    SwitchStatement following = null;
    List<StatEdge> edges = switchStatement.getSuccessorEdges(StatEdge.TYPE_REGULAR);
    if (edges.size() == 1 && edges.get(0).getDestination().type == Statement.TYPE_SWITCH) {
      following = (SwitchStatement)edges.get(0).getDestination();
    }

    SwitchExprent switchExprent = (SwitchExprent)switchStatement.getHeadexprent();
    Exprent value = switchExprent.getValue();
    if (isEnumArray(value)) {
      List<List<Exprent>> caseValues = switchStatement.getCaseValues();
      Map<Exprent, Exprent> mapping = new HashMap<>(caseValues.size());
      ArrayExprent array = (ArrayExprent)value;
      FieldExprent arrayField = (FieldExprent)array.getArray();
      ClassesProcessor.ClassNode classNode = DecompilerContext.getClassProcessor().getMapRootClasses().get(arrayField.getClassname());
      if (classNode != null) {
        ClassWrapper classWrapper = classNode.getWrapper();
        if (classWrapper != null) {
          MethodWrapper wrapper = classWrapper.getMethodWrapper(CodeConstants.CLINIT_NAME, "()V");
          if (wrapper != null && wrapper.root != null) {
            wrapper.getOrBuildGraph().iterateExprents(exprent -> {
              if (exprent instanceof AssignmentExprent) {
                AssignmentExprent assignment = (AssignmentExprent) exprent;
                Exprent left = assignment.getLeft();
                if (left.type == Exprent.EXPRENT_ARRAY && ((ArrayExprent) left).getArray().equals(arrayField)) {
                  mapping.put(assignment.getRight(), ((InvocationExprent) ((ArrayExprent) left).getIndex()).getInstance());
                }
              }
              return 0;
            });
          }
        }
      }

      List<List<Exprent>> realCaseValues = new ArrayList<>(caseValues.size());
      for (List<Exprent> caseValue : caseValues) {
        List<Exprent> values = new ArrayList<>(caseValue.size());
        realCaseValues.add(values);
        for (Exprent exprent : caseValue) {
          if (exprent == null) {
            values.add(null);
          }
          else {
            Exprent realConst = mapping.get(exprent);
            if (realConst == null) {
              DecompilerContext.getLogger()
                .writeMessage("Unable to simplify switch on enum: " + exprent + " not found, available: " + mapping + " in method " + mt.getClassQualifiedName() + mt.getName(),
                              IFernflowerLogger.Severity.ERROR);
              return false;
            }
            values.add(realConst.copy());
          }
        }
      }
      caseValues.clear();
      caseValues.addAll(realCaseValues);
      switchExprent.replaceExprent(value, ((InvocationExprent)array.getIndex()).getInstance().copy());
      return true;
    }
    else if (isSwitchOnString(switchStatement, following)) {
      Map<Integer, Exprent> caseMap = new HashMap<>();

      int i = 0;
      for (; i < switchStatement.getCaseStatements().size(); ++i) {

        Statement curr = switchStatement.getCaseStatements().get(i);

        while (curr != null && curr.type == Statement.TYPE_IF)  {
          IfStatement ifStat = (IfStatement)curr;
          Exprent condition = ifStat.getHeadexprent().getCondition();

          if (condition.type == Exprent.EXPRENT_FUNCTION && ((FunctionExprent)condition).getFuncType() == FunctionExprent.FUNCTION_NE) {
            condition = ((FunctionExprent)condition).getLstOperands().get(0);
          }

          if (condition.type == Exprent.EXPRENT_INVOCATION && ((InvocationExprent)condition).getLstParameters().size() == 1) {
            Exprent assign = ifStat.getIfstat().getExprents().get(0);
            int caseVal = ((ConstExprent)((AssignmentExprent)assign).getRight()).getIntValue();
            caseMap.put(caseVal, ((InvocationExprent)condition).getLstParameters().get(0));
          }

          curr = ifStat.getElsestat();
        }
      }

      List<List<Exprent>> realCaseValues = following.getCaseValues().stream()
        .map(l -> l.stream()
          .map(e -> e instanceof ConstExprent ? ((ConstExprent)e).getIntValue() : null)
          .map(caseMap::get)
          .collect(Collectors.toList()))
        .collect(Collectors.toList());

      following.getCaseValues().clear();
      following.getCaseValues().addAll(realCaseValues);

      Exprent followingVal = ((SwitchExprent)following.getHeadexprent()).getValue();
      following.getHeadexprent().replaceExprent(followingVal, ((InvocationExprent)value).getInstance());

      switchStatement.getFirst().getExprents().remove(switchStatement.getFirst().getExprents().size() - 1);
      switchStatement.getFirst().getAllPredecessorEdges().forEach(switchStatement.getFirst()::removePredecessor);
      switchStatement.getFirst().getAllSuccessorEdges().forEach(switchStatement.getFirst()::removeSuccessor);
      switchStatement.getParent().replaceStatement(switchStatement, switchStatement.getFirst());
      return true;
    }
    return false;
  }

  public static final int STATIC_FINAL_SYNTHETIC = CodeConstants.ACC_STATIC | CodeConstants.ACC_FINAL | CodeConstants.ACC_SYNTHETIC;
  /**
   * When Java introduced Enums they added the ability to use them in Switch statements.
   * This was done in a purely syntax sugar way using the old switch on int methods.
   * The compiler creates a synthetic class with a static int array field.
   * To support enums changing post compile, It initializes this field with a length of the current enum length.
   * And then for every referenced enum value it adds a mapping in the form of:
   *   try {
   *     field[Enum.VALUE.ordinal()] = 1;
   *   } catch (FieldNotFoundException e) {}
   *
   * If a class has multiple switches on multiple enums, the compiler adds the init and try list to the BEGINNING of the static initalizer.
   * But they add the field to the END of the fields list.
   * 
   * Note: SOME compilers name the field starting with $SwitchMap, so if we do not have full context this can be a guess.
   * But Obfuscated/renamed code could cause issues
   */
  private static boolean isEnumArray(Exprent exprent) {
    if (exprent instanceof ArrayExprent) {
      ArrayExprent arr = (ArrayExprent) exprent;
      Exprent tmp = arr.getArray();
      if (tmp instanceof FieldExprent) {
        FieldExprent field = (FieldExprent)tmp;
        Exprent index = arr.getIndex();
        ClassesProcessor.ClassNode classNode = DecompilerContext.getClassProcessor().getMapRootClasses().get(field.getClassname());
        
        if (classNode == null || !"[I".equals(field.getDescriptor().descriptorString)) {
          return field.getName().startsWith("$SwitchMap") || //This is non-standard but we don't have any more information so..
            (index instanceof InvocationExprent && ((InvocationExprent) index).getName().equals("ordinal"));
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

        boolean isSyncheticClass;
        if (classNode.getWrapper() == null) {
          isSyncheticClass = (classNode.classStruct.getAccessFlags() & CodeConstants.ACC_SYNTHETIC) == CodeConstants.ACC_SYNTHETIC;
        } else {
          isSyncheticClass = (classNode.getWrapper().getClassStruct().getAccessFlags() & CodeConstants.ACC_SYNTHETIC) == CodeConstants.ACC_SYNTHETIC;
        }

        if (isSyncheticClass) {
          return true; //TODO: Find a way to check the structure of the initalizer?
          //Exprent init = classNode.getWrapper().getStaticFieldInitializers().getWithKey(InterpreterUtil.makeUniqueKey(field.getName(), field.getDescriptor().descriptorString));
          //Above is null because we haven't preocess the class yet?
        }
      }
    }
    return false;
  }

  /**
   * Switch on string gets compiled into two sequential switch statements.
   *   The first is a switch on the hashcode of the string with the case statement
   *   being the actual if equal to string literal check. Hashcode collisions result in
   *   an else if chain. The body of the if block sets the switch variable for the
   *   following switch.
   *
   *   The second switch block has the case statements of the original switch on string.
   *
   *   byte b1 = -1;
   *   switch (stringVar.hashcode()) {
   *     case -390932093:
   *        if (stringVar.equals("foo") {
   *          b1 = 0;
   *        }
   *   }
   *
   *   switch(b1) {
   *     case 0 :
   *        // code for case "foo"
   *   }
   */
  private static boolean isSwitchOnString(SwitchStatement first, SwitchStatement second) {
    if (second != null) {
      Exprent firstValue = ((SwitchExprent)first.getHeadexprent()).getValue();
      Exprent secondValue = ((SwitchExprent)second.getHeadexprent()).getValue();

      if (firstValue.type == Exprent.EXPRENT_INVOCATION && secondValue.type == Exprent.EXPRENT_VAR && first.getCaseStatements().get(0).type == Statement.TYPE_IF) {
        InvocationExprent invExpr = (InvocationExprent)firstValue;
        VarExprent varExpr = (VarExprent)secondValue;

        if (invExpr.getName().equals("hashCode") && invExpr.getClassname().equals("java/lang/String")) {
          boolean matches = true;

          for (int i = 0; matches && i < first.getCaseStatements().size(); ++i) {
            if (!first.getCaseEdges().get(i).contains(first.getDefaultEdge())) {
              Statement curr = first.getCaseStatements().get(i);
              while (matches && curr != null) {
                if (curr.type == Statement.TYPE_IF) {
                  IfStatement ifStat = (IfStatement)curr;
                  Exprent condition = ifStat.getHeadexprent().getCondition();

                  if (condition.type == Exprent.EXPRENT_FUNCTION && ((FunctionExprent)condition).getFuncType() == FunctionExprent.FUNCTION_NE) {
                    condition = ((FunctionExprent)condition).getLstOperands().get(0);
                  }

                  if (condition.type == Exprent.EXPRENT_INVOCATION) {
                    InvocationExprent condInvocation = (InvocationExprent)condition;

                    if (condInvocation.getName().equals("equals") && condInvocation.getInstance().equals(invExpr.getInstance())) {
                      List<Exprent> block = ifStat.getIfstat().getExprents();

                      if (block != null && block.size() == 1 && block.get(0).type == Exprent.EXPRENT_ASSIGNMENT) {
                        AssignmentExprent assign = (AssignmentExprent)block.get(0);

                        if (assign.getRight().type == Exprent.EXPRENT_CONST && varExpr.equals(assign.getLeft())) {

                          curr = ifStat.getElsestat();
                          continue;
                        }
                      }
                    }
                  }
                }

                matches = false;
              }
            }
          }

          return matches;
        }
      }
    }

    return false;
  }
}
