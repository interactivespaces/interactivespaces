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

package org.ros.zeroconf.common.selector;

//import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.ros.exception.RosRuntimeException;
import org.ros.zeroconf.common.ZeroconfRosMasterInfo;

/**
 * tests for the {@link ZeroconfServiceSelector}.
 * 
 * @author Keith M. Hughes
 */
public class LightweightZeroconfServiceSelectorTest {
	private LightweightZeroconfServiceSelector<ZeroconfRosMasterInfo> serviceSelector;

	@Before
	public void setup() {
		serviceSelector = new LightweightZeroconfServiceSelector<ZeroconfRosMasterInfo>();
	}

	/**
	 * Should die if attempt to select a master and there are none.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNoRegistrations() throws Exception {
		try {
			serviceSelector.selectService();
			fail();
		} catch (RosRuntimeException e) {
			// What was wanted.
		}
	}

	/**
	 * Only one master placed in, should get it back.
	 */
	@Test
	public void testGetSingleRegistration() {
		ZeroconfRosMasterInfo masterInfo = new ZeroconfRosMasterInfo(
				"scoobydoo", "test", "http", "gloop", 11311, 1, 1);
		serviceSelector.addService(masterInfo);

		ZeroconfRosMasterInfo selected = serviceSelector.selectService();
		assertTrue(masterInfo == selected);
	}

	/**
	 * Only one master placed in then removed, should fail..
	 */
	@Test
	public void testGetSingleRegistrationRemove() {
		ZeroconfRosMasterInfo masterInfo = new ZeroconfRosMasterInfo(
				"scoobydoo", "test", "http", "gloop", 11311, 1, 1);
		serviceSelector.addService(masterInfo);

		serviceSelector.removeService(masterInfo);

		try {
			serviceSelector.selectService();
			fail();
		} catch (RosRuntimeException e) {
			// What was wanted.
		}
	}

	/**
	 * Place two masters in with different priorities. Put in lowest priority
	 * first.
	 */
	@Test
	public void testTwoRegistrationOrder1() {
		ZeroconfRosMasterInfo masterInfo1 = new ZeroconfRosMasterInfo(
				"scoobydoo1", "test1", "http", "gloop1", 11311, 1, 1);
		ZeroconfRosMasterInfo masterInfo2 = new ZeroconfRosMasterInfo(
				"scoobydoo2", "test2", "http", "gloop2", 11311, 2, 1);

		serviceSelector.addService(masterInfo1);
		serviceSelector.addService(masterInfo2);

		ZeroconfRosMasterInfo selected = serviceSelector.selectService();
		assertTrue(masterInfo1 == selected);
	}

	/**
	 * Place two masters in with different priorities. Put in lowest priority
	 * second.
	 */
	@Test
	public void testTwoRegistrationOrder2() {
		ZeroconfRosMasterInfo masterInfo1 = new ZeroconfRosMasterInfo(
				"scoobydoo1", "test1", "http", "gloop1", 11311, 1, 1);
		ZeroconfRosMasterInfo masterInfo2 = new ZeroconfRosMasterInfo(
				"scoobydoo2", "test2", "http", "gloop2", 11311, 2, 1);

		serviceSelector.addService(masterInfo2);
		serviceSelector.addService(masterInfo1);

		ZeroconfRosMasterInfo selected = serviceSelector.selectService();
		assertTrue(masterInfo1 == selected);
	}

	/**
	 * Place two masters in with different priorities. Remove lowest priority.
	 */
	@Test
	public void testTwoRegistrationRemoveLowest() {
		ZeroconfRosMasterInfo masterInfo1 = new ZeroconfRosMasterInfo(
				"scoobydoo1", "test1", "http", "gloop1", 11311, 1, 1);
		ZeroconfRosMasterInfo masterInfo2 = new ZeroconfRosMasterInfo(
				"scoobydoo2", "test2", "http", "gloop2", 11311, 2, 1);

		serviceSelector.addService(masterInfo1);
		serviceSelector.addService(masterInfo2);

		serviceSelector.removeService(masterInfo1);

		ZeroconfRosMasterInfo selected = serviceSelector.selectService();
		assertTrue(masterInfo2 == selected);
	}

	/**
	 * Test how well the weighting works.
	 * 
	 * This test may fail as it is testing random sampling of the equal priority
	 * services and sometimes the percentage off the expected frequency may be a
	 * tad larger than the expected bounds.
	 */
	@Test
	public void testWeighting() {
		int weight1 = 1;
		int weight2 = 5;
		int weight3 = 10;
		ZeroconfRosMasterInfo masterInfo1 = new ZeroconfRosMasterInfo(
				"scoobydoo1", "test1", "http", "gloop1", 11311, 1, weight1);
		ZeroconfRosMasterInfo masterInfo2 = new ZeroconfRosMasterInfo(
				"scoobydoo2", "test2", "http", "gloop2", 11311, 1, weight2);
		ZeroconfRosMasterInfo masterInfo3 = new ZeroconfRosMasterInfo(
				"scoobydoo3", "test2", "http", "gloop2", 11311, 1, weight3);

		serviceSelector.addService(masterInfo1);
		serviceSelector.addService(masterInfo2);
		serviceSelector.addService(masterInfo3);

		int numRounds = 5;
		int numSamples = 10000;

		int totalWeight = weight1 + weight2 + weight3;
		double expected1 = (double) weight1 / totalWeight;
		double expected2 = (double) weight2 / totalWeight;
		double expected3 = (double) weight3 / totalWeight;
		
		double tolerance = 0.10;
		
		int pass = 0;

		for (int round = 0; round < numRounds; round++) {
			int count1 = 0;
			int count2 = 0;
			int count3 = 0;

			for (int sample = 0; sample < numSamples; sample++) {
				ZeroconfRosMasterInfo selected = serviceSelector
						.selectService();
				if (masterInfo1 == selected) {
					count1++;
				} else if (masterInfo2 == selected) {
					count2++;
				} else if (masterInfo3 == selected) {
					count3++;
				} else {
					fail("Got unregistered master");
				}
			}

			double ratio1 = (double) count1 / numSamples;
			double ratio2 = (double) count2 / numSamples;
			double ratio3 = (double) count3 / numSamples;

			double percent1 = Math.abs((expected1 - ratio1) / expected1);
			double percent2 = Math.abs((expected2 - ratio2) / expected2);
			double percent3 = Math.abs((expected3 - ratio3) / expected3);

			if (percent1 < tolerance && percent2 < tolerance && percent3 < tolerance) {
				pass++;
			}
		}
		
		assertTrue("Passed at least 3 times", pass >= 4);
	}
}
