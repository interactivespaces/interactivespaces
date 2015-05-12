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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;

import com.google.common.collect.Lists;

import org.jdom2.Element;
import org.jdom2.Namespace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * A bundle resource for a {@link interactivespaces.workbench.project.Project}.
 *
 * <p>
 * The default constituent processing places the content in the build staging directory.
 *
 * @author Trevor Pering
 */
public class BundleContentProjectConstituent extends BaseContentProjectConstituent {

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

  /**
   * The file support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void processConstituent(Project project, File stagingDirectory, ProjectContext context) {
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

      context.getResourceSourceMap().put(outputFile, BUNDLE_SOURCE_MARKER_FILE);
    } catch (Exception e) {
      throw new SimpleInteractiveSpacesException("While processing bundle resource " + outputPath, e);
    } finally {
      closeQuietly(inputStream);
      fileSupport.close(outputStream, true);
    }
  }

  /**
   * Factory for the constituent components.
   */
  public static class BundleProjectConstituentBuilderFactory implements ProjectConstituentBuilderFactory {
    @Override
    public String getName() {
      return TYPE_NAME;
    }

    @Override
    public ProjectConstituentBuilder newBuilder() {
      return new BundleProjectConstituentBuilder();
    }
  }

  /**
   * Builder class for new bundle resources.
   */
  private static class BundleProjectConstituentBuilder extends BaseProjectConstituentBuilder {

    @Override
    public ProjectConstituent buildConstituentFromElement(Namespace namespace, Element element, Project project) {
      BundleContentProjectConstituent bundle = new BundleContentProjectConstituent();

      bundle.outputPath = element.getAttributeValue(DESTINATION_FILE_ATTRIBUTE);
      if (bundle.outputPath == null) {
        addError("Bundle has no outputFile");
      }

      @SuppressWarnings("unchecked")
      List<Element> sourceElements = element.getChildren("source", namespace);
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
