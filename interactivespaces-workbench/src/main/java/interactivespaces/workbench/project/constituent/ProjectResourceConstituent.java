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

import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;

import com.google.common.collect.Maps;

import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.util.Map;

/**
 * A simple resource for a {@link interactivespaces.workbench.project.Project}.
 *
 * @author Keith M. Hughes
 */
public class ProjectResourceConstituent extends ContainerConstituent {

  /**
   * Element type for a resource.
   */
  public static final String TYPE_NAME = "resource";

  /**
   * Element type for a source, which is functionally equivalent to a resource.
   */
  public static final String ALTERNATE_NAME = "source";

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
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
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

  @Override
  public Map<String, String> getAttributeMap() {
    Map<String, String> map = Maps.newHashMap();
    map.put(SOURCE_FILE_ATTRIBUTE, sourceFile);
    map.put(SOURCE_DIRECTORY_ATTRIBUTE, sourceDirectory);
    map.put(DESTINATION_FILE_ATTRIBUTE, destinationFile);
    map.put(DESTINATION_DIRECTORY_ATTRIBUTE, destinationDirectory);
    return map;
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
  public void processConstituent(Project project, File stagingDirectory, ProjectContext context) {
    File baseDirectory = project.getBaseDirectory();
    if (getDestinationDirectory() != null) {
      File destDir = context.getProjectTarget(stagingDirectory, getDestinationDirectory());
      fileSupport.directoryExists(destDir);

      if (getSourceDirectory() != null) {
        File srcDir = context.getProjectTarget(baseDirectory, getSourceDirectory());
        fileSupport.copyDirectory(srcDir, destDir, true, context.getResourceSourceMap());
      } else {
        // There is a file to be copied.
        File srcFile = context.getProjectTarget(baseDirectory, getSourceFile());
        File destination = new File(destDir, srcFile.getName());
        fileSupport.copyFile(srcFile, destination);
        context.getResourceSourceMap().put(destination, srcFile);
      }
    } else {
      // Have a dest file
      // There is a file to be copied.
      File destFile = context.getProjectTarget(stagingDirectory, getDestinationFile());
      File srcFile = context.getProjectTarget(baseDirectory, getSourceFile());
      fileSupport.directoryExists(destFile.getParentFile());
      fileSupport.copyFile(srcFile, destFile);
      context.getResourceSourceMap().put(destFile, srcFile);
    }
  }

  /**
   * Factory for building the constituent builder.
   *
   * @author Keith M. Hughes
   */
  public static class ProjectResourceBuilderFactory implements ProjectConstituentFactory {
    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder() {
      return new ProjectResourceBuilder();
    }
  }

  /**
   * Factory for building the constituent builder.
   *
   * @author Keith M. Hughes
   */
  public static class ProjectSourceBuilderFactory implements ProjectConstituentFactory {
    @Override
    public String getName() {
      return ALTERNATE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder() {
      return new ProjectResourceBuilder();
    }
  }

  /**
   * Builder class for creating new resource instances.
   */
  private static class ProjectResourceBuilder extends BaseProjectConstituentBuilder {

    @Override
    public ProjectConstituent
        buildConstituentFromElement(Namespace namespace, Element resourceElement, Project project) {
      String sourceDir = resourceElement.getAttributeValue(SOURCE_DIRECTORY_ATTRIBUTE);
      String sourceFile = resourceElement.getAttributeValue(SOURCE_FILE_ATTRIBUTE);
      String destDir = resourceElement.getAttributeValue(DESTINATION_DIRECTORY_ATTRIBUTE);
      String destFile = resourceElement.getAttributeValue(DESTINATION_FILE_ATTRIBUTE);

      if (destFile == null && destDir == null) {
        destDir = ".";
      }

      if (sourceFile == null && sourceDir == null) {
        addError("Resource has no source");
      }
      if (sourceDir != null) {
        if (sourceFile != null) {
          addError("Resource has both a source file and directory");
        }
        if (destFile != null) {
          addError("Resource has a source directory and a destination file");
        }
      }
      // TODO(keith): Enumerate all possible errors

      if (hasErrors()) {
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
}
