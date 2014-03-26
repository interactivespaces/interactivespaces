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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.util.io.Files;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A Javadoc generator that uses an external Javadoc compiler.
 *
 * @author Keith M. Hughes
 */
public class ExternalJavadocGenerator implements JavadocGenerator {

  @Override
  public void generate(ProjectBuildContext context) {
    File docBuildFolder = new File(context.getBuildDirectory(), "docs/html/javadoc");
    Files.directoryExists(docBuildFolder);
    Files.deleteDirectoryContents(docBuildFolder);

    File classesFolder =
        new File(context.getBuildDirectory(), ProjectJavaCompiler.BUILD_DIRECTORY_CLASSES_MAIN);
    if (!classesFolder.exists() || !classesFolder.isDirectory()) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Java class files folder %s does not exist", classesFolder.getAbsolutePath()));
    }

    File sourcesFolder =
        new File(context.getProject().getBaseDirectory(), JavaProjectType.SOURCE_MAIN_JAVA);
    if (!sourcesFolder.exists() || !sourcesFolder.isDirectory()) {
      throw new SimpleInteractiveSpacesException(String.format(
          "Java source files folder %s does not exist", classesFolder.getAbsolutePath()));
    }

    ArrayList<String> command = Lists.newArrayList();
    command.add("javadoc");
    command.add("-d");
    command.add(docBuildFolder.getAbsolutePath());
    command.add("-sourcepath");
    command.add(sourcesFolder.getAbsolutePath());
    command.add("-subpackages");

    getRootSubpackages(classesFolder, command);

    command.add("-public");

    System.out.format("Javadoc command is %s\n", command);

    try {
      Process process = Runtime.getRuntime().exec(command.toArray(new String[0]));

      waitForEnd(process, context);
    } catch (IOException e) {
      context.getWorkbench().handleError("Error while creating project", e);
    }
  }

  /**
   * Get the root subpackages into the Javadoc command being built.
   *
   * @param classesFolder
   *          the folder containing the packages
   * @param command
   *          the Javadoc command being built, this is the subpackages argument
   */
  private void getRootSubpackages(File classesFolder, List<String> command) {
    File[] packageDirs = classesFolder.listFiles();
    if (packageDirs != null) {
      for (File f : packageDirs) {
        if (f.isDirectory()) {
          command.add(f.getName());
        }
      }
    }
  }

  /**
   * Wait for the process to end and get any error codes.
   *
   * @param process
   *          the Javadoc process
   * @param context
   *          the build context
   */
  private void waitForEnd(Process process, ProjectBuildContext context) {
    try {
      int exitValue = process.waitFor();
      if (exitValue != 0) {
        StringBuilder result = new StringBuilder();
        getStreamContents(result, process.getErrorStream(), context);

        System.out.println("Javadoc build process failed");
        System.out.println(result.toString());
      }
    } catch (InterruptedException e) {
      context.getWorkbench().handleError("Error while creating project", e);
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
  private void getStreamContents(StringBuilder result, InputStream stream, ProjectBuildContext context) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        result.append(line);
      }
    } catch (Exception e) {
      context.getWorkbench().handleError("Error while creating project", e);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }
}
