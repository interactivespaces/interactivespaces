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

import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;

import org.apache.commons.logging.Log;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Interface for project constituents.
 *
 * @author Trevor Pering
 */
public interface ProjectConstituent {

  /**
   * Process the needed constituent for the project.
   *
   * @param project
   *          the project being built
   * @param context
   *          project context in which to process the constituent
   */
  void processConstituent(Project project, ProjectContext context);

  /**
   * Factory for project constituent builders.
   *
   * @author Keith M. Hughes
   */
  interface ProjectConstituentBuilderFactory {

    /**
     * Get the type name of this project constituent.
     *
     * @return constituent type name
     */
    String getName();

    /**
     * Create a new builder.
     *
     * @return the new builder
     */
    ProjectConstituentBuilder newBuilder();
  }

  /**
   * Builder interface for creating new constituent instances.
   */
  interface ProjectConstituentBuilder {

    /**
     * Get a new constituent of the appropriate type.
     *
     * @param namespace
     *          XML namespace for all elements
     * @param constituentElement
     *          project file definition element
     * @param project
     *          the project description being built
     *
     * @return new project object or {@code null} if there were errors
     */
    ProjectConstituent buildConstituentFromElement(Namespace namespace, Element constituentElement, Project project);

    /**
     * Set the logging provider for use by the builder.
     *
     * @param log
     *          logging provider to use
     */
    void setLog(Log log);
  }
}
