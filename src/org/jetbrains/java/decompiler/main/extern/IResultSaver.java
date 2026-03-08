// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.main.extern;

import org.jetbrains.java.decompiler.api.ClassContent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.jar.Manifest;

public interface IResultSaver extends AutoCloseable {
  void saveFolder(String path);

  void copyFile(String source, String path, String entryName);

  @Deprecated(forRemoval = true)
  default void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {

  }

  void saveClassFile(String path, String qualifiedName, String entryName, ClassContent content);

  void createArchive(String path, String archiveName, Manifest manifest);

  void saveDirEntry(String path, String archiveName, String entryName);

  void copyEntry(String source, String path, String archiveName, String entry);

  void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content);

  @Deprecated(forRemoval = true)
  default void saveClassEntry(final String path, final String archiveName, final String qualifiedName, final String entryName, final String content, final int[] mapping) {
    this.saveClassEntry(path, archiveName, qualifiedName, entryName, content);
  }

  default void saveClassEntry(final String path, final String archiveName, final String qualifiedName, final String entryName, ClassContent content) {
    this.saveClassEntry(path, archiveName, qualifiedName, entryName, content.content(), null);
  }

  void closeArchive(String path, String archiveName);

  @Override
  default void close() throws IOException {}

  default byte[] getCodeLineData(int[] mappings) {
    if (mappings == null || mappings.length == 0) {
      return null;
    }
    ByteBuffer buf = ByteBuffer.allocate(5 + (mappings.length * 2));
    buf.order(ByteOrder.LITTLE_ENDIAN);
    // Zip Extra entry header, described in http://www.info-zip.org/doc/appnote-19970311-iz.zip
    buf.putShort((short)0x4646); // FF - ForgeFlower
    buf.putShort((short)((mappings.length * 2) + 1)); // Mapping data + our version marker
    buf.put((byte)1); // Version code, in case we want to change it in the future.
    for (int line : mappings) {
        buf.putShort((short)line);
    }
    return buf.array();
  }

  default byte[] getCodeLineData(Map<Integer, Integer> mappings) {
    if (mappings == null || mappings.isEmpty()) {
      return null;
    }
    int len = mappings.size();
    ByteBuffer buf = ByteBuffer.allocate(5 + (len * 2));
    buf.order(ByteOrder.LITTLE_ENDIAN);
    // Zip Extra entry header, described in http://www.info-zip.org/doc/appnote-19970311-iz.zip
    buf.putShort((short)0x4646); // FF - ForgeFlower
    buf.putShort((short)((len * 2) + 1)); // Mapping data + our version marker
    buf.put((byte)1); // Version code, in case we want to change it in the future.
    for (Map.Entry<Integer, Integer> e : mappings.entrySet()) {
      buf.putShort(e.getKey().shortValue());
      buf.putShort(e.getValue().shortValue());
    }
    return buf.array();
  }
}
