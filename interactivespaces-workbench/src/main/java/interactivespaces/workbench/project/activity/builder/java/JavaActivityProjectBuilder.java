/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.workbench.project.activity.builder.java;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.activity.builder.BaseActivityProjectBuilder;

import java.io.File;

/**
 * An {@link ProjectBuilder} for java projects.
 *
 * @author Keith M. Hughes
 */
public class JavaActivityProjectBuilder extends BaseActivityProjectBuilder {

  /**
   * File extension to give the build artifact
   */
  private static final String JAR_FILE_EXTENSION = "jar";

  /**
   * The extensions for this builder.
   */
  private JavaProjectExtensions extensions;

  /**
   * The compiler for Java JARs
   */
  private JavaJarCompiler compiler = new JavaxJavaJarCompiler();

  /**
   * Construct a builder with no extensions.
   */
  public JavaActivityProjectBuilder() {
    this(null);
  }

  /**
   * Construct a builder with the given extensions.
   *
   * @param extensions
   *          the extensions to use, can be {@code null}
   */
  public JavaActivityProjectBuilder(JavaProjectExtensions extensions) {
    this.extensions = extensions;
  }

  @Override
  public boolean onBuild(Project project, ProjectBuildContext context, File stagingDirectory) {
    try {
      File buildDirectory = context.getBuildDirectory();
      File compilationFolder = getOutputDirectory(buildDirectory);
      File jarDestinationFile = getBuildDestinationFile(project, stagingDirectory);

      return compiler.build(jarDestinationFile, compilationFolder, extensions, context);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

      return false;
    }
  }

  /**
   * Create the output directory for the activity compilation
   *
   * @param buildDirectory
   *          the root of the build folder
   *
   * @return the output directory for building
   */
  private File getOutputDirectory(File buildDirectory) {
    File outputDirectory = new File(buildDirectory, JavaJarCompiler.CLASSES_DIRECTORY);
    if (!outputDirectory.exists()) {
      if (!outputDirectory.mkdirs()) {
        throw new InteractiveSpacesException(String.format(
            "Cannot create Java compiler output directory %s", outputDirectory));
      }
    }

    return outputDirectory;
  }

  /**
   * Get the build destination file.
   *
   * <p>
   * Any subdirectories needed will be created.
   *
   * @param project
   *          the project being built
   * @param buildDirectory
   *          where the artifact will be built
   *
   * @return the file where the build should be written
   */
  private File getBuildDestinationFile(Project project, File buildDirectory) {
    return new File(buildDirectory, project.getIdentifyingName() + "-" + project.getVersion() + "."
        + JAR_FILE_EXTENSION);
  }
}
