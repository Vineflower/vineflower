// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.code.interpreter;

import org.jetbrains.java.decompiler.code.CodeConstants;
import org.jetbrains.java.decompiler.code.Instruction;
import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.struct.consts.LinkConstant;
import org.jetbrains.java.decompiler.struct.consts.PooledConstant;
import org.jetbrains.java.decompiler.struct.consts.PrimitiveConstant;
import org.jetbrains.java.decompiler.struct.gen.*;
import org.jetbrains.java.decompiler.util.collections.ListStack;

public final class InstructionImpact {

  // {read, write}
  private static final CodeType[][][] stack_impact = {

    {null, null},                                                //		public final static int		opc_nop = 0;
    null,                                                                //		public final static int		opc_aconst_null = 1;
    null,                                                                //		public final static int		opc_iconst_m1 = 2;
    null,                                                                //		public final static int		opc_iconst_0 = 3;
    null,                                                                //		public final static int		opc_iconst_1 = 4;
    null,                                                                //		public final static int		opc_iconst_2 = 5;
    null,                        //		public final static int		opc_iconst_3 = 6;
    null,                        //		public final static int		opc_iconst_4 = 7;
    null,                        //		public final static int		opc_iconst_5 = 8;
    {null, {CodeType.LONG}},                        //		public final static int		opc_lconst_0 = 9;
    {null, {CodeType.LONG}},                        //		public final static int		opc_lconst_1 = 10;
    {null, {CodeType.FLOAT}},                        //		public final static int		opc_fconst_0 = 11;
    {null, {CodeType.FLOAT}},                        //		public final static int		opc_fconst_1 = 12;
    {null, {CodeType.FLOAT}},                        //		public final static int		opc_fconst_2 = 13;
    {null, {CodeType.DOUBLE}},                        //		public final static int		opc_dconst_0 = 14;
    {null, {CodeType.DOUBLE}},                        //		public final static int		opc_dconst_1 = 15;
    {null, {CodeType.INT}},                        //		public final static int		opc_bipush = 16;
    {null, {CodeType.INT}},                        //		public final static int		opc_sipush = 17;
    null,                        //		public final static int		opc_ldc = 18;
    null,                        //		public final static int		opc_ldc_w = 19;
    null,                        //		public final static int		opc_ldc2_w = 20;
    {null, {CodeType.INT}},                        //		public final static int		opc_iload = 21;
    {null, {CodeType.LONG}},                        //		public final static int		opc_lload = 22;
    {null, {CodeType.FLOAT}},                        //		public final static int		opc_fload = 23;
    {null, {CodeType.DOUBLE}},                        //		public final static int		opc_dload = 24;
    null,                        //		public final static int		opc_aload = 25;
    null,                        //		public final static int		opc_iload_0 = 26;
    null,                        //		public final static int		opc_iload_1 = 27;
    null,                        //		public final static int		opc_iload_2 = 28;
    null,                        //		public final static int		opc_iload_3 = 29;
    null,                        //		public final static int		opc_lload_0 = 30;
    null,                        //		public final static int		opc_lload_1 = 31;
    null,                        //		public final static int		opc_lload_2 = 32;
    null,                        //		public final static int		opc_lload_3 = 33;
    null,                        //		public final static int		opc_fload_0 = 34;
    null,                        //		public final static int		opc_fload_1 = 35;
    null,                        //		public final static int		opc_fload_2 = 36;
    null,                        //		public final static int		opc_fload_3 = 37;
    null,                        //		public final static int		opc_dload_0 = 38;
    null,                        //		public final static int		opc_dload_1 = 39;
    null,                        //		public final static int		opc_dload_2 = 40;
    null,                        //		public final static int		opc_dload_3 = 41;
    null,                        //		public final static int		opc_aload_0 = 42;
    null,                        //		public final static int		opc_aload_1 = 43;
    null,                        //		public final static int		opc_aload_2 = 44;
    null,                        //		public final static int		opc_aload_3 = 45;
    {{CodeType.OBJECT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_iaload = 46;
    {{CodeType.OBJECT, CodeType.INT}, {CodeType.LONG}},
    //		public final static int		opc_laload = 47;
    {{CodeType.OBJECT, CodeType.INT}, {CodeType.FLOAT}},
    //		public final static int		opc_faload = 48;
    {{CodeType.OBJECT, CodeType.INT}, {CodeType.DOUBLE}},
    //		public final static int		opc_daload = 49;
    null,                        //		public final static int		opc_aaload = 50;
    {{CodeType.OBJECT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_baload = 51;
    {{CodeType.OBJECT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_caload = 52;
    {{CodeType.OBJECT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_saload = 53;
    {{CodeType.INT}, null},                        //		public final static int		opc_istore = 54;
    {{CodeType.LONG}, null},                        //		public final static int		opc_lstore = 55;
    {{CodeType.FLOAT}, null},                        //		public final static int		opc_fstore = 56;
    {{CodeType.DOUBLE}, null},                        //		public final static int		opc_dstore = 57;
    null,                        //		public final static int		opc_astore = 58;
    null,                        //		public final static int		opc_istore_0 = 59;
    null,                        //		public final static int		opc_istore_1 = 60;
    null,                        //		public final static int		opc_istore_2 = 61;
    null,                        //		public final static int		opc_istore_3 = 62;
    null,                        //		public final static int		opc_lstore_0 = 63;
    null,                        //		public final static int		opc_lstore_1 = 64;
    null,                        //		public final static int		opc_lstore_2 = 65;
    null,                        //		public final static int		opc_lstore_3 = 66;
    null,                        //		public final static int		opc_fstore_0 = 67;
    null,                        //		public final static int		opc_fstore_1 = 68;
    null,                        //		public final static int		opc_fstore_2 = 69;
    null,                        //		public final static int		opc_fstore_3 = 70;
    null,                        //		public final static int		opc_dstore_0 = 71;
    null,                        //		public final static int		opc_dstore_1 = 72;
    null,                        //		public final static int		opc_dstore_2 = 73;
    null,                        //		public final static int		opc_dstore_3 = 74;
    null,                        //		public final static int		opc_astore_0 = 75;
    null,                        //		public final static int		opc_astore_1 = 76;
    null,                        //		public final static int		opc_astore_2 = 77;
    null,                        //		public final static int		opc_astore_3 = 78;
    {{CodeType.OBJECT, CodeType.INT, CodeType.INT}, null},
    //		public final static int		opc_iastore = 79;
    {{CodeType.OBJECT, CodeType.INT, CodeType.LONG}, null},
    //		public final static int		opc_lastore = 80;
    {{CodeType.OBJECT, CodeType.INT, CodeType.FLOAT}, null},
    //		public final static int		opc_fastore = 81;
    {{CodeType.OBJECT, CodeType.INT, CodeType.DOUBLE}, null},
    //		public final static int		opc_dastore = 82;
    {{CodeType.OBJECT, CodeType.INT, CodeType.OBJECT}, null},
    //		public final static int		opc_aastore = 83;
    {{CodeType.OBJECT, CodeType.INT, CodeType.INT}, null},
    //		public final static int		opc_bastore = 84;
    {{CodeType.OBJECT, CodeType.INT, CodeType.INT}, null},
    //		public final static int		opc_castore = 85;
    {{CodeType.OBJECT, CodeType.INT, CodeType.INT}, null},
    //		public final static int		opc_sastore = 86;
    {{CodeType.ANY}, null},                        //		public final static int		opc_pop = 87;
    {{CodeType.ANY, CodeType.ANY}, null},                        //		public final static int		opc_pop2 = 88;
    null,                        //		public final static int		opc_dup = 89;
    null,                        //		public final static int		opc_dup_x1 = 90;
    null,                        //		public final static int		opc_dup_x2 = 91;
    null,                        //		public final static int		opc_dup2 = 92;
    null,                        //		public final static int		opc_dup2_x1 = 93;
    null,                        //		public final static int		opc_dup2_x2 = 94;
    null,                        //		public final static int		opc_swap = 95;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_iadd = 96;
    {{CodeType.LONG, CodeType.LONG}, {CodeType.LONG}},
    //		public final static int		opc_ladd = 97;
    {{CodeType.FLOAT, CodeType.FLOAT}, {CodeType.FLOAT}},
    //		public final static int		opc_fadd = 98;
    {{CodeType.DOUBLE, CodeType.DOUBLE}, {CodeType.DOUBLE}},
    //		public final static int		opc_dadd = 99;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_isub = 100;
    {{CodeType.LONG, CodeType.LONG}, {CodeType.LONG}},
    //		public final static int		opc_lsub = 101;
    {{CodeType.FLOAT, CodeType.FLOAT}, {CodeType.FLOAT}},
    //		public final static int		opc_fsub = 102;
    {{CodeType.DOUBLE, CodeType.DOUBLE}, {CodeType.DOUBLE}},
    //		public final static int		opc_dsub = 103;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_imul = 104;
    {{CodeType.LONG, CodeType.LONG}, {CodeType.LONG}},
    //		public final static int		opc_lmul = 105;
    {{CodeType.FLOAT, CodeType.FLOAT}, {CodeType.FLOAT}},
    //		public final static int		opc_fmul = 106;
    {{CodeType.DOUBLE, CodeType.DOUBLE}, {CodeType.DOUBLE}},
    //		public final static int		opc_dmul = 107;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_idiv = 108;
    {{CodeType.LONG, CodeType.LONG}, {CodeType.LONG}},
    //		public final static int		opc_ldiv = 109;
    {{CodeType.FLOAT, CodeType.FLOAT}, {CodeType.FLOAT}},
    //		public final static int		opc_fdiv = 110;
    {{CodeType.DOUBLE, CodeType.DOUBLE}, {CodeType.DOUBLE}},
    //		public final static int		opc_ddiv = 111;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_irem = 112;
    {{CodeType.LONG, CodeType.LONG}, {CodeType.LONG}},
    //		public final static int		opc_lrem = 113;
    {{CodeType.FLOAT, CodeType.FLOAT}, {CodeType.FLOAT}},
    //		public final static int		opc_frem = 114;
    {{CodeType.DOUBLE, CodeType.DOUBLE}, {CodeType.DOUBLE}},
    //		public final static int		opc_drem = 115;
    {{CodeType.INT}, {CodeType.INT}},                        //		public final static int		opc_ineg = 116;
    {{CodeType.LONG}, {CodeType.LONG}},                        //		public final static int		opc_lneg = 117;
    {{CodeType.FLOAT}, {CodeType.FLOAT}},                        //		public final static int		opc_fneg = 118;
    {{CodeType.DOUBLE}, {CodeType.DOUBLE}},                        //		public final static int		opc_dneg = 119;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_ishl = 120;
    {{CodeType.LONG, CodeType.INT}, {CodeType.LONG}},
    //		public final static int		opc_lshl = 121;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_ishr = 122;
    {{CodeType.LONG, CodeType.INT}, {CodeType.LONG}},
    //		public final static int		opc_lshr = 123;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_iushr = 124;
    {{CodeType.LONG, CodeType.INT}, {CodeType.LONG}},
    //		public final static int		opc_lushr = 125;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_iand = 126;
    {{CodeType.LONG, CodeType.LONG}, {CodeType.LONG}},
    //		public final static int		opc_land = 127;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_ior = 128;
    {{CodeType.LONG, CodeType.LONG}, {CodeType.LONG}},
    //		public final static int		opc_lor = 129;
    {{CodeType.INT, CodeType.INT}, {CodeType.INT}},
    //		public final static int		opc_ixor = 130;
    {{CodeType.LONG, CodeType.LONG}, {CodeType.LONG}},
    //		public final static int		opc_lxor = 131;
    {null, null},                        //		public final static int		opc_iinc = 132;
    {{CodeType.INT}, {CodeType.LONG}},                        //		public final static int		opc_i2l = 133;
    {{CodeType.INT}, {CodeType.FLOAT}},                        //		public final static int		opc_i2f = 134;
    {{CodeType.INT}, {CodeType.DOUBLE}},                        //		public final static int		opc_i2d = 135;
    {{CodeType.LONG}, {CodeType.INT}},                        //		public final static int		opc_l2i = 136;
    {{CodeType.LONG}, {CodeType.FLOAT}},                        //		public final static int		opc_l2f = 137;
    {{CodeType.LONG}, {CodeType.DOUBLE}},                        //		public final static int		opc_l2d = 138;
    {{CodeType.FLOAT}, {CodeType.INT}},                        //		public final static int		opc_f2i = 139;
    {{CodeType.FLOAT}, {CodeType.LONG}},                        //		public final static int		opc_f2l = 140;
    {{CodeType.FLOAT}, {CodeType.DOUBLE}},                        //		public final static int		opc_f2d = 141;
    {{CodeType.DOUBLE}, {CodeType.INT}},                        //		public final static int		opc_d2i = 142;
    {{CodeType.DOUBLE}, {CodeType.LONG}},                        //		public final static int		opc_d2l = 143;
    {{CodeType.DOUBLE}, {CodeType.FLOAT}},                        //		public final static int		opc_d2f = 144;
    {{CodeType.INT}, {CodeType.INT}},                        //		public final static int		opc_i2b = 145;
    {{CodeType.INT}, {CodeType.INT}},                        //		public final static int		opc_i2c = 146;
    {{CodeType.INT}, {CodeType.INT}},                        //		public final static int		opc_i2s = 147;
    {{CodeType.LONG, CodeType.LONG}, {CodeType.INT}},
    //		public final static int		opc_lcmp = 148;
    {{CodeType.FLOAT, CodeType.FLOAT}, {CodeType.INT}},
    //		public final static int		opc_fcmpl = 149;
    {{CodeType.FLOAT, CodeType.FLOAT}, {CodeType.INT}},
    //		public final static int		opc_fcmpg = 150;
    {{CodeType.DOUBLE, CodeType.DOUBLE}, {CodeType.INT}},
    //		public final static int		opc_dcmpl = 151;
    {{CodeType.DOUBLE, CodeType.DOUBLE}, {CodeType.INT}},
    //		public final static int		opc_dcmpg = 152;
    {{CodeType.INT}, null},                        //		public final static int		opc_ifeq = 153;
    {{CodeType.INT}, null},                        //		public final static int		opc_ifne = 154;
    {{CodeType.INT}, null},                        //		public final static int		opc_iflt = 155;
    {{CodeType.INT}, null},                        //		public final static int		opc_ifge = 156;
    {{CodeType.INT}, null},                        //		public final static int		opc_ifgt = 157;
    {{CodeType.INT}, null},                        //		public final static int		opc_ifle = 158;
    {{CodeType.INT, CodeType.INT}, null},                        //		public final static int		opc_if_icmpeq = 159;
    {{CodeType.INT, CodeType.INT}, null},                        //		public final static int		opc_if_icmpne = 160;
    {{CodeType.INT, CodeType.INT}, null},                        //		public final static int		opc_if_icmplt = 161;
    {{CodeType.INT, CodeType.INT}, null},                        //		public final static int		opc_if_icmpge = 162;
    {{CodeType.INT, CodeType.INT}, null},                        //		public final static int		opc_if_icmpgt = 163;
    {{CodeType.INT, CodeType.INT}, null},                        //		public final static int		opc_if_icmple = 164;
    {{CodeType.OBJECT, CodeType.OBJECT}, null},
    //		public final static int		opc_if_acmpeq = 165;
    {{CodeType.OBJECT, CodeType.OBJECT}, null},
    //		public final static int		opc_if_acmpne = 166;
    {null, null},                        //		public final static int		opc_goto = 167;
    {null, {CodeType.ADDRESS}},                        //		public final static int		opc_jsr = 168;
    {null, null},                        //		public final static int		opc_ret = 169;
    {{CodeType.INT}, null},                        //		public final static int		opc_tableswitch = 170;
    {{CodeType.INT}, null},                        //		public final static int		opc_lookupswitch = 171;
    {{CodeType.INT}, null},                        //		public final static int		opc_ireturn = 172;
    {{CodeType.LONG}, null},                        //		public final static int		opc_lreturn = 173;
    {{CodeType.FLOAT}, null},                        //		public final static int		opc_freturn = 174;
    {{CodeType.DOUBLE}, null},                        //		public final static int		opc_dreturn = 175;
    {{CodeType.OBJECT}, null},                        //		public final static int		opc_areturn = 176;
    {null, null},                        //		public final static int		opc_return = 177;
    null,                        //		public final static int		opc_getstatic = 178;
    null,                        //		public final static int		opc_putstatic = 179;
    null,                        //		public final static int		opc_getfield = 180;
    null,                        //		public final static int		opc_putfield = 181;
    null,                        //		public final static int		opc_invokevirtual = 182;
    null,                        //		public final static int		opc_invokespecial = 183;
    null,                        //		public final static int		opc_invokestatic = 184;
    null,                        //		public final static int		opc_invokeinterface = 185;
    null,                        //		public final static int		opc_xxxunusedxxx = 186;
    null,                        //		public final static int		opc_new = 187;
    null,                        //		public final static int		opc_newarray = 188;
    null,                        //		public final static int		opc_anewarray = 189;
    {{CodeType.OBJECT}, {CodeType.INT}},                        //		public final static int		opc_arraylength = 190;
    null,
    //		public final static int		opc_athrow = 191;
    null,
    //		public final static int		opc_checkcast = 192;
    null,
    //		public final static int		opc_instanceof = 193;
    {{CodeType.OBJECT}, null},                                //		public final static int		opc_monitorenter = 194;
    {{CodeType.OBJECT}, null},                                //		public final static int		opc_monitorexit = 195;
    null,
    //		public final static int		opc_wide = 196;
    null,
    //		public final static int		opc_multianewarray = 197;
    {{CodeType.OBJECT}, null},                                //		public final static int		opc_ifnull = 198;
    {{CodeType.OBJECT}, null},                                //		public final static int		opc_ifnonnull = 199;
    {null, null},                                                                        //		public final static int		opc_goto_w = 200;
    {null, {CodeType.ADDRESS}},                        //		public final static int		opc_jsr_w = 201;
  };

  private static final CodeType[] arr_type = new CodeType[]{
    CodeType.BOOLEAN,
    CodeType.CHAR,
    CodeType.FLOAT,
    CodeType.DOUBLE,
    CodeType.BYTE,
    CodeType.SHORT,
    CodeType.INT,
    CodeType.LONG
  };


  // Sonderbehandlung
  //	null,			//		public final static int		opc_aconst_null = 1;
  //	null, 			//		public final static int		opc_ldc = 18;
  //	null, 			//		public final static int		opc_ldc_w = 19;
  //	null, 			//		public final static int		opc_ldc2_w = 20;
  //	null,			//		public final static int		opc_aload = 25;
  //	null,			//		public final static int		opc_aaload = 50;
  //	null,			//		public final static int		opc_astore = 58;
  //	null, 			//		public final static int		opc_dup = 89;
  //	null, 			//		public final static int		opc_dup_x1 = 90;
  //	null, 			//		public final static int		opc_dup_x2 = 91;
  //	null, 			//		public final static int		opc_dup2 = 92;
  //	null, 			//		public final static int		opc_dup2_x1 = 93;
  //	null, 			//		public final static int		opc_dup2_x2 = 94;
  //	null, 			//		public final static int		opc_swap = 95;
  //	null, 			//		public final static int		opc_getstatic = 178;
  //	null, 			//		public final static int		opc_putstatic = 179;
  //	null, 			//		public final static int		opc_getfield = 180;
  //	null, 			//		public final static int		opc_putfield = 181;
  //	null, 			//		public final static int		opc_invokevirtual = 182;
  //	null, 			//		public final static int		opc_invokespecial = 183;
  //	null, 			//		public final static int		opc_invokestatic = 184;
  //	null, 			//		public final static int		opc_invokeinterface = 185;
  //	null,			//		public final static int		opc_new = 187;
  //	null,			//		public final static int		opc_newarray = 188;
  //	null,			//		public final static int		opc_anewarray = 189;
  //	null, 			//		public final static int		opc_athrow = 191;
  //	null,			//		public final static int		opc_checkcast = 192;
  //	null,			//		public final static int		opc_instanceof = 193;
  //	null, 			//		public final static int		opc_multianewarray = 197;


  public static void stepTypes(DataPoint data, Instruction instr, ConstantPool pool) {
    ListStack<VarType> stack = data.getStack();
    CodeType[][] arr = stack_impact[instr.opcode];

    if (arr != null) {
      // simple types only

      CodeType[] read = arr[0];
      CodeType[] write = arr[1];

      if (read != null) {
        int depth = 0;
        for (CodeType type : read) {
          depth++;
          if (type == CodeType.LONG ||
              type == CodeType.DOUBLE) {
            depth++;
          }
        }

        stack.removeMultiple(depth);
      }

      if (write != null) {
        for (CodeType type : write) {
          stack.push(new VarType(type));
          if (type == CodeType.LONG ||
              type == CodeType.DOUBLE) {
            stack.push(new VarType(CodeType.GROUP2EMPTY));
          }
        }
      }
    }
    else {
      // Sonderbehandlung
      processSpecialInstructions(data, instr, pool);
    }
  }

  private static void processSpecialInstructions(DataPoint data, Instruction instr, ConstantPool pool) {

    VarType var1;
    PrimitiveConstant cn;
    LinkConstant ck;

    ListStack<VarType> stack = data.getStack();

    switch (instr.opcode) {
      case CodeConstants.opc_aconst_null:
        stack.push(new VarType(CodeType.NULL, 0, null));
        break;
      case CodeConstants.opc_ldc:
      case CodeConstants.opc_ldc_w:
      case CodeConstants.opc_ldc2_w:
        PooledConstant constant = pool.getConstant(instr.operand(0));
        switch (constant.type) {
          case CodeConstants.CONSTANT_Integer:
            stack.push(new VarType(CodeType.INT));
            break;
          case CodeConstants.CONSTANT_Float:
            stack.push(new VarType(CodeType.FLOAT));
            break;
          case CodeConstants.CONSTANT_Long:
            stack.push(new VarType(CodeType.LONG));
            stack.push(new VarType(CodeType.GROUP2EMPTY));
            break;
          case CodeConstants.CONSTANT_Double:
            stack.push(new VarType(CodeType.DOUBLE));
            stack.push(new VarType(CodeType.GROUP2EMPTY));
            break;
          case CodeConstants.CONSTANT_String:
            stack.push(new VarType(CodeType.OBJECT, 0, "java/lang/String"));
            break;
          case CodeConstants.CONSTANT_Class:
            stack.push(new VarType(CodeType.OBJECT, 0, "java/lang/Class"));
            break;
          case CodeConstants.CONSTANT_MethodHandle:
            stack.push(new VarType(((LinkConstant)constant).descriptor));
            break;
          case CodeConstants.CONSTANT_Dynamic:
            ck = pool.getLinkConstant(instr.operand(0));
            FieldDescriptor fd = FieldDescriptor.parseDescriptor(ck.descriptor);
            if (fd.type.type != CodeType.VOID) {
              stack.push(fd.type);
              if (fd.type.stackSize == 2) {
                stack.push(new VarType(CodeType.GROUP2EMPTY));
              }
            }
            break;
        }
        break;
      case CodeConstants.opc_aload:
        var1 = data.getVariable(instr.operand(0));
        if (var1 != null) {
          stack.push(var1);
        }
        else {
          stack.push(new VarType(CodeType.OBJECT, 0, null));
        }
        break;
      case CodeConstants.opc_aaload:
        var1 = stack.pop(2);
        stack.push(new VarType(var1.type, var1.arrayDim - 1, var1.value));
        break;
      case CodeConstants.opc_astore:
        data.setVariable(instr.operand(0), stack.pop());
        break;
      case CodeConstants.opc_dup:
      case CodeConstants.opc_dup_x1:
      case CodeConstants.opc_dup_x2:
        int depth1 = 88 - instr.opcode;
        stack.insertByOffset(depth1, stack.getByOffset(-1));
        break;
      case CodeConstants.opc_dup2:
      case CodeConstants.opc_dup2_x1:
      case CodeConstants.opc_dup2_x2:
        int depth2 = 90 - instr.opcode;
        stack.insertByOffset(depth2, stack.getByOffset(-2));
        stack.insertByOffset(depth2, stack.getByOffset(-1));
        break;
      case CodeConstants.opc_swap:
        var1 = stack.pop();
        stack.insertByOffset(-1, var1);
        break;
      case CodeConstants.opc_getfield:
        stack.pop();
      case CodeConstants.opc_getstatic:
        ck = pool.getLinkConstant(instr.operand(0));
        var1 = new VarType(ck.descriptor);
        stack.push(var1);
        if (var1.stackSize == 2) {
          stack.push(new VarType(CodeType.GROUP2EMPTY));
        }
        break;
      case CodeConstants.opc_putfield:
        stack.pop();
      case CodeConstants.opc_putstatic:
        ck = pool.getLinkConstant(instr.operand(0));
        var1 = new VarType(ck.descriptor);
        stack.pop(var1.stackSize);
        break;
      case CodeConstants.opc_invokevirtual:
      case CodeConstants.opc_invokespecial:
      case CodeConstants.opc_invokeinterface:
        stack.pop();
      case CodeConstants.opc_invokestatic:
      case CodeConstants.opc_invokedynamic:
        if (instr.opcode != CodeConstants.opc_invokedynamic || instr.bytecodeVersion.hasInvokeDynamic()) {
          ck = pool.getLinkConstant(instr.operand(0));
          MethodDescriptor md = MethodDescriptor.parseDescriptor(ck.descriptor);
          for (int i = 0; i < md.params.length; i++) {
            stack.pop(md.params[i].stackSize);
          }
          if (md.ret.type != CodeType.VOID) {
            stack.push(md.ret);
            if (md.ret.stackSize == 2) {
              stack.push(new VarType(CodeType.GROUP2EMPTY));
            }
          }
        }
        break;
      case CodeConstants.opc_new:
        cn = pool.getPrimitiveConstant(instr.operand(0));
        stack.push(new VarType(CodeType.OBJECT, 0, cn.getString()));
        break;
      case CodeConstants.opc_newarray:
        stack.pop();
        stack.push(new VarType(arr_type[instr.operand(0) - 4], 1).resizeArrayDim(1));
        break;
      case CodeConstants.opc_athrow:
        var1 = stack.pop();
        stack.clear();
        stack.push(var1);
        break;
      case CodeConstants.opc_checkcast:
      case CodeConstants.opc_instanceof:
        stack.pop();
        cn = pool.getPrimitiveConstant(instr.operand(0));
        stack.push(new VarType(CodeType.OBJECT, 0, cn.getString()));
        break;
      case CodeConstants.opc_anewarray:
      case CodeConstants.opc_multianewarray:
        int dimensions = (instr.opcode == CodeConstants.opc_anewarray) ? 1 : instr.operand(1);
        stack.pop(dimensions);
        cn = pool.getPrimitiveConstant(instr.operand(0));
        if (cn.isArray) {
          var1 = new VarType(CodeType.OBJECT, 0, cn.getString());
          var1 = var1.resizeArrayDim(var1.arrayDim + dimensions);
          stack.push(var1);
        }
        else {
          stack.push(new VarType(CodeType.OBJECT, dimensions, cn.getString()));
        }
    }
  }
}
