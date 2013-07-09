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
import interactivespaces.util.data.resource.ResourceSignature;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;

/**
 * The bundle to be used for a live activity.
 *
 * @author Keith M. Hughes
 */
public class LiveActivityBundle {

  /**
   * The context to be used for loading the bundle.
   */
  private BundleContext bundleContext;

  /**
   * The bundle containing the current executable for the live activity.
   */
  private File currentBundleFile;

  /**
   * The current OSGi bundle containing the activity
   */
  private Bundle currentBundle;

  /**
   * Signature for the current bundle.
   */
  private String currentBundleSignature;

  /**
   * For comparing bundle files.
   */
  private ResourceSignature bundleSignature;

  public LiveActivityBundle(BundleContext bundleContext, ResourceSignature bundleSignature) {
    this.bundleContext = bundleContext;
    this.bundleSignature = bundleSignature;
  }

  /**
   * Get the bundle for a given bundle file.
   *
   * @param bundleFile
   *          the file containing the bundle
   *
   * @return the bundle
   */
  public Bundle getBundle(File bundleFile) {
    String newBundleSignature = bundleSignature.getBundleSignature(bundleFile);

    if (currentBundle != null) {
      if (!newBundleSignature.equals(currentBundleSignature)) {
        // It is a totally different bundle, so time to clean out the
        // old one.
        unloadCurrentBundle();
        loadNewBundle(bundleFile, newBundleSignature);
      }
    } else {
      // Never been a bundle loaded
      loadNewBundle(bundleFile, newBundleSignature);
    }

    return currentBundle;
  }

  /**
   * Load in a new bundle into the container.
   *
   * @param newBundleFile
   *          the new bundle file
   * @param newBundleFileSignature
   *          signature of the new bundle file
   */
  private void loadNewBundle(File newBundleFile, String newBundleFileSignature) {
    try {
      String newBundleUri = newBundleFile.toURI().toString();

      Bundle newBundle = bundleContext.installBundle(newBundleUri);

      newBundle.start();

      currentBundle = newBundle;
      currentBundleFile = newBundleFile;
      currentBundleSignature = newBundleFileSignature;
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Cannot load bundle %s",
          newBundleFile.getAbsolutePath()), e);
    }
  }

  /**
   * Unload the current bundle from the container.
   */
  private void unloadCurrentBundle() {
    try {
      currentBundle.uninstall();
      currentBundle = null;
      currentBundleFile = null;
    } catch (Exception e) {
      throw new InteractiveSpacesException(String.format("Could not unload bundle at %s",
          currentBundle.getLocation()), e);
    }
  }
}
