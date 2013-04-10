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

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Unit tests for {@link LiveActivityBundle}.
 * 
 * @author Keith M. Hughes
 */
public class LiveActivityBundleTest {
	private BundleContext bundleContext;

	private BundleSignature bundleSignature;

	private LiveActivityBundle liveActivityBundle;

	@Before
	public void setup() {
		bundleContext = mock(BundleContext.class);
		bundleSignature = mock(BundleSignature.class);

		liveActivityBundle = new LiveActivityBundle(bundleContext,
				bundleSignature);
	}

	/**
	 * Test seeing a bundle for the first time.
	 */
	@Test
	public void testFirstTime() throws Exception {
		File bundleFile = new File("banana");
		String newBundleUri = bundleFile.toURI().toString();

		Bundle bundle = mock(Bundle.class);

		when(bundleContext.installBundle(newBundleUri)).thenReturn(bundle);

		Bundle loadedBundle = liveActivityBundle.getBundle(bundleFile);
		assertEquals(bundle, loadedBundle);

		verify(bundle, times(1)).start();
	}

	/**
	 * Test loading a bundle twice, and the signature stays the same.
	 */
	@Test
	public void testFileTwiceSame() throws Exception {
		File bundleFile = new File("banana");
		String newBundleUri = bundleFile.toURI().toString();

		Bundle bundle = mock(Bundle.class);

		when(bundleContext.installBundle(newBundleUri)).thenReturn(bundle);
		when(bundleSignature.getBundleSignature(bundleFile)).thenReturn("foo");

		Bundle loadedBundle1 = liveActivityBundle.getBundle(bundleFile);
		assertEquals(bundle, loadedBundle1);

		Bundle loadedBundle2 = liveActivityBundle.getBundle(bundleFile);
		assertEquals(bundle, loadedBundle2);

		verify(bundle, times(1)).start();
		verify(bundleContext, times(1)).installBundle(newBundleUri);
	}

	/**
	 * Test loading a bundle twice, and the signature stays the same.
	 */
	@Test
	public void testFileTwiceDifferent() throws Exception {
		File bundleFile = new File("banana");
		String newBundleUri = bundleFile.toURI().toString();

		Bundle bundle1 = mock(Bundle.class);
		Bundle bundle2 = mock(Bundle.class);

		when(bundleContext.installBundle(newBundleUri)).thenReturn(bundle1)
				.thenReturn(bundle2);
		when(bundleSignature.getBundleSignature(bundleFile)).thenReturn("foo1")
				.thenReturn("foo2");

		Bundle loadedBundle1 = liveActivityBundle.getBundle(bundleFile);
		assertEquals(bundle1, loadedBundle1);

		Bundle loadedBundle2 = liveActivityBundle.getBundle(bundleFile);
		assertEquals(bundle2, loadedBundle2);

		verify(bundle1, times(1)).start();
		verify(bundle1, times(1)).uninstall();
		verify(bundle2, times(1)).start();
		verify(bundleContext, times(2)).installBundle(newBundleUri);
	}

	/**
	 * Test loading two files with the same signature.
	 */
	@Test
	public void testTwoFilesSameSignature() throws Exception {
		File bundleFile1 = new File("banana");
		String newBundleUri1 = bundleFile1.toURI().toString();
		
		File bundleFile2 = new File("komquat");
		String newBundleUri2 = bundleFile2.toURI().toString();

		Bundle bundle1 = mock(Bundle.class);
		Bundle bundle2 = mock(Bundle.class);

		when(bundleContext.installBundle(newBundleUri1)).thenReturn(bundle1);
		when(bundleContext.installBundle(newBundleUri2)).thenReturn(bundle2);
		
		when(bundleSignature.getBundleSignature(bundleFile1)).thenReturn("foo1");
		when(bundleSignature.getBundleSignature(bundleFile2)).thenReturn("foo1");

		Bundle loadedBundle1 = liveActivityBundle.getBundle(bundleFile1);
		assertEquals(bundle1, loadedBundle1);

		Bundle loadedBundle2 = liveActivityBundle.getBundle(bundleFile2);
		assertEquals(bundle1, loadedBundle2);

		verify(bundle1, times(1)).start();
		verify(bundle1, never()).uninstall();
		verify(bundle2, never()).start();
		verify(bundleContext, times(1)).installBundle(newBundleUri1);
		verify(bundleContext, never()).installBundle(newBundleUri2);
	}

	/**
	 * Test loading two files with different signatures.
	 */
	@Test
	public void testTwoFilesDifferentSignature() throws Exception {
		File bundleFile1 = new File("banana");
		String newBundleUri1 = bundleFile1.toURI().toString();
		
		File bundleFile2 = new File("komquat");
		String newBundleUri2 = bundleFile2.toURI().toString();

		Bundle bundle1 = mock(Bundle.class);
		Bundle bundle2 = mock(Bundle.class);

		when(bundleContext.installBundle(newBundleUri1)).thenReturn(bundle1);
		when(bundleContext.installBundle(newBundleUri2)).thenReturn(bundle2);
		
		when(bundleSignature.getBundleSignature(bundleFile1)).thenReturn("foo1");
		when(bundleSignature.getBundleSignature(bundleFile2)).thenReturn("foo2");

		Bundle loadedBundle1 = liveActivityBundle.getBundle(bundleFile1);
		assertEquals(bundle1, loadedBundle1);

		Bundle loadedBundle2 = liveActivityBundle.getBundle(bundleFile2);
		assertEquals(bundle2, loadedBundle2);

		verify(bundle1, times(1)).start();
		verify(bundle1, times(1)).uninstall();
		verify(bundle2, times(1)).start();
		verify(bundleContext, times(1)).installBundle(newBundleUri1);
		verify(bundleContext, times(1)).installBundle(newBundleUri2);
	}
}
