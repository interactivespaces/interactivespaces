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
import java.net.InetAddress;

/**
 * An Expect Client for a network reachable service.
 *
 * @author Keith M. Hughes
 */
public interface NetworkReachableExpectClient extends ExpectClient {

  /**
   * Get the target host for the connection.
   *
   * @return the target host
   */
  InetAddress getTargetHost();

  /**
   * Set the target host for the connection.
   *
   * @param targetHost
   *          the target host
   */
  void setTargetHost(InetAddress targetHost);

  /**
   * Get the target port.
   *
   * @return the target port
   */
  int getTargetPort();

  /**
   * Set the target port.
   *
   * @param targetPort
   *          the target port
   */
  void setTargetPort(int targetPort);
}