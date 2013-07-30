/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.service.mail.receiver.internal;

import interactivespaces.service.mail.common.SimpleMailMessage;
import interactivespaces.service.mail.receiver.MailReceiverListener;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A Dumbster mail store that can call back.
 *
 * @author Keith M. Hughes
 */
public class CallbackMailStore implements MailStore {

  /**
   * List of all messages.
   */
  private List<MailMessage> messages = new CopyOnWriteArrayList<MailMessage>();

  /**
   * List of all messages.
   */
  private List<MailReceiverListener> listeners = new CopyOnWriteArrayList<MailReceiverListener>();

  @Override
  public void addMessage(MailMessage message) {
    messages.add(message);

    String body = message.getBody();

    SimpleMailMessage m = new SimpleMailMessage();
    for (MailReceiverListener listener : listeners) {
      listener.onMailMessageReceive(m);
    }
  }

  @Override
  public void clearMessages() {
    messages.clear();
  }

  @Override
  public int getEmailCount() {
    return messages.size();
  }

  @Override
  public MailMessage getMessage(int messageNumber) {
    return messages.get(messageNumber);
  }

  @Override
  public MailMessage[] getMessages() {
    return messages.toArray(new MailMessage[0]);
  }

  /**
   * Add a listener to the service.
   *
   * @param listener
   *          the listener to add
   */
  public void addListener(MailReceiverListener listener) {
    listeners.add(listener);
  }
}
