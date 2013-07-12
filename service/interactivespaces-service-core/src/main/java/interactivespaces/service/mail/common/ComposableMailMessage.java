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

package interactivespaces.service.mail.common;

/**
 * A {@link MailMessage} which is composable
 *
 * @author Keith M. Hughes
 */
public interface ComposableMailMessage extends MailMessage {

  /**
   * Add a To address.
   *
   * @param address
   *          the address to add
   */
  void addToAddress(String address);

  /**
   * Add an CC address.
   *
   * @param address
   *          the address to add
   */
  void addCcAddress(String address);

  /**
   * Add a BCC address.
   *
   * @param address
   *          the address to add
   */
  void addBccAddress(String address);

  /**
   * Set the from address of the mail.
   *
   * @param address
   *          the address
   */
  void setFromAddress(String address);

  /**
   * Set the subject of the mail.
   *
   * @param subject
   *          the subject
   */
  void setSubject(String subject);

  /**
   * Set the body of the message.
   *
   * @param body
   *          the complete body of the message
   */
  void setBody(String body);
}
