/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime;

import interactivespaces.activity.ActivityStatus;

/**
 * A publisher for live activity statuses.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityStatusPublisher {

  /**
   * Publish a live activity status in a safe manner.
   *
   * @param uuid
   *          uuid of the live activity
   * @param status
   *          the status
   */
  void publishActivityStatus(String uuid, ActivityStatus status);
}
