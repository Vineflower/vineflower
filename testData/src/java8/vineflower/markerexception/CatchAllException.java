package vineflower.markerexception;

public class CatchAllException extends RuntimeException {
  public CatchAllException(String message) {
    super(message);
  }
}
