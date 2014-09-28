package interactivespaces.controller.standalone.messaging;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Simple standalone router that just loops messages back -- useful when no other valid interface is available.
 */
public class LoopbackRouter implements StandaloneRouter {

  /**
   * Internal message queue of messages for loopback.
   */
  private final BlockingQueue<MessageUtils.MessageMap> messageQueue = new LinkedBlockingQueue<MessageUtils.MessageMap>();

  /**
   * Current state of this component.
   */
  private volatile boolean isRunning;

  /**
   * Basic constructor.
   *
   * @param configuration
   *          configuration of the router
   */
  public LoopbackRouter(Configuration configuration) {
  }

  @Override
  public void startup() {
    isRunning = true;
  }

  @Override
  public void shutdown() {
    isRunning = false;
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }

  @Override
  public void send(MessageUtils.MessageMap messageObject) {
    messageQueue.add(messageObject);
  }

  @Override
  public MessageUtils.MessageMap receive() {
    try {
      return messageQueue.take();
    } catch (InterruptedException e) {
      throw new SimpleInteractiveSpacesException("LoopbackRouter interrupted", e);
    }
  }
}
