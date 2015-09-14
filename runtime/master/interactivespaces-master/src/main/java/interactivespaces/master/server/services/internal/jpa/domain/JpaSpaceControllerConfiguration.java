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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * A JPA implementation of a {@link SpaceControllerConfiguration}.
 *
 * @author Keith M. Hughes
 */
@Entity
@Table(name = "space_controller_configurations")
public class JpaSpaceControllerConfiguration implements SpaceControllerConfiguration {

  /**
   * The persistence ID for the activity.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, length = 64)
  private String id;

  /**
   * The name of the configuration.
   */
  @Column(nullable = true, length = 512)
  private String name;

  /**
   * The description of the configuration.
   */
  @Column(nullable = true, length = 2048)
  private String description;

  /**
   * The parameters in the configuration.
   */
  @OneToMany(targetEntity = JpaSpaceControllerConfigurationParameter.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER,
      orphanRemoval = true)
  private Set<JpaSpaceControllerConfigurationParameter> parameters = Sets.newHashSet();

  /**
   * The database version. Used for detecting concurrent modifications.
   */
  @Version
  private long databaseVersion;

  /**
   * Get the ID of this configuration.
   *
   * @return the ID of this configuration
   */
  // TODO(keith): Move into interface once the migration is over.
  public String getId() {
    return id;
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
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void addParameter(ConfigurationParameter parameter) {
    JpaSpaceControllerConfigurationParameter p = (JpaSpaceControllerConfigurationParameter) parameter;
    p.setConfiguration(this);

    parameters.add(p);
  }

  @Override
  public void removeParameter(ConfigurationParameter parameter) {
    parameters.remove(parameter);
  }

  @Override
  public Set<ConfigurationParameter> getParameters() {
    Set<ConfigurationParameter> result = Sets.newHashSet();

    result.addAll(parameters);

    return result;
  }

  @Override
  public Map<String, ConfigurationParameter> getParameterMap() {
    Map<String, ConfigurationParameter> map = Maps.newHashMap();

    for (ConfigurationParameter parameter : parameters) {
      map.put(parameter.getName(), parameter);
    }

    return map;
  }

  @Override
  public String toString() {
    return "JpaSpaceControllerConfiguration [id=" + id + ", name=" + name + ", description=" + description
        + ", parameters=" + parameters + "]";
  }

}
