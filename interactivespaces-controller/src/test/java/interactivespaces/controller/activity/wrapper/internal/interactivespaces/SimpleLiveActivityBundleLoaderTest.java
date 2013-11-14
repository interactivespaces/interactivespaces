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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import interactivespaces.activity.Activity;
import interactivespaces.resource.Version;
import interactivespaces.util.data.resource.ResourceSignature;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;

/**
 * Unit tests for {@link SimpleLiveActivityBundleLoader}
 *
 * @author Keith M. Hughes
 */
public class SimpleLiveActivityBundleLoaderTest {
  private BundleContext bundleContext;

  private ResourceSignature bundleSignature;

  private SimpleLiveActivityBundleLoader loader;

  @Before
  public void setup() {
    bundleContext = mock(BundleContext.class);
    bundleSignature = mock(ResourceSignature.class);

    loader = new SimpleLiveActivityBundleLoader(bundleContext, bundleSignature);
  }

  /**
   * Load a name/version pair for the first time.
   */
  @Test
  public void testOneLoad() throws Exception {
    File bundleFile = new File("foo");
    String bundleFileUri = bundleFile.toURI().toString();

    String bundleName = "foo";
    Version bundleVersion = new Version(1, 0, 0);
    String className = "Foop";

    Bundle bundle = mock(Bundle.class);
    Class<?> expectedClass = Activity.class;

    when(bundleContext.installBundle(bundleFileUri)).thenReturn(bundle);
    when(bundle.loadClass(className)).thenReturn(expectedClass);

    assertEquals(0, loader.getNumberEntries());

    Class<?> clazz = loader.getBundleClass(bundleFile, bundleName, bundleVersion, className);

    assertEquals(expectedClass, clazz);
    assertEquals(1, loader.getNumberEntries());
  }

  /**
   * Load two different name/version pair1 for the first time.
   */
  @Test
  public void testTwoLoad() throws Exception {
    File bundleFile1 = new File("foo");
    String bundleFileUri1 = bundleFile1.toURI().toString();

    String bundleName1 = "foo";
    Version bundleVersion1 = new Version(1, 0, 0);
    String className1 = "Foop";

    Bundle bundle1 = mock(Bundle.class);
    Class<?> expectedClass1 = Activity.class;

    when(bundleContext.installBundle(bundleFileUri1)).thenReturn(bundle1);
    when(bundle1.loadClass(className1)).thenReturn(expectedClass1);

    File bundleFile2 = new File("komquat");
    String bundleFileUri2 = bundleFile2.toURI().toString();

    String bundleName2 = "komquat";
    Version bundleVersion2 = new Version(1, 0, 0);
    String className2 = "Komquat";

    Bundle bundle2 = mock(Bundle.class);
    Class<?> expectedClass2 = Integer.class;

    when(bundleContext.installBundle(bundleFileUri2)).thenReturn(bundle2);
    when(bundle2.loadClass(className2)).thenReturn(expectedClass2);

    assertEquals(0, loader.getNumberEntries());

    Class<?> clazz1 = loader.getBundleClass(bundleFile1, bundleName1, bundleVersion1, className1);
    Class<?> clazz2 = loader.getBundleClass(bundleFile2, bundleName2, bundleVersion2, className2);

    assertEquals(expectedClass1, clazz1);
    assertEquals(expectedClass2, clazz2);
    assertEquals(2, loader.getNumberEntries());
  }

  /**
   * Load a name/version pair with the same file twice in a row.
   */
  @Test
  public void testTwoLoadSameFile() throws Exception {
    File bundleFile = new File("foo");
    String bundleFileUri = bundleFile.toURI().toString();

    String bundleName = "foo";
    Version bundleVersion = new Version(1, 0, 0);
    String className = "Foop";

    Bundle bundle = mock(Bundle.class);
    Class<?> expectedClass = Activity.class;

    when(bundleSignature.getBundleSignature(bundleFile)).thenReturn("foo");

    when(bundleContext.installBundle(bundleFileUri)).thenReturn(bundle);
    when(bundle.loadClass(className)).thenReturn(expectedClass);

    assertEquals(0, loader.getNumberEntries());

    Class<?> clazz1 = loader.getBundleClass(bundleFile, bundleName, bundleVersion, className);
    Class<?> clazz2 = loader.getBundleClass(bundleFile, bundleName, bundleVersion, className);

    assertEquals(expectedClass, clazz1);
    assertEquals(expectedClass, clazz2);

    assertEquals(1, loader.getNumberEntries());

    verify(bundleContext, times(1)).installBundle(bundleFileUri);
    verify(bundle, times(2)).loadClass(className);
  }

  /**
   * Load a name/version pair with the same file twice in a row but the
   * signature changes.
   */
  @Test
  public void testTwoLoadSignatureDifferent() throws Exception {
    File bundleFile = new File("foo");
    String bundleFileUri = bundleFile.toURI().toString();

    String bundleName = "foo";
    Version bundleVersion = new Version(1, 0, 0);
    String className = "Foop";

    Bundle bundle1 = mock(Bundle.class);
    Bundle bundle2 = mock(Bundle.class);
    Class<?> expectedClass1 = Activity.class;
    Class<?> expectedClass2 = Integer.class;

    when(bundleSignature.getBundleSignature(bundleFile)).thenReturn("foo1").thenReturn("foo2");

    when(bundleContext.installBundle(bundleFileUri)).thenReturn(bundle1).thenReturn(bundle2);
    when(bundle1.loadClass(className)).thenReturn(expectedClass1);
    when(bundle2.loadClass(className)).thenReturn(expectedClass2);

    assertEquals(0, loader.getNumberEntries());

    Class<?> clazz1 = loader.getBundleClass(bundleFile, bundleName, bundleVersion, className);
    Class<?> clazz2 = loader.getBundleClass(bundleFile, bundleName, bundleVersion, className);

    assertEquals(expectedClass1, clazz1);
    assertEquals(expectedClass2, clazz2);

    assertEquals(1, loader.getNumberEntries());

    verify(bundleContext, times(2)).installBundle(bundleFileUri);
    verify(bundle1, times(1)).loadClass(className);
    verify(bundle2, times(1)).loadClass(className);
  }

  /**
   * Load a name/version pair with different files, but the signature is the
   * same.
   */
  @Test
  public void testTwoLoadSignatureSame() throws Exception {
    File bundleFile1 = new File("foo");
    String bundleFileUri1 = bundleFile1.toURI().toString();

    File bundleFile2 = new File("komquat");
    String bundleFileUri2 = bundleFile2.toURI().toString();

    String bundleName = "foo";
    Version bundleVersion = new Version(1, 0, 0);
    String className = "Foop";

    Bundle bundle1 = mock(Bundle.class);
    Bundle bundle2 = mock(Bundle.class);
    Class<?> expectedClass1 = Activity.class;
    Class<?> expectedClass2 = Integer.class;

    when(bundleSignature.getBundleSignature(bundleFile1)).thenReturn("foo1");
    when(bundleSignature.getBundleSignature(bundleFile2)).thenReturn("foo1");

    when(bundleContext.installBundle(bundleFileUri1)).thenReturn(bundle1);
    when(bundleContext.installBundle(bundleFileUri2)).thenReturn(bundle2);

    when(bundle1.loadClass(className)).thenReturn(expectedClass1);
    when(bundle2.loadClass(className)).thenReturn(expectedClass2);

    assertEquals(0, loader.getNumberEntries());

    Class<?> clazz1 = loader.getBundleClass(bundleFile1, bundleName, bundleVersion, className);
    Class<?> clazz2 = loader.getBundleClass(bundleFile2, bundleName, bundleVersion, className);

    assertEquals(expectedClass1, clazz1);
    assertEquals(expectedClass1, clazz2);

    assertEquals(1, loader.getNumberEntries());

    verify(bundleContext, times(1)).installBundle(bundleFileUri1);
    verify(bundleContext, never()).installBundle(bundleFileUri2);
    verify(bundle1, times(2)).loadClass(className);
    verify(bundle2, never()).loadClass(className);
  }
}
