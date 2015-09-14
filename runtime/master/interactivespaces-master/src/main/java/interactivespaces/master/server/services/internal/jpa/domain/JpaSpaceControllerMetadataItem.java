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
 * JPA implementation of a metadata item for a {@link JpaSpaceController}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "space_controller_metadata")
public class JpaSpaceControllerMetadataItem {

  /**
   * The space controller the metadata item is for.
   */
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private JpaSpaceController spaceController;

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
  public JpaSpaceControllerMetadataItem() {
  }

  /**
   * Construct a new metadata item.
   *
   * @param spaceController
   *          the space controller the metadata item is for
   * @param name
   *          the name of the metadata item
   * @param value
   *          the value of the metadata item
   */
  JpaSpaceControllerMetadataItem(JpaSpaceController spaceController, String name, String value) {
    this.spaceController = spaceController;
    this.name = name;
    this.value = value;
  }

  /**
   * Get the space controller the metadata item is for.
   *
   * @return the space controller
   */
  public JpaSpaceController getSpaceController() {
    return spaceController;
  }

  /**
   * Set the space controller the metadata item is for.
   *
   * @param spaceController
   *          the space controller
   */
  public void setSpaceController(JpaSpaceController spaceController) {
    this.spaceController = spaceController;
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
