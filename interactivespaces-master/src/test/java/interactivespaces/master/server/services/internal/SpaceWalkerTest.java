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

package interactivespaces.master.server.services.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import interactivespaces.domain.space.Space;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link SpaceWalker}.
 *
 * @author Keith M. Hughes
 */
public class SpaceWalkerTest extends BaseSpaceTest {
	@Before
	public void setup() {
		baseSetup();
	}
	
	@Test
	public void testWalk() {
		Space spaceTree = space(0).addSpaces(
			space(1).addSpaces(
				space(4), 
				space(5)), 
			space(2).addSpaces(
				space(6)
			), 
			space(3).addSpaces(
				space(7), 
				space(8), 
				space(9)
			)
		);
		
		List<String> expectedWalkSequence = getIdList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		
		final List<String> idsInOrderWalked = new ArrayList<String>();
		final AtomicBoolean beginRan = new AtomicBoolean(false);
		final AtomicBoolean endRan = new AtomicBoolean(false);
		SpaceWalker walker = new SpaceWalker() {
			
			@Override
			protected void doBegin() {
				assertFalse(beginRan.get());
				assertFalse(endRan.get());
				assertTrue(idsInOrderWalked.isEmpty());
				
				beginRan.set(true);
			}

			@Override
			protected void doEnd() {
				assertTrue(beginRan.get());
				assertFalse(endRan.get());
				assertFalse(idsInOrderWalked.isEmpty());
				
				endRan.set(true);
			}

			@Override
			protected void doVisit(Space space) {
				assertTrue(beginRan.get());
				assertFalse(endRan.get());
				
				idsInOrderWalked.add(space.getName());
			}
			
		};
		
		walker.walk(spaceTree);
		
		assertTrue(beginRan.get());
		assertTrue(endRan.get());
		assertEquals(expectedWalkSequence, idsInOrderWalked);
	}
}
