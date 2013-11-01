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

package interactivespaces.workbench.project.library;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.builder.BaseProjectBuilder;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import interactivespaces.workbench.project.activity.builder.java.JavaJarCompiler;
import interactivespaces.workbench.project.activity.builder.java.JavaxJavaJarCompiler;

import java.io.File;

/**
 * A Java library project builder.
 *
 * @author Keith M. Hughes
 */
public class JavaLibraryProjectBuilder extends BaseProjectBuilder {

  /**
   * File extension to give the build artifact
   */
  private static final String JAR_FILE_EXTENSION = "jar";

  /**
   * The compiler for Java JARs
   */
  private JavaJarCompiler compiler = new JavaxJavaJarCompiler();

  @Override
  public boolean build(Project project, ProjectBuildContext context) {
    File buildDirectory = context.getBuildDirectory();
    File compilationFolder = getOutputDirectory(buildDirectory);
    File jarDestinationFile =
        getBuildDestinationFile(project, buildDirectory, JAR_FILE_EXTENSION);

    return compiler.build(jarDestinationFile, compilationFolder, null, context);
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
    File outputDirectory = new File(buildDirectory, "classes");
    if (!outputDirectory.exists()) {
      if (!outputDirectory.mkdirs()) {
        throw new InteractiveSpacesException(String.format(
            "Cannot create Java compiler output directory %s", outputDirectory));
      }
    }

    return outputDirectory;
  }

}
