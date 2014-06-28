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

package interactivespaces.master.resource.deployment.internal;

import interactivespaces.master.resource.deployment.Feature;
import interactivespaces.master.resource.deployment.ResourceDependencyResolver;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

/**
 * A very simple feature bundle resolver.
 *
 * @author Keith M. Hughes
 */
public class ObrFeatureBundleResolver implements ResourceDependencyResolver {

  @Override
  public Set<String> getDependencies(Collection<Feature> features) {
    Set<String> dependencies = Sets.newHashSet();

    // TODO(keith): Eventually this should look inside the bundles and see what
    // they need
    // and figure out the whole graph.
    for (Feature feature : features) {
      dependencies.addAll(feature.getRootBundles());
    }

    return dependencies;
  }

  // /**
  // * @param repositoryAdmin the repositoryAdmin to set
  // */
  // public void setRepositoryAdmin(RepositoryAdmin repositoryAdmin) {
  // this.repositoryAdmin = repositoryAdmin;
  // }
  //
  // /**
  // * @param repositoryAdmin the repositoryAdmin to set
  // */
  // public void unsetRepositoryAdmin(RepositoryAdmin rrepositoryAdmin) {
  // this.repositoryAdmin = null;
  // }

}
