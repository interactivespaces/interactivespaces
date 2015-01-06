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

package interactivespaces.workbench.project.source;

import interactivespaces.workbench.project.Project;

/**
 * A listener for events from a project file manager.
 *
 * @author Keith M. Hughes
 */
public interface ProjectFileManagerListener {

  /**
   * A new project has been opened.
   *
   * @param project
   *          the new project
   */
  void onNewProject(Project project);

  /**
   * A new source file has been added.
   *
   * @param source
   *          the new sourrce file which has been added
   */
  void onNewSource(Source source);
}
