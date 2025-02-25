package model.exceptions;

/**
 * Exception thrown when an event is not found.
 */
public class EventNotFoundException extends RuntimeException {
  public EventNotFoundException(String message) {
    super(message);
  }
}
