// Copyright 2019 FabricMC project. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package net.fabricmc.fernflower.api;

import org.jetbrains.java.decompiler.main.extern.IResultSaver;

public interface IFabricResultSaver extends IResultSaver {
  void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content, int[] mapping);
}
