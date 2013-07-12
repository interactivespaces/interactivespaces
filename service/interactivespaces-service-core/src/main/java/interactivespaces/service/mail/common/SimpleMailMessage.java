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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * A concrete mail message which is composable.
 *
 * @author Keith M. Hughes
 */
public class SimpleMailMessage implements ComposableMailMessage {

  /**
   * All To addresses for this mail.
   */
  private List<String> toAddresses = Lists.newArrayList();

  /**
   * All CC addresses for this mail.
   */
  private List<String> ccAddresses = Lists.newArrayList();

  /**
   * All BCC addresses for this mail.
   */
  private List<String> bccAddresses = Lists.newArrayList();

  /**
   * The FROM address for this email.
   */
  private String fromAddress;

  /**
   * Subject of the email.
   */
  private String subject;

  /**
   * Body of the email.
   */
  private String body;

  @Override
  public List<String> getToAdresses() {
    return toAddresses;
  }

  @Override
  public List<String> getCcAdresses() {
    return ccAddresses;
  }

  @Override
  public List<String> getBccAdresses() {
    return bccAddresses;
  }

  @Override
  public String getFromAddress() {
    return fromAddress;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public String getBody() {
    return body;
  }

  @Override
  public void addToAddress(String address) {
    toAddresses.add(address);
  }

  @Override
  public void addCcAddress(String address) {
    ccAddresses.add(address);
  }

  @Override
  public void addBccAddress(String address) {
    bccAddresses.add(address);
  }

  @Override
  public void setFromAddress(String address) {
    this.fromAddress = address;
  }

  @Override
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @Override
  public void setBody(String body) {
    this.body = body;
  }
}
