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

package interactivespaces.controller.client.node;

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link SpaceControllerFileControl} class.
 *
 * @author Keith M. Hughes
 */
public class SpaceControllerFileControlTest {

  private InteractiveSpacesEnvironment spaceEnvironment;

  private InteractiveSpacesSystemControl spaceSystemControl;

  private SpaceControllerControl spaceControllerControl;

  private SpaceControllerFileControl fileControl;

  @Before
  public void setup() {
    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);
    spaceSystemControl = Mockito.mock(InteractiveSpacesSystemControl.class);
    spaceControllerControl = Mockito.mock(SpaceControllerControl.class);

    fileControl =
        new SpaceControllerFileControl(spaceControllerControl, spaceSystemControl, spaceEnvironment);
  }

  /**
   * Make sure shutdown is called on control if a shutdown command is received.
   */
  @Test
  public void testShutdownCall() {
    fileControl.handleCommand(SpaceControllerFileControl.COMMAND_SHUTDOWN);

    Mockito.verify(spaceSystemControl, Mockito.times(1)).shutdown();
  }
}
