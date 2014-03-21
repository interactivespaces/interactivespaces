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

package interactivespaces.workbench.project;

/**
 * An item in a project configuration.
 *
 * @author Keith M. Hughes
 */
public class ProjectConfigurationProperty {

  /**
   * The name of the configuration property.
   */
  private final String name;

  /**
   * The description of the configuration property. Can be {@code null}.
   */
  private final String description;

  /**
   * {@code true} if the property is required.
   */
  private final boolean required;

  /**
   * The value of the property. Can be {@code null}.
   */
  private final String value;

  /**
   * Construct a new property.
   *
   * @param name
   *          the name of the configuration property
   * @param description
   *          the description of the configuration property, can be {@code null}
   * @param required
   *          {@code true} if the property is required.
   * @param value
   *          the default value of the property, can be {@code null}
   */
  public ProjectConfigurationProperty(String name, String description, boolean required, String value) {
    this.name = name;
    this.description = description;
    this.required = required;
    this.value = value;
  }

  /**
   * Get the name of the property.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the description of the property.
   *
   * @return the description, can be {@code null}
   */
  public String getDescription() {
    return description;
  }

  /**
   * Is the property required?
   *
   * @return {@code true} if the property is required
   */
  public boolean isRequired() {
    return required;
  }

  /**
   * Get the value of the property.
   *
   * @return the value, can be {@code null}
   */
  public String getValue() {
    return value;
  }
}
