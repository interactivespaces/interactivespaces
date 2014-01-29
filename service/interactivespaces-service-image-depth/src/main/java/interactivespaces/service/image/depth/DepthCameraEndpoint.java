/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.service.image.depth;

import interactivespaces.interaction.model.entity.TrackedEntityListener;
import interactivespaces.util.geometry.Vector3;
import interactivespaces.util.resource.ManagedResource;

/**
 * @author Keith M. Hughes
 */
public interface DepthCameraEndpoint extends ManagedResource {

  /**
   * Add a new tracked entity listener to the endpoint.
   *
   * @param listener
   *          the tracked entity listener to add
   */
  void addTrackedEntityListener(TrackedEntityListener<Vector3> listener);

  /**
   * Remove a tracked entity listener from the endpoint.
   *
   * <p>
   * Does nothing if the listener was never added.
   *
   * @param listener
   *          the tracked entity listener to remove
   */
  void removeTrackedEntityListener(TrackedEntityListener<Vector3> listener);
}
