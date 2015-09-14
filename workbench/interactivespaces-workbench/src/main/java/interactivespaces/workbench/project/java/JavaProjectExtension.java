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

package interactivespaces.workbench.project.java;

import interactivespaces.workbench.project.ProjectTaskContext;

import java.io.File;
import java.util.List;

/**
 * Extensions for adding to a Java project.
 *
 * @author Keith M. Hughes
 */
public interface JavaProjectExtension {

  /**
   * Add new items to the classpath.
   *
   * @param classpath
   *          the classpath so far
   * @param context
   *            the project build context
   */
  void addToClasspath(List<File> classpath, ProjectTaskContext context);

  /**
   * Do any needed post-processing of a jar.
   *
   * @param context
   *          the build context information
   * @param jarFile
   *          the jar file to postprocess
   */
  void postProcessJar(ProjectTaskContext context, File jarFile);
}
