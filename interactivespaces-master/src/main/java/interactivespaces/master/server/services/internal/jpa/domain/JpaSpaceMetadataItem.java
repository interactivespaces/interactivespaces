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
 * JPO implementation of a metadata item for a {@link JpaSpace}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "space_metadata")
public class JpaSpaceMetadataItem {

  /**
   * The space the metadata item is for.
   */
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private JpaSpace space;

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
  public JpaSpaceMetadataItem() {
  }

  /**
   * Construct a new metadata item.
   *
   * @param space
   *          the space the metadata is for
   * @param name
   *          the name of the metadata item
   * @param value
   *          the value of the metadata item
   */
  JpaSpaceMetadataItem(JpaSpace space, String name, String value) {
    this.space = space;
    this.name = name;
    this.value = value;
  }

  /**
   * Get the space for this metadata item.
   *
   * @return the space
   */
  public JpaSpace getSpace() {
    return space;
  }

  /**
   * Set the space for this metadata item.
   *
   * @param space
   *          the space
   */
  public void setSpace(JpaSpace space) {
    this.space = space;
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
