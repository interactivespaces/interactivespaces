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
public class TemplateFile {

  /**
   * The input path.
   */
  private String template;

  /**
   * Output path.
   */
  private String output;

  public TemplateFile() {
  }

    /**
     * Create a new file template.
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
   * @return file input template path
   */
  public String getTemplate() {
    return template;
  }


  /**
   * @return output path
   */
  public String getOutput() {
    return output;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public void setOutput(String output) {
    this.output = output;
  }
}
