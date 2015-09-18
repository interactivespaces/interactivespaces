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

package interactivespaces.interaction.model.entity;

import java.util.List;

/**
 * A listener for tracked entity events.
 *
 * @param <T>
 *          the location type for the tracked entities
 *
 * @author Keith M. Hughes
 */
public interface TrackedEntityListener<T> {

  /**
   * Handle an update of the state of a collection of tracked entities.
   *
   * @param entities
   *          the entities
   */
  void onTrackedEntityUpdate(List<TrackedEntity<T>> entities);
}
