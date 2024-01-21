package org.jetbrains.java.decompiler.main.plugins;

public class PluginImplementationException extends RuntimeException {
  public PluginImplementationException() {
    this("Plugin should implement custom handling");
  }

  public PluginImplementationException(String message) {
    super(message);
  }
}
