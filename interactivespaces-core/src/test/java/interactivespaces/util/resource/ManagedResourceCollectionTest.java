/*
 * Copyright (C) 2012 Google Inc.
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

package interactivespaces.util.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link ManagedResourceCollection} class.
 *
 * @author Keith M. Hughes
 */
public class ManagedResourceCollectionTest {
	private ManagedResourceCollection resources;

	private Log log;

	@Before
	public void setup() {
		log = Mockito.mock(Log.class);

		resources = new ManagedResourceCollection();

	}

	/**
	 * Test that a clean startup and shutdown works.
	 */
	@Test
	public void testCleanStartup() {
		ManagedResource resource1 = Mockito.mock(ManagedResource.class);
		ManagedResource resource2 = Mockito.mock(ManagedResource.class);

		resources.addResource(resource1);
		resources.addResource(resource2);
		
		resources.startupResources(log);
		resources.shutdownResources(log);
		
		Mockito.verify(resource1, Mockito.times(1)).startup();
		Mockito.verify(resource1, Mockito.times(1)).shutdown();
		Mockito.verify(resource2, Mockito.times(1)).startup();
		Mockito.verify(resource2, Mockito.times(1)).shutdown();
	}

	/**
	 * Test that a broken startup works.
	 */
	@Test
	public void testBrokenStartup() {

		ManagedResource resource1 = Mockito.mock(ManagedResource.class);
		ManagedResource resource2 = Mockito.mock(ManagedResource.class);
		ManagedResource resource3 = Mockito.mock(ManagedResource.class);

		Exception e = new RuntimeException();
		Mockito.doThrow(e).when(resource2).startup();

		resources.addResource(resource1);
		resources.addResource(resource2);
		resources.addResource(resource3);
		
		try {
			resources.startupResources(log);
			
			fail();
		} catch (Exception e1) {
			assertEquals(e, e1.getCause());
		}
		
		Mockito.verify(resource1, Mockito.times(1)).startup();
		Mockito.verify(resource1, Mockito.times(1)).shutdown();
		Mockito.verify(resource2, Mockito.times(1)).startup();
		Mockito.verify(resource2, Mockito.never()).shutdown();
		Mockito.verify(resource3, Mockito.never()).startup();
		Mockito.verify(resource3, Mockito.never()).shutdown();
	}

	/**
	 * Test that a broken shutdown works.
	 */
	@Test
	public void testBrokenShutdown() {
		ManagedResource resource1 = Mockito.mock(ManagedResource.class);
		ManagedResource resource2 = Mockito.mock(ManagedResource.class);
		ManagedResource resource3 = Mockito.mock(ManagedResource.class);

		Exception e = new RuntimeException();
		Mockito.doThrow(e).when(resource2).shutdown();

		resources.addResource(resource1);
		resources.addResource(resource2);
		resources.addResource(resource3);
		
		resources.startupResources(log);
		resources.shutdownResources(log);
		
		Mockito.verify(resource1, Mockito.times(1)).startup();
		Mockito.verify(resource1, Mockito.times(1)).shutdown();
		Mockito.verify(resource2, Mockito.times(1)).startup();
		Mockito.verify(resource2, Mockito.times(1)).shutdown();
		Mockito.verify(resource3, Mockito.times(1)).startup();
		Mockito.verify(resource3, Mockito.times(1)).shutdown();

		Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(),
				Mockito.eq(e));
	}
}
