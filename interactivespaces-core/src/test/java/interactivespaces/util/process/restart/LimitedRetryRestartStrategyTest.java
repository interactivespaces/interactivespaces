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

import interactivespaces.system.StandaloneInteractiveSpacesEnvironment;
import interactivespaces.time.TimeProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * tests for the {@link LimitedRetryRestartStrategy}.
 *
 * @author Keith M. Hughes
 */
public class LimitedRetryRestartStrategyTest {

  private StandaloneInteractiveSpacesEnvironment spaceEnvironment;
  private Restartable restartable;
  private RestartStrategyListener restartListener;
  private TimeProvider timeProvider;

  @Before
  public void setup() {
    spaceEnvironment = StandaloneInteractiveSpacesEnvironment.newStandaloneInteractiveSpacesEnvironment();

    restartListener = Mockito.mock(RestartStrategyListener.class);

    restartable = Mockito.mock(Restartable.class);

    timeProvider = Mockito.mock(TimeProvider.class);
    spaceEnvironment.setTimeProvider(timeProvider);
  }

  @After
  public void cleanup() {
    spaceEnvironment.shutdown();
  }

  @Test
  public void testSuccess() throws Exception{
    LimitedRetryRestartStrategy strategy = new LimitedRetryRestartStrategy(1, 20, 40, spaceEnvironment);
    strategy.addRestartStrategyListener(restartListener);

    Mockito.when(restartable.isRestarted()).thenReturn(true);
    Mockito.when(restartListener.onRestartAttempt(strategy, restartable, true)).thenReturn(true);

    Mockito.when(timeProvider.getCurrentTime()).thenReturn(0l, 100l);

    RestartStrategyInstance instance = strategy.newInstance(restartable);

    Thread.sleep(1000);

    Mockito.verify(restartable, Mockito.times(1)).attemptRestart();
    Mockito.verify(restartable, Mockito.times(1)).restartComplete(true);
    Mockito.verify(restartListener, Mockito.times(0)).onRestartFailure(strategy, restartable);
    Mockito.verify(restartListener, Mockito.times(1)).onRestartAttempt(strategy, restartable, true);
    Mockito.verify(restartListener, Mockito.times(1)).onRestartSuccess(strategy, restartable);
  }

  /**
   * The restartable never returns true through all attempts.
   *
   * @throws Exception
   */
  @Test
  public void testFailure() throws Exception {
    LimitedRetryRestartStrategy strategy = new LimitedRetryRestartStrategy(3, 20, 40, spaceEnvironment);
    strategy.addRestartStrategyListener(restartListener);

    Mockito.when(restartable.isRestarted()).thenReturn(false);
    Mockito.when(restartListener.onRestartAttempt(strategy, restartable, true)).thenReturn(true);

    Mockito.when(timeProvider.getCurrentTime()).thenReturn(0l, 100l);

    RestartStrategyInstance instance = strategy.newInstance(restartable);

    Thread.sleep(1000);

    Mockito.verify(restartable, Mockito.times(3)).attemptRestart();
    Mockito.verify(restartable, Mockito.times(1)).restartComplete(false);
    Mockito.verify(restartListener, Mockito.times(1)).onRestartFailure(strategy, restartable);
    Mockito.verify(restartListener, Mockito.times(3)).onRestartAttempt(strategy, restartable, true);
    Mockito.verify(restartListener, Mockito.times(0)).onRestartSuccess(strategy, restartable);
  }

  /**
   * The restartable returns false for isrestarted() but the listener vetos the next attempt.
   *
   * @throws Exception
   */
  @Test
  public void testFailureFromListener() throws Exception {
    LimitedRetryRestartStrategy strategy = new LimitedRetryRestartStrategy(3, 20, 40, spaceEnvironment);
    strategy.addRestartStrategyListener(restartListener);

    Mockito.when(restartable.isRestarted()).thenReturn(false);
    Mockito.when(restartListener.onRestartAttempt(strategy, restartable, true)).thenReturn(false);

    Mockito.when(timeProvider.getCurrentTime()).thenReturn(0l, 100l);

    RestartStrategyInstance instance = strategy.newInstance(restartable);

    Thread.sleep(1000);

    Mockito.verify(restartable, Mockito.times(0)).attemptRestart();
    Mockito.verify(restartable, Mockito.times(1)).restartComplete(false);
    Mockito.verify(restartListener, Mockito.times(1)).onRestartFailure(strategy, restartable);
    Mockito.verify(restartListener, Mockito.times(1)).onRestartAttempt(strategy, restartable, true);
    Mockito.verify(restartListener, Mockito.times(0)).onRestartSuccess(strategy, restartable);
  }

}
