/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.util.process;

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.resource.ManagedResource;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A collection of {@link NativeApplicationRunner} instances.
 *
 * <p>
 * This collection ensures the runners are properly sampled and shutdown properly, do not attempt to handle them
 * yourself.
 *
 * @author Keith M. Hughes
 */
public class NativeApplicationRunnerCollection implements ManagedResource {

  /**
   * The default for how often to sample the runners to see if they are still running, in milliseconds.
   */
  public static final long SAMPLING_PERIOD_DEFAULT = 500;

  /**
   * How often to sample the runners to see if they are still running, in milliseconds.
   */
  private long samplingPeriod = SAMPLING_PERIOD_DEFAULT;

  /**
   * The space environment to use.
   */
  private final InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The logger for issues.
   */
  private final Log log;

  /**
   * All runners currently running.
   */
  private final List<NativeApplicationRunner> runners = Lists.newCopyOnWriteArrayList();

  /**
   * The factory for new runners.
   */
  private final NativeApplicationRunnerFactory runnerFactory;

  /**
   * The future used to control the sampling thread.
   */
  private Future<?> samplingFuture;

  /**
   * Construct a new collection.
   *
   * @param spaceEnvironment
   *          the space environment for the collection to use
   * @param log
   *          the logger
   */
  public NativeApplicationRunnerCollection(InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;

    runnerFactory = new SimpleNativeApplicationRunnerFactory(spaceEnvironment);
  }

  @Override
  public synchronized void startup() {
    if (samplingFuture == null) {
      for (NativeApplicationRunner runner : runners) {
        runner.startup();
      }
      samplingFuture = spaceEnvironment.getExecutorService().scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          sampleRunners();
        }
      }, samplingPeriod, samplingPeriod, TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public synchronized void shutdown() {
    if (samplingFuture != null) {
      samplingFuture.cancel(true);
      samplingFuture = null;
    }

    for (NativeApplicationRunner runner : runners) {
      runner.shutdown();
    }
  }

  /**
   * Create a new application runner with the given config.
   *
   * <p>
   * The runner is added to the collection. The collection will handle the lifecycle of the runner.
   *
   * @param config
   *          the configuration for the runner
   *
   * @return a new application runner appropriate for the current platform
   */
  public NativeApplicationRunner addNativeApplicationRunner(Map<String, Object> config) {
    NativeApplicationRunner runner = newNativeApplicationRunner();
    runner.configure(config);

    addNativeApplicationRunner(runner);

    return runner;
  }

  /**
   * Add a native application runner to the collection.
   *
   * <p>
   * The runner should not be started yet. The collection will control the lifecycle of the runner.
   *
   * @param runner
   *          the runner to add to the collection
   */
  public synchronized void addNativeApplicationRunner(NativeApplicationRunner runner) {
    runners.add(runner);

    if (samplingFuture != null) {
      runner.startup();
    }
  }

  /**
   * Create a new application runner.
   *
   * <p>
   * The collection will not own the runner, it will need to be added.
   *
   * @return a new runner
   */
  public NativeApplicationRunner newNativeApplicationRunner() {
    return runnerFactory.newPlatformNativeApplicationRunner(log);
  }

  /**
   * Sample all runners and see if they are still running.
   */
  private void sampleRunners() {
    // No need to synchronize as the collection is properly threadsafe.
    for (NativeApplicationRunner runner : runners) {
      if (!runner.isRunning()) {
        // This works because of the copy on write
        runners.remove(runner);
      }
    }
  }
}
