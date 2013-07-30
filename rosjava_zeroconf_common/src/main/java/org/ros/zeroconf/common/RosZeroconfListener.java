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

package org.ros.zeroconf.common;

/**
 * A listener for {@link RosZeroconf} events.
 *
 * @author Keith M. Hughes
 */
public interface RosZeroconfListener {

  /**
   * A new master has come in.
   *
   * @param masterInfo
   *          the master which has been added
   */
  void onNewRosMaster(ZeroconfRosMasterInfo masterInfo);

  /**
   * A master has been unregistered.
   *
   * @param masterInfo
   *          the master which has been unregistered
   */
  void onRemoveRosMaster(ZeroconfRosMasterInfo masterInfo);
}
