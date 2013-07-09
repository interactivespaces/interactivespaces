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

import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.util.data.resource.ResourceSignature;

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
  private BundleContext loadingBundleContext;

  /**
   * Compare two bundle with each other
   */
  private ResourceSignature bundleSignature;

  /**
   * The bundles currently loaded by the loader, indexed by the bundle IS
   */
  private Map<BundleId, LiveActivityBundle> loadedBundles = Maps.newHashMap();

  public SimpleLiveActivityBundleLoader(BundleContext loadingBundleContext,
      ResourceSignature bundleSignature) {
    this.loadingBundleContext = loadingBundleContext;
    this.bundleSignature = bundleSignature;
  }

  @Override
  public synchronized Class<?> getBundleClass(File bundleFile, String bundleName,
      String bundleVersion, String className) {
    BundleId bundleId = new BundleId(bundleName, bundleVersion);

    LiveActivityBundle labundle = loadedBundles.get(bundleId);
    if (labundle == null) {
      labundle = new LiveActivityBundle(loadingBundleContext, bundleSignature);
      loadedBundles.put(bundleId, labundle);
    }

    Bundle bundle = labundle.getBundle(bundleFile);

    try {
      return bundle.loadClass(className);
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not load class %s from bundle",
          className));
    }
  }

  /**
   * Get the number of bundles known to the loader.
   *
   * @return the number of bundles known to the loader.
   */
  int getNumberEntries() {
    return loadedBundles.size();
  }

  public static class BundleId {

    /**
     * The name of the bundle, the symbolic name in OSGi parlance.
     */
    private String name;

    /**
     * The version of the bundle.
     */
    private String version;

    public BundleId(String name, String version) {
      this.name = name;
      this.version = version;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + name.hashCode();
      result = prime * result + version.hashCode();

      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      BundleId other = (BundleId) obj;
      if (!name.equals(other.name))
        return false;
      return version.equals(other.version);
    }
  }
}
