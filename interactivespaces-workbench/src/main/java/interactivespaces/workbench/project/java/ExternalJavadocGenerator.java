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
import interactivespaces.workbench.project.ProjectTaskContext;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * A Javadoc generator that uses an external Javadoc compiler.
 *
 * @author Keith M. Hughes
 */
public class ExternalJavadocGenerator implements JavadocGenerator {

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
    if (!classesFolder.exists() || !classesFolder.isDirectory()) {
      context.getWorkbenchTaskContext().handleError(
          String.format("Java class files folder %s does not exist", classesFolder.getAbsolutePath()));
    }

    File sourcesFolder = fileSupport.newFile(context.getProject().getBaseDirectory(), JavaProjectType.SOURCE_MAIN_JAVA);
    if (!sourcesFolder.exists() || !sourcesFolder.isDirectory()) {
      context.getWorkbenchTaskContext().handleError(
          String.format("Java source files folder %s does not exist", classesFolder.getAbsolutePath()));
    }

    List<String> command = Lists.newArrayList();
    command.add("javadoc");
    command.add("-d");
    command.add(docBuildFolder.getAbsolutePath());
    command.add("-sourcepath");
    command.add(sourcesFolder.getAbsolutePath());

    command.add("-subpackages");
    command.add(getRootSubpackages(classesFolder));

    command.add("-public");

    context.getWorkbenchTaskContext().getWorkbench().getLog().info(String.format("Javadoc command is %s", command));

    try {
      // TODO(keith): Move over to using IS process commands.
      Process process = Runtime.getRuntime().exec(command.toArray(new String[0]));

      waitForEnd(process, context);
    } catch (IOException e) {
      context.getWorkbenchTaskContext().handleError("Error while creating project", e);
    }
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
          builder.append(":").append(f.getName());
        }
      }
    }

    // Strip off the leading :
    return builder.substring(1);
  }

  /**
   * Wait for the process to end and get any error codes.
   *
   * @param process
   *          the Javadoc process
   * @param context
   *          the build context
   */
  private void waitForEnd(Process process, ProjectTaskContext context) {
    try {
      int exitValue = process.waitFor();
      if (exitValue != 0) {
        StringBuilder result = new StringBuilder();
        getStreamContents(result, process.getErrorStream(), context);

        context.getWorkbenchTaskContext().getWorkbench().getLog().error("Javadoc build process failed");
        context.getWorkbenchTaskContext().getWorkbench().getLog().error(result.toString());
      }
    } catch (InterruptedException e) {
      context.getWorkbenchTaskContext().handleError("Error while creating project", e);
    }
  }

  /**
   * Get the stream contents.
   *
   * @param result
   *          where the contents will be written
   * @param stream
   *          the stream to read the contents from
   * @param context
   *          project build context
   */
  private void getStreamContents(StringBuilder result, InputStream stream, ProjectTaskContext context) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        result.append(line);
      }
    } catch (Exception e) {
      context.getWorkbenchTaskContext().handleError("Error while creating project", e);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }
}
