// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.attr;

import org.jetbrains.java.decompiler.struct.StructMember;
import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/*
  u2 max_stack;
  u2 max_locals;
  u4 code_length;
  u1 code[];
  u2 exception_table_length;
  exception_table[] {
     u2 start_pc;
     u2 end_pc;
     u2 handler_pc;
     u2 catch_type;
  };
  u2 attributes_count;
  attribute_info attributes[];
*/
public class StructCodeAttribute extends StructGeneralAttribute {
  public int localVariables = 0;
  public byte[] codeAndExceptionData;
  public Map<String, StructGeneralAttribute> codeAttributes;

  @Override
  public void initContent(DataInputFullStream data, ConstantPool pool) throws IOException {
    data.discard(2);
    localVariables = data.readUnsignedShort();
    byte[] codeData = new byte[data.readInt()];
    data.readFully(codeData);
    int excLength = data.readUnsignedShort();
    byte[] exceptionData = new byte[excLength * 8];
    data.readFully(exceptionData);
    ByteArrayOutputStream baos = new ByteArrayOutputStream(codeData.length + 2 + exceptionData.length);
    DataOutputStream d = new DataOutputStream(baos);
    d.writeInt(codeData.length);
    d.write(codeData);
    d.writeShort(excLength);
    d.write(exceptionData);
    codeAndExceptionData = baos.toByteArray();
    codeAttributes = StructMember.readAttributes(data, pool);
  }
}
