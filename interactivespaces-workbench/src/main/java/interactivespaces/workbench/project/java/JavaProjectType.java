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
import interactivespaces.resource.NamedVersionedResourceCollection;
import interactivespaces.resource.NamedVersionedResourceWithData;
import interactivespaces.resource.analysis.OsgiResourceAnalyzer;
import interactivespaces.system.core.container.ContainerFilesystemLayout;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.InteractiveSpacesWorkbench;
import interactivespaces.workbench.project.ProjectDependency;
import interactivespaces.workbench.project.activity.type.ProjectType;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

import java.io.File;
import java.util.List;

/**
 * Useful constants and methods for working with Java projects.
 *
 * @author Keith M. Hughes
 */
public abstract class JavaProjectType implements ProjectType {

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Source location for the Java source files.
   */
  public static final String SOURCE_MAIN_JAVA = "src/main/java";

  /**
   * Source location for tests.
   */
  public static final String SOURCE_MAIN_TESTS = "src/test/java";

  /**
   * The extras component for testing support.
   */
  public static final String TESTING_EXTRAS_COMPONENT = "testing";

  /**
   * Get a classpath that would be used at runtime for the project.
   *
   * @param context
   *          the project build context
   * @param classpath
   *          the classpath list to add to
   * @param extension
   *          any Java extension, can be {@code null}
   * @param workbench
   *          the workbench
   */
  public void getRuntimeClasspath(ProjectBuildContext context, List<File> classpath, JavaProjectExtension extension,
      InteractiveSpacesWorkbench workbench) {
    classpath.addAll(workbench.getControllerSystemBootstrapClasspath());

    addDependenciesFromUserBootstrap(context, classpath, workbench);

    if (extension != null) {
      extension.addToClasspath(classpath, context);
    }
  }

  /**
   * Add dependencies to the classpath if they are found in the user bootstrap folder of the controller.
   *
   * @param context
   *          the project build context
   * @param classpath
   *          the classpath list
   * @param workbench
   *          the workbench
   */
  private void addDependenciesFromUserBootstrap(ProjectBuildContext context, List<File> classpath,
      InteractiveSpacesWorkbench workbench) {
    NamedVersionedResourceCollection<NamedVersionedResourceWithData<String>> startupResources =
        new OsgiResourceAnalyzer(workbench.getLog()).getResourceCollection(fileSupport.newFile(
            workbench.getControllerDirectory(), ContainerFilesystemLayout.FOLDER_USER_BOOTSTRAP));
    for (ProjectDependency dependency : context.getProject().getDependencies()) {
      NamedVersionedResourceWithData<String> dependencyProvider =
          startupResources.getResource(dependency.getIdentifyingName(), dependency.getVersionRange());
      if (dependencyProvider != null) {
        classpath.add(fileSupport.newFile(dependencyProvider.getData()));
      } else {
        // TODO(keith): Collect all missing and put into a single exception.
        throw new SimpleInteractiveSpacesException(String.format(
            "Project has listed dependency that isn't available %s:%s", dependency.getIdentifyingName(),
            dependency.getVersionRange()));
      }
    }
  }

  /**
   * Get a classpath that would be used as part of the project for the project.
   *
   * <p>
   * This includes runtime classes.
   *
   * @param context
   *          the project build context
   * @param classpath
   *          the classpath to add to
   * @param extension
   *          any Java extension, can be {@code null}
   * @param workbench
   *          the workbench
   */
  public void getProjectClasspath(ProjectBuildContext context, List<File> classpath, JavaProjectExtension extension,
      InteractiveSpacesWorkbench workbench) {
    getRuntimeClasspath(context, classpath, extension, workbench);

    context.getWorkbench().addExtrasControllerExtensionsClasspath(classpath, TESTING_EXTRAS_COMPONENT);
  }
}
