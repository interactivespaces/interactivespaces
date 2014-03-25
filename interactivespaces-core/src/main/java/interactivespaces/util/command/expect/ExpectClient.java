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

package interactivespaces.util.command.expect;

import interactivespaces.InteractiveSpacesException;

/**
 * A client for connecting to a resource and using an Expect session to
 * communicate with the resource.
 *
 * @author Keith M. Hughes
 */
public interface ExpectClient {

  /**
   * Connect to the remote device and prepare for communication.
   *
   * @throws InteractiveSpacesException
   *           something else bad happened
   */
  void connect() throws InteractiveSpacesException;

  /**
   * Disconnect from remote connection.
   *
   * <p>
   * Once disconnected, this client cannot be used again.
   */
  void disconnect();

  /**
   * Expect a particular string.
   *
   * @param expectedString
   *          the string to expect, can be a Java regex expression
   *
   * @throws InteractiveSpacesException
   *           no match was found, the client was not logged in, or something
   *           else bad happened
   */
  void expect(String expectedString) throws InteractiveSpacesException;

  /**
   * Send content to the remote system.
   *
   * <p>
   * A newline is appended.
   *
   * @param content
   *          the content to send
   *
   * @throws InteractiveSpacesException
   *           the client was not logged in, or something else bad happened
   */
  void sendLn(String content) throws InteractiveSpacesException;

  /**
   * Send content to the remote system.
   *
   * @param content
   *          the content to send
   *
   * @throws InteractiveSpacesException
   *           the client was not logged in, or something else bad happened
   */
  void send(String content) throws InteractiveSpacesException;

  /**
   * Get the timeout for content from the remote.
   *
   * @return the timeout, in milliseconds
   */
  int getTargetTimeout();

  /**
   * Set the timeout for content from the remote.
   *
   * @param targetTimeout
   *          the timeout, in milliseconds
   */
  void setTargetTimeout(int targetTimeout);
}