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

package interactivespaces.domain.basic.pojo;

import interactivespaces.domain.basic.ConfigurationParameter;

/**
 * A POJO implementation of a {@link ConfigurationParameter}.
 *
 * @author Keith M. Hughes
 */
public class SimpleConfigurationParameter implements ConfigurationParameter {

  /**
   * The name of the parameter.
   */
  private String name;

  /**
   * The value of the parameter.
   */
  private String value;

  /**
   * Construct a parameter.
   *
   * <p>
   * All fields will start as {@code null}.
   */
  public SimpleConfigurationParameter() {
  }

  /**
   * Construct a parameter.
   *
   * @param name
   *          the name of the parameter
   * @param value
   *          the value of the parameter
   */
  public SimpleConfigurationParameter(String name, String value) {
    this.name = name;
    this.value = value;
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
}
