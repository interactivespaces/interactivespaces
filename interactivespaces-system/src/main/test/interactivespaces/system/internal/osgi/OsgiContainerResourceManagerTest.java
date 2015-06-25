/*
 * Copyright (C) 2015 Google Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import interactivespaces.resource.Version;
import interactivespaces.resource.io.ResourceSource;
import interactivespaces.system.InteractiveSpacesFilesystem;
import interactivespaces.system.resources.ContainerResource;
import interactivespaces.system.resources.ContainerResourceLocation;
import interactivespaces.system.resources.ContainerResourceType;
import interactivespaces.util.data.resource.ResourceSignatureCalculator;
import interactivespaces.util.io.FileSupport;

import com.google.common.collect.Lists;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.wiring.FrameworkWiring;

import java.io.File;
import java.net.URI;

/**
 * Test the {@link OsgiContainerResourceManager}.
 *
 * @author Keith M. Hughes
 */
public class OsgiContainerResourceManagerTest {

  private OsgiContainerResourceManager manager;

  @Mock
  private BundleContext bundleContext;

  @Mock
  private FrameworkWiring frameworkWiring;

  @Mock
  private InteractiveSpacesFilesystem filesystem;

  private File configFile = null;

  @Mock
  private Log log;

  @Mock
  private FileSupport fileSupport;

  @Mock
  private ResourceSignatureCalculator resourceSignatureCalculator;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    manager = new OsgiContainerResourceManager(bundleContext, frameworkWiring, filesystem, configFile, log);
    manager.setFileSupport(fileSupport);
    manager.setResourceSignatureCalculator(resourceSignatureCalculator);
  }

  /**
   * Test deploying when the resource is new.
   */
  @Test
  public void testDeployNew() throws Exception {
    ResourceSource resourceSource = Mockito.mock(ResourceSource.class);

    String resourceName = "foo.bar";
    Version resourceVersion = new Version(1, 2, 3);
    ContainerResource resource =
        new ContainerResource(resourceName, resourceVersion, ContainerResourceType.LIBRARY,
            ContainerResourceLocation.USER_BOOTSTRAP, "signature1");

    Bundle[] bundles = new Bundle[] {};
    Mockito.when(bundleContext.getBundles()).thenReturn(bundles);

    File installFolder = new File("/test/install");
    Mockito.when(filesystem.getInstallDirectory()).thenReturn(installFolder);
    File userBootstrapFolder = new File("/test/install/startup");
    Mockito.when(fileSupport.newFile(installFolder, "startup")).thenReturn(userBootstrapFolder);
    String destinationFilePath = resourceName + "-" + resourceVersion.toString() + ".jar";
    File destinationFile = new File(userBootstrapFolder, destinationFilePath);
    Mockito.when(fileSupport.newFile(userBootstrapFolder, destinationFilePath)).thenReturn(destinationFile);

    Bundle installedBundle = Mockito.mock(Bundle.class);
    Mockito.when(bundleContext.installBundle(destinationFile.toURI().toString())).thenReturn(installedBundle);

    manager.addResource(resource, resourceSource);

    Mockito.verify(resourceSource).copyTo(destinationFile);
    Mockito.verify(installedBundle).start();
  }

  /**
   * Test deploying when the resource is already existing and the signature differs.
   */
  @Test
  public void testDeployExistingReplace() throws Exception {
    installExistingBundle("signatureNew", "signatureOld", 1);
  }

  /**
   * Test deploying when the resource is already existing and the signatures match so nothing is copied or changed.
   */
  @Test
  public void testDeployExistingSignatureMatch() throws Exception {
    installExistingBundle("signature", "signature", 0);
  }

  /**
   * Test the installation replacement of an existing bundle.
   *
   * @param signatureIncoming
   *          the signature of the incoming bundle
   * @param signatureInstalled
   *          the signature of the installed bundle
   * @param timesCalled
   *          how many times the bundle.update() and source copy methods should be called
   *
   * @throws Exception
   *           something bad happened
   */
  private void installExistingBundle(final String signatureIncoming, String signatureInstalled, int timesCalled)
      throws Exception {
    ResourceSource resourceSource = Mockito.mock(ResourceSource.class);

    String resourceName = "foo.bar";
    Version resourceVersion = new Version(1, 2, 3);
    ContainerResource resource =
        new ContainerResource(resourceName, resourceVersion, ContainerResourceType.LIBRARY,
            ContainerResourceLocation.USER_BOOTSTRAP, signatureIncoming);

    File installFolder = new File("/test/install");
    Mockito.when(filesystem.getInstallDirectory()).thenReturn(installFolder);
    File userBootstrapFolder = new File("/test/install/startup");
    Mockito.when(fileSupport.newFile(installFolder, "startup")).thenReturn(userBootstrapFolder);
    String destinationFilePath = resourceName + "-" + resourceVersion.toString() + ".jar";
    File destinationFile = new File(userBootstrapFolder, destinationFilePath);
    Mockito.when(fileSupport.newFile(userBootstrapFolder, destinationFilePath)).thenReturn(destinationFile);

    final Bundle installedBundle = Mockito.mock(Bundle.class);

    // Make sure bundle will be found as belonging to the given resource.
    Mockito.when(installedBundle.getSymbolicName()).thenReturn(resourceName);
    Mockito.when(installedBundle.getVersion()).thenReturn(
        new org.osgi.framework.Version(resourceVersion.getMajor(), resourceVersion.getMinor(), resourceVersion
            .getMicro(), resourceVersion.getQualifier()));
    URI destinationFileUri = destinationFile.toURI();
    Mockito.when(installedBundle.getLocation()).thenReturn(destinationFileUri.toString());
    Mockito.when(fileSupport.newFile(destinationFileUri)).thenReturn(destinationFile);

    Mockito.when(resourceSignatureCalculator.getResourceSignature(destinationFile)).thenReturn(signatureInstalled);

    Bundle[] bundles = new Bundle[] { installedBundle };
    Mockito.when(bundleContext.getBundles()).thenReturn(bundles);

    Mockito.when(bundleContext.installBundle(destinationFileUri.toString())).thenReturn(installedBundle);

    // Have to create the framework event for OSGi so bundle updaters don't hang forever.
    Mockito
        .doAnswer(new Answer<Void>() {
          @Override
          public Void answer(InvocationOnMock invocation) throws Throwable {
            manager.getBundleUpdater(installedBundle).frameworkEvent(
                new FrameworkEvent(FrameworkEvent.PACKAGES_REFRESHED, installedBundle, null));
            return null;
          }
        }).when(frameworkWiring)
        .refreshBundles(Mockito.eq(Lists.newArrayList(installedBundle)), Mockito.any(FrameworkListener.class));

    manager.addResource(resource, resourceSource);

    Mockito.verify(resourceSource, Mockito.times(timesCalled)).copyTo(destinationFile);
    Mockito.verify(installedBundle, Mockito.times(timesCalled)).update();

    Assert.assertEquals(timesCalled != 0 ? signatureIncoming : signatureInstalled,
        manager.getContainerResource(destinationFileUri.toString()).getSignature());
  }

  /**
   * Load an activity bundle then unload it.
   */
  @Test
  public void testLoadAndUnloadActivity() throws Exception {
    File bundleFile = new File("/foo/activity.jar");
    String bundleFileUri = bundleFile.toURI().toString();

    when(fileSupport.isFile(bundleFile)).thenReturn(true);

    Bundle bundle = mock(Bundle.class);

    when(bundleContext.installBundle(bundleFileUri)).thenReturn(bundle);
    when(bundleContext.getBundles()).thenReturn(new Bundle[0]);
    when(bundle.getVersion()).thenReturn(new org.osgi.framework.Version(1, 2, 3));
    when(bundle.getLocation()).thenReturn(bundleFileUri);

    Bundle loadedBundle = manager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY);

    assertEquals(bundle, loadedBundle);

    verify(bundle).start();

    ContainerResource containerResource = manager.getContainerResource(bundleFileUri);
    Assert.assertEquals(ContainerResourceType.ACTIVITY, containerResource.getType());

    manager.uninstallBundle(bundle);

    verify(bundle).uninstall();

    Assert.assertNull(manager.getContainerResource(bundleFileUri));
  }

  /**
   * Load an activity bundle that throws an exception during install.
   */
  @Test
  public void testLoadExceptionInstall() throws Exception {
    File bundleFile = new File("foo");
    String bundleFileUri = bundleFile.toURI().toString();

    when(fileSupport.isFile(bundleFile)).thenReturn(true);

    Bundle bundle = mock(Bundle.class);

    when(bundleContext.installBundle(bundleFileUri)).thenThrow(new RuntimeException());

    try {
      manager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY);

      fail();
    } catch (Throwable e) {
      verify(bundle, times(0)).start();

      Assert.assertNull(manager.getContainerResource(bundleFileUri));
    }
  }

  /**
   * Load an activity bundle that throws an exception during start.
   */
  @Test
  public void testLoadExceptionStart() throws Exception {
    File bundleFile = new File("foo");
    String bundleFileUri = bundleFile.toURI().toString();

    when(fileSupport.isFile(bundleFile)).thenReturn(true);

    Bundle bundle = mock(Bundle.class);

    when(bundleContext.installBundle(bundleFileUri)).thenReturn(bundle);
    doThrow(new BundleException("foo")).when(bundle).start();

    try {
      manager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY);

      fail();
    } catch (Throwable e) {
      Assert.assertNull(manager.getContainerResource(bundleFileUri));
    }
  }

  /**
   * Check loading a file that doesn't exist
   */
  @Test
  public void testFileDoesntExist() throws Exception {
    File bundleFile = new File("foo");
    String bundleFileUri = bundleFile.toURI().toString();

    Bundle bundle = mock(Bundle.class);

    when(bundleContext.installBundle(bundleFileUri)).thenReturn(bundle);

    when(fileSupport.isFile(bundleFile)).thenReturn(false);

    try {
      manager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY);

      fail();
    } catch (Throwable e) {
      // This is the success path
    }
  }
}
