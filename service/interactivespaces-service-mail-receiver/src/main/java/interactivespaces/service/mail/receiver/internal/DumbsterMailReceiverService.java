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

import com.google.common.collect.Maps;

import interactivespaces.service.mail.common.MailMessage;
import interactivespaces.service.mail.receiver.MailReceiverListener;
import interactivespaces.service.mail.receiver.MailReceiverService;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.dumbster.smtp.SmtpServer;
import com.dumbster.smtp.SmtpServerFactory;

import java.util.Map;

/**
 * A {@link MailReceiverService} which uses Dumbster.
 *
 * @author Keith M. Hughes
 */
public class DumbsterMailReceiverService implements MailReceiverService, MailReceiverListener {

  /**
   * Space environment being run under.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The SMTP server.
   */
  private SmtpServer server;

  /**
   * The mail store to use..
   */
  private CallbackMailStore mailStore = new CallbackMailStore();

  /**
   * The metadata for the service.
   */
  private Map<String, Object> metadata = Maps.newHashMap();

  @Override
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  @Override
  public String getName() {
    return MailReceiverService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    spaceEnvironment.getLog().info("Starting mail server");

    server = SmtpServerFactory.startServer(9999);
    server.setThreaded(true);
    server.setMailStore(mailStore);

    // Eventually will go away
    mailStore.addListener(this);
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
  public void onMailMessageReceive(MailMessage message) {
    spaceEnvironment.getLog().info(String.format("Got email message\n%s", message.getBody()));
  }

  @Override
  public void setSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    this.spaceEnvironment = spaceEnvironment;
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void bindSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    setSpaceEnvironment(spaceEnvironment);
  }

  /**
   * @param spaceEnvironment
   *          the spaceEnvironment to set
   */
  public void unbindSpaceEnvironment(InteractiveSpacesEnvironment spaceEnvironment) {
    setSpaceEnvironment(null);
  }
}