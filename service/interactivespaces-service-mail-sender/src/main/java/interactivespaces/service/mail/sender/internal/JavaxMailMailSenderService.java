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

package interactivespaces.service.mail.sender.internal;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.service.BaseSupportedService;
import interactivespaces.service.mail.common.MailMessage;
import interactivespaces.service.mail.sender.MailSenderService;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A {@link MailMessage} which uses Javax mail.
 *
 * @author Keith M. Hughes
 */
public class JavaxMailMailSenderService extends BaseSupportedService implements MailSenderService {

  /**
   * Configuration property for SMTP host interactive spaces should use. >>>>>>>
   * master
   */
  public static final String MAIL_TRANSPORT_SMTP = "smtp";

  /**
   * The javax.mail property for setting the host for the remote
   * SMTP server.
   */
  public static final String PROPERTY_MAIL_SMTP_HOST = "mail.smtp.host";

  /**
   * The javax.mail property for setting the port for the remote
   * SMTP server.
   */
  public static final String PROPERTY_MAIL_SMTP_PORT = "mail.smtp.port";

  /**
   * The Javamail mailerSession for sending messages.
   */
  private Session mailerSession;

  @Override
  public String getName() {
    return MailSenderService.SERVICE_NAME;
  }

  @Override
  public void startup() {
    getSpaceEnvironment().getLog().info("Mail sending service starting up");

    Properties props = System.getProperties();

    // Setup mail server
    String smtpHost =
        getSpaceEnvironment().getSystemConfiguration().getPropertyString(
            CONFIGURATION_MAIL_SMTP_HOST);
    if (smtpHost != null) {
      props.put(PROPERTY_MAIL_SMTP_HOST, smtpHost);
      props.put(PROPERTY_MAIL_SMTP_PORT, getSpaceEnvironment().getSystemConfiguration()
          .getPropertyString(CONFIGURATION_MAIL_SMTP_PORT, CONFIGURATION_DEFAULT_MAIL_SMTP_PORT));

      mailerSession = Session.getDefaultInstance(props, null);
    } else {
      getSpaceEnvironment().getLog().warn("Mail service not configured. No smptp host given");
    }
  }

  @Override
  public void sendMailMessage(MailMessage message) {
    if (mailerSession == null) {
      throw new SimpleInteractiveSpacesException("Mail service not configured");
    }

    // Define message
    MimeMessage msg = new MimeMessage(mailerSession);
    try {
      msg.setFrom(new InternetAddress(message.getFromAddress()));

      for (String address : message.getToAdresses()) {
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
      }

      for (String address : message.getCcAdresses()) {
        msg.addRecipient(Message.RecipientType.CC, new InternetAddress(address));
      }

      for (String address : message.getBccAdresses()) {
        msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(address));
      }

      msg.setSubject(message.getSubject());
      msg.setText(message.getBody());

      // Send message
      Transport transport = mailerSession.getTransport(MAIL_TRANSPORT_SMTP);
      transport.connect();
      transport.sendMessage(msg, msg.getAllRecipients());
      transport.close();

      getSpaceEnvironment().getLog().info("Sent mail successfully");
    } catch (Throwable e) {
      throw new InteractiveSpacesException("Could not send mail", e);
    }
  }
}
