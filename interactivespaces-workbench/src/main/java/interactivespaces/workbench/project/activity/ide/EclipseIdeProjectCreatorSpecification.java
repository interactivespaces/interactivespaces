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
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.Project;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

/**
 * Specification data for project creation.
 *
 * @author Keith M. Hughes
 */
public interface EclipseIdeProjectCreatorSpecification {

  /**
   * The Eclipse project file field name for project builders.
   */
  public static final String ECLIPSE_PROJECT_FIELD_BUILDER = "builder";

  /**
   * The Eclipse project file field name for project natures.
   */
  public static final String ECLIPSE_PROJECT_FIELD_NATURES = "natures";

  /**
   * Add further IDE generation context needed for the activity project.
   *
   * @param project
   *          the project
   * @param freemarkerContext
   *          the context containing freemarker data
   */
  void addSpecificationData(Project project, Map<String, Object> freemarkerContext);

  /**
   * Write any additional files.
   *
   * @param project
   *          the project being built
   * @param freemarkerContext
   *          the context of objects to be given to freemarker
   * @param templater
   *          the freemarker templater
   *
   * @throws IOException
   * @throws TemplateException
   */
  void writeAdditionalFiles(Project project, Map<String, Object> freemarkerContext,
      FreemarkerTemplater templater, InteractiveSpacesWorkbench workbench) throws IOException,
      TemplateException;
}
