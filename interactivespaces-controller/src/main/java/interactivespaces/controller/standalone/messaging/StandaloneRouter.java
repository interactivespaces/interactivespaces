package interactivespaces.controller.standalone.messaging;

import interactivespaces.util.resource.ManagedResource;

/**
 */
public interface StandaloneRouter extends ManagedResource {
  @Override
  void startup();

  @Override
  void shutdown();

  /**
   * @return {@code true} if the component is currently running.
   */
  boolean isRunning();

  /**
   * Send a message. Will modify the outgoing message with medium-specific information.
   *
   * @param messageObject
   *          message to send.
   */
  void send(MessageUtils.MessageMap messageObject);

  /**
   * Receive a multicast message.
   *
   * @return received message
   */
  MessageUtils.MessageMap receive();
}
