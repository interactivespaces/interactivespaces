package interactivespaces.controller.standalone.messaging;

/**
 * A warning from the messaging layer, wrapped as an exception.
 */
public class MessageWarning extends RuntimeException {

  /**
   * Create a new warning.
   *
   * @param message
   *          warning message
   */
  public MessageWarning(String message) {
     super(message);
  }
}

