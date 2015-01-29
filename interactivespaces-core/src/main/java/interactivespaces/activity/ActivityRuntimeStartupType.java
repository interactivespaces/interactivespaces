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

package interactivespaces.activity;

/**
 * How the activity should respond to activity startup.
 *
 * @author Keith M. Hughes
 */
public enum ActivityRuntimeStartupType {

  /**
   * The activity should only be ready to run at runtime startup.
   */
  READY,

  /**
   * The activity should transition to startup at runtime startup.
   */
  STARTUP,

  /**
   * The activity should transition to activate at runtime startup.
   */
  ACTIVATE,

  /**
   * The activity should transition to its last known state at runtime startup.
   */
  LAST_KNOWN
}
