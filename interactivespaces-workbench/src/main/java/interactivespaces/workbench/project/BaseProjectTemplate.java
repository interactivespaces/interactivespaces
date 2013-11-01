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

package interactivespaces.workbench.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Base template for any kind of project.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseProjectTemplate implements ProjectTemplate {

  /**
   * The display name for the template.
   */
  private String displayName;

  /**
   * List of all source directories needed.
   */
  private List<String> sourceDirectories = Lists.newArrayList();

  public BaseProjectTemplate(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return displayName;
  }

  /**
   * Add a source directory to the project.
   *
   * @param directory
   *          the directory to add
   */
  public void addSourceDirectory(String directory) {
    sourceDirectories.add(directory);
  }

  public List<String> getSourceDirectories() {
    return sourceDirectories;
  }

  @Override
  public void process(ProjectCreationSpecification spec, InteractiveSpacesWorkbench workbench,
      FreemarkerTemplater templater, Map<String, Object> templateData) {
    Project project = spec.getProject();

    Map<String, Object> fullTemplateData = Maps.newHashMap(templateData);

    onTemplateSetup(spec, fullTemplateData);

    createProjectStructure(project);

    writeSpecificTemplates(spec, workbench, templater, fullTemplateData);
    writeCommonTemplates(spec, workbench, templater, fullTemplateData);
    writeProjectXml(templater, spec, fullTemplateData);
  }

  /**
   * Create all common directory structures for the project.
   *
   * @param project
   *          the project being created.
   */
  private void createProjectStructure(Project project) {
    File baseDirectory = project.getBaseDirectory();
    makeDirectory(baseDirectory);

    for (String srcDir : getSourceDirectories()) {
      makeDirectory(new File(baseDirectory, srcDir));
    }
  }

  /**
   * Make a directory, including all needed parent directories.
   *
   * @param directory
   *          the directory to create
   *
   * @throws InteractiveSpacesException
   *           could not create directory
   */
  public void makeDirectory(File directory) throws InteractiveSpacesException {
    if (!directory.mkdirs()) {
      throw new InteractiveSpacesException(String.format("Cannot create directory %s",
          directory.getAbsolutePath()));
    }
  }

  /**
   * Template is being set up.
   *
   * @param spec
   *          spec for the project
   * @param fullTemplateData
   *          the template data to be handed to this template
   */
  public abstract void onTemplateSetup(ProjectCreationSpecification spec,
      Map<String, Object> fullTemplateData);

  /**
   * Write out all templates specific for the template type.
   *
   * @param spec
   *          specification for the project
   * @param workbench
   *          the workbench the project is being built under
   * @param templater
   *          the templater to use
   * @param fullTemplateData
   *          the full data to be used for the template
   */
  public abstract void writeSpecificTemplates(ProjectCreationSpecification spec,
      InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData);

  /**
   * Write templates common to all projects of a given type.
   *
   * @param spec
   *          specification for the project
   * @param workbench
   *          the workbench the project is being built under
   * @param templater
   *          the templater to use
   * @param fullTemplateData
   *          the full data to be used for the template
   */
  public void writeCommonTemplates(ProjectCreationSpecification spec,
      InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    // Default is none.
  }

  /**
   * Write out the project.xml file.
   *
   * @param spec
   *          the build specification
   * @param templateData
   *          data for any templates
   */
  private void writeProjectXml(FreemarkerTemplater templater, ProjectCreationSpecification spec,
      Map<String, Object> templateData) {
    templater.writeTemplate(templateData, new File(spec.getProject().getBaseDirectory(),
        "project.xml"), "project.xml.ftl");
  }
}
