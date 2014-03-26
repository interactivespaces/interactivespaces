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

package interactivespaces.workbench.project;

/**
 * A simple input/output pair specification.
 *
 * @author Trevor Pering
 */
public class TemplateVar {

  public static final String GROUP_ELEMENT_NAME = "templateVars";

  public static final String ELEMENT_NAME = "var";

  public static final String NAME_KEY = "name";

  public static final String VALUE_KEY = "value";

  /**
   * The variable name.
   */
  private String name;

  /**
   * The variable value.
   */
  private String value;

  /**
   * Create a new template variable entry.
   */
  public TemplateVar() {
  }

  /**
   * Create a new template variable entry.
   *
   * @param name
   *          var name
   * @param value
   *          var value
   */
  public TemplateVar(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Set the name part of this variable.
   *
   * @param name
   *          variable name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return output path
   */
  public String getName() {
    return name;
  }

  /**
   * Set the value part of this variable.
   *
   * @param value
   *          variable value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return file input template path
   */
  public String getValue() {
    return value;
  }
}
