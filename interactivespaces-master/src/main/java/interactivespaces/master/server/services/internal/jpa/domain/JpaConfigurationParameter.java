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

import interactivespaces.domain.basic.ConfigurationParameter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of a {@link ConfigurationParameter}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "config_parameter")
public class JpaConfigurationParameter implements ConfigurationParameter {

  /**
   * The configuration this parameter is part of.
   */
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  private JpaActivityConfiguration configuration;

  /**
   * The name of the parameter.
   */
  private String name;

  /**
   * The value of the parameter.
   */
  private String value;

  /**
   * The database version. Used for detecting concurrent modifications.
   */
  @Version
  private long databaseVersion;

  public JpaConfigurationParameter() {
  }

  JpaConfigurationParameter(JpaActivityConfiguration configuration, String name, String value) {
    this.configuration = configuration;
    this.name = name;
    this.value = value;
  }

  /**
   * Get the associated configuration.
   *
   * @return the configuration
   */
  public JpaActivityConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * Set the asssociated configuration.
   *
   * @param configuration
   *          the configuration to set
   */
  public void setConfiguration(JpaActivityConfiguration configuration) {
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
    return "JpaConfigurationParameter [name=" + name + ", value=" + value + "]";
  }
}
