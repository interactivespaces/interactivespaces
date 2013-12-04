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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Support for writing {@link RestartStrategy} classes.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseRestartStrategy implements RestartStrategy {

  /**
   * The listeners for the strategy.
   */
  private final List<RestartStrategyListener> listeners = Lists.newCopyOnWriteArrayList();

  @Override
  public void addRestartStrategyListener(RestartStrategyListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeRestartStrategyListener(RestartStrategyListener listener) {
    listeners.remove(listener);
  }

  /**
   * A restart is being attempted.
   *
   * <p>
   * All listeners will be called, even if someone voted to cancel the restart.
   *
   * @param restartable
   *          the restartable needs to be restarted
   *
   * @return {@code true} if all of the listeners said it was OK to restart,
   *         {@code false} if any said punt
   */
  protected boolean sendRestartAttempt(Restartable restartable) {
    boolean continueRestart = true;
    for (RestartStrategyListener listener : listeners) {
      continueRestart &= listener.onRestartAttempt(this, restartable, continueRestart);
    }

    return continueRestart;
  }

  /**
   * Restart has suceeeded.
   *
   * @param restartable
   *          the restartable which has been restarted
   */
  protected void sendRestartSuccess(Restartable restartable) {
    for (RestartStrategyListener listener : listeners) {
      listener.onRestartSuccess(this, restartable);
    }
  }

  /**
   * Restart has failed.
   *
   * @param restartable
   *          the restartable which has failed
   */
  protected void sendRestartFailure(Restartable restartable) {
    for (RestartStrategyListener listener : listeners) {
      listener.onRestartFailure(this, restartable);
    }
  }

  /**
   * A base implementation of a {@link RestartStrategyInstance}.
   *
   * @author Keith M. Hughes
   */
  public abstract static class BaseRestartStrategyInstance implements RestartStrategyInstance {

    /**
     * The object being restarted.
     */
    private final Restartable restartable;

    /**
     * The strategy which created the instance.
     */
    private final RestartStrategy strategy;

    /**
     * Construct a new base instance.
     *
     * @param restartable
     *          the restartable being restarted
     * @param strategy
     *          the strategy being used
     */
    public BaseRestartStrategyInstance(Restartable restartable, RestartStrategy strategy) {
      this.restartable = restartable;
      this.strategy = strategy;
    }

    @Override
    public RestartStrategy getStrategy() {
      return strategy;
    }

    @Override
    public Restartable getRestartable() {
      return restartable;
    }
  }
}
