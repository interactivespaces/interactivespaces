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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.system.resources.ContainerResourceManager;
import interactivespaces.system.resources.ContainerResourceType;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import org.osgi.framework.Bundle;

import java.io.File;
import java.util.Set;

/**
 * A basic implementation of a {@link LiveActivityBundleLoader}.
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityBundleLoader implements LiveActivityBundleLoader {

  /**
   * The container resource manager.
   */
  private final ContainerResourceManager containerResourceManager;

  /**
   * The bundles currently loaded by the loader, indexed by the resource representing the bundle.
   */
  private final Set<Bundle> loadedBundles = Sets.newHashSet();

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a bundle loader.
   *
   * @param containerResourceManager
   *          the manager for container resources
   */
  public StandardLiveActivityBundleLoader(ContainerResourceManager containerResourceManager) {
    this.containerResourceManager = containerResourceManager;
  }

  @Override
  public synchronized Bundle loadLiveActivityBundle(File bundleFile) {
    if (!fileSupport.isFile(bundleFile)) {
      throw SimpleInteractiveSpacesException.newFormattedException(
          "Bundle file %s does not exist", fileSupport.getAbsolutePath(bundleFile));
    }

    Bundle bundle = containerResourceManager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY);

    loadedBundles.add(bundle);

    return bundle;
  }

  @Override
  public void dismissLiveActivityBundle(Bundle activityBundle) {
    if (loadedBundles.remove(activityBundle)) {
      containerResourceManager.uninstallBundle(activityBundle);
    } else {
      throw SimpleInteractiveSpacesException.newFormattedException(
          "Bundle %s was not loaded by the native live activity loader", activityBundle.getLocation());
    }
  }

  /**
   * Get the number of bundles known to the loader.
   *
   * @return the number of bundles known to the loader
   */
  @VisibleForTesting
  int getNumberEntries() {
    return loadedBundles.size();
  }

  /**
   * Set the file support to use.
   *
   * @param fileSupport
   *          the file support to use
   */
  @VisibleForTesting
  void setFileSupport(FileSupport fileSupport) {
    this.fileSupport = fileSupport;
  }
}
