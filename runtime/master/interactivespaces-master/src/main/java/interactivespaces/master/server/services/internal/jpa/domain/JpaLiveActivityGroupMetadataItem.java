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

package interactivespaces.master.server.services.internal.jpa.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * JPA implementation of a metadata item for a {@link JpaLiveActivityGroup}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "live_activity_group_metadata")
public class JpaLiveActivityGroupMetadataItem {

  /**
   * The live activity group the metadata item is for.
   */
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private JpaLiveActivityGroup liveActivityGroup;

  /**
   * Name of the metadata item.
   */
  @Column(nullable = false, length = 512)
  private String name;

  /**
   * Value of the metadata item.
   */
  @Column(nullable = false, length = 32672)
  private String value;

  /**
   * Construct a new metadata item.
   */
  public JpaLiveActivityGroupMetadataItem() {
  }

  /**
   * Construct a new metadata item.
   *
   * @param liveActivityGroup
   *          the live activity group the metadata item is for
   * @param name
   *          the name of the metadata item
   * @param value
   *          the value of the metadata item
   */
  JpaLiveActivityGroupMetadataItem(JpaLiveActivityGroup liveActivityGroup, String name, String value) {
    this.liveActivityGroup = liveActivityGroup;
    this.name = name;
    this.value = value;
  }

  /**
   * Get the live activity group the metadata item is for.
   *
   * @return the live activity group
   */
  public JpaLiveActivityGroup getLiveActivityGroup() {
    return liveActivityGroup;
  }

  /**
   * Set the live activity group the metadata item is for.
   *
   * @param liveActivityGroup
   *          the live activity group
   */
  public void setLiveActivityGroup(JpaLiveActivityGroup liveActivityGroup) {
    this.liveActivityGroup = liveActivityGroup;
  }

  /**
   * Get the name of the metadata item.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the metadata item.
   *
   * @param name
   *          the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the value of the metadata item.
   *
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Set the value of the metadata item.
   *
   * @param value
   *          the value
   */
  public void setValue(String value) {
    this.value = value;
  }
}
