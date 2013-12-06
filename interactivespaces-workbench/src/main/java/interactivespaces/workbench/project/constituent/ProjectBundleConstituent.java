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

import static com.google.common.io.Closeables.closeQuietly;

import com.google.common.collect.Lists;

import org.jdom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.io.Files;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

/**
 * A bundle resource for a {@link interactivespaces.workbench.project.Project}.
 *
 * @author Trevor Pering
 */
public class ProjectBundleConstituent implements ProjectConstituent {

  /**
   * Project type for a bundle resource.
   */
  public static final String PROJECT_TYPE = "bundle";

  /**
   * Builder class for new bundle resources.
   */
  public static class Builder implements ProjectConstituent.Builder {
    /**
     * Get an project dependency from the dependency element.
     *
     * @param element
     *          the element containing the data
     * @param errors
     *          any errors found in the metadata
     *
     * @return the dependency found in the element
     */
    public ProjectConstituent buildConstituentFromElement(Element element, List<String> errors) {
      int errorsStartSize = errors.size();

      ProjectBundleConstituent bundle = new ProjectBundleConstituent();

      bundle.outputPath = element.getAttributeValue(DESTINATION_FILE_ATTRIBUTE);
      if (bundle.outputPath == null) {
        errors.add("Bundle has no outputFile");
      }

      @SuppressWarnings("unchecked")
      List<Element> sourceElements = element.getChildren("source");
      if (sourceElements == null || sourceElements.size() == 0) {
        errors.add("No source elements specified");
      } else {
        for (Element sourceElement : sourceElements) {
          String source = sourceElement.getAttributeValue(SOURCE_FILE_ATTRIBUTE);
          if (source == null) {
            errors.add("Missing '" + SOURCE_FILE_ATTRIBUTE + "' attribute on source element");
          } else {
            bundle.sourcePaths.add(source);
          }
        }
      }

      return errors.size() != errorsStartSize ? null : bundle;
    }
  }

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
  private List<String> sourcePaths = Lists.newArrayList();

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectBuildContext context) {
    OutputStream outputStream = null;
    InputStream inputStream = null;

    try {
      File baseDirectory = project.getBaseDirectory();
      File outputFile = context.getProjectTarget(stagingDirectory, outputPath);
      Files.directoryExists(outputFile.getParentFile());
      outputStream = new FileOutputStream(outputFile);

      for (String sourcePath : sourcePaths) {
        File sourceFile = context.getProjectTarget(baseDirectory, sourcePath);
        if (!sourceFile.exists()) {
          throw new SimpleInteractiveSpacesException(
              "Source file does not exist " + sourceFile.getAbsolutePath());
        } else if (sourceFile.isDirectory()) {
          throw new SimpleInteractiveSpacesException(
              "Source file is a directory " + sourceFile.getAbsolutePath());
        }
        inputStream = new FileInputStream(sourceFile);
        Files.copyStream(inputStream, outputStream, false);
        inputStream.close();
      }
      outputStream.close();
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException(
          "While processing bundle resource " + outputPath, e);
    } finally {
      closeQuietly(inputStream);
      closeQuietly(outputStream);
    }
  }

  @Override
  public String getSourceDirectory() throws SimpleInteractiveSpacesException {
    throw new SimpleInteractiveSpacesException("Source directory not supported for Bundle constituents");
  }
}
