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

package interactivespaces.master.resource.deployment;

import java.util.Collection;
import java.util.Set;

/**
 * Take a set of features and figure out all bundles needed.
 *
 * @author Keith M. Hughes
 */
public interface ResourceDependencyResolver {

  /**
   * Take a set of features to deploy and figure out the bundles needed by them.
   *
   * @param features
   *          the features to deploy
   *
   * @return Bundles needed for the features.
   */
  Set<String> getDependencies(Collection<Feature> features);
}
