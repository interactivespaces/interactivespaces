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

import org.jdom.Element;

import java.io.File;
import java.util.List;

import interactivespaces.util.io.Files;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

/**
 * A simple resource for a {@link interactivespaces.workbench.project.Project}.
 *
 * @author Keith M. Hughes
 */
public class ProjectResourceConstituent implements ProjectConstituent {

  /**
   * Element type for a resource.
   */
  public static final String PROJECT_TYPE = "resource";

  /**
   * Element type for a source, which is functionally equivalent to a resource.
   */
  public static final String PROJECT_TYPE_ALTERNATE = "source";

  /**
   * A directory from which all contents will be copied.
   */
  private String sourceDirectory;

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

  /**
   * The file to which a file will be copied.
   *
   * <p>
   * This file will be relative to the project's installed folder.
   */
  private String destinationFile;

  /**
   * Builder class for creating new resource instances.
   */
  public static class Builder implements ProjectConstituent.Builder {
    /**
     * Get an project dependency from the dependency element.
     *
     * @param resourceElement
     *          the element containing the data
     * @param errors
     *          any errors found in the metadata
     *
     * @return the dependency found in the element
     */
    public ProjectConstituent buildConstituentFromElement(Element resourceElement, List<String> errors) {
      boolean addedErrors = false;

      String sourceDir = resourceElement.getAttributeValue(SOURCE_DIRECTORY_ATTRIBUTE);
      String sourceFile = resourceElement.getAttributeValue(SOURCE_FILE_ATTRIBUTE);
      String destDir = resourceElement.getAttributeValue(DESTINATION_DIRECTORY_ATTRIBUTE);
      String destFile = resourceElement.getAttributeValue(DESTINATION_FILE_ATTRIBUTE);

      if (destFile == null && destDir == null) {
        destDir = ".";
      }

      if (sourceFile == null && sourceDir == null) {
        addedErrors = true;
        errors.add("Resource has no source");
      }
      if (sourceDir != null) {
        if (sourceFile != null) {
          addedErrors = true;
          errors.add("Resource has both a source file and directory");
        }
        if (destFile != null) {
          addedErrors = true;
          errors.add("Resource has a source directory and a destination file");
        }
      }
      // TODO(keith): Enumerate all possible errors

      if (addedErrors) {
        return null;
      } else {

        ProjectResourceConstituent resource = new ProjectResourceConstituent();

        resource.setDestinationDirectory(destDir);
        resource.setSourceDirectory(sourceDir);
        resource.setDestinationFile(destFile);
        resource.setSourceFile(sourceFile);

        return resource;
      }
    }
  }

  /**
   * @return the sourceDirectory
   */
  public String getSourceDirectory() {
    return sourceDirectory;
  }

  /**
   * @param sourceDirectory
   *          the sourceDirectory to set
   */
  public void setSourceDirectory(String sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  /**
   * @return the sourceFile
   */
  public String getSourceFile() {
    return sourceFile;
  }

  /**
   * @param sourceFile
   *          the sourceFile to set
   */
  public void setSourceFile(String sourceFile) {
    this.sourceFile = sourceFile;
  }

  /**
   * @return the destinationDirectory
   */
  public String getDestinationDirectory() {
    return destinationDirectory;
  }

  /**
   * @param destinationDirectory
   *          the destinationDirectory to set
   */
  public void setDestinationDirectory(String destinationDirectory) {
    this.destinationDirectory = destinationDirectory;
  }

  /**
   * @return the destinationFile
   */
  public String getDestinationFile() {
    return destinationFile;
  }

  /**
   * @param destinationFile
   *          the destinationFile to set
   */
  public void setDestinationFile(String destinationFile) {
    this.destinationFile = destinationFile;
  }

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectBuildContext context) {
    File baseDirectory = project.getBaseDirectory();
    if (getDestinationDirectory() != null) {
      File destDir = context.getProjectTarget(stagingDirectory, getDestinationDirectory());
      Files.directoryExists(destDir);

      if (getSourceDirectory() != null) {
        File srcDir = context.getProjectTarget(baseDirectory, getSourceDirectory());
        Files.copyDirectory(srcDir, destDir, true);
      } else {
        // There is a file to be copied.
        File srcFile = context.getProjectTarget(baseDirectory, getSourceFile());
        Files.copyFile(srcFile, new File(destDir, srcFile.getName()));
      }
    } else {
      // Have a dest file
      // There is a file to be copied.
      File destFile = context.getProjectTarget(stagingDirectory, getDestinationFile());
      File srcFile = context.getProjectTarget(baseDirectory, getSourceFile());
      Files.directoryExists(destFile.getParentFile());
      Files.copyFile(srcFile, destFile);
    }
  }
}
