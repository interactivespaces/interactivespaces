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
import interactivespaces.workbench.FreemarkerTemplater;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Base template for any kind of project.
 *
 * @author Keith M. Hughes
 */
public class BaseProjectTemplate extends BaseTemplate implements ProjectTemplate {

  /**
   * List of all source directories needed.
   */
  private List<String> sourceDirectories = Lists.newArrayList();

  /**
   * Create a new project template.
   *
   * @param displayName
   *          name to be displayed for the project template
   */
  public BaseProjectTemplate(String displayName) {
    super(displayName);
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

  /**
   * @return source directories associated with this project
   */
  public List<String> getSourceDirectories() {
    return sourceDirectories;
  }

  @Override
  protected void onTemplateSetup(CreationSpecification spec) {
    super.onTemplateSetup(spec);

    Project project = ((ProjectCreationSpecification) spec).getProject();
    addAllFileTemplate(project.getTemplates());

    FreemarkerTemplater templater = spec.getTemplater();
    for (TemplateVar templateVar : project.getTemplateVars()) {
      templater.processStringTemplate(spec.getTemplateData(), templateVar.getValue(), templateVar.getName());
    }
  }

  @Override
  protected void onTemplateWrite(CreationSpecification spec) {
    super.onTemplateWrite(spec);
    writeProjectXml(spec);
  }

  protected File getBaseDirectory(CreationSpecification spec, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    Project project = ((ProjectCreationSpecification) spec).getProject();
    String identifyingName = templater.processStringTemplate(fullTemplateData, project.getIdentifyingName(), null);
    return new File(spec.getBaseDirectory(), identifyingName);
  }

  /**
   * Write out the project.xml file.
   *
   * @param spec
   *          the build specification
   *
   */
  private void writeProjectXml(CreationSpecification spec) {
    FreemarkerTemplater templater = spec.getTemplater();
    Map<String, Object> templateData = spec.getTemplateData();
    File baseDirectory = getBaseDirectory(spec, templater, templateData);
    templater.writeTemplate(templateData, new File(baseDirectory, "project.xml"), "project.xml.ftl");
  }
}
