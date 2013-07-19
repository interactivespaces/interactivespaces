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

package interactivespaces.domain.space;

import interactivespaces.domain.PersistedObject;
import interactivespaces.domain.basic.LiveActivityGroup;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A space.
 *
 * <p>
 * "Space" is a dynamic concept. The actual physical space will often be static,
 * but these spaces are meant to be more about describing how the physical space
 * is being used.
 *
 * <p>
 * Each space can have multiple {@link LiveActivityGroup} instances, even if the
 * children have their own activity groups.
 *
 * @author Keith M. Hughes
 */
public interface Space extends PersistedObject, Serializable {

  /**
   * Get the name of this space.
   *
   * @return the name
   */
  String getName();

  /**
   * Set the name of the space.
   *
   * @param name
   *          The new name for the space.
   */
  void setName(String name);

  /**
   * Get the current description for the space.
   *
   * @return The current description.
   */
  String getDescription();

  /**
   * Set the description for the space.
   *
   * @param description
   *          the new description
   */
  void setDescription(String description);

  /**
   * Set the metadata for the space.
   *
   * <p>
   * This removes the old metadata completely.
   *
   * @param metadata
   *          the metadata for the space (can by {@link null}
   */
  void setMetadata(Map<String, Object> metadata);

  /**
   * Get the metadata for the space.
   *
   * @return the space's meta data
   */
  Map<String, Object> getMetadata();

  /**
   * Get all children from the space.
   *
   * @return a freshly allocated list of the current children
   */
  List<? extends Space> getSpaces();

  /**
   * Add in a new child of this space.
   *
   * <p>
   * The parent of the added space will changed to this space.
   *
   * @param space
   *          the space to add
   *
   * @return this space
   */
  Space addSpace(Space space);

  /**
   * Add in a series of new children to this space.
   *
   * <p>
   * The parent of the added spaces will changed to this space.
   *
   * @param spaces
   *          the new spaces
   *
   * @return this space
   */
  Space addSpaces(Space... spaces);

  /**
   * Remove a space from the space.
   *
   * <p>
   * This does nothing if the space isn't part of the space already.
   *
   * @param space
   *          the space to remove
   */
  void removeSpace(Space space);

  /**
   * Clear all spaces from the space.
   */
  void clearSpaces();

  /**
   * Get the activity groups of this space.
   *
   * @return The activity groups of this space.
   */
  List<? extends LiveActivityGroup> getActivityGroups();

  /**
   * Add an activity group.
   *
   * @param activityGroup
   *          the activity group to add
   *
   * @return this space
   */
  Space addActivityGroup(LiveActivityGroup activityGroup);

  /**
   * Add a set of activity groups.
   *
   * @param activityGroups
   *          the activity groups to add
   *
   * @return this space
   */
  Space addActivityGroups(LiveActivityGroup... activityGroup);

  /**
   * Remove a activity group from the space.
   *
   * <p>
   * This does nothing if the activity group isn't part of the space already.
   *
   * @param activityGroup
   *          the activity group to remove
   */
  void removeActivityGroup(LiveActivityGroup activityGroup);

  /**
   * Clear all activity groups from the space.
   */
  void clearActivityGroups();
}
