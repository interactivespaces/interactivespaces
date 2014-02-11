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

package interactivespaces.domain.basic;

import interactivespaces.domain.PersistedObject;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * An activity which can be run in the space.
 *
 * <p>
 * The identifying name must be globally unique, but be vaguely readable, e.g.
 * com.google.blobtracker
 *
 * @author Keith M. Hughes
 */
public interface Activity extends PersistedObject, Serializable {

  /**
   * Get the identifying name for the activity.
   *
   * @return The identifying name
   */
  String getIdentifyingName();

  /**
   * Set the identifying name for the activity.
   *
   * @param name
   *          The identifying name
   */
  void setIdentifyingName(String name);

  /**
   * Get the version for the activity.
   *
   * @return The version
   */
  String getVersion();

  /**
   * Set the version for the activity.
   *
   * @param version
   *          The version
   */
  void setVersion(String version);

  /**
   * Get the descriptive name for the activity.
   *
   * @return The descriptive name
   */
  String getName();

  /**
   * Set the descriptive name for the activity.
   *
   * @param name
   *          The descriptive name
   */
  void setName(String name);

  /**
   * Get the description of the activity.
   *
   * @return the description. Can be {@code null}.
   */
  String getDescription();

  /**
   * Set the description of the activity.
   *
   * @param description
   *          the description. Can be {@code null}.
   */
  void setDescription(String description);

  /**
   * Get when the activity was last uploaded into the master.
   *
   * @return the date the activity was last loaded.
   */
  Date getLastUploadDate();

  /**
   * Set when the activity was last uploaded into the master.
   *
   * @param lastUploadDate
   *          the last upload date, can be {@code null}
   */
  void setLastUploadDate(Date lastUploadDate);

  /**
   * Get when the activity was last started.
   *
   * @return the date the activity was last started.
   */
  Date getLastStartDate();

  /**
   * Set when the activity was last started.
   *
   * @param lastStartDate
   *          the last start date, can be {@code null}
   */
  void setLastStartDate(Date lastStartDate);

  /**
   * Get the hash value of the activity bundle.
   *
   * @return the date the activity was last loaded.
   */
  String getBundleContentHash();

  /**
   * Set the hash value of the activity bundle.
   *
   * @param bundleContentHash
   *          the hash value for the activity bundle.
   */
  void setBundleContentHash(String bundleContentHash);

  /**
   * Set dependencies to the activity.
   *
   * @param dependencies
   *          the dependencies to set
   */
  void setDependencies(List<ActivityDependency> dependencies);

  /**
   * Get a list of all dependencies the activity has.
   *
   * @return list of activity dependencies
   */
  List<? extends ActivityDependency> getDependencies();

  /**
   * Set the metadata for the activity.
   *
   * <p>
   * This removes the old metadata completely.
   *
   * @param metadata
   *          the metadata for the activity (can be {@link null}
   */
  void setMetadata(Map<String, Object> metadata);

  /**
   * Get the metadata for the activity.
   *
   * @return the activity's meta data
   */
  Map<String, Object> getMetadata();
}
