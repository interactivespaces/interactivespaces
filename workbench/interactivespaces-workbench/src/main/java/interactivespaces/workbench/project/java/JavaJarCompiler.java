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

import interactivespaces.workbench.project.ProjectTaskContext;

import java.io.File;

/**
 * A compiler which creates Java jars from a series of source directories.
 *
 * @author Keith M. Hughes
 */
public interface JavaJarCompiler {

  /**
   * Compile Java classes and build a proper JAR file.
   *
   * @param jarDestinationFile
   *          the JAR file to be created
   * @param compilationFolder
   *          the folder where the classes should be compiled into
   * @param extensions
   *          any extensions for the build
   * @param containerInfo
   *          any OSGi info for the build
   * @param context
   *          the context for building a project
   */
  void buildJar(File jarDestinationFile, File compilationFolder, JavaProjectExtension extensions,
      ContainerInfo containerInfo, ProjectTaskContext context);
}
