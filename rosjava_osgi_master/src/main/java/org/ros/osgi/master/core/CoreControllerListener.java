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

package org.ros.osgi.master.core;

/**
 * A listener for events on the {@link CoreController}.
 *
 * @author Keith M. Hughes
 */
public interface CoreControllerListener {

  /**
   * The master has started up.
   *
   */
  void onCoreStartup();

  /**
   * The master has shut down.
   */
  void onCoreShutdown();
}
