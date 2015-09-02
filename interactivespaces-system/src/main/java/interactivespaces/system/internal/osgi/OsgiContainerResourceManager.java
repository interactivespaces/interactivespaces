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
import interactivespaces.logging.ExtendedLog;
import interactivespaces.resource.Version;
import interactivespaces.resource.io.ResourceSource;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.core.container.ContainerFilesystemLayout;
import interactivespaces.system.resources.ContainerResource;
import interactivespaces.system.resources.ContainerResourceCollection;
import interactivespaces.system.resources.ContainerResourceLocation;
import interactivespaces.system.resources.ContainerResourceManager;
import interactivespaces.system.resources.ContainerResourceType;
import interactivespaces.util.data.resource.MessageDigestResourceSignatureCalculator;
import interactivespaces.util.data.resource.ResourceSignatureCalculator;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.resource.ManagedResource;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.wiring.FrameworkWiring;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
   * What to search for in an OSGi bundle location for being in the system bootstrap folder.
   */
  private static final String SYSTEM_BOOTSTRAP_COMPONENT = "/" + ContainerFilesystemLayout.FOLDER_SYSTEM_BOOTSTRAP
      + "/";

  /**
   * What to search for in an OSGi bundle location for being in the user bootstrap folder.
   */
  private static final String USER_BOOTSTRAP_COMPONENT = "/" + ContainerFilesystemLayout.FOLDER_USER_BOOTSTRAP + "/";

  /**
   * The bundle context the manager is installed in.
   */
  private final BundleContext bundleContext;

  /**
   * The OSGi service for wiring together bundles.
   */
  private final FrameworkWiring frameworkWiring;

  /**
   * The file system for the container.
   */
  private final InteractiveSpacesFilesystem filesystem;

  /**
   * Folder for configs. Can be {@code null}.
   */
  private final File configFolder;

  /**
   * Logger for this manager.
   */
  private final ExtendedLog log;

  /**
   * A map from bundle IDs to bundle updaters.
   */
  private final Map<Long, BundleUpdater> bundleUpdaters = Maps.newConcurrentMap();

  /**
   * The resource signature calculator.
   */
  private ResourceSignatureCalculator resourceSignatureCalculator = new MessageDigestResourceSignatureCalculator();

  /**
   * The resources in the container.
   */
  private Map<String, ContainerResource> cachedResources;

  /**
   * The file support to use.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new resource manager.
   *
   * @param bundleContext
   *          the OSGi bundle context
   * @param frameworkWiring
   *          the OSGi service for wiring together bundles
   * @param filesystem
   *          the Interactive Spaces container filesystem
   * @param configFolder
   *          the folder for configurations, can be {@code null}
   * @param log
   *          the log to use
   */
  public OsgiContainerResourceManager(BundleContext bundleContext, FrameworkWiring frameworkWiring,
      InteractiveSpacesFilesystem filesystem, File configFolder, ExtendedLog log) {
    this.bundleContext = bundleContext;
    this.frameworkWiring = frameworkWiring;
    this.filesystem = filesystem;
    this.configFolder = configFolder;
    this.log = log;
  }

  @Override
  public void startup() {
    // Can't cache resources in here as cannot know if all bundles have been loaded.
  }

  @Override
  public void shutdown() {
    // Nothing to do.
  }

  @Override
  public synchronized ContainerResourceCollection getResources() {
    loadContainerResources();

    return new ContainerResourceCollection(cachedResources.values());
  }

  @Override
  public synchronized void addResource(ContainerResource incomingResource, ResourceSource resourceSource) {
    log.formatInfo("Adding container resource %s from URI %s", incomingResource, resourceSource.getLocation());

    loadContainerResources();

    ContainerResourceLocation location = incomingResource.getLocation();

    File resourceDestinationFolder = getResourceDestinationFolder(location);

    File resourceDestinationFile =
        fileSupport.newFile(resourceDestinationFolder, incomingResource.getName() + "-"
            + incomingResource.getVersion().toString() + ".jar");
    String resourceDestinationFileUri = resourceDestinationFile.toURI().toString();

    if (location.isImmediateLoad()) {
      Bundle installedBundle = locateBundleForResource(incomingResource);

      if (installedBundle != null) {
        if (installedBundle.getLocation().equals(resourceDestinationFileUri)) {
          updateExactInstalledBundle(incomingResource, resourceSource, resourceDestinationFile, installedBundle);
        } else {
          swapInstalledBundle(incomingResource, resourceSource, resourceDestinationFile, resourceDestinationFileUri,
              installedBundle);
        }

      } else {
        loadNewBundle(incomingResource, resourceSource, resourceDestinationFile, resourceDestinationFileUri);
      }
    } else {
      // TODO(keith): need to copy content
    }
  }

  @Override
  public synchronized Bundle loadAndStartBundle(File bundleFile, ContainerResourceType type)
      throws InteractiveSpacesException {
    if (!fileSupport.isFile(bundleFile)) {
      throw SimpleInteractiveSpacesException.newFormattedException("Could not find bundle file %s of type %s",
          fileSupport.getAbsolutePath(bundleFile), type);
    }

    loadContainerResources();

    String bundleUri = bundleFile.toURI().toString();

    Bundle bundle = null;
    try {
      bundle = bundleContext.installBundle(bundleUri);

      bundle.start();

      addBundleToResources(bundle, type, null);

      return bundle;
    } catch (Throwable e) {
      // if managed to install the bundle then it should be uninstalled since we are in an error.
      if (bundle != null) {
        try {
          bundle.uninstall();
        } catch (BundleException e1) {
          log.formatError(e, "Could not uninstall an OSGi bundle that could not be started: %s of type",
              fileSupport.getAbsolutePath(bundleFile), type);
        }
      }

      throw InteractiveSpacesException.newFormattedException(e, "Cannot load bundle file %s of type %s", type,
          fileSupport.getAbsoluteFile(bundleFile), type);
    }
  }

  /**
   * Add in a bundle to the known resources.
   *
   * @param bundle
   *          the bundle being added
   * @param type
   *          the type of the bundle
   * @param resourceLocation
   *          the location of the bundle in the container, can be {@code null}
   *
   * @throws Exception
   *           was unable to add bundle in
   */
  private void addBundleToResources(Bundle bundle, ContainerResourceType type,
      ContainerResourceLocation resourceLocation) throws Exception {
    org.osgi.framework.Version version = bundle.getVersion();
    String bundleLocation = bundle.getLocation();
    ContainerResource containerResource =
        new ContainerResource(bundle.getSymbolicName(), new Version(version.getMajor(), version.getMinor(),
            version.getMicro(), version.getQualifier()), type, resourceLocation,
            resourceSignatureCalculator.getResourceSignature(getBundleFile(bundle)));
    cachedResources.put(bundleLocation, containerResource);
  }

  @Override
  public synchronized void uninstallBundle(Bundle bundle) {
    loadContainerResources();

    String bundleUri = bundle.getLocation();
    ContainerResource containerResource = cachedResources.remove(bundleUri);
    if (containerResource == null) {
      throw SimpleInteractiveSpacesException.newFormattedException(
          "Attempting to uninstall a bundle that is not installed: %s", bundleUri);
    }

    try {
      bundle.uninstall();
    } catch (BundleException e) {
      throw InteractiveSpacesException.newFormattedException(e, "Cannot unload bundle %s", bundleUri, e);
    }
  }

  /**
   * Load in a bundle into the container for the first time.
   *
   * @param resource
   *          the resource to be loaded
   * @param resourceSource
   *          the resource source
   * @param resourceDestinationFile
   *          the file where the resource will be copied
   * @param resourceDestinationFileUri
   *          the URI of the destination file
   */
  private void loadNewBundle(ContainerResource resource, ResourceSource resourceSource, File resourceDestinationFile,
      String resourceDestinationFileUri) {
    log.formatDebug("New Resource %s being loaded into the container from %s to %s", resource,
        resourceSource.getLocation(), resourceDestinationFileUri);

    // Always copy, it isn't there.
    resourceSource.copyTo(resourceDestinationFile);

    try {
      Bundle installedBundle = bundleContext.installBundle(resourceDestinationFileUri);

      installedBundle.start();

      addNewContainerResource(installedBundle);
    } catch (Throwable e) {
      throw InteractiveSpacesException.newFormattedException(e, "Could not load a new bundle %s from source %s",
          resourceDestinationFileUri, resourceSource.getLocation());
    }
  }

  /**
   * Update the bundle that has the exact same filename as the resource coming in.
   *
   * @param incomingResource
   *          the container resource for the replacement
   * @param resourceSource
   *          the source of the resource
   * @param resourceDestinationFile
   *          the file where the resource will ultimately end up
   * @param installedBundle
   *          the bundle of the exact file that will be replaced
   */
  private void updateExactInstalledBundle(ContainerResource incomingResource, ResourceSource resourceSource,
      File resourceDestinationFile, Bundle installedBundle) {
    log.formatDebug("Resource %s already was loaded. Attempting update.", incomingResource);

    ContainerResource existingResource = getContainerResource(getBundleUri(installedBundle).toString());

    String signatureNew = incomingResource.getSignature();
    if (existingResource.getSignature().equals(signatureNew)) {
      log.formatDebug("Resource %s was not updated, signatures identical.", incomingResource);
      return;
    }

    Collection<Bundle> activitiesDependentOnBundle = getLoadedActivitiesDependentOnBundle(installedBundle);
    if (!activitiesDependentOnBundle.isEmpty()) {
      throw SimpleInteractiveSpacesException.newFormattedException(
          "Cannot update dependency %s, an activity depends on it and the activity is running",
          installedBundle.getLocation());
    }

    resourceSource.copyTo(resourceDestinationFile);

    log.formatDebug("Resource %s update beginning.", incomingResource);
    newBundleUpdater(installedBundle, existingResource, signatureNew).updateBundle();
  }

  /**
   * Get a list of all activity bundles that are dependent on the dependency bundle.
   *
   * @param dependencyBundle
   *          the dependency bundle
   *
   * @return {@code true} if an activity is dependent
   */
  private Collection<Bundle> getLoadedActivitiesDependentOnBundle(Bundle dependencyBundle) {
    List<Bundle> dependentActivities = Lists.newArrayList();

    Collection<Bundle> dependencyClosure = frameworkWiring.getDependencyClosure(Lists.newArrayList(dependencyBundle));
    for (Bundle bundle : dependencyClosure) {
      String bundleUri = bundle.getLocation();
      ContainerResource containerResource = cachedResources.get(bundleUri);
      if (containerResource != null) {
        if (containerResource.getType() == ContainerResourceType.ACTIVITY) {
          dependentActivities.add(bundle);
        }
      } else {
        log.formatWarn("The OSGi container has an untracked bundle at %s", bundleUri);
      }
    }

    return dependentActivities;
  }

  /**
   * Get the bundle URI.
   *
   * @param bundle
   *          the bundle
   *
   * @return the URI
   */
  private URI getBundleUri(Bundle bundle) {
    try {
      return new URI(bundle.getLocation());
    } catch (URISyntaxException e) {
      throw SimpleInteractiveSpacesException.newFormattedException(e, "Could not parse URI for %s",
          bundle.getLocation());
    }
  }

  /**
   * Swap a bundle that was named one way with a new bundle named another way.
   *
   * @param resource
   *          the container resource that is being installed
   * @param resourceSource
   *          the source of the resource
   * @param resourceDestinationFile
   *          the file for the new resource
   * @param resourceDestinationFileUri
   *          the destination URI for the new resource
   * @param installedBundle
   *          the bundle that is being swapped with a new file
   */
  private void swapInstalledBundle(ContainerResource resource, ResourceSource resourceSource,
      File resourceDestinationFile, String resourceDestinationFileUri, Bundle installedBundle) {
    log.formatDebug("Resource %s already was loaded with another name. Uninstalling old and loading new.", resource);

    // No matter what, copy the new bundle without checking the signature since we want it with its new name.
    resourceSource.copyTo(resourceDestinationFile);

    try {
      installedBundle.uninstall();
      fileSupport.delete(getBundleFile(installedBundle));
      installedBundle = bundleContext.installBundle(resourceDestinationFileUri);

      installedBundle.start();
    } catch (Throwable e) {
      throw InteractiveSpacesException.newFormattedException(e, "Could not swap a resource %s", resource);
    }
  }

  /**
   * Get the destination for a resource.
   *
   * @param location
   *          the final location for the resource
   *
   * @return the corresponding file for the location
   *
   * @throws InteractiveSpacesException
   *           no file location was available for the specified location
   */
  private File getResourceDestinationFolder(ContainerResourceLocation location) throws InteractiveSpacesException {
    switch (location) {
      case SYSTEM_BOOTSTRAP:
        return filesystem.getSystemBootstrapDirectory();

      case CONFIG:
        if (configFolder == null) {
          throw new SimpleInteractiveSpacesException("Configurations are not modifiable");
        }

        return fileSupport.newFile(configFolder, ContainerFilesystemLayout.FOLDER_CONFIG_INTERACTIVESPACES);

      case LIB_SYSTEM:
        return fileSupport.newFile(filesystem.getInstallDirectory(),
            ContainerFilesystemLayout.FOLDER_INTERACTIVESPACES_SYSTEM);

      case USER_BOOTSTRAP:
        return fileSupport.newFile(filesystem.getInstallDirectory(), ContainerFilesystemLayout.FOLDER_USER_BOOTSTRAP);

      case ROOT:
        return filesystem.getInstallDirectory();

      default:
        throw SimpleInteractiveSpacesException.newFormattedException("Unsupported container location %s", location);
    }
  }

  /**
   * Locate the bundle for a resource if it exists in the OSGi container.
   *
   * @param resource
   *          the resource
   *
   * @return the bundle for the resource, or {@code null} if no bundles are available that provide the resource
   */
  private Bundle locateBundleForResource(ContainerResource resource) {
    Version version = resource.getVersion();
    org.osgi.framework.Version osgiVersion =
        new org.osgi.framework.Version(version.getMajor(), version.getMinor(), version.getMicro(),
            version.getQualifier());

    for (Bundle bundle : bundleContext.getBundles()) {
      if (bundle.getSymbolicName().equals(resource.getName()) && bundle.getVersion().equals(osgiVersion)) {
        return bundle;
      }
    }

    return null;
  }

  /**
   * Load the container resource cache.
   */
  private void loadContainerResources() {
    if (cachedResources != null) {
      return;
    }

    cachedResources = Maps.newHashMap();

    for (Bundle bundle : bundleContext.getBundles()) {
      addNewContainerResource(bundle);
    }
  }

  /**
   * Add in the container resource for a bundle into the cache.
   *
   * @param bundle
   *          the bundle
   */
  private void addNewContainerResource(Bundle bundle) {
    String bundleLocation = bundle.getLocation();
    ContainerResourceLocation resourceLocation = null;
    if (bundleLocation.contains(SYSTEM_BOOTSTRAP_COMPONENT)) {
      resourceLocation = ContainerResourceLocation.SYSTEM_BOOTSTRAP;
    } else if (bundleLocation.contains(USER_BOOTSTRAP_COMPONENT)) {
      resourceLocation = ContainerResourceLocation.USER_BOOTSTRAP;
    }

    // Only add if in a monitored area.
    if (resourceLocation != null) {
      try {
        org.osgi.framework.Version osgiVersion = bundle.getVersion();
        ContainerResource resource =
            new ContainerResource(bundle.getSymbolicName(), new Version(osgiVersion.getMajor(),
                osgiVersion.getMinor(), osgiVersion.getMicro(), osgiVersion.getQualifier()),
                ContainerResourceType.LIBRARY, resourceLocation,
                resourceSignatureCalculator.getResourceSignature(getBundleFile(bundle)));

        cachedResources.put(bundleLocation, resource);
      } catch (Throwable e) {
        log.formatInfo(e, "Could not create container resource information for %s", bundleLocation);
      }
    }
  }

  /**
   * Get the container resource for the given URI.
   *
   * @param bundleUri
   *          the bundle URI
   *
   * @return the container resource, or {@code null} if no such bundle being tracked
   */
  @VisibleForTesting
  ContainerResource getContainerResource(String bundleUri) {
    return cachedResources.get(bundleUri);
  }

  /**
   * Get the file associated with a bundle.
   *
   * @param installedBundle
   *          the installed bundle
   *
   * @return the file associated with the bundle
   *
   * @throws Exception
   *           something happened while getting the bundle file
   */
  private File getBundleFile(Bundle installedBundle) throws Exception {
    return fileSupport.newFile(getBundleUri(installedBundle));
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
   * Set the source signature calculator to use.
   *
   * @param resourceSignatureCalculator
   *          the calculator
   */
  @VisibleForTesting
  void setResourceSignatureCalculator(ResourceSignatureCalculator resourceSignatureCalculator) {
    this.resourceSignatureCalculator = resourceSignatureCalculator;
  }

  /**
   * Create a bundle updater for a bundle.
   *
   * @param bundle
   *          the bundle whose updater should be gotten
   * @param existingResource
   *          the existing resource
   * @param signatureNew
   *          the signature of the incoming bundle
   *
   * @return the updater
   */
  private BundleUpdater newBundleUpdater(Bundle bundle, ContainerResource existingResource, String signatureNew) {
    BundleUpdater updater = getBundleUpdater(bundle);
    if (updater == null) {
      updater = new BundleUpdater(bundle, existingResource, signatureNew);
      bundleUpdaters.put(bundle.getBundleId(), updater);
    }

    return updater;
  }

  /**
   * Get the bundle updater for a bundle.
   *
   * @param bundle
   *          the bundle
   *
   * @return the updater for the bundle, or {@code null} if none
   */
  @VisibleForTesting
  BundleUpdater getBundleUpdater(Bundle bundle) {
    return bundleUpdaters.get(bundle.getBundleId());
  }

  /**
   * Complete a bundle update.
   *
   * @param bundle
   *          the bundle whose update is complete
   * @param existingResource
   *          the resource that has just been updated
   * @param newSignature
   *          the new signature for the bundle
   */
  private synchronized void
      completeBundleUpdate(Bundle bundle, ContainerResource existingResource, String newSignature) {
    bundleUpdaters.remove(bundle.getBundleId());

    existingResource.setSignature(newSignature);
  }

  /**
   * An updater for bundle updates. This is for bundles which are already loaded and being used and that very bundle is
   * being updated.
   *
   * @author Keith M. Hughes
   */
  public class BundleUpdater implements FrameworkListener {

    /**
     * The bundle being updated.
     */
    private final Bundle bundle;

    /**
     * The existing resource being updated.
     */
    private final ContainerResource existingResource;

    /**
     * The new signature for the bundle.
     */
    private final String newSignature;

    /**
     * A latch for declaring the update is done.
     */
    private final CountDownLatch doneUpdateLatch = new CountDownLatch(1);

    /**
     * An exception found during updating.
     */
    private Throwable throwable;

    /**
     * {@code true} if update() has been called.
     */
    private AtomicBoolean updateStarted = new AtomicBoolean();

    /**
     * Construct a new updater.
     *
     * @param bundle
     *          the bundle to be updated
     * @param existingResource
     *          the existing resource being updated
     * @param newSignature
     *          the new signature for the bundle
     */
    public BundleUpdater(Bundle bundle, ContainerResource existingResource, String newSignature) {
      this.bundle = bundle;
      this.existingResource = existingResource;
      this.newSignature = newSignature;
    }

    /**
     * Start the updating of the bundle.
     */
    public void updateBundle() {
      // If was true already, just return
      if (updateStarted.getAndSet(true)) {
        return;
      }

      try {
        bundle.update();

        // The refresh happens in another thread.
        frameworkWiring.refreshBundles(Lists.newArrayList(bundle), this);

        try {
          if (!doneUpdateLatch.await(BUNDLE_UPDATER_TIMEOUT, TimeUnit.MILLISECONDS)) {
            throw new SimpleInteractiveSpacesException("Could not update bundle in time");
          }
        } catch (InterruptedException e) {
          // Don't care
        }
      } catch (BundleException e) {
        throwable = e;
      }

      if (throwable != null) {
        throw new InteractiveSpacesException("Could not update resource", throwable);
      } else {
        completeBundleUpdate(bundle, existingResource, newSignature);
      }
    }

    @Override
    public void frameworkEvent(FrameworkEvent event) {
      if (event.getType() == FrameworkEvent.ERROR) {
        throwable = event.getThrowable();
      }

      doneUpdateLatch.countDown();
    }
  }
}
