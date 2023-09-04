package org.jetbrains.java.decompiler.main.decompiler;

/**
 * Used for cancelling the decompilation process. This can for example be useful in GUI frontends with cancelation
 * support.
 */
public final class CancelationManager {
  private static Runnable cancelationChecker = () -> {};

  private CancelationManager() {
  }

  /**
   * Cancels the decompilation process by throwing a {@linkplain CanceledException}.
   */
  public static void cancel() {
    throw CanceledException.INSTANCE;
  }

  /**
   * Polled frequently by the decompiler to check if decompilation has been canceled. Use
   * {@linkplain #setCancelationChecker(Runnable)} to set the logic for checking cancelation.
   */
  public static void checkCanceled() {
    cancelationChecker.run();
  }

  /**
   * Sets the logic for checking cancelation. To cancel decompilation, call {@linkplain #cancel()} inside the checker.
   */
  public static void setCancelationChecker(Runnable checker) {
    cancelationChecker = checker;
  }

  /**
   * The exception that is thrown upon cancelation.
   */
  public static final class CanceledException extends RuntimeException {
    public static final CanceledException INSTANCE = new CanceledException();

    private CanceledException() {
    }

    @Override
    public Throwable fillInStackTrace() {
      return this;
    }
  }
}
