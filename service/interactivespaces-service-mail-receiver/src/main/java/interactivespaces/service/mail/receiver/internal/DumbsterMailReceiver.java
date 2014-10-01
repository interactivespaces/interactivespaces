/*
 * Copyright (C) 2014 Google Inc.
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

import interactivespaces.service.mail.common.MailMessage;
import interactivespaces.service.mail.receiver.MailReceiver;
import interactivespaces.service.mail.receiver.MailReceiverListener;

import com.dumbster.smtp.SmtpServer;
import com.dumbster.smtp.SmtpServerFactory;
import org.apache.commons.logging.Log;

/**
 * @author Keith M. Hughes
 */
public class DumbsterMailReceiver implements MailReceiver, MailReceiverListener {

  /**
   * The port to listen to for SMTP messages.
   */
  private final int port;

  /**
   * The SMTP server.
   */
  private SmtpServer server;

  /**
   * The mail store to use.
   */
  private final CallbackMailStore mailStore = new CallbackMailStore();

  /**
   * The logger to use.
   */
  private final Log log;

  /**
   * Construct a mail receiver.
   *
   * @param port
   *          the port to listen to for SMTP messages
   * @param log
   *          the logger to use
   */
  public DumbsterMailReceiver(int port, Log log) {
    this.port = port;
    this.log = log;
  }

  @Override
  public void startup() {
    log.info("Starting mail server");

    server = SmtpServerFactory.startServer(port);
    server.setThreaded(true);
    server.setMailStore(mailStore);
  }

  @Override
  public void shutdown() {
    if (server != null) {
      server.stop();
      server = null;

      mailStore.clearMessages();
    }
  }

  @Override
  public void addListener(MailReceiverListener listener) {
    mailStore.addListener(listener);
  }

  @Override
  public void removeListener(MailReceiverListener listener) {
    mailStore.removeListener(listener);
  }

  @Override
  public void onMailMessageReceive(MailMessage message) {
    log.debug(String.format("Got email message\n%s", message.getBody()));
  }
}
