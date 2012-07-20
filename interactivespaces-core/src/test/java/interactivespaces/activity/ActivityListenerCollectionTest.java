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

package interactivespaces.activity;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * A series of tests for activity listeners.
 *
 * @author Keith M. Hughes
 */
public class ActivityListenerCollectionTest {
	private Activity activity;
	private ActivityListenerCollection listeners;

	private Log log;

	@Before
	public void setup() {
		log = Mockito.mock(Log.class);
		
		activity = Mockito.mock(Activity.class);
		Mockito.when(activity.getLog()).thenReturn(log);

		listeners = new ActivityListenerCollection(activity);
	}

	/**
	 * Test that a clean startup and shutdown works.
	 */
	@Test
	public void testCleanSend() {
		ActivityListener listener1 = Mockito.mock(ActivityListener.class);
		ActivityListener listener2 = Mockito.mock(ActivityListener.class);

		listeners.addListener(listener1);
		listeners.addListener(listener2);
		
		ActivityStatus oldStatus = Mockito.mock(ActivityStatus.class);
		ActivityStatus newStatus = Mockito.mock(ActivityStatus.class);
		
		listeners.signalActivityStatusChange(oldStatus, newStatus);
		
		Mockito.verify(listener1, Mockito.times(1)).onActivityStatusChange(activity, oldStatus, newStatus);
		Mockito.verify(listener2, Mockito.times(1)).onActivityStatusChange(activity, oldStatus, newStatus);
	}

	/**
	 * Test that a broken startup works.
	 */
	@Test
	public void testBrokenStartup() {

		ActivityListener listener1 = Mockito.mock(ActivityListener.class);
		ActivityListener listener2 = Mockito.mock(ActivityListener.class);
		ActivityListener listener3 = Mockito.mock(ActivityListener.class);
		
		ActivityStatus oldStatus = Mockito.mock(ActivityStatus.class);
		ActivityStatus newStatus = Mockito.mock(ActivityStatus.class);

		Exception e = new RuntimeException();
		Mockito.doThrow(e).when(listener2).onActivityStatusChange(activity, oldStatus, newStatus);

		listeners.addListener(listener1);
		listeners.addListener(listener2);
		listeners.addListener(listener3);
		
		listeners.signalActivityStatusChange(oldStatus, newStatus);
		
		Mockito.verify(listener1, Mockito.times(1)).onActivityStatusChange(activity, oldStatus, newStatus);
		Mockito.verify(listener2, Mockito.times(1)).onActivityStatusChange(activity, oldStatus, newStatus);
		Mockito.verify(listener3, Mockito.times(1)).onActivityStatusChange(activity, oldStatus, newStatus);

		Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(),
				Mockito.eq(e));
	}
}
