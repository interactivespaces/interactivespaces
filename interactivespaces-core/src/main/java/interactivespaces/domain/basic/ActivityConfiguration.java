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

import java.util.Map;
import java.util.Set;

/**
 * A collection of {@link ConfigurationParameter} instances for either an {@link Activity} or an {@link LiveActivity}.
 *
 * @author Keith M. Hughes
 */
public interface ActivityConfiguration {

  /**
   * Get the name of the configuration.
   *
   * @return the name of the configuration
   */
  String getName();

  /**
   * Set the name of the configuration.
   *
   * @param name
   *          the name of the configuration
   */
  void setName(String name);

  /**
   * Get the description of the configuration.
   *
   * @return the description of the configuration
   */
  String getDescription();

  /**
   * Set the description of the configuration.
   *
   * @param description
   *          the description of the configuration
   */
  void setDescription(String description);

  /**
   * Add a new parameter to the configuration.
   *
   * @param parameter
   *          the parameter to add
   */
  void addParameter(ConfigurationParameter parameter);

  /**
   * Remove a parameter from the configuration.
   *
   * <p>
   * Does nothing if the parameter wasn't there.
   *
   * @param parameter
   *          the parameter to remove
   */
  void removeParameter(ConfigurationParameter parameter);

  /**
   * Get a set of all parameters in the collection.
   *
   * @return a newly created set of the parameters
   */
  Set<ConfigurationParameter> getParameters();

  /**
   * Get the configuration parameters as a map.
   *
   * @return a copy of the parameters as of the time of the call.
   */
  Map<String, ConfigurationParameter> getParameterMap();
}
