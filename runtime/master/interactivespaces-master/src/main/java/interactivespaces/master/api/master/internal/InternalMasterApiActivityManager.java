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

package interactivespaces.master.api.master.internal;

import interactivespaces.domain.basic.SpaceController;
import interactivespaces.master.api.master.MasterApiActivityManager;

import java.util.List;
import java.util.Map;

/**
 * A {@link MasterApiActivityManager} which provides some additional internal functionality.
 *
 * @author Keith M. Hughes
 */
public interface InternalMasterApiActivityManager extends MasterApiActivityManager {

  /**
   * Get a list of all live activities and, if any, the associated active
   * counterpart, which are on the specified controller.
   *
   * <p>
   * The latter won't be there if the live activity isn't associated with a
   * controller.
   *
   * @param controller
   *          the controller which contains the activities
   *
   * @return all UI live activities for the controller
   */
  List<Map<String, Object>> getAllUiLiveActivitiesByController(SpaceController controller);
}
