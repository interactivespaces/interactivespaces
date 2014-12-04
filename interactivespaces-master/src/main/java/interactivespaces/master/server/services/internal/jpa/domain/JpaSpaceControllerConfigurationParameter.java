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

package interactivespaces.master.server.services.internal.jpa.domain;

import interactivespaces.domain.basic.ConfigurationParameter;
import interactivespaces.domain.basic.SpaceControllerConfiguration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of a {@link ConfigurationParameter} for {@link SpaceControllerConfiguration}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "space_controller_config_parameter")
public class JpaSpaceControllerConfigurationParameter implements ConfigurationParameter {

  /**
   * The configuration this parameter is part of.
   */
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private JpaSpaceControllerConfiguration configuration;

  /**
   * The name of the parameter.
   */
  @Column(nullable = false, length = 512)
  private String name;

  /**
   * The value of the parameter.
   */
  @Column(nullable = true, length = 32672)
  private String value;

  /**
   * The database version. Used for detecting concurrent modifications.
   */
  @Version
  private long databaseVersion;

  /**
   * Construct a parameter without a configuration or any value.
   */
  public JpaSpaceControllerConfigurationParameter() {
  }

  /**
   * Construct a parameter with a configuration and a name/value pair.
   *
   * @param configuration
   *          the configuration this is part of
   * @param name
   *          the name of the parameter
   * @param value
   *          the value of the parameter
   */
  JpaSpaceControllerConfigurationParameter(JpaSpaceControllerConfiguration configuration, String name, String value) {
    this.configuration = configuration;
    this.name = name;
    this.value = value;
  }

  /**
   * Get the associated configuration.
   *
   * @return the configuration
   */
  public JpaSpaceControllerConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * Set the associated configuration.
   *
   * @param configuration
   *          the configuration to set
   */
  public void setConfiguration(JpaSpaceControllerConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "JpaSpaceControllerConfigurationParameter [name=" + name + ", value=" + value + "]";
  }
}
