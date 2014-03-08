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

import com.google.common.collect.Lists;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import org.apache.commons.logging.Log;
import org.jdom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * A bundle resource for a {@link interactivespaces.workbench.project.Project}.
 *
 * @author Trevor Pering
 */
public class ProjectBundleConstituent extends ContainerConstituent {

  /**
   * Project type for a bundle resource.
   */
  public static final String TYPE_NAME = "bundle";

  /**
   * Marker file to use for bundle source recording.
   */
  public static final File BUNDLE_SOURCE_MARKER_FILE = new File("SOURCE-BUNDLE");

  /**
   * The directory to which contents will be copied.
   *
   * <p>
   * This directory will be relative to the project's installed folder.
   */
  private String outputPath;

  /**
   * List of source paths to assemble.
   */
  private final List<String> sourcePaths = Lists.newArrayList();

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectBuildContext context) {
    OutputStream outputStream = null;
    InputStream inputStream = null;

    try {
      File baseDirectory = project.getBaseDirectory();
      File outputFile = context.getProjectTarget(stagingDirectory, outputPath);
      fileSupport.directoryExists(outputFile.getParentFile());
      outputStream = new FileOutputStream(outputFile);

      for (String sourcePath : sourcePaths) {
        File sourceFile = context.getProjectTarget(baseDirectory, sourcePath);
        if (!sourceFile.exists()) {
          throw new SimpleInteractiveSpacesException("Source file does not exist " + sourceFile.getAbsolutePath());
        } else if (sourceFile.isDirectory()) {
          throw new SimpleInteractiveSpacesException("Source file is a directory " + sourceFile.getAbsolutePath());
        }
        inputStream = new FileInputStream(sourceFile);
        fileSupport.copyStream(inputStream, outputStream, false);
        inputStream.close();
      }
      outputStream.close();
      context.getResourceSourceMap().put(outputFile, BUNDLE_SOURCE_MARKER_FILE);
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("While processing bundle resource " + outputPath, e);
    } finally {
      closeQuietly(inputStream);
      closeQuietly(outputStream);
    }
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  /**
   * Factory for the constituent components.
   */
  public static class ProjectBundleConstituentFactory implements ProjectConstituentFactory {
    @Override
    public ProjectConstituentBuilder newBuilder(Log log) {
      return new ProjectBundleConstituentBuilder(log);
    }
  }

  /**
   * Builder class for new bundle resources.
   */
  private static class ProjectBundleConstituentBuilder extends BaseProjectConstituentBuilder {

    /**
     * Construct the new builder.
     *
     * @param log
     *          logger for the builder
     */
    ProjectBundleConstituentBuilder(Log log) {
      super(log);
    }

    @Override
    public ProjectConstituent buildConstituentFromElement(Element element, Project project) {
      ProjectBundleConstituent bundle = new ProjectBundleConstituent();

      bundle.outputPath = element.getAttributeValue(DESTINATION_FILE_ATTRIBUTE);
      if (bundle.outputPath == null) {
        addError("Bundle has no outputFile");
      }

      @SuppressWarnings("unchecked")
      List<Element> sourceElements = element.getChildren("source");
      if (sourceElements == null || sourceElements.size() == 0) {
        addError("No source elements specified");
      } else {
        for (Element sourceElement : sourceElements) {
          String source = sourceElement.getAttributeValue(SOURCE_FILE_ATTRIBUTE);
          if (source == null) {
            addError("Missing '" + SOURCE_FILE_ATTRIBUTE + "' attribute on source element");
          } else {
            bundle.sourcePaths.add(source);
          }
        }
      }

      return hasErrors() ? null : bundle;
    }
  }
}
