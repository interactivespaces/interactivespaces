package interactivespaces.controller.standalone.messaging;

import interactivespaces_msgs.GenericMessage;
import org.ros.internal.message.RawMessage;

/**
 * A generic message wrapper to use for standalone systems.
 */
public class StandaloneGenericMessage implements GenericMessage {
  /**
   * Type of the message.
   */
  private String type;

  /**
   * The message itself.
   */
  private String message;

  @Override
  public String getType() {
    return type;
  }

  @Override
  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public RawMessage toRawMessage() {
    throw new UnsupportedOperationException();
  }
}
