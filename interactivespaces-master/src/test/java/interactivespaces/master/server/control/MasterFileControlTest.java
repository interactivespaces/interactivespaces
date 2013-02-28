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

package interactivespaces.master.server.control;

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link MasterFileControl} class.
 * 
 * @author Keith M. Hughes
 */
public class MasterFileControlTest {

	private InteractiveSpacesEnvironment spaceEnvironment;

	private InteractiveSpacesSystemControl spaceSystemControl;

	private MasterFileControl fileControl;

	@Before
	public void setup() {
		spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);
		spaceSystemControl = Mockito.mock(InteractiveSpacesSystemControl.class);

		fileControl = new MasterFileControl();
		fileControl.setSpaceEnvironment(spaceEnvironment);
		fileControl.setSpaceSystemControl(spaceSystemControl);
	}

	/**
	 * Make sure shutdown is called on control if a shutdown command is
	 * received.
	 */
	@Test
	public void testShutdownCall() {
		fileControl.handleCommand(MasterFileControl.COMMAND_SHUTDOWN);
		
		Mockito.verify(spaceSystemControl, Mockito.times(1)).shutdown();
	}
}
