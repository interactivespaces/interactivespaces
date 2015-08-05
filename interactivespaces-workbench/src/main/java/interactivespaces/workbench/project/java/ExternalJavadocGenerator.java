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

package interactivespaces.workbench.project.java;

import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.process.NativeApplicationDescription;
import interactivespaces.workbench.project.ProjectTaskContext;
import interactivespaces.workbench.tasks.WorkbenchTaskContext;

import java.io.File;

/**
 * A Javadoc generator that uses an external Javadoc compiler.
 *
 * @author Keith M. Hughes
 */
public class ExternalJavadocGenerator implements JavadocGenerator {

  /**
   * The separator between subpackage paths for the Javadoc compiler.
   */
  public static final String SUBPACKAGE_SEPARATOR = ":";

  /**
   * The amount of time to wait for the javadoc compiler to finish, in milliseconds.
   */
  public static final int MAX_TIME_FOR_JAVADOC_RUN = 60000;

  /**
   * The command executable for running Javadoc.
   */
  public static final String COMMAND_EXECUTABLE_JAVADOC = "javadoc";

  /**
   * Javadoc command arg for just generating public Javadoc.
   */
  public static final String COMMAND_ARG_PUBLIC = "-public";

  /**
   * Javadoc command arg for specifying the subpackages to be covered by Javadoc.
   */
  public static final String COMMAND_ARG_SUBPACKAGES = "-subpackages";

  /**
   * Javadoc command arg for just specifying the source path for the javadoc sources.
   */
  public static final String COMMAND_ARG_SOURCE_PATH = "-sourcepath";

  /**
   * Javadoc command arg for specifying where the generated Javadoc files should go.
   */
  public static final String COMMAND_ARG_DOC_BUILD_FOLDER = "-d";

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  @Override
  public void generate(ProjectTaskContext context) {
    File docBuildFolder = fileSupport.newFile(context.getBuildDirectory(), "docs/html/javadoc");
    fileSupport.directoryExists(docBuildFolder);
    fileSupport.deleteDirectoryContents(docBuildFolder);

    File classesFolder =
        fileSupport.newFile(context.getBuildDirectory(), ProjectJavaCompiler.BUILD_DIRECTORY_CLASSES_MAIN);
    WorkbenchTaskContext workbenchTaskContext = context.getWorkbenchTaskContext();
    if (!classesFolder.exists() || !classesFolder.isDirectory()) {
      workbenchTaskContext.handleError(String.format("Java class files folder %s does not exist",
          classesFolder.getAbsolutePath()));
    }

    File sourcesFolder =
        fileSupport.newFile(context.getProject().getBaseDirectory(), JavaProjectType.SOURCE_MAIN_JAVA);
    if (!sourcesFolder.exists() || !sourcesFolder.isDirectory()) {
      workbenchTaskContext.handleError(String.format("Java source files folder %s does not exist",
          classesFolder.getAbsolutePath()));
    }

    NativeApplicationDescription description = new NativeApplicationDescription();
    description.setExecutablePath(COMMAND_EXECUTABLE_JAVADOC);
    description.addArguments(COMMAND_ARG_DOC_BUILD_FOLDER, docBuildFolder.getAbsolutePath(), COMMAND_ARG_SOURCE_PATH,
        sourcesFolder.getAbsolutePath(), COMMAND_ARG_SUBPACKAGES, getRootSubpackages(classesFolder),
        COMMAND_ARG_PUBLIC);

    workbenchTaskContext.getWorkbench().getLog().info(String.format("Javadoc command is %s", description));

    workbenchTaskContext.getNativeApplicationRunners().runNativeApplicationRunner(description,
        MAX_TIME_FOR_JAVADOC_RUN);

    context.getLog().info(
        String.format("Completed Javadoc build, Javadoc found in %s", docBuildFolder.getAbsolutePath()));
  }

  /**
   * Get the root subpackages into the Javadoc command being built.
   *
   * @param classesFolder
   *          the folder containing the packages
   *
   * @return the subpackages argument for the Javadoc command
   */
  private String getRootSubpackages(File classesFolder) {
    StringBuilder builder = new StringBuilder();

    File[] packageDirs = classesFolder.listFiles();
    if (packageDirs != null) {
      for (File f : packageDirs) {
        if (f.isDirectory()) {
          builder.append(SUBPACKAGE_SEPARATOR).append(f.getName());
        }
      }
    }

    // Strip off the leading subpackage designator.
    return builder.substring(1);
  }
}
