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

package interactivespaces.workbench.project.constituent;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.TemplateFile;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import org.apache.commons.logging.Log;
import org.jdom.Element;

import java.io.File;

/**
 * A bundle resource for a {@link interactivespaces.workbench.project.Project}.
 *
 * @author Trevor Pering
 */
public class ProjectTemplateConstituent extends ContainerConstituent {

  /**
   * Project type for a bundle resource.
   */
  public static final String TYPE_NAME = TemplateFile.ELEMENT_NAME;

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectBuildContext context) {
    // Nothing to do.
  }

  /**
   * Factory for the constituent components.
   */
  public static class ProjectTemplateConstituentFactory implements ProjectConstituentFactory {
    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder(Log log) {
      return new ProjectTemplateConstituentBuilder(log);
    }
  }

  /**
   * Builder class for new templates..
   */
  private static class ProjectTemplateConstituentBuilder extends BaseProjectConstituentBuilder {

    /**
     * Get a template file from given element.
     *
     * @param element
     *          element providing the template file
     *
     * @return constituent representing the template file
     */
    private TemplateFile getTemplateFileFromElement(Element element) {
      if (!TemplateFile.ELEMENT_NAME.equals(element.getName())) {
        throw new SimpleInteractiveSpacesException("Bad element name " + element.getName());
      }
      TemplateFile templateFile = new TemplateFile();

      String sourcePath = element.getAttributeValue(SOURCE_FILE_ATTRIBUTE);
      if (sourcePath == null) {
        throw new SimpleInteractiveSpacesException("Template specification has no sourceFile");
      }
      templateFile.setTemplate(sourcePath);

      String destinationFile = element.getAttributeValue(DESTINATION_FILE_ATTRIBUTE);
      if (destinationFile == null) {
        throw new SimpleInteractiveSpacesException("Template specification has no destinationFile");
      }
      templateFile.setOutput(destinationFile);
      return templateFile;
    }

    /**
     * Construct the new builder.
     *
     * @param log
     *          logger for the builder
     */
    ProjectTemplateConstituentBuilder(Log log) {
      super(log);
    }

    @Override
    public ProjectConstituent buildConstituentFromElement(Element element, Project project) {
      TemplateFile templateFile = getTemplateFileFromElement(element);

      project.addTemplate(templateFile);

      return null;
    }
  }
}
