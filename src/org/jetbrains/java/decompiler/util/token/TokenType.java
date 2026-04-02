package org.jetbrains.java.decompiler.util.token;

public enum TokenType {
  PUNCTUATION,
  TEXT,
  OPERATOR,
  KEYWORD,
  COMMENT,
  NUMBER,

  CLASS,
  FIELD,
  METHOD,
  PARAMETER,
  LOCAL_VARIABLE,
  LABEL,
  ANNOTATION,
  GENERIC,
  MODULE,
}
