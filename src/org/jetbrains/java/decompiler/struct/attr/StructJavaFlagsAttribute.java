// Copyright 2021 QuiltMC Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.struct.attr;

import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.IOException;

/*
  JavaFlags_attribute {
      u2 attribute_name_index;
      u4 attribute_length;
      u2 flags;
  }
 */
public class StructJavaFlagsAttribute extends StructGeneralAttribute {
  private int flags;

  public int getFlags() {
    return flags;
  }

  @Override
  public void initContent(DataInputFullStream data, ConstantPool pool) throws IOException {
    flags = data.readUnsignedShort();
  }
}
