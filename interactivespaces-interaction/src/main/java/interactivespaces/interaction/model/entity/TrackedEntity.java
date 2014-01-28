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

package interactivespaces.interaction.model.entity;

/**
 * A positional entity which is being tracked.
 *
 * @param <T>
 *          the location type
 *
 * @author Keith M. Hughes
 */
public interface TrackedEntity<T> extends PositionableEntity<T> {

  /**
   * Is the entity new?
   *
   * @return {@code true} if the entity is new
   */
  boolean isNew();

  /**
   * Is the entity visible?
   *
   * @return {@code true} if the entity is visible
   */
  boolean isVisible();

  /**
   * Is the entity lost, that is no longer being tracked?
   *
   * @return {@code true} if the entity is lost
   */
  boolean isLost();
}
