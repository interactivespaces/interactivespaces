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

package interactivespaces.master.ui.internal.web.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * A shim to make the webapp application context know about the bundle context.
 *
 * @author Keith M. Hughes
 */
public class WebappActivator implements BundleActivator {

  /**
   * Bundle context we are running in.
   */
  private static BundleContext bundleContext;

  /**
   * Get the bundle context.
   *
   * @return
   */
  public static BundleContext getBundleContext() {
    return bundleContext;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    bundleContext = context;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    bundleContext = null;
  }
}
