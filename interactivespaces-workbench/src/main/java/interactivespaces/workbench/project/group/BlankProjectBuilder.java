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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.workbench.project.builder.BaseProjectBuilder;
import interactivespaces.workbench.project.builder.ProjectBuildContext;

/**
 * Builder for a blank project. These can not be built so it simply throws an exception.
 *
 * @author Trevor Pering
 */
public class BlankProjectBuilder extends BaseProjectBuilder<BlankProject> {
  @Override
  public boolean build(BlankProject project, ProjectBuildContext context) {
    throw new SimpleInteractiveSpacesException("Can't build blank projects");
  }
}
