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
 * A simple implementation of a tracked entity.
 *
 * @param <T>
 *          the location type
 *
 * @author Keith M. Hughes
 */
public class SimpleTrackedEntity<T> extends SimplePositionableEntity<T> implements TrackedEntity<T> {

  /**
   * {@code true} if the entity is appearing for the first time.
   */
  private final boolean stateNew;

  /**
   * {@code true} if the entity is currently visible.
   */
  private final boolean stateVisible;

  /**
   * {@code true} if the entity has been lost.
   */
  private final boolean stateLost;

  /**
   * Construct a new tracked entity.
   *
   * @param id
   *          ID of the entity
   * @param location
   *          location of the entity
   * @param stateNew
   *          {@code true} if the first time the entity is seen
   * @param stateVisible
   *          {@code true} if the entity is visible
   * @param stateLost
   *          {@code true} if the entity has been lost
   */
  public SimpleTrackedEntity(String id, T location, boolean stateNew, boolean stateVisible, boolean stateLost) {
    super(id, location);
    this.stateNew = stateNew;
    this.stateVisible = stateVisible;
    this.stateLost = stateLost;
  }

  @Override
  public boolean isNew() {
    return stateNew;
  }

  @Override
  public boolean isVisible() {
    return stateVisible;
  }

  @Override
  public boolean isLost() {
    return stateLost;
  }

}
