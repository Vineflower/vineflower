package org.jetbrains.java.decompiler.main.decompiler;

import javax.swing.*;
import java.awt.*;

public class GuiHelp {
  public static boolean check() {
    if (System.console() == null && !Boolean.getBoolean("QF_NO_GUI_HELP") && !GraphicsEnvironment.isHeadless()) {
      JOptionPane.showMessageDialog(null, "Quiltflower needs to be run from the command line.\n\njava -jar quiltflower.jar <source> <destination>", "Quiltflower", JOptionPane.ERROR_MESSAGE);
      return true;
    }
    return false;
  }
}
