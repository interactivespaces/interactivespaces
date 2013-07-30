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

package org.ros.osgi.deployment.node.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Bundle activator for this bundle.
 *
 * @author Keith M. Hughes
 */
public class RosOsgiNodeBundleActivator implements BundleActivator {

  @Override
  public void start(BundleContext context) throws Exception {
    // TODO(keith): This needs to start up the deployment client.
    // RosOsgiDeploymentClient
  }

  @Override
  public void stop(BundleContext context) throws Exception {
  }

}
