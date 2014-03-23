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
 * A file template specification.
 *
 * @author Trevor Pering
 */
public class TemplateFile {

  public static final String GROUP_ELEMENT_NAME = "templates";

  public static final String ELEMENT_NAME = "template";

  /**
   * The input path.
   */
  private String template;

  /**
   * Output path.
   */
  private String output;

  /**
   * Create am empty template instance.
   */
  public TemplateFile() {
  }

  /**
   * Create a new file template with supplied parameters.
   *
   * @param output
   *          output path
   * @param template
   *          input path
   */
  public TemplateFile(String output, String template) {
    this.output = output;
    this.template = template;
  }

  /**
   * Set the template source for this template.
   *
   * @param template
   *          template source specification
   */
  public void setTemplate(String template) {
    this.template = template;
  }

  /**
   * @return file input template path
   */
  public String getTemplate() {
    return template;
  }

  /**
   * Set the template output specification.
   *
   * @param output
   *          template output
   */
  public void setOutput(String output) {
    this.output = output;
  }

  /**
   * @return output path
   */
  public String getOutput() {
    return output;
  }
}
