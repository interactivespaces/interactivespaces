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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.liveactivity.runtime.domain.InstalledLiveActivity;
import interactivespaces.liveactivity.runtime.domain.pojo.SimpleInstalledLiveActivity;
import interactivespaces.resource.Version;
import interactivespaces.system.resources.ContainerResourceManager;
import interactivespaces.system.resources.ContainerResourceType;
import interactivespaces.util.data.resource.ResourceSignatureCalculator;
import interactivespaces.util.io.FileSupport;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.io.File;

/**
 * Unit tests for {@link StandardLiveActivityBundleLoader}
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityBundleLoaderTest {
  private ContainerResourceManager containerResourceManager;

  private StandardLiveActivityBundleLoader loader;
  private FileSupport fileSupport;
  private ResourceSignatureCalculator signatureCalculator;

  private InstalledLiveActivity liveActivity;

  @Before
  public void setup() {
    signatureCalculator = mock(ResourceSignatureCalculator.class);
    containerResourceManager = mock(ContainerResourceManager.class);
    fileSupport = mock(FileSupport.class);

    loader = new StandardLiveActivityBundleLoader(containerResourceManager, signatureCalculator);
    loader.setFileSupport(fileSupport);

    liveActivity = new SimpleInstalledLiveActivity();
    String bundleName = "foop";
    Version bundleVersion = new Version(1, 0, 0);

    liveActivity.setIdentifyingName(bundleName);
    liveActivity.setVersion(bundleVersion);
  }

  /**
   * Load an activity bundle.
   */
  @Test
  public void testLoad() throws Exception {

    File bundleFile = new File("foo");

    when(signatureCalculator.getResourceSignature(bundleFile)).thenReturn("signature");

    when(fileSupport.isFile(bundleFile)).thenReturn(true);

    Bundle bundle = mock(Bundle.class);

    when(containerResourceManager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY)).thenReturn(bundle);

    assertEquals(0, loader.getNumberLoadedBundles());

    Bundle loadedBundle = loader.loadLiveActivityBundle(liveActivity, bundleFile);

    assertEquals(bundle, loadedBundle);
    assertEquals(1, loader.getNumberLoadedBundles());
  }

  /**
   * Load an activity bundle then unload it.
   */
  @Test
  public void testLoadAndUnload() throws Exception {
    File bundleFile = new File("foo");

    when(signatureCalculator.getResourceSignature(bundleFile)).thenReturn("signature");

    when(fileSupport.isFile(bundleFile)).thenReturn(true);

    Bundle bundle = mock(Bundle.class);

    when(containerResourceManager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY)).thenReturn(bundle);

    assertEquals(0, loader.getNumberLoadedBundles());

    Bundle loadedBundle = loader.loadLiveActivityBundle(liveActivity, bundleFile);

    assertEquals(bundle, loadedBundle);
    assertEquals(1, loader.getNumberLoadedBundles());

    loader.dismissLiveActivityBundle(liveActivity);

    verify(containerResourceManager, times(1)).uninstallBundle(loadedBundle);
    assertEquals(0, loader.getNumberLoadedBundles());
  }

  /**
   * Load an activity bundle that throws an exception request for install.
   */
  @Test
  public void testLoadExceptionStart() throws Exception {
    File bundleFile = new File("foo");

    when(signatureCalculator.getResourceSignature(bundleFile)).thenReturn("signature");

    when(fileSupport.isFile(bundleFile)).thenReturn(true);

    when(containerResourceManager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY)).thenThrow(
        new InteractiveSpacesException("foo"));

    assertEquals(0, loader.getNumberLoadedBundles());

    try {
      loader.loadLiveActivityBundle(liveActivity, bundleFile);

      fail();
    } catch (Throwable e) {
      assertEquals(0, loader.getNumberLoadedBundles());
    }
  }

  /**
   * Load an activity bundle then loading another bundle for the same activity with the same signature.
   */
  @Test
  public void testLoad2() throws Exception {

    String signature1 = "signature";

    File bundleFile1 = new File("foo");
    when(signatureCalculator.getResourceSignature(bundleFile1)).thenReturn(signature1);
    when(fileSupport.isFile(bundleFile1)).thenReturn(true);

    File bundleFile2 = new File("foo2");
    when(signatureCalculator.getResourceSignature(bundleFile2)).thenReturn(signature1);
    when(fileSupport.isFile(bundleFile2)).thenReturn(true);

    Bundle bundle1 = mock(Bundle.class);
    Bundle bundle2 = mock(Bundle.class);

    when(containerResourceManager.loadAndStartBundle(bundleFile1, ContainerResourceType.ACTIVITY)).thenReturn(bundle1);
    when(containerResourceManager.loadAndStartBundle(bundleFile2, ContainerResourceType.ACTIVITY)).thenReturn(bundle2);

    assertEquals(0, loader.getNumberLoadedBundles());

    Bundle loadedBundle1 = loader.loadLiveActivityBundle(liveActivity, bundleFile1);
    Bundle loadedBundle2 = loader.loadLiveActivityBundle(liveActivity, bundleFile2);

    assertEquals(bundle1, loadedBundle1);
    assertEquals(bundle1, loadedBundle2);
    assertEquals(1, loader.getNumberLoadedBundles());

    verify(containerResourceManager, times(0)).loadAndStartBundle(bundleFile2, ContainerResourceType.ACTIVITY);
  }

  /**
   * Load an activity bundle then loading another bundle for the same activity with the same signature. Then unload. The
   * bundle should not be unloaded at all.
   */
  @Test
  public void testLoad2Unload() throws Exception {

    String signature1 = "signature";

    File bundleFile1 = new File("foo");
    when(signatureCalculator.getResourceSignature(bundleFile1)).thenReturn(signature1);
    when(fileSupport.isFile(bundleFile1)).thenReturn(true);

    File bundleFile2 = new File("foo2");
    when(signatureCalculator.getResourceSignature(bundleFile2)).thenReturn(signature1);
    when(fileSupport.isFile(bundleFile2)).thenReturn(true);

    Bundle bundle1 = mock(Bundle.class);
    Bundle bundle2 = mock(Bundle.class);

    when(containerResourceManager.loadAndStartBundle(bundleFile1, ContainerResourceType.ACTIVITY)).thenReturn(bundle1);
    when(containerResourceManager.loadAndStartBundle(bundleFile2, ContainerResourceType.ACTIVITY)).thenReturn(bundle2);

    assertEquals(0, loader.getNumberLoadedBundles());

    Bundle loadedBundle1 = loader.loadLiveActivityBundle(liveActivity, bundleFile1);
    Bundle loadedBundle2 = loader.loadLiveActivityBundle(liveActivity, bundleFile2);

    assertEquals(bundle1, loadedBundle1);
    assertEquals(bundle1, loadedBundle2);
    assertEquals(1, loader.getNumberLoadedBundles());

    verify(containerResourceManager, times(0)).loadAndStartBundle(bundleFile2, ContainerResourceType.ACTIVITY);

    loader.dismissLiveActivityBundle(liveActivity);
    verify(containerResourceManager, times(0)).uninstallBundle(bundle1);

    loader.dismissLiveActivityBundle(liveActivity);
    verify(containerResourceManager, times(1)).uninstallBundle(bundle1);

  }

  /**
   * Load an activity bundle then loading another bundle for the same activity with the same signature.
   */
  @Test
  public void testLoad2Fail() throws Exception {

    String signature1 = "signature";

    File bundleFile1 = new File("foo");
    when(signatureCalculator.getResourceSignature(bundleFile1)).thenReturn(signature1);
    when(fileSupport.isFile(bundleFile1)).thenReturn(true);

    String signature2 = "signature2";

    File bundleFile2 = new File("foo2");
    when(signatureCalculator.getResourceSignature(bundleFile2)).thenReturn(signature2);
    when(fileSupport.isFile(bundleFile2)).thenReturn(true);

    Bundle bundle1 = mock(Bundle.class);
    Bundle bundle2 = mock(Bundle.class);

    when(containerResourceManager.loadAndStartBundle(bundleFile1, ContainerResourceType.ACTIVITY)).thenReturn(bundle1);
    when(containerResourceManager.loadAndStartBundle(bundleFile2, ContainerResourceType.ACTIVITY)).thenReturn(bundle2);

    assertEquals(0, loader.getNumberLoadedBundles());

    Bundle loadedBundle1 = loader.loadLiveActivityBundle(liveActivity, bundleFile1);

    assertEquals(bundle1, loadedBundle1);
    try {
      loader.loadLiveActivityBundle(liveActivity, bundleFile2);

      fail();
    } catch (Throwable e) {
      // Should end up here
    }

    verify(containerResourceManager, times(0)).loadAndStartBundle(bundleFile2, ContainerResourceType.ACTIVITY);
  }
}
