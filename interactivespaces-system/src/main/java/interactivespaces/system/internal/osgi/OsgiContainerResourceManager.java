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

package interactivespaces.system.internal.osgi;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.resource.Version;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.core.container.ContainerFilesystemLayout;
import interactivespaces.system.resources.ContainerResource;
import interactivespaces.system.resources.ContainerResourceCollection;
import interactivespaces.system.resources.ContainerResourceLocation;
import interactivespaces.system.resources.ContainerResourceManager;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.resource.ManagedResource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.wiring.FrameworkWiring;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A container resource manager using OSGi.
 *
 * @author Keith M. Hughes
 */
public class OsgiContainerResourceManager implements ContainerResourceManager, ManagedResource {

  /**
   * Time to wait for a bundle update to take place. In milliseconds.
   */
  public static final int BUNDLE_UPDATER_TIMEOUT = 4000;

  /**
   * What to search for in an OSGi bundle location for being in the system
   * bootstrap folder.
   */
  private static final String SYSTEM_BOOTSTRAP_COMPONENT = "/" + ContainerFilesystemLayout.FOLDER_SYSTEM_BOOTSTRAP
      + "/";

  /**
   * What to search for in an OSGi bundle location for being in the user
   * bootstrap folder.
   */
  private static final String USER_BOOTSTRAP_COMPONENT = "/" + ContainerFilesystemLayout.FOLDER_USER_BOOTSTRAP + "/";

  /**
   * The bundle context the manager is installed in.
   */
  private final BundleContext bundleContext;

  /**
   * The file system for the container.
   */
  private final InteractiveSpacesFilesystem filesystem;

  /**
   * Logger for this manager.
   */
  private final Log log;

  /**
   * The file support to be used.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * A map from bundle IDs to bundle updaters.
   */
  private final Map<Long, BundleUpdater> bundleUpdaters = Maps.newConcurrentMap();

  /**
   * Construct a new resource manager.
   *
   * @param bundleContext
   *          the OSGi bundle context
   * @param filesystem
   *          the Interactive Spaces container filesystem
   * @param log
   *          the log to use
   */
  public OsgiContainerResourceManager(BundleContext bundleContext, InteractiveSpacesFilesystem filesystem, Log log) {
    this.bundleContext = bundleContext;
    this.filesystem = filesystem;
    this.log = log;
  }

  @Override
  public void startup() {
    // Nothing to do
  }

  @Override
  public void shutdown() {
    // Nothing to do
  }

  @Override
  public ContainerResourceCollection getResources() {
    Set<ContainerResource> resources = Sets.newHashSet();

    for (Bundle bundle : bundleContext.getBundles()) {
      org.osgi.framework.Version version = bundle.getVersion();
      String bundleLocation = bundle.getLocation();
      ContainerResourceLocation resourceLocation = null;
      if (bundleLocation.contains(SYSTEM_BOOTSTRAP_COMPONENT)) {
        resourceLocation = ContainerResourceLocation.SYSTEM_BOOTSTRAP;
      } else if (bundleLocation.contains(USER_BOOTSTRAP_COMPONENT)) {
        resourceLocation = ContainerResourceLocation.USER_BOOTSTRAP;
      }

      // Only add if in a monitored area.
      if (resourceLocation != null) {
        ContainerResource resource =
            new ContainerResource(bundle.getSymbolicName(), new Version(version.getMajor(), version.getMinor(),
                version.getMicro(), version.getQualifier()), resourceLocation);
        resources.add(resource);
      }
    }

    return new ContainerResourceCollection(resources);
  }

  @Override
  public void addResource(ContainerResource resource, File resourceFile) {
    log.info(String.format("Adding container resource %s from file %s", resource, resourceFile.getAbsolutePath()));

    File resourceDestination = null;
    ContainerResourceLocation location = resource.getLocation();
    switch (location) {
      case SYSTEM_BOOTSTRAP:
        resourceDestination = filesystem.getSystemBootstrapDirectory();
        break;

      case CONFIG:
        resourceDestination =
            new File(filesystem.getInstallDirectory(), ContainerFilesystemLayout.FOLDER_CONFIG_INTERACTIVESPACES);

        break;
      case LIB_SYSTEM:
        resourceDestination =
            new File(filesystem.getInstallDirectory(), ContainerFilesystemLayout.FOLDER_INTERACTIVESPACES_SYSTEM);
        break;

      case USER_BOOTSTRAP:
        resourceDestination =
            new File(filesystem.getInstallDirectory(), ContainerFilesystemLayout.FOLDER_USER_BOOTSTRAP);
        break;

      case ROOT:
        resourceDestination = filesystem.getInstallDirectory();
        break;

      default:
        throw new SimpleInteractiveSpacesException(String.format("Unsupported container location %s", location));
    }

    Version version = resource.getVersion();
    resourceDestination = new File(resourceDestination, resource.getName() + "-" + version + ".jar");
    String resourceDestinationUri = resourceDestination.toURI().toString();
    try {
      fileSupport.copyFile(resourceFile, resourceDestination);

      // now add to the live container if necessary
      if (location.isImmediateLoad()) {
        Bundle installedBundle = null;
        org.osgi.framework.Version osgiVersion =
            new org.osgi.framework.Version(version.getMajor(), version.getMinor(), version.getMicro(),
                version.getQualifier());
        for (Bundle bundle : bundleContext.getBundles()) {
          if (bundle.getSymbolicName().equals(resource.getName()) && bundle.getVersion().equals(osgiVersion)) {
            installedBundle = bundle;
            break;
          }
        }

        if (installedBundle != null) {
          if (installedBundle.getLocation().endsWith(resourceDestinationUri)) {
            log.info(String.format("Resource %s already was loaded. Updating.", resource));
            if (bundleUpdaters.get(installedBundle.getBundleId()) == null) {
              BundleUpdater updater = new BundleUpdater(installedBundle);
              bundleUpdaters.put(installedBundle.getBundleId(), updater);

              updater.updateBundle();
            }
          } else {
            log.info(String.format(
                "Resource %s already was loaded with another name. Uninstalling old and loading new.", resource));
            installedBundle.uninstall();
            new File(new URI(installedBundle.getLocation())).delete();
            installedBundle = bundleContext.installBundle(resourceDestinationUri);

            installedBundle.start();
          }

        } else {
          log.info(String.format("New Resource %s being loaded into the container", resource));
          installedBundle = bundleContext.installBundle(resourceDestinationUri);

          installedBundle.start();
        }
      }
    } catch (Exception e) {
      throw new InteractiveSpacesException("Could not install new resource", e);
    }
  }

  /**
   * Set the file support to use.
   *
   * @param fileSupport
   *          the file support
   */
  @VisibleForTesting
  void setFileSupport(FileSupport fileSupport) {
    this.fileSupport = fileSupport;
  }

  /**
   * An updater for bundle updates. This is for bundles which are already loaded
   * and being used and that very bundle is being updated.
   *
   * @author Keith M. Hughes
   */
  public class BundleUpdater implements FrameworkListener {

    /**
     * The bundle being updated.
     */
    private final Bundle bundle;

    /**
     * A latch for declaring the update is done.
     */
    private final CountDownLatch doneUpdateLatch = new CountDownLatch(1);

    /**
     * Construct a new updater.
     *
     * @param bundle
     *          the bundle to be updated
     */
    public BundleUpdater(Bundle bundle) {
      this.bundle = bundle;
    }

    /**
     * Start the updating of the bundle.
     */
    public void updateBundle() {
      try {
        bundle.update();

        // The refresh happens in another thread.
        bundleContext.getBundle(0).adapt(FrameworkWiring.class).refreshBundles(Lists.newArrayList(bundle), this);

        try {
          if (!doneUpdateLatch.await(BUNDLE_UPDATER_TIMEOUT, TimeUnit.MILLISECONDS)) {
            throw new SimpleInteractiveSpacesException("Could not update bundle in time");
          }
        } catch (InterruptedException e) {
          // Don't care
        }
      } catch (BundleException e) {
        endUpdate();

        throw new InteractiveSpacesException("Could not update resource", e);
      }
    }

    @Override
    public void frameworkEvent(FrameworkEvent event) {
      endUpdate();
    }

    /**
     * The update has ended, with or without an error.
     */
    private void endUpdate() {
      bundleUpdaters.remove(bundle.getBundleId());

      doneUpdateLatch.countDown();
    }
  }
}
