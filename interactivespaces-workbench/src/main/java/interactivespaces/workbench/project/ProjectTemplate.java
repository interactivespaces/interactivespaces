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

import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;

import java.util.Map;

/**
 * A template for a project.
 *
 * @author Keith M. Hughes
 */
public interface ProjectTemplate {

  /**
   * Get the display name for the template.
   *
   * @return display name for the template
   */
  String getDisplayName();

  /**
   * Process the source description.
   *
   * <p>
   * This may fill in portions of the specification
   *
   * @param spec
   *          the build specification
   * @param workbench
   *          the workbench for the project
   * @param templater
   *          the templater to use
   * @param templateData
   *          the template data to use
   */
  void process(ProjectCreationSpecification spec, InteractiveSpacesWorkbench workbench,
      FreemarkerTemplater templater, Map<String, Object> templateData);
}
