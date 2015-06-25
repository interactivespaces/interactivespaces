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

package interactivespaces.liveactivity.runtime.activity.wrapper.internal.interactivespaces;

import org.osgi.framework.Bundle;

import java.io.File;

/**
 * Loads classes from bundles.
 *
 * <p>
 * This class makes sure that bundles are loaded and unloaded while trying to make sure the proper class is obtained.
 * This means bundles can be shared as long as they are the same bundle.
 *
 * @author Keith M. Hughes
 */
public interface LiveActivityBundleLoader {

  /**
   * Get the OSGi bundle for a live activity..
   *
   * @param bundleFile
   *          the bundle file
   *
   * @return the OSGi bundle
   */
  Bundle loadLiveActivityBundle(File bundleFile);

  /**
   * Dismiss an activity bundle.
   *
   * @param activityBundle
   *          the activity bundle
   */
  void dismissLiveActivityBundle(Bundle activityBundle);
}
