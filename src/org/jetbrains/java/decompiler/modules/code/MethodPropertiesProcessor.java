package org.jetbrains.java.decompiler.modules.code;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.FullInstructionSequence;
import org.jetbrains.java.decompiler.code.Instruction;
import org.jetbrains.java.decompiler.code.MethodProperties;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.StructField;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.struct.consts.LinkConstant;
import org.jetbrains.java.decompiler.struct.gen.MethodDescriptor;

public class MethodPropertiesProcessor {
  public static void parse(StructMethod mt, StructClass cl, MethodProperties props) {
    if (!mt.containsCode()) {
      return;
    }

    findSyntheticReferenceLambda(mt, cl, props);
    findAssertField(mt, cl, props);
  }

  private static void findAssertField(StructMethod mt, StructClass cl, MethodProperties props) {
    if (mt.getName().equals("<clinit>")) {
      try {
        mt.expandData(cl);
      } catch (Exception e) {
        e.printStackTrace();
        return;
      }

      ConstantPool pool = cl.getPool();

      String name = cl.qualifiedName;

      FullInstructionSequence seq = mt.getInstructionSequence();
      for (int i = 0; i < seq.instructions().size(); i++) {
        Instruction instr = seq.getInstr(i);
        // We're looking for:
        // ldc <thisclass>
        // invi Class.desiredAssertionStatus
        // ifne
        // iconst1 (bipush 1)
        // goto end
        // iconst0 (bipush 0)
        // end:
        // putstatic <field name>
        if (instr.opcode == CodeConstants.opc_invokevirtual && i > 0 && i + 5 < seq.instructions().size()) {
          LinkConstant link = pool.getLinkConstant(instr.operand(0));
          if ("java/lang/Class".equals(link.classname) && "desiredAssertionStatus".equals(link.elementname)) {
            Instruction last = seq.getInstr(i - 1);
            if (last.opcode == CodeConstants.opc_ldc || last.opcode == CodeConstants.opc_ldc_w) {
              if (seq.getInstr(i + 1).opcode == CodeConstants.opc_ifne
              && seq.getInstr(i + 2).opcode == CodeConstants.opc_bipush && seq.getInstr(i + 2).operand(0) == 1
              && seq.getInstr(i + 3).opcode == CodeConstants.opc_goto
              && seq.getInstr(i + 4).opcode == CodeConstants.opc_bipush && seq.getInstr(i + 4).operand(0) == 0) {
                Instruction field = seq.getInstr(i + 5);
                if (field.opcode == CodeConstants.opc_putstatic) {
                  LinkConstant putstatic = pool.getLinkConstant(field.operand(0));
                  if (name.equals(putstatic.classname) && "Z".equals(putstatic.descriptor)) {
                    props.assertField = cl.getField(putstatic.elementname, putstatic.descriptor);
                    return;
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private static void findSyntheticReferenceLambda(StructMethod mt, StructClass cl, MethodProperties props) {
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
