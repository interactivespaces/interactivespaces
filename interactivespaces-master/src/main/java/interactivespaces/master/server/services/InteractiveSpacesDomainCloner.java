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

package interactivespaces.master.server.services;

import interactivespaces.domain.basic.ActivityConfiguration;
import interactivespaces.domain.basic.LiveActivity;
import interactivespaces.domain.basic.LiveActivityGroup;
import interactivespaces.domain.basic.SpaceController;
import interactivespaces.domain.space.Space;

import java.util.Map;

/**
 * A cloner of Interactive Spaces Activity objects.
 *
 * <p>
 * These classes are not guaranteed to be threadsafe, and should only be used for a single cloning operation as they are
 * not guaranteed to not contain state.
 *
 * @author Keith M. Hughes
 */
public interface InteractiveSpacesDomainCloner {

  /**
   * Set the name prefix for each created entity.
   *
   * @param namePrefix
   *          the namePrefix to set
   */
  void setNamePrefix(String namePrefix);

  /**
   * Set the map from controllers in the old space to controllers in the new space.
   *
   * @param controllerMap
   *          keys are the old controller, values are the controller to replace (can be {@code null})
   */
  void setControllerMap(Map<SpaceController, SpaceController> controllerMap);

  /**
   * Save all generated clones in their respective repositories.
   */
  void saveClones();

  /**
   * Clone a live activity.
   *
   * @param src
   *          the source live activity
   *
   * @return the cloned live activity
   */
  LiveActivity cloneLiveActivity(LiveActivity src);

  /**
   * Get the cloned live activity by the ID of the original live activity that was cloned.
   *
   * @param srcId
   *          the ID of the live activity that was cloned
   *
   * @return the cloned live activity, or {@code null} if none associated
   */
  LiveActivity getClonedLiveActivity(String srcId);

  /**
   * Clone a configuration.
   *
   * @param src
   *          the configuration to clone
   *
   * @return the cloned configuration
   */
  ActivityConfiguration cloneConfiguration(ActivityConfiguration src);

  /**
   * Clone a live activity group.
   *
   * @param src
   *          the source live activity group
   *
   * @return the cloned live activity group
   */
  LiveActivityGroup cloneLiveActivityGroup(LiveActivityGroup src);

  /**
   * Get the cloned live activity group by the ID of the original live activity group that was cloned.
   *
   * @param srcId
   *          the ID of the live activity group that was cloned
   *
   * @return the cloned live activity group, or {@code null} if none associated
   */
  LiveActivityGroup getClonedLiveActivityGroup(String srcId);

  /**
   * Clone a space.
   *
   * @param src
   *          the source space
   *
   * @return the cloned space
   */
  Space cloneSpace(Space src);

  /**
   * Get the cloned space by the ID of the original space that was cloned.
   *
   * @param srcId
   *          the ID of the space that was cloned
   *
   * @return the cloned space, or {@code null} if none associated
   */
  Space getClonedSpace(String srcId);
}
