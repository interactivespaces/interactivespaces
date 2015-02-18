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

package interactivespaces.workbench.project.constituent;

import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.workbench.project.Project;
import interactivespaces.workbench.project.ProjectContext;

import java.util.Map;

/**
 * Abstract class for all constituents than can be used in project containers. This class primarily exists for creating
 * output files from templates, to ensure that some methods are always defined (even if they return null).
 *
 * @author Trevor Pering
 */
public abstract class BaseProjectConstituent implements ProjectConstituent {

  /**
   * File support instance for file operations.
   */
  protected final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Get attributes that can be used for reconstructing the constituent.
   *
   * @return the attributes, or {@code null} if none
   */
  public Map<String, String> getAttributeMap() {
    return null;
  }

  @Override
  public void processConstituent(Project project, ProjectContext context) {
    // Default is do nothing.
  }
}
