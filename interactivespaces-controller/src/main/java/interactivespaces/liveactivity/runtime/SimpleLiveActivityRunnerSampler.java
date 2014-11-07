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

package interactivespaces.liveactivity.runtime;

import interactivespaces.activity.ActivityStatus;
import interactivespaces.system.InteractiveSpacesEnvironment;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Sample Live Activity Runners periodically.
 *
 * <p>
 * Runners are removed from the sampler once they stop running.
 *
 * @author Keith M. Hughes
 */
public class SimpleLiveActivityRunnerSampler implements LiveActivityRunnerSampler {

  /**
   * The default number of milliseconds the activity activityWatcher thread delays between scans.
   */
  private static final int LIVE_ACTIVITY_RUNNER_SAMPLER_DELAY_DEFAULT = 1000;

  /**
   * Control for the live activity runner sampler.
   */
  private ScheduledFuture<?> liveActivityRunnerSamplerControl;

  /**
   * Number of milliseconds the live activity runner sampler waits before scanning for runner state.
   */
  private final long liveActivityRunnerSamplerDelay = LIVE_ACTIVITY_RUNNER_SAMPLER_DELAY_DEFAULT;

  /**
   * All runners being sampled by the sampler.
   */
  private List<LiveActivityRunner> runners = Lists.newCopyOnWriteArrayList();

  /**
   * The space environment to use.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The logger.
   */
  private Log log;

  /**
   * Construct a new watcher.
   *
   * @param spaceEnvironment
   *          the space envieonment to use
   * @param log
   *          the logger to be used
   */
  public SimpleLiveActivityRunnerSampler(InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    this.log = log;
  }

  @Override
  public void startup() {
    liveActivityRunnerSamplerControl = spaceEnvironment.getExecutorService().scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        sample();
      }
    }, liveActivityRunnerSamplerDelay, liveActivityRunnerSamplerDelay, TimeUnit.MILLISECONDS);
  }

  @Override
  public void shutdown() {
    if (liveActivityRunnerSamplerControl != null) {
      liveActivityRunnerSamplerControl.cancel(true);
      liveActivityRunnerSamplerControl = null;
    }
  }

  @Override
  public void startSamplingRunner(LiveActivityRunner runner) {
    runners.add(runner);
  }

  /**
   * Sample all runners.
   */
  private void sample() {
    try {
      for (LiveActivityRunner runner : runners) {
        // No need to synchronize. If something bad happens we'll catch it the next time around.
        sampleRunner(runner);
      }
    } catch (Throwable e) {
      log.error("Error during runner sampler run", e);
    }
  }

  /**
   * See what happens with a given activity.
   *
   * @param runner
   *          the runner to be sampled
   */
  private void sampleRunner(LiveActivityRunner runner) {
    ActivityStatus newStatus = runner.sampleActivityStatus();
    if (!newStatus.getState().isRunning()) {
      runners.remove(runner);
    }
  }
}
