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
 * A very simple positional entity.
 *
 * @param <T>
 *          the location type
 *
 * @author Keith M. Hughes
 */
public class SimplePositionableEntity<T> implements PositionableEntity<T> {

  /**
   * ID of the entity.
   */
  private final String id;

  /**
   * Position of the entity.
   */
  private final T position;

  /**
   * Construct a new entity.
   *
   * @param id
   *          ID of the entity
   * @param position
   *          location of the entity
   */
  public SimplePositionableEntity(String id, T position) {
    this.id = id;
    this.position = position;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public T getPosition() {
    return position;
  }
}
