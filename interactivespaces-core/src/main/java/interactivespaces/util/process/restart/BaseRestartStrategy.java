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
 * @param <T>
 *          the type of {@link Restartable}
 *
 * @author Keith M. Hughes
 */
public abstract class BaseRestartStrategy<T extends Restartable> implements RestartStrategy<T> {

  /**
   * The listeners for the strategy.
   */
  private final List<RestartStrategyListener<T>> listeners = Lists.newCopyOnWriteArrayList();

  @Override
  public void addRestartStrategyListener(RestartStrategyListener<T> listener) {
    listeners.add(listener);
  }

  @Override
  public void removeRestartStrategyListener(RestartStrategyListener<T> listener) {
    listeners.remove(listener);
  }

  /**
   * Get the listener list from the strategy.
   *
   * @return the listener list from the strategy
   */
  protected List<RestartStrategyListener<T>> getListeners() {
    return listeners;
  }

  /**
   * A base implementation of a {@link RestartStrategyInstance}.
   *
   * @param <U>
   *          the type of {@link Restartable}
   *
   * @author Keith M. Hughes
   */
  public abstract static class BaseRestartStrategyInstance<U extends Restartable> implements RestartStrategyInstance<U> {

    /**
     * The object being restarted.
     */
    private final U restartable;

    /**
     * The strategy which created the instance.
     */
    private final RestartStrategy<U> strategy;

    /**
     * The listeners for the strategy.
     */
    private final List<RestartStrategyListener<U>> listeners;

    /**
     * Construct a new base instance.
     *
     * @param restartable
     *          the restartable being restarted
     * @param strategy
     *          the strategy being used
     * @param listeners
     *          the listeners to use
     */
    public BaseRestartStrategyInstance(U restartable, RestartStrategy<U> strategy,
        List<RestartStrategyListener<U>> listeners) {
      this.restartable = restartable;
      this.strategy = strategy;
      this.listeners = listeners;
    }

    @Override
    public RestartStrategy<U> getStrategy() {
      return strategy;
    }

    @Override
    public U getRestartable() {
      return restartable;
    }

    /**
     * A restart is being attempted.
     *
     * <p>
     * All listeners will be called, even if someone voted to cancel the
     * restart.
     *
     * @param restartable
     *          the restartable needs to be restarted
     *
     * @return {@code true} if all of the listeners said it was OK to restart,
     *         {@code false} if any said punt
     */
    protected boolean notifyRestartAttempt(U restartable) {
      boolean continueRestart = true;
      for (RestartStrategyListener<U> listener : listeners) {
        continueRestart &= listener.onRestartAttempt(strategy, restartable, continueRestart);
      }

      return continueRestart;
    }

    /**
     * Restart has succeeded.
     */
    protected void notifyRestartSuccess() {
      for (RestartStrategyListener<U> listener : listeners) {
        listener.onRestartSuccess(strategy, restartable);
      }
    }

    /**
     * Restart has failed.
     */
    protected void notifyRestartFailure() {
      for (RestartStrategyListener<U> listener : listeners) {
        listener.onRestartFailure(strategy, restartable);
      }
    }
  }
}
