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

import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.activity.type.ProjectType;

import java.io.File;
import java.util.List;

/**
 * Useful constants and methods for working with Java projects.
 *
 * @author Keith M. Hughes
 */
public abstract class JavaProjectType implements ProjectType {

  /**
   * Source location for the Java source files.
   */
  public static final String SOURCE_MAIN_JAVA = "src/main/java";

  /**
   * Source location for tests.
   */
  public static final String SOURCE_MAIN_TESTS = "src/test/java";

  /**
   * Get a classpath that would be used at runtime for the project.
   *
   * @param classpath
   *          the classpath list to add to
   * @param extension
   *          any Java extension, can be {@code null}
   * @param workbench
   *          the workbench
   */
  public void getRuntimeClasspath(List<File> classpath, JavaProjectExtension extension,
      InteractiveSpacesWorkbench workbench) {
    classpath.addAll(workbench.getControllerClasspath());

    if (extension != null) {
      extension.addToClasspath(classpath, workbench);
    }
  }

  /**
   * Get a classpath that would be used as part of the project for the project.
   *
   * <p>
   * This includes runtime classes.
   *
   * @param classpath
   *          the classpath to add to
   * @param extension
   *          any Java extension, can be {@code null}
   * @param workbench
   *          the workbench
   */
  public void getProjectClasspath(List<File> classpath, JavaProjectExtension extension,
      InteractiveSpacesWorkbench workbench) {
    getRuntimeClasspath(classpath, extension, workbench);

    for (File file : workbench.getAllWorkbenchBootstrapFiles()) {
      if (file.getName().contains("junit-4")) {
        classpath.add(file);
      }
    }
  }

}
