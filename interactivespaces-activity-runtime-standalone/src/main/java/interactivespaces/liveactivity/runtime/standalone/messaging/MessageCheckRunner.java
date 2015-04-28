/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package interactivespaces.liveactivity.runtime.standalone.messaging;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageMap;
import interactivespaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageSet;
import interactivespaces.liveactivity.runtime.standalone.messaging.MessageUtils.MessageSetList;

import java.io.File;

/**
 * Object to manage checking produced messages to see if they match the indicated check file.
 *
 * @author Trevor Pering
 */
class MessageCheckRunner {

  /**
   * File that contains the check messages.
   */
  private final File traceCheckFile;

  /**
   * Parsed list of messages.
   */
  private final MessageSetList expectedMessageList;

  /**
   * The next message object to check.
   */
  private MessageMap nextExpectedMessage;

  /**
   * Next message input to check.
   */
  private int messageIndex = 0;

  /**
   * The message router that we are checking.
   */
  private final StandaloneMessageRouter standaloneMessageRouter;

  /**
   * Create a check runner.
   * @param standaloneMessageRouter
   *          message router to verify
   * @param traceCheckPath
   *          path of file containing check messages
   */
  public MessageCheckRunner(StandaloneMessageRouter standaloneMessageRouter, String traceCheckPath) {
    this.standaloneMessageRouter = standaloneMessageRouter;
    traceCheckFile = new File(traceCheckPath);
    expectedMessageList = MessageUtils.readMessageList(traceCheckFile);
  }

  /**
   * Check the message against the next expected message.
   *
   * @param message
   *          message to check
   *
   * @return {@code true} if all messages have been verified
   */
  public synchronized boolean checkMessage(MessageMap message) {
    message = standaloneMessageRouter.makeTraceMessage(message);
    if (message == null) {
      return false;
    }

    if (messageIndex < expectedMessageList.size()) {
      if (nextExpectedMessage == null) {
        nextExpectedMessage = expectedMessageList.get(messageIndex).get(MessageSet.MESSAGE_KEY);

        // Use the delay of the checked messages to figure out how long to wait. This isn't precise, but
        // it should at least be a flexible way of waiting.  Note that this is *2 to make sure there is
        // enough wait time.
        Object timeDelay = nextExpectedMessage.get(StandaloneMessageRouter.TIME_DELAY_KEY);
        if (!(timeDelay instanceof Integer)) {
          throw new SimpleInteractiveSpacesException(String.format("Message must have a valid Integer '%s' field: %s",
              StandaloneMessageRouter.TIME_DELAY_KEY, nextExpectedMessage));
        }
        long finishDelayMs = ((Integer) timeDelay) * 2;
        standaloneMessageRouter.setFinishDelta(finishDelayMs);

        nextExpectedMessage.remove(StandaloneMessageRouter.SEGMENT_KEY);
        nextExpectedMessage.remove(StandaloneMessageRouter.SOURCE_UUID_KEY);
        nextExpectedMessage.remove(StandaloneMessageRouter.TIME_DELAY_KEY);
      }

      if (MessageUtils.deepMatches(message, nextExpectedMessage)) {
        standaloneMessageRouter.getLog().info("Message index " + messageIndex + " verified");
        messageIndex++;
        nextExpectedMessage = null;
      }
    }

    return messageIndex >= expectedMessageList.size();
  }

  /**
   * Verify completion. Will signal success or failure to containing activity runner..
   */
  synchronized void verifyFinished() {
    if (messageIndex < expectedMessageList.size()) {
      standaloneMessageRouter.getLog().error(String.format("Failed to verify message #%d: %s",
          messageIndex, expectedMessageList.get(messageIndex)));
      standaloneMessageRouter.getActivityRunner().signalCompletion(false);
    } else {
      standaloneMessageRouter.getLog().info("All messages verified consumed");
      standaloneMessageRouter.getActivityRunner().signalCompletion(true);
    }
  }
}
