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

package interactivespaces.workbench.project.constituent;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.builder.ProjectBuildContext;
import org.jdom.Element;

import java.io.File;
import java.util.List;

/**
 * Interface for project constituents.
 *
 * @author peringknife@google.com (Trevor Pering)
 */
public interface ProjectConstituent {

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
   * Builder interface for creating new constituent instances.
   */
  interface Builder {

    /**
     * Get a new constituent of the appropriate type.
     *
     * @param constituentElement
     *          project file definition element
     * @param errors
     *          error accumulator
     *
     * @return  new project object
     */
    ProjectConstituent buildConstituentFromElement(Element constituentElement, List<String> errors);
  }

  /**
   * Process the needed constituent for the project.
   *
   * @param project
   *          the project being built
   * @param stagingDirectory
   *          where the items will be copied
   * @param context
 *          context for the build
   */
  void processConstituent(Project project, File stagingDirectory, ProjectBuildContext context);

  /**
   * Return the source directory for this constituent.
   *
   * @return source directory
   *
   * @throws SimpleInteractiveSpacesException
   *           if constituent type does not provide a source directory
   */
  String getSourceDirectory() throws SimpleInteractiveSpacesException;
}
