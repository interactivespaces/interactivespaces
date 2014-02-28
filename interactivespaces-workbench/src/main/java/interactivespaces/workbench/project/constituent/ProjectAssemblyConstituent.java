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
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

import org.apache.commons.logging.Log;
import org.jdom.Element;

import java.io.File;

/**
 * An assembly resource for a
 * {@link interactivespaces.workbench.project.Project}.
 *
 * @author Trevor Pering
 */
public class ProjectAssemblyConstituent implements ProjectConstituent {

  /**
   * Project type for an assembly resource.
   */
  public static final String PROJECT_TYPE = "assembly";

  /**
   * Pack format attribute name.
   */
  public static final String PACK_FORMAT_ATTRIBUTE = "packFormat";

  /**
   * Pack format type for zip files.
   */
  public static final String ZIP_PACK_FORMAT = "zip";

  /**
   * File support instance for file operations.
   */
  private static final FileSupport FILE_SUPPORT = FileSupportImpl.INSTANCE;

  /**
   * A file to be copied.
   */
  private String sourceFile;

  /**
   * The directory to which contents will be copied.
   *
   * <p>
   * This directory will be relative to the project's installed folder.
   */
  private String destinationDirectory;

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectBuildContext context) {
    File baseDirectory = project.getBaseDirectory();
    File sourceZipFile = context.getProjectTarget(baseDirectory, sourceFile);
    File outputDirectory = context.getProjectTarget(stagingDirectory, destinationDirectory);
    FILE_SUPPORT.directoryExists(outputDirectory);
    FILE_SUPPORT.unzip(sourceZipFile, outputDirectory, context.getResourceSourceMap());
  }

  @Override
  public String getSourceDirectory() throws SimpleInteractiveSpacesException {
    throw new SimpleInteractiveSpacesException("Source directory not supported for Assembly constituents");
  }

  /**
   * Factory for creating new assembly resources.
   */
  public static class ProjectAssemblyConstituentFactory implements ProjectConstituentFactory {
    @Override
    public ProjectConstituentBuilder newBuilder(Log log) {
      return new ProjectAssemblyBuilder(log);
    }
  }

  /**
   * Builder class for creating new assembly resources.
   */
  private static class ProjectAssemblyBuilder extends BaseProjectConstituentBuilder {

    /**
     * Construct a new builder.
     *
     * @param log
     *          logger for the builder
     */
    ProjectAssemblyBuilder(Log log) {
      super(log);
    }

    /**
     * Get an project dependency from the dependency element.
     *
     * @param resourceElement
     *          the element containing the data
     *
     * @return the dependency found in the element
     */
    @Override
    public ProjectConstituent buildConstituentFromElement(Element resourceElement) {
      String packFormat = resourceElement.getAttributeValue(PACK_FORMAT_ATTRIBUTE);
      if (!ZIP_PACK_FORMAT.equals(packFormat)) {
        addError(String.format("Pack format '%s' not supported (currently must be '%s')", packFormat, ZIP_PACK_FORMAT));
      }
      String sourceFile = resourceElement.getAttributeValue(SOURCE_FILE_ATTRIBUTE);
      String destinationDirectory = resourceElement.getAttributeValue(DESTINATION_DIRECTORY_ATTRIBUTE);

      if (destinationDirectory == null) {
        destinationDirectory = ".";
      }

      if (sourceFile == null) {
        addError("Assembly has no source");
      }

      if (hasErrors()) {
        return null;
      } else {
        ProjectAssemblyConstituent assembly = new ProjectAssemblyConstituent();

        assembly.destinationDirectory = destinationDirectory;
        assembly.sourceFile = sourceFile;

        return assembly;
      }
    }
  }
}
