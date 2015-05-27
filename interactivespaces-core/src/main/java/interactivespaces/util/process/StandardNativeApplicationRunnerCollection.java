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

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.process.NativeApplicationRunner.NativeApplicationRunnerState;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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
public class StandardNativeApplicationRunnerCollection implements NativeApplicationRunnerCollection {

  /**
   * The count for the countdown latch for {@link #runNativeApplicationRunner(NativeApplicationDescription, long)}.
   */
  public static final int BLOCKING_RUN_COUNTDOWN_COUNT = 1;

  /**
   * The default for how often to sample the runners to see if they are still running, in milliseconds.
   */
  public static final long SAMPLING_PERIOD_DEFAULT = 500;

  /**
   * How often to sample the runners to see if they are still running, in milliseconds.
   */
  private long samplingPeriod = SAMPLING_PERIOD_DEFAULT;

  /**
   * The parser for native applications.
   */
  private NativeApplicationRunnerParser runnerParser = new StandardNativeApplicationRunnerParser();

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
  public StandardNativeApplicationRunnerCollection(InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;

    runnerFactory = new SimpleNativeApplicationRunnerFactory(runnerParser, spaceEnvironment);
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

  @Override
  public NativeApplicationRunner addNativeApplicationRunner(NativeApplicationDescription description) {
    NativeApplicationRunner runner = newNativeApplicationRunner();
    runner.configure(description);

    addNativeApplicationRunner(runner);

    return runner;
  }

  @Override
  @Deprecated
  public NativeApplicationRunner addNativeApplicationRunner(Map<String, Object> config) {
    NativeApplicationRunner runner = newNativeApplicationRunner();
    runner.configure(config);

    addNativeApplicationRunner(runner);

    return runner;
  }

  @Override
  public synchronized void addNativeApplicationRunner(NativeApplicationRunner runner) {
    runners.add(runner);

    if (samplingFuture != null) {
      runner.startup();
    }
  }

  @Override
  public NativeApplicationRunner newNativeApplicationRunner() {
    return runnerFactory.newPlatformNativeApplicationRunner(log);
  }

  @Override
  public NativeApplicationRunnerState runNativeApplicationRunner(NativeApplicationDescription description,
      long waitTime) {
    NativeApplicationRunner runner = newNativeApplicationRunner();
    runner.configure(description);

    final CountDownLatch runnerComplete = new CountDownLatch(BLOCKING_RUN_COUNTDOWN_COUNT);
    runner.addNativeApplicationRunnerListener(new BaseNativeApplicationRunnerListener() {

      @Override
      public void onNativeApplicationRunnerStartupFailed(NativeApplicationRunner runner) {
        runnerComplete.countDown();
      }

      @Override
      public void onNativeApplicationRunnerShutdown(NativeApplicationRunner runner) {
        runnerComplete.countDown();
      }
    });

    addNativeApplicationRunner(runner);

    try {
      if (!runnerComplete.await(waitTime, TimeUnit.MILLISECONDS)) {
        SimpleInteractiveSpacesException.throwFormattedException("The command %s did not complete in %d msec",
            description, waitTime);
      }
    } catch (SimpleInteractiveSpacesException e) {
      throw e;
    } catch (InterruptedException e) {
      SimpleInteractiveSpacesException.throwFormattedException("The command %s wait was interrupted", description);
    }

    return runner.getState();
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
