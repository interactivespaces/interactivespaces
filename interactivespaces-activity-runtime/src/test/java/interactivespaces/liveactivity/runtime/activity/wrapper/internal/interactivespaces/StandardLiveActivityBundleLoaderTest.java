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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.system.resources.ContainerResourceManager;
import interactivespaces.system.resources.ContainerResourceType;
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

  @Before
  public void setup() {
    containerResourceManager = mock(ContainerResourceManager.class);
    fileSupport = mock(FileSupport.class);

    loader = new StandardLiveActivityBundleLoader(containerResourceManager);
    loader.setFileSupport(fileSupport);
  }

  /**
   * Load an activity bundle.
   */
  @Test
  public void testLoad() throws Exception {
    File bundleFile = new File("foo");

    when(fileSupport.isFile(bundleFile)).thenReturn(true);

    Bundle bundle = mock(Bundle.class);

    when(containerResourceManager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY)).thenReturn(bundle);

    assertEquals(0, loader.getNumberEntries());

    Bundle loadedBundle = loader.loadLiveActivityBundle(bundleFile);

    assertEquals(bundle, loadedBundle);
    assertEquals(1, loader.getNumberEntries());
  }

  /**
   * Load an activity bundle then unload it.
   */
  @Test
  public void testLoadAndUnload() throws Exception {
    File bundleFile = new File("foo");

    when(fileSupport.isFile(bundleFile)).thenReturn(true);

    Bundle bundle = mock(Bundle.class);

    when(containerResourceManager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY)).thenReturn(bundle);

    assertEquals(0, loader.getNumberEntries());

    Bundle loadedBundle = loader.loadLiveActivityBundle(bundleFile);

    assertEquals(bundle, loadedBundle);
    assertEquals(1, loader.getNumberEntries());

    loader.dismissLiveActivityBundle(bundle);

    assertEquals(0, loader.getNumberEntries());
  }

  /**
   * Load an activity bundle that throws an exception request for install.
   */
  @Test
  public void testLoadExceptionStart() throws Exception {
    File bundleFile = new File("foo");

    when(fileSupport.isFile(bundleFile)).thenReturn(true);

    when(containerResourceManager.loadAndStartBundle(bundleFile, ContainerResourceType.ACTIVITY)).thenThrow(
        new InteractiveSpacesException("foo"));

    assertEquals(0, loader.getNumberEntries());

    try {
      loader.loadLiveActivityBundle(bundleFile);
    } catch (Throwable e) {
      assertEquals(0, loader.getNumberEntries());
    }
  }
}
