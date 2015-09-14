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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A feature for a ROS node.
 *
 * @author Keith M. Hughes
 */
public class SimpleFeature implements Feature {

  /**
   * ID of the feature.
   */
  private String id;

  /**
   * Bundles needed by the feature.
   */
  private List<String> bundles = new ArrayList<String>();

  private List<String> bundlesToGive;

  public SimpleFeature(String id) {
    this.id = id;

    bundles = new ArrayList<String>();
    bundlesToGive = Collections.unmodifiableList(bundles);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public List<String> getRootBundles() {
    return bundlesToGive;
  }

  /**
   * Add a new root bundle to the feature.
   *
   * @param bundleName
   *          Name of the new root bundle.
   */
  public void addRootBundle(String bundleName) {
    bundles.add(bundleName);
  }

}
