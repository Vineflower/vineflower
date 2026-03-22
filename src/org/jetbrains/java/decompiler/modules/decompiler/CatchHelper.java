// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.java.decompiler.modules.decompiler;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.decompiler.modules.decompiler.stats.*;
import org.jetbrains.java.decompiler.modules.decompiler.stats.Statement.EdgeDirection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public final class CatchHelper {
  public static boolean condenseTryCatch(RootStatement root) {
    if (!root.hasTryCatch()) {
      return false;
    }

    // Condense nested try catches where the inner try catch has empty catches
    // These are most likely created by the exception deobfuscator.
    ValidationHelper.validateStatement(root);

    boolean res = condenseTryCatchRec(root);

    ValidationHelper.validateStatement(root);

    return res;
  }

  private static boolean condenseTryCatchRec(Statement stat) {
    boolean res = false;
    while (true) {
      Statement newStat = condense(stat);
      if (newStat == null) break;
      stat = newStat;
      res = true;
    }
    for (Statement subStat : new ArrayList<>(stat.getStats())) {
      res |= condenseTryCatchRec(subStat);
    }
    return res;
  }

  private static @Nullable Statement condense(Statement stat) {
    if (stat instanceof CatchStatement outerCatch
      && outerCatch.getFirst() instanceof CatchStatement innerCatch
      && outerCatch.isSingleCatchAll()
    ) {
      for (int i = 0; i < innerCatch.getExctStrings().size(); i++) {
        List<String> excStrings = innerCatch.getExctStrings().get(i);

        if (excStrings.size() == 1 && excStrings.get(0).equals("java/lang/Throwable")) {
          // Can't flatten due to syntactic reasons
          return null;
        }

        Statement handler = innerCatch.getHandler(i);
        if (!(handler instanceof BasicBlockStatement bbStat
          && (bbStat.getExprents() == null || bbStat.getExprents().isEmpty()))) {
          // Only flatten if catch is empty
          return null;
        }
      }

      Statement outerHandler = outerCatch.getHandler(0);
      innerCatch.getVars().add(outerCatch.getVars().get(0));
      innerCatch.getExctStrings().add(outerCatch.getExctStrings().get(0));
      innerCatch.getStats().addWithKey(outerHandler, outerHandler.id);
      outerHandler.setParent(innerCatch);
      outerCatch.replaceWith(innerCatch);

      return innerCatch;
    }
    return null;
  }

}
