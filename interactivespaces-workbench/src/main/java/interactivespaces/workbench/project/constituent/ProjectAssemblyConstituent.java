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

import com.google.common.collect.Maps;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;
import org.jdom.Element;

import java.io.File;
import java.util.Map;

/**
 * An assembly resource for a
 * {@link interactivespaces.workbench.project.Project}.
 *
 * @author Trevor Pering
 */
public class ProjectAssemblyConstituent extends ContainerConstituent {

  /**
   * Project type for an assembly resource.
   */
  public static final String TYPE_NAME = "assembly";

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
  public void processConstituent(Project project, File stagingDirectory, ProjectContext context) {
    File baseDirectory = project.getBaseDirectory();
    File sourceZipFile = context.getProjectTarget(baseDirectory, sourceFile);
    File outputDirectory = context.getProjectTarget(stagingDirectory, destinationDirectory);
    FILE_SUPPORT.directoryExists(outputDirectory);
    FILE_SUPPORT.unzip(sourceZipFile, outputDirectory, context.getResourceSourceMap());
  }

  @Override
  public Map<String, String> getAttributeMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put(PACK_FORMAT_ATTRIBUTE, ZIP_PACK_FORMAT);
    map.put(SOURCE_FILE_ATTRIBUTE, sourceFile);
    map.put(DESTINATION_DIRECTORY_ATTRIBUTE, destinationDirectory);
    return map;
  }

  /**
   * Create a new project assembly constituent from a string.
   *
   * @param input
   *          specification string
   *
   * @return parsed constituent
   */
  public static ProjectAssemblyConstituent fromString(String input) {
    String[] parts = input.split(",");
    if (parts.length > 2) {
      throw new SimpleInteractiveSpacesException("Extra parts when parsing assembly: " + input);
    }
    ProjectAssemblyConstituent constituent = new ProjectAssemblyConstituent();
    constituent.sourceFile = parts[0];
    constituent.destinationDirectory = parts.length > 1 ? parts[1] : null;
    return constituent;
  }

  /**
   * Factory for creating new assembly resources.
   */
  public static class ProjectAssemblyConstituentFactory implements ProjectConstituentFactory {
    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder() {
      return new ProjectAssemblyBuilder();
    }
  }

  /**
   * Set the assembly source file.
   *
   * @param sourceFile
   *          assembly source path
   */
  public void setSourceFile(String sourceFile) {
    this.sourceFile = sourceFile;
  }

  /**
   * Set the destination directory for assembly expansion.
   *
   * @param destinationDirectory
   *          directory to receive contents
   */
  public void setDestinationDirectory(String destinationDirectory) {
    this.destinationDirectory = destinationDirectory;
  }

  /**
   * Builder class for creating new assembly resources.
   */
  private static class ProjectAssemblyBuilder extends BaseProjectConstituentBuilder {

    @Override
    public ProjectConstituent buildConstituentFromElement(Element resourceElement, Project project) {
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

        assembly.setDestinationDirectory(destinationDirectory);
        assembly.setSourceFile(sourceFile);

        return assembly;
      }
    }
  }
}
