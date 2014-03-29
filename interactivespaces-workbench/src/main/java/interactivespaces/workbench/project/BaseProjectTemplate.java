/*
 * Copyright (C) 2014 Google Inc.
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
import com.google.common.io.Closeables;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.FreemarkerTemplater;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Base template for any kind of project.
 *
 * @author Keith M. Hughes
 */
public class BaseProjectTemplate implements ProjectTemplate {

  /**
   * Output file for temporarily writing variables.
   */
  public static final File TEMPLATE_VARIABLES_TMP = new File("template_variables.tmp");

  /**
   * Template variable name to use for holding the base directory.
   */
  public static final String BASE_DIRECTORY_VARIABLE = "baseDirectory";

  /**
   * List of file/template pairs to add to the created project.
   */
  private final List<TemplateFile> fileTemplates = Lists.newLinkedList();

  /**
   * List of template variables to add to the project.
   */
  private final List<TemplateVar> templateVars = Lists.newArrayList();

  /**
   * Templater to use for constructing the template.
   */
  private FreemarkerTemplater templater;

  /**
   * Process the given creation specificaiton.
   *
   * @param spec
   *          specification to process.
   */
  public void process(ProjectCreationSpecification spec) {
    try {
      templateSetup(spec);
      onTemplateSetup(spec);
      processTemplateVariables(spec);
      templateWrite(spec);
      onTemplateWrite(spec);
    } catch (Exception e) {
      dumpVariables(TEMPLATE_VARIABLES_TMP, spec.getTemplateData());
      throw new SimpleInteractiveSpacesException(
          "Template variables can be found in " + TEMPLATE_VARIABLES_TMP.getAbsolutePath(), e);
    }
  }

  /**
   * Template is being set up. Can be overridden in a project-type specific project template.
   *
   * @param spec
   *          spec for the project
   */
  protected void onTemplateSetup(ProjectCreationSpecification spec) {
    // Default is to do nothing.
  }

  /**
   * Setup the template as necessary for basic operation.
   *
   * @param spec
   *          spec for the project
   *
   */
  private void templateSetup(ProjectCreationSpecification spec) {
    Project project = spec.getProject();

    spec.addTemplateDataEntry("baseDirectory", spec.getBaseDirectory().getAbsolutePath());
    spec.addTemplateDataEntry("internalTemplates", FreemarkerTemplater.TEMPLATE_LOCATION.getAbsoluteFile());
    spec.addTemplateDataEntry("spec", spec);
    spec.addTemplateDataEntry("project", project);
  }

  /**
   * Process the defined template variables.
   *
   * @param spec
   *          spec for the project
   *
   */
  private void processTemplateVariables(ProjectCreationSpecification spec) {
    FreemarkerTemplater templater = getTemplater();
    int evaluationPasses = 1;
    for (TemplateVar templateVar : spec.getProject().getTemplateVars()) {
      templater.processStringTemplate(spec.getTemplateData(), templateVar.getValue(),
          templateVar.getName(), evaluationPasses);
    }
  }

  /**
   * Dump the given variables to an output file.
   *
   * @param outputFile
   *          variable dump output file
   * @param variables
   *          variables to dump
   */
  private void dumpVariables(File outputFile, Map<String, Object> variables) {
    PrintWriter variableWriter = null;
    try {
      variableWriter = new PrintWriter(outputFile);
      for (Map.Entry<String, Object> entry : variables.entrySet()) {
        variableWriter.println(String.format("%s=%s", entry.getKey(), entry.getValue()));
      }
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(
          "Error writing variable dump file " + outputFile.getAbsolutePath(), e);
    } finally {
      Closeables.closeQuietly(variableWriter);
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
   * Add all the indicated template files to the template.
   *
   * @param addFileTemplate
   *          project template files to add
   */
  public void addAllFileTemplates(List<TemplateFile> addFileTemplate) {
    fileTemplates.addAll(addFileTemplate);
  }

  /**
   * @return list of template variables
   */
  public List<TemplateVar> getTemplateVars() {
    return templateVars;
  }

  /**
   * Add all the indicated variables.
   *
   * @param addTemplateVars
   *          variables to add
   */
  public void addAllTemplateVars(List<TemplateVar> addTemplateVars) {
    templateVars.addAll(addTemplateVars);
  }

  /**
   * Write templates common to all projects of a given type.
   *
   * @param spec
   *          specification for the project
   *
   */
  private void templateWrite(ProjectCreationSpecification spec) {
    for (TemplateFile template : spec.getProject().getTemplates()) {

      FreemarkerTemplater templater = getTemplater();
      Map<String, Object> templateData = spec.getTemplateData();

      String outPath = templater.processStringTemplate(templateData, template.getOutput());
      File outFile = new File(outPath);
      if (!outFile.isAbsolute()) {
        String newBasePath = (String) templateData.get(BASE_DIRECTORY_VARIABLE);
        outFile = new File(newBasePath, outFile.getPath());
      }

      String inPath = templater.processStringTemplate(templateData, template.getTemplate());
      File inFile = new File(inPath);
      if (!inFile.isAbsolute()) {
        inPath = new File(spec.getSpecificationBase(), inPath).getAbsolutePath();
      }

      int evaluationPasses = 2;
      templater.writeTemplate(templateData, outFile, inPath, evaluationPasses);
    }
  }

  /**
   * Function called on template write. Can be overridden to provide different functionality for other project
   * types.
   *
   * @param spec
   *          specification that is being written
   */
  public void onTemplateWrite(ProjectCreationSpecification spec) {
    // Default is to do nothing.
  }

  /**
   * Get the templater used by this template.
   *
   * @return templater in use
   */
  public FreemarkerTemplater getTemplater() {
    return templater;
  }

  /**
   * Set the templater to use for this project.
   *
   * @param templater
   *          templater to use
   */
  public void setTemplater(FreemarkerTemplater templater) {
    this.templater = templater;
  }
}
