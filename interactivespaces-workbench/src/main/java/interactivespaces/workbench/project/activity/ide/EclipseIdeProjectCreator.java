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

package interactivespaces.workbench.project.activity.ide;

import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

import com.google.common.collect.Maps;

import java.io.File;
import java.util.Map;

/**
 * Create an eclipse project.
 *
 * @author Keith M. Hughes
 */
public class EclipseIdeProjectCreator {

  /**
   * Where the template for the Eclipse project file is.
   */
  private static final String TEMPLATE_FILEPATH_ECLIPSE_PROJECT = "ide/eclipse/project.ftl";

  /**
   * What the Eclpse project file will be called.
   */
  private static final String FILENAME_PROJECT_FILE = ".project";

  /**
   * The Templater.
   */
  private final FreemarkerTemplater templater;

  /**
   * Construct a new project creator.
   *
   * @param templater
   *          the templater to use
   */
  public EclipseIdeProjectCreator(FreemarkerTemplater templater) {
    this.templater = templater;
  }

  /**
   * Create the IDE project.
   *
   * @param project
   *          project creating the IDE version for param spec the specification
   *          giving details about the IDE build
   * @param context
   *          the build context
   * @param spec
   *          the specification for the IDE project
   *
   * @return {@code true} if successful
   */
  public boolean createProject(Project project, ProjectBuildContext context,
      EclipseIdeProjectCreatorSpecification spec) {
    try {
      // Create the freemarkerContext hash
      Map<String, Object> freemarkerContext = Maps.newHashMap();
      freemarkerContext.put("project", project);

      spec.addSpecificationData(project, context, freemarkerContext);

      writeProjectFile(project, freemarkerContext);

      spec.writeAdditionalFiles(project, context, freemarkerContext, templater);

      return true;
    } catch (Exception e) {
      context.getWorkbench().handleError("Error while creating eclipse project", e);

      return false;
    }
  }

  /**
   * Write the project file.
   *
   * @param freemarkerConfig
   *          the Freemarker configuration
   * @param freemarkerContext
   *          the Freemarker context
   *
   * @throws Exception
   *           something bad happened
   */
  private void writeProjectFile(Project project, Map<String, Object> freemarkerContext)
      throws Exception {
    templater.writeTemplate(freemarkerContext, new File(project.getBaseDirectory(),
        FILENAME_PROJECT_FILE), TEMPLATE_FILEPATH_ECLIPSE_PROJECT);
  }
}
