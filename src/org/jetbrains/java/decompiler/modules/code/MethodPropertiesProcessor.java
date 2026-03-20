package org.jetbrains.java.decompiler.modules.code;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.FullInstructionSequence;
import org.jetbrains.java.decompiler.code.Instruction;
import org.jetbrains.java.decompiler.code.MethodProperties;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;

public class MethodPropertiesProcessor {
  public static void parse(StructMethod mt, StructClass cl, MethodProperties props) {
    if (!mt.containsCode()) {
      return;
    }

    if (mt.hasModifier(CodeConstants.ACC_STATIC) && mt.hasModifier(CodeConstants.ACC_SYNTHETIC)) {
      try {
        mt.expandData(cl);
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }

      FullInstructionSequence seq = mt.getInstructionSequence();
      MethodDescriptor md = mt.methodDescriptor();

      LambdaInstrProgress progress = LambdaInstrProgress.LOADS;

      for (int i = 0; i < seq.instructions().size(); i++) {
        Instruction instr = seq.getInstr(i);

        LambdaInstrProgress next = chooseNextProgress(progress, instr);
        if (next != null) {
          progress = next;
        }

        if (!isInstrAcceptable(progress, instr)) {
          return;
        }
      }

      props.isSyntheticReferenceLambda = true;
    }
  }

  private static boolean isInstrAcceptable(LambdaInstrProgress progress, Instruction instr) {
    if (progress == LambdaInstrProgress.LOADS) {
      return instr.opcode >= CodeConstants.opc_iload && instr.opcode <= CodeConstants.opc_aload;
    } else if (progress == LambdaInstrProgress.INVOKE) {
      return instr.opcode == CodeConstants.opc_invokevirtual;
    } else if (progress == LambdaInstrProgress.POPRETURN) {
      return instr.opcode == CodeConstants.opc_pop || instr.opcode == CodeConstants.opc_pop2 || instr.opcode >= CodeConstants.opc_ireturn && instr.opcode <= CodeConstants.opc_return;
    }

    return false;
  }

  private static @Nullable LambdaInstrProgress chooseNextProgress(LambdaInstrProgress progress, Instruction instr) {
    if (progress == LambdaInstrProgress.LOADS) {
      if (instr.opcode == CodeConstants.opc_invokevirtual) {
        return LambdaInstrProgress.INVOKE;
      }
    }

    // Only 1 invoke allowed
    if (progress == LambdaInstrProgress.INVOKE) {
      return LambdaInstrProgress.POPRETURN;
    }

    return null;
  }

  enum LambdaInstrProgress {
    LOADS,
    INVOKE,
    POPRETURN
  }
}
