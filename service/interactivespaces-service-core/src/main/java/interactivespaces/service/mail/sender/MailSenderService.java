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

package interactivespaces.service.mail.sender;

import interactivespaces.service.SupportedService;
import interactivespaces.service.mail.common.MailMessage;

/**
 * An Interactive Spaces service for sending email.
 *
 * @author Keith M. Hughes
 */
public interface MailSenderService extends SupportedService {

  /**
   * The name of the service.
   */
  String SERVICE_NAME = "mail.sender";

  /**
   * Configuration property for SMTP host interactive spaces should use.
   */
  String CONFIGURATION_MAIL_SMTP_HOST = "interactivespaces.service.mail.sender.smtp.host";

  /**
   * Configuration property for SMTP host port interactive spaces should use.
   */
  String CONFIGURATION_MAIL_SMTP_PORT = "interactivespaces.service.mail.sender.smtp.port";

  /**
   * The default value for the {@link #CONFIGURATION_MAIL_SMTP_PORT} parameter.
   */
  String CONFIGURATION_DEFAULT_MAIL_SMTP_PORT = "25";

  /**
   * Send a mail message.
   *
   * @param message
   *          the message to send
   */
  void sendMailMessage(MailMessage message);
}
