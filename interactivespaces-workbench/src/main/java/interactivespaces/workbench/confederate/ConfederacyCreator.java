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

package interactivespaces.workbench.confederate;

import com.google.common.collect.Maps;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.BaseTemplate;
import interactivespaces.workbench.project.CreationSpecification;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectCreationSpecification;
import interactivespaces.workbench.project.TemplateFile;
import interactivespaces.workbench.project.activity.creator.ProjectCreator;
import interactivespaces.workbench.project.activity.creator.ProjectCreatorImpl;

import java.io.File;
import java.util.Map;

/**
 * A {@link interactivespaces.workbench.project.activity.creator.ProjectCreator} implementation.
 *
 * @author Keith M. Hughes
 */
public class ConfederacyCreator {

  /**
   * Templater to use.
   */
  private final FreemarkerTemplater templater;

  /**
   * The workbench used by the creator.
   */
  private final InteractiveSpacesWorkbench workbench;

  private final ProjectCreator projectCreator;

  /**
   * Create a basic instance.
   *
   * @param workbench
   *          containing workbench
   *
   */
  public ConfederacyCreator(InteractiveSpacesWorkbench workbench) {
    this.workbench = workbench;
    templater = new FreemarkerTemplater();
    templater.startup();
    templater.addEvaluationPass();
    projectCreator = new ProjectCreatorImpl(workbench, templater);
  }

  public void create(Confederacy spec) {
    try {
      for (Project project : spec.getProjectList()) {
        createProject(project, spec);
      }
      writeConfederacyTemplates(spec);
    } catch (Exception e) {
      workbench.handleError("Error while creating confederacy", e);
    }
  }

  private void writeConfederacyTemplates(Confederacy spec) {

    CreationSpecification creator = new CreationSpecification();
    creator.addAllTemplateVars(spec.getTemplateVars());
    creator.setBaseDirectory(spec.getBaseDirectory());
    creator.setSpecification(spec.getSpecificationSource());

    BaseTemplate template = new BaseTemplate("Confederacy Writer");
    Map<String, Object> templateData = Maps.newHashMap();
    templateData.put("baseDirectory", spec.getBaseDirectory());
    template.addAllFileTemplate(spec.getTemplateFiles());
    template.process(creator, workbench, templater, templateData);
  }

  private void createProject(Project project, Confederacy confederacy) {
    ProjectCreationSpecification spec = new ProjectCreationSpecification();
    spec.setProject(project);
    spec.setLanguage(project.getBuilderType());
    spec.addAllTemplateVars(confederacy.getTemplateVars());
    spec.setBaseDirectory(confederacy.getBaseDirectory());
    spec.setSpecification(confederacy.getSpecificationSource());
    projectCreator.createProject(spec);
  }
}
