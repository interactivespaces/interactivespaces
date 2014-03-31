/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.workbench.project.group;

import interactivespaces.workbench.project.Project;

/**
 * A project specification for a meta project that represents the group of projects (therefore meta).
 * This is different from a {@link GroupProject}, in that it doesn't directly encompass the other projects.
 *
 * @author Trevor Pering
 */
public class MetaProject extends Project {

  /**
   * Name of the project type.
   */
  public static final String PROJECT_TYPE_NAME = "meta";
}
