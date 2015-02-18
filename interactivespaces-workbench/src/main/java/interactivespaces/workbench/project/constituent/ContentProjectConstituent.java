/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.workbench.project.constituent;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;

import java.io.File;

/**
 * A project constituent that gives source content.
 *
 * @author Keith M. Hughes
 */
public interface ContentProjectConstituent extends ProjectConstituent {

  /**
   * Attribute value specifying a source file.
   */
  String SOURCE_FILE_ATTRIBUTE = "sourceFile";

  /**
   * Attribute value specifying a source directory.
   */
  String SOURCE_DIRECTORY_ATTRIBUTE = "sourceDirectory";

  /**
   * Attribute value specifying a destination file.
   */
  String DESTINATION_FILE_ATTRIBUTE = "destinationFile";

  /**
   * Attribute value specifying a destination directory.
   */
  String DESTINATION_DIRECTORY_ATTRIBUTE = "destinationDirectory";

  /**
   * Return the source directory for this constituent.
   *
   * @return source directory
   *
   * @throws InteractiveSpacesException
   *           if constituent type does not provide a source directory
   */
  String getSourceDirectory() throws InteractiveSpacesException;

  /**
   * Process the constituent with a very specific directory.
   *
   * @param project
   *          the project
   * @param stagingDirectory
   *          the staging directory for processing
   * @param context
   *          the task context
   */
  void processConstituent(Project project, File stagingDirectory, ProjectContext context);
}
