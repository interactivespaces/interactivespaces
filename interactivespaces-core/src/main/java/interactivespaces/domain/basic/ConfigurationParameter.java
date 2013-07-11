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

/**
 * A configuration parameter.
 *
 * <p>
 * Equality and hashing should only depend on the name of the parameter.
 *
 * @author Keith M. Hughes
 */
public interface ConfigurationParameter {

  /**
   * Get the name of the configuration parameter.
   *
   * @return
   */
  String getName();

  /**
   * Get the name of the configuration parameter.
   *
   * @param name
   *          the name of the configuration parameter
   */
  void setName(String name);

  /**
   * Get the value of the configuration parameter.
   *
   * @return the value of the configuration parameter
   */
  String getValue();

  /**
   * Set the value of the configuration parameter.
   *
   * @param value
   *          the value of the configuration parameter
   */
  void setValue(String value);
}
