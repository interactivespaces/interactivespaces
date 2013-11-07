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

package interactivespaces.workbench.project.activity.creator;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.ProjectTemplate;
import interactivespaces.workbench.project.activity.type.android.GenericAndroidActivityProjectTemplate;
import interactivespaces.workbench.project.activity.type.java.GenericJavaActivityProjectTemplate;
import interactivespaces.workbench.project.assembly.AssemblyProjectTemplate;
import interactivespaces.workbench.project.library.LibraryProjectTemplate;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link ProjectCreator} implementation.
 *
 * @author Keith M. Hughes
 */
public class ProjectCreatorImpl implements ProjectCreator {

  /**
   * The list of activities to be handed to clients.
   */
  private final List<ProjectTemplate> activityProjectTemplates;

  /**
   * The list of activities to be used internally.
   */
  private final List<ProjectTemplate> activityProjectTemplatesInternal;

  /**
   * Templater to use
   */
  private final FreemarkerTemplater templater;

  /**
   * The workbench used by the creator.
   */
  private final InteractiveSpacesWorkbench workbench;

  /**
   * Create a basic instance.
   *
   * @param workbench containing workbench
   * @param templater templater to use
   */
  public ProjectCreatorImpl(InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater) {
    this.workbench = workbench;
    this.templater = templater;

    activityProjectTemplatesInternal = Lists.newArrayList();
    activityProjectTemplates = Collections.unmodifiableList(activityProjectTemplatesInternal);

    activityProjectTemplatesInternal.add(new GenericJavaActivityProjectTemplate());
    activityProjectTemplatesInternal.add(new GenericJavascriptActivityProjectTemplate());
    activityProjectTemplatesInternal.add(new GenericPythonActivityProjectTemplate());
    activityProjectTemplatesInternal.add(new GenericAndroidActivityProjectTemplate());
  }

  @Override
  public List<ProjectTemplate> getActivityProjectTemplates() {
    return activityProjectTemplates;
  }

  @Override
  public void createProject(ProjectCreationSpecification spec) {
    try {
      // Create the templateData hash
      Map<String, Object> templateData = new HashMap<String, Object>();
      templateData.put("spec", spec);
      templateData.put("project", spec.getProject());

      writeProjectTemplate(spec, templateData);

    } catch (Exception e) {
      workbench.logError("Error while creating project", e);
    }
  }

  /**
   * Write out the code template.
   *
   * @param spec
   *          the build specification
   * @param templateData
   *          data to go into the template
   */
  private void writeProjectTemplate(ProjectCreationSpecification spec,
      Map<String, Object> templateData) {
    ProjectTemplate template = spec.getTemplate();
    if (template == null) {
      String projectType = spec.getProject().getType();

      // TODO(khughes): Put all project types in constants somewhere.
      if ("activity".equals(projectType)) {
        template = getActivityProjectTemplateByLanguage(spec.getLanguage());
      } else if ("library".equals(projectType)) {
        template = new LibraryProjectTemplate();
      } else if ("resource".equals(projectType)) {
        template = new AssemblyProjectTemplate();
      }
    }

    writeProjectTemplate(template, spec, templateData);
  }

  /**
   * Get a generic project template by language.
   *
   * @param language
   *          the language
   *
   * @return the generic template for that language
   */
  private ProjectTemplate getActivityProjectTemplateByLanguage(String language) {
    if ("java".equals(language)) {
      return new GenericJavaActivityProjectTemplate();
    } else if ("python".equals(language)) {
      return new GenericPythonActivityProjectTemplate();
    } else if ("javascript".equals(language)) {
      return new GenericJavascriptActivityProjectTemplate();
    } else if ("android".equals(language)) {
      return new GenericAndroidActivityProjectTemplate();
    } else {
      throw new InteractiveSpacesException(String.format("Unknown language %s", language));
    }
  }

  /**
   * Write out the code template.
   *
   * @param spec
   *          the build specification
   * @param templateData
   *          data to go into the template
   */
  private void writeProjectTemplate(ProjectTemplate sourceDescription,
      ProjectCreationSpecification spec, Map<String, Object> templateData) {
    sourceDescription.process(spec, workbench, templater, templateData);
  }
}
