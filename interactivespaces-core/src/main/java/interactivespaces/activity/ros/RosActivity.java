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

package interactivespaces.activity.ros;

import interactivespaces.activity.Activity;

import org.ros.node.ConnectedNode;
import org.ros.osgi.common.RosEnvironment;

/**
 * An {@link Activity} which uses ROS.
 *
 * @author Keith M. Hughes
 */
public interface RosActivity extends Activity {

  /**
   * Get the current ROS Environment being used by this activity.
   *
   * @return Can be null.
   */
  RosEnvironment getRosEnvironment();

  /**
   * Get the main ROS node for this activity.
   *
   * @return
   */
  ConnectedNode getMainNode();
}
