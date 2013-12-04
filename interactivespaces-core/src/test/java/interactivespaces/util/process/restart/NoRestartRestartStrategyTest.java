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

package interactivespaces.util.process.restart;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the {@link NoRestartRestartStrategy}.
 *
 * @author Keith M. Hughes
 */
public class NoRestartRestartStrategyTest {

  /**
   * Test that no restart happens.
   */
  @Test
  public void testRestart() {
    Restartable restartable = Mockito.mock(Restartable.class);

    RestartStrategyListener listener = Mockito.mock(RestartStrategyListener.class);

    NoRestartRestartStrategy strategy = new NoRestartRestartStrategy();
    strategy.addRestartStrategyListener(listener);

    RestartStrategyInstance instance = strategy.newInstance(restartable);
    Assert.assertFalse(instance.isRestarting());

    Mockito.verify(restartable, Mockito.times(1)).restartComplete(false);
    Mockito.verify(listener, Mockito.times(1)).onRestartFailure(strategy, restartable);
    Mockito.verify(listener, Mockito.times(0)).onRestartAttempt(strategy, restartable, true);
    Mockito.verify(listener, Mockito.times(0)).onRestartSuccess(strategy, restartable);
  }
}
