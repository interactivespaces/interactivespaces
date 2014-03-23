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
import com.google.common.io.Closeables;
import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;
import interactivespaces.workbench.InteractiveSpacesWorkbench;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Base template for any kind of project.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseProjectTemplate implements ProjectTemplate {

  public static final String TEMPLATE_VARIABLES_TMP = "template_variables.tmp";
  /**
   * The display name for the template.
   */
  private String displayName;

  /**
   * List of all source directories needed.
   */
  private List<String> sourceDirectories = Lists.newArrayList();

  /**
   * Map of file/template pairs to add to the created project.
   */
  private final List<TemplateFile> fileTemplates = Lists.newLinkedList();

  /**
   * Create a new project template.
   *
   * @param displayName
   *          name to be displayed for the project template
   */
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

  /**
   * @return source directories associated with this project
   */
  public List<String> getSourceDirectories() {
    return sourceDirectories;
  }

  @Override
  public void process(ProjectCreationSpecification spec, InteractiveSpacesWorkbench workbench,
      FreemarkerTemplater templater, Map<String, Object> templateData) {
    Project project = spec.getProject();

    Map<String, Object> fullTemplateData = Maps.newTreeMap();
    fullTemplateData.put("baseDirectory", spec.getConfederacyDirectory());
    fullTemplateData.putAll(templateData);

    fileTemplates.addAll(project.getTemplates());

    onTemplateSetup(spec, templater, fullTemplateData);

    try {
      for (TemplateVar templateVar : spec.getTemplateVars()) {
        templater.processStringTemplate(fullTemplateData, templateVar.getValue(), templateVar.getName());
      }
      for (TemplateVar templateVar : project.getTemplateVars()) {
        templater.processStringTemplate(fullTemplateData, templateVar.getValue(), templateVar.getName());
      }

      writeTemplateList(spec, workbench, templater, fullTemplateData);
      writeSpecificTemplates(spec, workbench, templater, fullTemplateData);
      writeCommonTemplates(spec, workbench, templater, fullTemplateData);
      writeProjectXml(templater, spec, fullTemplateData);
    } catch (Exception e) {
      File variableDump = new File(TEMPLATE_VARIABLES_TMP);
      dumpVariables(fullTemplateData, variableDump);
      throw new SimpleInteractiveSpacesException("Template variables are in " + variableDump.getAbsolutePath(), e);
    }
  }

  private void dumpVariables(Map<String, Object> fullTemplateData, File variableDump) {
    PrintWriter variableWriter = null;
    try {
      variableWriter = new PrintWriter(variableDump);
      for (Map.Entry<String, Object> entry : fullTemplateData.entrySet()) {
        variableWriter.println(String.format("%s=%s", entry.getKey(), entry.getValue()));
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(
          "Error writing variable dump file " + variableDump.getAbsolutePath(), e);
    } finally {
      Closeables.closeQuietly(variableWriter);
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
    if (!directory.isDirectory() && !directory.mkdirs()) {
      throw new InteractiveSpacesException(String.format("Cannot create directory %s",
          directory.getAbsolutePath()));
    }
  }

  /**
   * Add a file template to the common collection.
   *
   * @param dest
   *          output destination
   * @param source
   *          template source
   */
  public void addFileTemplate(String dest, String source) {
    fileTemplates.add(new TemplateFile(dest, source));
  }

  /**
   * Template is being set up.
   *
   * @param spec
   *          spec for the project
   * @param templater
   *          the templater to use for setup
   * @param fullTemplateData
   *          template data to setup
   */
  public abstract void onTemplateSetup(ProjectCreationSpecification spec,
      FreemarkerTemplater templater, Map<String, Object> fullTemplateData);

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
  }

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
  public void writeTemplateList(ProjectCreationSpecification spec,
      InteractiveSpacesWorkbench workbench, FreemarkerTemplater templater,
      Map<String, Object> fullTemplateData) {
    for (TemplateFile template : fileTemplates) {
      Project project = spec.getProject();

      File outFile = new File(templater.processStringTemplate(fullTemplateData, template.getOutput(), null));
      if (!outFile.isAbsolute()) {
        File projectDirectory = getBaseDirectory(spec, templater, fullTemplateData, project);
        outFile = new File(projectDirectory, outFile.getPath());
      }

      String relativeInPath = templater.processStringTemplate(fullTemplateData, template.getTemplate(), null);
      File specificationSource = project.getSpecificationSource();
      String absoluteInPath = new File(specificationSource.getParentFile(), relativeInPath).getAbsolutePath();
      templater.writeTemplate(fullTemplateData, outFile, absoluteInPath);
    }
  }

  private File getBaseDirectory(ProjectCreationSpecification spec, FreemarkerTemplater templater, Map<String, Object> fullTemplateData, Project project) {
    String identifyingName = templater.processStringTemplate(fullTemplateData, project.getIdentifyingName(), null);
    return new File(spec.getConfederacyDirectory(), identifyingName);
  }

  /**
   * Write out the project.xml file.
   *
   * @param templater
   *          the templater to use
   * @param spec
   *          the build specification
   * @param templateData
   *          data for any templates
   */
  private void writeProjectXml(FreemarkerTemplater templater, ProjectCreationSpecification spec,
      Map<String, Object> templateData) {
    Project project = spec.getProject();
    File baseDirectory = getBaseDirectory(spec, templater, templateData, project);
    templater.writeTemplate(templateData, new File(baseDirectory, "project.xml"), "project.xml.ftl");
  }
}
