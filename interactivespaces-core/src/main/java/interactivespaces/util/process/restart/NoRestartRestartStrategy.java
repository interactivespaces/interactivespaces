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

package interactivespaces.util.process.restart;

/**
 * A restart strategy which refuses to try a restart within Interactive Spaces.
 *
 * @author Keith M. Hughes
 */
public class NoRestartRestartStrategy implements RestartStrategy {

  @Override
  public RestartStrategyInstance newInstance(Restartable restartable) {
    NoRestartRestartStrategyInstance instance = new NoRestartRestartStrategyInstance(restartable);
    instance.attemptRestart();

    return instance;
  }

  /**
   * A {@link RestartStrategyInstance} which will never attempt a restart.
   *
   * @author Keith M. Hughes
   */
  private static class NoRestartRestartStrategyInstance implements RestartStrategyInstance {
    private Restartable restartable;

    public NoRestartRestartStrategyInstance(Restartable restartable) {
      this.restartable = restartable;
    }

    /**
     * Attempt the restart.
     */
    public void attemptRestart() {
      restartable.restartComplete(false);
    }

    @Override
    public void quit() {
      // Nothing to do.
    }

    @Override
    public boolean isRestarting() {
      // Restarts will never happen.
      return false;
    }
  }
}
