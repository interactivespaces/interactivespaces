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
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.resource.NamedVersionedResource;
import interactivespaces.system.resources.ContainerResourceManager;
import interactivespaces.system.resources.ContainerResourceType;
import interactivespaces.util.data.resource.MessageDigestResourceSignatureCalculator;
import interactivespaces.util.data.resource.ResourceSignatureCalculator;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import org.osgi.framework.Bundle;

import java.io.File;
import java.util.Map;

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
   * Compare two bundle with each other.
   */
  private final ResourceSignatureCalculator bundleSignatureCalculator;

  /**
   * The bundles currently loaded by the loader, indexed by the resource representing the bundle.
   */
  private final Map<NamedVersionedResource, LiveActivityBundle> loadedBundles = Maps.newHashMap();

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
    this(containerResourceManager, new MessageDigestResourceSignatureCalculator());
  }

  /**
   * Construct a bundle loader.
   *
   * @param containerResourceManager
   *          the manager for container resources
   * @param bundleSignatureCalculator
   *          the bundle signature calculator
   */
  StandardLiveActivityBundleLoader(ContainerResourceManager containerResourceManager,
      ResourceSignatureCalculator bundleSignatureCalculator) {
    this.containerResourceManager = containerResourceManager;
    this.bundleSignatureCalculator = bundleSignatureCalculator;
  }

  @Override
  public synchronized Bundle loadLiveActivityBundle(InstalledLiveActivity liveActivity, File bundleFile) {
    if (!fileSupport.isFile(bundleFile)) {
      throw SimpleInteractiveSpacesException.newFormattedException("Bundle file %s does not exist",
          fileSupport.getAbsolutePath(bundleFile));
    }

    NamedVersionedResource bundleId = getLiveActivityBundleId(liveActivity);
    LiveActivityBundle bundleInfo = loadedBundles.get(bundleId);
    if (bundleInfo == null) {
      // This live activity has never been loaded before.

      String signature = bundleSignatureCalculator.getResourceSignature(bundleFile);
      Bundle bundle = containerResourceManager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY);

      bundleInfo = new LiveActivityBundle(bundle, signature);
      loadedBundles.put(bundleId, bundleInfo);
    } else {
      // This live activity has been loaded before.

      // Check the new bundle file's signature to see if it matches the old bundle file.
      // if they don't match, we must fail since otherwise the container would have to have two bundles with the
      // same name and version and that isn't allowed.
      String newFileSignature = bundleSignatureCalculator.getResourceSignature(bundleFile);
      if (!newFileSignature.equals(bundleInfo.getSignature())) {
        throw SimpleInteractiveSpacesException.newFormattedException(
            "The live activity bundle file %s cannot be loaded since it is different than a "
                + "bundle file already loaded by the same identifying name/version", bundleFile.getAbsolutePath());
      }

      bundleInfo.incrementUsage();
    }

    return bundleInfo.getBundle();
  }

  @Override
  public synchronized void dismissLiveActivityBundle(InstalledLiveActivity liveActivity) {
    NamedVersionedResource bundleId = getLiveActivityBundleId(liveActivity);
    LiveActivityBundle bundleInfo = loadedBundles.get(bundleId);
    if (bundleInfo != null) {
      if (bundleInfo.decrementUsage()) {
        loadedBundles.remove(bundleId);

        containerResourceManager.uninstallBundle(bundleInfo.getBundle());
      }
    } else {
      throw SimpleInteractiveSpacesException.newFormattedException(
          "Bundle for live activity %s was not loaded by the native live activity loader",
          liveActivity.getDisplayName());
    }
  }

  /**
   * Get the bundle ID for the live activity.
   *
   * @param liveActivity
   *          the live activity
   *
   * @return the bundle ID
   */
  private NamedVersionedResource getLiveActivityBundleId(InstalledLiveActivity liveActivity) {
    return new NamedVersionedResource(liveActivity.getIdentifyingName(), liveActivity.getVersion());
  }

  /**
   * Get the number of bundles known to the loader.
   *
   * @return the number of bundles known to the loader
   */
  @VisibleForTesting
  int getNumberLoadedBundles() {
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

  /**
   * The bundle information for a live activity bundle.
   *
   * @author Keith M. Hughes
   */
  public static class LiveActivityBundle {

    /**
     * The current usage count for the bundle.
     */
    private int usageCount = 1;

    /**
     * The bundle for the live activity.
     */
    private Bundle bundle;

    /**
     * The signature of the original bundle file.
     */
    private String signature;

    /**
     * construct a new bundle info.
     *
     * @param bundle
     *          the bundle
     * @param signature
     *          the signature of its source file
     */
    public LiveActivityBundle(Bundle bundle, String signature) {
      this.bundle = bundle;
      this.signature = signature;
    }

    /**
     * Increment the usage count for the bundle.
     */
    public void incrementUsage() {
      usageCount++;
    }

    /**
     * Decrement the usage count on the bundle.
     *
     * @return {@code true} if no one is using the bundle any longer.
     */
    public boolean decrementUsage() {
      return (--usageCount <= 0);
    }

    /**
     * Get the bundle.
     *
     * @return the bundle
     */
    public Bundle getBundle() {
      return bundle;
    }

    /**
     * Get the signature for the original bundle file.
     *
     * @return the signature for the original bundle file
     */
    public String getSignature() {
      return signature;
    }

  }
}
