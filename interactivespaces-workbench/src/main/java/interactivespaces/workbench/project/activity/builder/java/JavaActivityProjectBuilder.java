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
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.ProjectTaskContext;
import interactivespaces.workbench.project.activity.ActivityProject;
import interactivespaces.workbench.project.activity.builder.BaseActivityProjectBuilder;
import interactivespaces.workbench.project.java.ContainerInfo;
import interactivespaces.workbench.project.java.JavaJarCompiler;
import interactivespaces.workbench.project.java.JavaProjectExtension;
import interactivespaces.workbench.project.java.JavaxJavaJarCompiler;
import interactivespaces.workbench.project.java.ProjectJavaCompiler;
import interactivespaces.workbench.project.test.IsolatedClassloaderJavaTestRunner;
import interactivespaces.workbench.project.test.JavaTestRunner;

import java.io.File;

/**
 * A {@link ProjectBuilder} for Java-base activity projects.
 *
 * @author Keith M. Hughes
 */
public class JavaActivityProjectBuilder extends BaseActivityProjectBuilder {

  /**
   * File extension to give the build artifact.
   */
  private static final String JAR_FILE_EXTENSION = "jar";

  /**
   * The extensions for this builder.
   */
  private final JavaProjectExtension extensions;

  /**
   * The compiler for Java JARs.
   */
  private final JavaJarCompiler compiler = new JavaxJavaJarCompiler();

  /**
   * File support to use.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

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
  public JavaActivityProjectBuilder(JavaProjectExtension extensions) {
    this.extensions = extensions;
  }

  @Override
  public void onBuild(ActivityProject project, ProjectTaskContext context, File stagingDirectory)
      throws InteractiveSpacesException {
    File buildDirectory = context.getBuildDirectory();
    File compilationDirectory = getCompilationOutputDirectory(buildDirectory);
    File jarDestinationFile = getBuildDestinationFile(project, stagingDirectory, JAR_FILE_EXTENSION);
    project.setActivityExecutable(jarDestinationFile.getName());

    ContainerInfo containerInfo = new ContainerInfo();
    addActivityContainerInformation(project, containerInfo);

    compiler.buildJar(jarDestinationFile, compilationDirectory, extensions, containerInfo, context);

    runTests(jarDestinationFile, context);
  }

  /**
   * Add in any container info data needed from the activity.
   *
   * @param project
   *          the project
   * @param containerInfo
   *          the container info to modify
   */
  private void addActivityContainerInformation(ActivityProject project, ContainerInfo containerInfo) {
    String activityClass = project.getActivityClass();
    String activityPackage = null;
    int classnamePos = activityClass.lastIndexOf(".");
    if (classnamePos != -1) {
      activityPackage = activityClass.substring(0, classnamePos);
    } else {
      SimpleInteractiveSpacesException.throwFormattedException("Activity class in the root package: %s", activityClass);
    }
    containerInfo.addExportPackages(activityPackage);
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

    runner.runTests(jarDestinationFile, extensions, context);
  }

  /**
   * Create the output directory for the activity compilation.
   *
   * @param buildDirectory
   *          the root of the build folder
   *
   * @return the output directory for building
   */
  private File getCompilationOutputDirectory(File buildDirectory) {
    File outputDirectory = new File(buildDirectory, ProjectJavaCompiler.BUILD_DIRECTORY_CLASSES_MAIN);
    fileSupport.directoryExists(outputDirectory);

    return outputDirectory;
  }
}
