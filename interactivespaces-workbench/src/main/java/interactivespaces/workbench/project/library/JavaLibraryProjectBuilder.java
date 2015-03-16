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
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.ProjectTaskContext;
import interactivespaces.workbench.project.builder.BaseProjectBuilder;
import interactivespaces.workbench.project.java.JavaJarCompiler;
import interactivespaces.workbench.project.java.JavaxJavaJarCompiler;
import interactivespaces.workbench.project.java.ProjectJavaCompiler;
import interactivespaces.workbench.project.test.IsolatedClassloaderJavaTestRunner;
import interactivespaces.workbench.project.test.JavaTestRunner;

import java.io.File;

/**
 * A Java library project builder.
 *
 * @author Keith M. Hughes
 */
public class JavaLibraryProjectBuilder extends BaseProjectBuilder<LibraryProject> {

  /**
   * File extension to give the build artifact.
   */
  private static final String JAR_FILE_EXTENSION = "jar";

  /**
   * The compiler for Java JARs.
   */
  private final JavaJarCompiler compiler = new JavaxJavaJarCompiler();

  /**
   * File support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void build(LibraryProject project, ProjectTaskContext context) throws InteractiveSpacesException {
    File buildDirectory = context.getBuildDirectory();
    File compilationFolder = getOutputDirectory(buildDirectory);
    File jarDestinationFile = getBuildDestinationFile(project, buildDirectory, JAR_FILE_EXTENSION);

    // The resources go to the compilation folder. They will then be in the
    // right place for creating the JAR file.
    context.processGeneratedResources(compilationFolder);
    context.processResources(compilationFolder);

    compiler.buildJar(jarDestinationFile, compilationFolder, null, project.getContainerInfo(), context);
    runTests(jarDestinationFile, context);
    context.addGeneratedArtifact(jarDestinationFile);
  }

  /**
   * Run any tests for the project.
   *
   * @param jarDestinationFile
   *          the destination file for the built project
   * @param context
   *          the project build context
   *
   * @throws InteractiveSpacesException
   *           the tests failed
   */
  private void runTests(File jarDestinationFile, ProjectTaskContext context) throws InteractiveSpacesException {
    JavaTestRunner runner = new IsolatedClassloaderJavaTestRunner();

    runner.runTests(jarDestinationFile, null, context);
  }

  /**
   * Create the output directory for the library compilation.
   *
   * @param buildDirectory
   *          the root of the build folder
   *
   * @return the output directory for building
   */
  private File getOutputDirectory(File buildDirectory) {
    File outputDirectory = new File(buildDirectory, ProjectJavaCompiler.BUILD_DIRECTORY_CLASSES_MAIN);
    fileSupport.directoryExists(outputDirectory);

    return outputDirectory;
  }
}
