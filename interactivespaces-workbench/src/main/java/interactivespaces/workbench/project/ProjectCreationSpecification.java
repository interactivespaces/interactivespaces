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

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * A specification for creating a project.
 *
 * @author Keith M. Hughes
 */
public class ProjectCreationSpecification extends CreationSpecification {

  /**
   * The language for the project.
   */
  private String language;

  /**
   * The project itself.
   */
  private Project project;

  /**
   * Get the programming language for the project.
   *
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Set the programming language for the project.
   *
   * @param language
   *          the language to set
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Get the project description associated with the spec.
   *
   * @param <T>
   *          the project type
   *
   * @return the project
   */
  @SuppressWarnings("unchecked")
  public <T extends Project> T getProject() {
    return (T) project;
  }

  /**
   * Set the project description for the spec.
   *
   * @param project
   *          the project to set
   */
  public void setProject(Project project) {
    this.project = project;
  }

}
