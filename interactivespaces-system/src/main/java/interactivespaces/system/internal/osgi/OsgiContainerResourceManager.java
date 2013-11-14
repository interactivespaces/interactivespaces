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
import com.google.common.collect.Sets;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

/**
 * A container resource manager using OSGi.
 *
 * @author Keith M. Hughes
 */
public class OsgiContainerResourceManager implements ContainerResourceManager, ManagedResource {

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
   * The file support to be used.
   */
  private FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * Construct a new resource manager.
   *
   * @param bundleContext
   *          the OSGi bundle context
   * @param filesystem
   *          the Interactive Spaces container filesystem
   */
  public OsgiContainerResourceManager(BundleContext bundleContext, InteractiveSpacesFilesystem filesystem) {
    this.bundleContext = bundleContext;
    this.filesystem = filesystem;
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
        ContainerResource resource = new ContainerResource(bundle.getSymbolicName(), new Version(version.getMajor(), version
            .getMinor(), version.getMicro(), version.getQualifier()), resourceLocation);
        resources.add(resource);
      }
    }

    return new ContainerResourceCollection(resources);
  }

  @Override
  public void addResource(ContainerResourceLocation location, String name, InputStream resourceStream) {
    File resourceDestination = null;
    switch (location) {
      case SYSTEM_BOOTSTRAP:
        resourceDestination = filesystem.getBootstrapDirectory();
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

    resourceDestination = new File(resourceDestination, name);

    try {
      fileSupport.copyInputStream(resourceStream, resourceDestination);

      // now add to the live container if necessary
      if (location.isImmediateLoad()) {
        bundleContext.installBundle(resourceDestination.toURI().toString());
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
}
