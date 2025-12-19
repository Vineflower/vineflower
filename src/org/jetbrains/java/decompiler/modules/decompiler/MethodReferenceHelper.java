package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.FullInstructionSequence;
import org.jetbrains.java.decompiler.code.Instruction;
import org.jetbrains.java.decompiler.main.ClassesProcessor;
import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode;
import org.jetbrains.java.decompiler.main.ClassesProcessor.ClassNode.LambdaInformation;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.modules.decompiler.exps.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.RootStatement;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.consts.LinkConstant;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.io.IOException;
import java.util.Iterator;

public class MethodReferenceHelper {
  public static boolean convertToMethodReference(RootStatement root) throws IOException {
    return convertToMethodReferenceRec(root);
  }

  public static boolean convertToMethodReferenceRec(Statement stat) throws IOException {
    boolean result = false;
    for (Statement subStat : stat.getStats()) {
      result |= convertToMethodReferenceRec(subStat);
    }
    if (stat.getExprents() != null) {
      for (int i = 0; i < stat.getExprents().size(); i++) {
        Exprent exp = stat.getExprents().get(i);
        for (Exprent subExp : exp.getAllExprents(true)) {
          if (convertToMethodReference(stat, i, subExp)) {
            result |= true;
          }
          for (int j = 0; j < stat.getExprents().size(); j++) {
            if (stat.getExprents().get(j) == exp) {
              i = j;
              break;
            }
          }
          if (removeInstanceAssignment(stat, i, subExp)) {
            result |= true;
          }
          for (int j = 0; j < stat.getExprents().size(); j++) {
            if (stat.getExprents().get(j) == exp) {
              i = j;
              break;
            }
          }
        }
      }
    }
    return result;
  }

  private static boolean convertToMethodReference(Statement stat, int i, Exprent exp) throws IOException {
    if (exp instanceof NewExprent newExprent
        && newExprent.isLambda()
        && !newExprent.isMethodReference()) {
      if (stat.getTopParent().mt.getName().contains("<init>")) {
        System.out.println();
      }
      String className = newExprent.getNewType().value;
      ClassNode node = DecompilerContext.getClassProcessor().getMapRootClasses().get(className);
      LambdaInformation info = node.lambdaInformation;
      StructClass struct = DecompilerContext.getStructContext().getClass(info.content_class_name);
      StructMethod method = struct.getMethod(info.content_method_key);
      if (!method.hasModifier(CodeConstants.ACC_STATIC))
        return false;
      method.expandData(struct);
      FullInstructionSequence seq = method.getInstructionSequence();
      Iterator<Instruction> iterator = seq.iterator();
      Instruction next = null;
      int argument = 0;

      if (!iterator.hasNext())
        return false;
      next = iterator.next();

      // new instance
      if (next.opcode == CodeConstants.opc_new) {
        if (!iterator.hasNext())
          return false;
        next = iterator.next();
        if (next.opcode != CodeConstants.opc_dup)
          return false;

        if (!iterator.hasNext())
          return false;
        next = iterator.next();
      }

      // Load arguments
      while (iterator.hasNext()
          && next.opcode >= CodeConstants.opc_iload && next.opcode <= CodeConstants.opc_aload
          && next.operand(0) == argument) {
        next = iterator.next();
        argument++;
      }

      if (next == null)
        return false;
      boolean varargs = false;
      int varargsCount = 0;
      // varargs array length
      if (next.opcode >= CodeConstants.opc_bipush && next.opcode <= CodeConstants.opc_sipush) {
        varargsCount = next.operand(0);
        varargs = true;

        // varargs array creation
        if (!iterator.hasNext())
          return false;
        next = iterator.next();
        if (next.opcode < CodeConstants.opc_newarray || next.opcode > CodeConstants.opc_anewarray)
          return false;
        for (int j = 0; j < varargsCount; j++) {

          // duplicate varargs array
          if (!iterator.hasNext())
            return false;
          next = iterator.next();
          if (next.opcode != CodeConstants.opc_dup)
            return false;

          // varargs array index
          if (!iterator.hasNext())
            return false;
          next = iterator.next();
          if (next.opcode < CodeConstants.opc_bipush || next.opcode > CodeConstants.opc_sipush
              || next.operand(0) != j)
            return false;

          // load argument for varargs array
          if (!iterator.hasNext())
            return false;
          next = iterator.next();
          if (next.opcode < CodeConstants.opc_iload || next.opcode > CodeConstants.opc_aload
              || next.operand(0) != argument + j)
            return false;

          // store argument in varargs array
          if (!iterator.hasNext())
            return false;
          next = iterator.next();
          if (next.opcode < CodeConstants.opc_iastore || next.opcode > CodeConstants.opc_sastore)
            return false;
        }

        if (!iterator.hasNext())
          return false;
        next = iterator.next();
      }
      // invoke method
      if (next.opcode < CodeConstants.opc_invokevirtual || next.opcode > CodeConstants.opc_invokeinterface)
        return false;

      Instruction invoke = next;

      if (!iterator.hasNext())
        return false;
      next = iterator.next();

      if (next.opcode == CodeConstants.opc_pop) {
        if (!iterator.hasNext())
          return false;
        next = iterator.next();
      }

      // return
      if (next.opcode < CodeConstants.opc_ireturn || next.opcode > CodeConstants.opc_return)
        return false;

      if (iterator.hasNext())
        return false;

      LinkConstant link = struct.getPool().getLinkConstant(invoke.operand(0));
      boolean instance = invoke.opcode != CodeConstants.opc_invokestatic
          && !link.elementname.equals(CodeConstants.INIT_NAME);

      String newClass = link.classname;
      String newMethod = link.elementname;
      String newDescriptor = link.descriptor;
      String newKey = InterpreterUtil.makeUniqueKey(newMethod, newDescriptor);

      StructClass newStructClass = DecompilerContext.getStructContext().getClass(newClass);
      if (newStructClass == null)
        return false;
      StructMethod newStructMethod = newStructClass.getMethodRecursive(newMethod, newDescriptor);
      if (newStructMethod == null)
        return false;

      if (newStructMethod.hasModifier(CodeConstants.ACC_SYNTHETIC))
        return false;

      ClassNode newNode = DecompilerContext.getClassProcessor().getMapRootClasses().get(newClass);
      if (newNode != null && newNode.type == ClassesProcessor.ClassNode.Type.ANONYMOUS)
        return false;

      if (method.methodDescriptor().params.length - (varargs ? varargsCount - 1 : 0) != newStructMethod.methodDescriptor().params.length + (instance ? 1 : 0))
        return false;
      if (newExprent.getConstructor().getLstParameters().size() > (instance ? 1 : 0))
        return false;
      info.content_class_name = newClass;
      info.content_method_name = newMethod;
      info.content_method_descriptor = newDescriptor;
      info.content_method_invocation_type = switch (invoke.opcode) {
        case CodeConstants.opc_invokevirtual -> CodeConstants.CONSTANT_MethodHandle_REF_invokeVirtual;
        case CodeConstants.opc_invokespecial -> instance ? CodeConstants.CONSTANT_MethodHandle_REF_newInvokeSpecial : CodeConstants.CONSTANT_MethodHandle_REF_invokeSpecial;
        case CodeConstants.opc_invokestatic -> CodeConstants.CONSTANT_MethodHandle_REF_invokeStatic;
        case CodeConstants.opc_invokeinterface -> CodeConstants.CONSTANT_MethodHandle_REF_invokeInterface;
        default -> -1;
      };
      info.content_method_key = newKey;
      info.is_method_reference = true;
      info.is_content_method_static = invoke.opcode == CodeConstants.opc_invokestatic;
      newExprent.setMethodReference(true);
      InvocationExprent constructor = newExprent.getConstructor();
      if (instance && constructor.getLstParameters().size() == 1) {
        constructor.setInstance(constructor.getLstParameters().get(0));
      }

      if (i > 0) {
        Exprent instanceExp = newExprent.getConstructor().getInstance();
        Exprent previous = stat.getExprents().get(i - 1);
        if (instanceExp instanceof VarExprent varExp
            && previous instanceof AssignmentExprent assignment
            && varExp.equalsVersions(assignment.getLeft())
            && !varExp.isVarReferenced(stat.getTopParent(), (VarExprent) assignment.getLeft())) {
          newExprent.getConstructor().setInstance(assignment.getRight());
          newExprent.getConstructor().getLstParameters().set(0, assignment.getRight());
          stat.getExprents().remove(i - 1);
        }
      }
      return true;
    }
    return false;
  }

  private static boolean removeInstanceAssignment(Statement stat, int i, Exprent exp) {
    if (exp instanceof NewExprent newExp
        && newExp.isLambda()
        && newExp.isMethodReference()
        && i >= 2
        && newExp.getConstructor().getInstance() instanceof VarExprent stackVar
        && stackVar.isStack()) {
        Exprent nonNull = stat.getExprents().get(i - 1);
        Exprent assign = stat.getExprents().get(i - 2);
        if (nonNull instanceof InvocationExprent inv
            && inv.getClassname().equals("java/util/Objects")
            && inv.getName().equals("requireNonNull")
            && inv.getStringDescriptor().equals("(Ljava/lang/Object;)Ljava/lang/Object;")
            && stackVar.equalsVersions(inv.getLstParameters().get(0))
            && assign instanceof AssignmentExprent stackAssign
            && stackVar.equalsVersions(stackAssign.getLeft())) {
          newExp.getConstructor().setInstance(stackAssign.getRight());
          newExp.getConstructor().getLstParameters().set(0, stackAssign.getRight());
          stat.getExprents().remove(i - 1);
          stat.getExprents().remove(i - 2);
          return true;
        }
      }
    return false;
  }
}
