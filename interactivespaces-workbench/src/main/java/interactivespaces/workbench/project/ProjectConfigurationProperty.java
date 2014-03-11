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

import interactivespaces.SimpleInteractiveSpacesException;

/**
 * An item in a project configuration.
 *
 * @author Keith M. Hughes
 */
public class ProjectConfigurationProperty {

  /**
   * Maximum number of string parts for a string specification.
   */
  public static final int MAX_INPUT_PARTS = 3;

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
   * @param value
   *          the default value of the property, can be {@code null}
   * @param required
   *          {@code true} if the property is required.
   * @param description
   *          the description of the configuration property, can be {@code null}
   */
  public ProjectConfigurationProperty(String name, String value, boolean required, String description) {
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

  /**
   * Construct a new instance given a specification input string.
   *
   * @param input
   *          specification string
   *
   * @return new property instance
   */
  public static ProjectConfigurationProperty fromString(String input) {
    String[] parts = input.split(",");
    if (parts.length > MAX_INPUT_PARTS) {
      throw new SimpleInteractiveSpacesException("Excess parts found for input string: " + input);
    }
    String name = parts[0];
    String value = parts.length > 1 ? parts[1] : "";
    boolean required = !value.isEmpty();
    String description = parts.length > 2 ? parts[2] : null;
    return new ProjectConfigurationProperty(name, value, required, description);
  }
}
