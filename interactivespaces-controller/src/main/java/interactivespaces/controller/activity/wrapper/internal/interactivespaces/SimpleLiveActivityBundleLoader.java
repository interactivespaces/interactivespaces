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

package interactivespaces.controller.activity.wrapper.internal.interactivespaces;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.resource.NamedVersionedResource;
import interactivespaces.resource.Version;
import interactivespaces.util.data.resource.ResourceSignature;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.util.Map;

/**
 * A basic implementation of a {@link LiveActivityBundleLoader}.
 *
 * @author Keith M. Hughes
 */
public class SimpleLiveActivityBundleLoader implements LiveActivityBundleLoader {

  /**
   * The OSGi bundle context used to load bundles.
   */
  private final BundleContext loadingBundleContext;

  /**
   * Compare two bundle with each other.
   */
  private final ResourceSignature bundleSignature;

  /**
   * The bundles currently loaded by the loader, indexed by the esource representing the bundle.
   */
  private final Map<NamedVersionedResource, NativeInteractiveSpacesLiveActivityOsgiBundle> loadedBundles = Maps
      .newHashMap();

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a bundle loader.
   *
   * @param loadingBundleContext
   *          the OSGi bundle context used for loading
   * @param bundleSignature
   *          the bundle signature
   */
  public SimpleLiveActivityBundleLoader(BundleContext loadingBundleContext, ResourceSignature bundleSignature) {
    this.loadingBundleContext = loadingBundleContext;
    this.bundleSignature = bundleSignature;
  }

  @Override
  public synchronized Class<?> getBundleClass(File bundleFile, String bundleName, Version bundleVersion,
      String className) {
    if (!fileSupport.isFile(bundleFile)) {
      throw new SimpleInteractiveSpacesException(String.format("Could not find bundle file %s",
          fileSupport.getAbsolutePath(bundleFile)));
    }

    NamedVersionedResource bundleId = new NamedVersionedResource(bundleName, bundleVersion);

    NativeInteractiveSpacesLiveActivityOsgiBundle liveActivityBundle = loadedBundles.get(bundleId);
    if (liveActivityBundle == null) {
      liveActivityBundle = new NativeInteractiveSpacesLiveActivityOsgiBundle(loadingBundleContext, bundleSignature);
      loadedBundles.put(bundleId, liveActivityBundle);
    }

    Bundle bundle = liveActivityBundle.getBundle(bundleFile);

    try {
      return bundle.loadClass(className);
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not load class %s from bundle", className));
    }
  }

  /**
   * Get the number of bundles known to the loader.
   *
   * @return the number of bundles known to the loader.
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
