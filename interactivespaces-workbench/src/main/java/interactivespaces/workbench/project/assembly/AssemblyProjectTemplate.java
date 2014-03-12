/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.workbench.project.assembly;

import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.BaseProjectTemplate;
import interactivespaces.workbench.project.ProjectCreationSpecification;

import java.util.Map;

/**
 * A basic template for assemblies.
 *
 * @author Trevor Pering
 */
public class AssemblyProjectTemplate extends BaseProjectTemplate {

  /**
   * The activity project type.
   */
  public static final String PROJECT_TYPE = "assembly";

  /**
   * Create a basic resource project template.
   */
  public AssemblyProjectTemplate() {
    super("Assembly Project Template");
  }

  @Override
  public void onTemplateSetup(ProjectCreationSpecification spec,
      FreemarkerTemplater templater, Map<String, Object> fullTemplateData) {
  }

  @Override
  public void writeSpecificTemplates(ProjectCreationSpecification spec,
      InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
  }

}
