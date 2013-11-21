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

package interactivespaces.activity.component;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.SupportedActivity;
import interactivespaces.util.InteractiveSpacesUtilities;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A context for {@link ActivityComponent} instances to run in.
 *
 * @author Keith M. Hughes
 */
public class ActivityComponentContext {

  /**
   * The amount of time, in msecs, that handlers will wait for startup.
   */
  public static final long STARTUP_LATCH_TIMEOUT = 5000;

  /**
   * The activity the components are running for.
   */
  private SupportedActivity activity;

  /**
   * All components running in the current context.
   */
  private ActivityComponentCollection components;

  /**
   * Factory for new components.
   */
  private ActivityComponentFactory componentFactory;

  /**
   * The latch that threads can wait on for startup completion.
   */
  private CountDownLatch startupLatch = new CountDownLatch(1);

  /**
   * {@code true} if handlers are allowed to run.
   */
  private AtomicBoolean handlersAllowed = new AtomicBoolean();

  /**
   * Keeps count of the number of handlers running.
   */
  private AtomicInteger numberProcessingHandlers = new AtomicInteger();

  /**
   * @param activity
   *          the activity which will use this context
   * @param components
   *          the component collection for this context
   * @param componentFactory
   *          the factory for any new components that will be needed
   */
  public ActivityComponentContext(SupportedActivity activity, ActivityComponentCollection components,
      ActivityComponentFactory componentFactory) {
    this.activity = activity;
    this.components = components;
    this.componentFactory = componentFactory;
  }

  /**
   * Get an activity component from the collection.
   *
   * @param name
   *          name of the component
   * @param <T>
   *          type of activity component
   *
   * @return the component with the given name or {@code null} if not present
   */
  public <T extends ActivityComponent> T getActivityComponent(String name) {
    return components.getActivityComponent(name);
  }

  /**
   * Get an activity component from the collection.
   *
   * @param name
   *          name of the component
   * @param <T>
   *          type of activity component
   *
   * @return the component with the given name
   *
   * @throws SimpleInteractiveSpacesException
   *           if named component is not present
   */
  public <T extends ActivityComponent> T getRequiredActivityComponent(String name)
      throws SimpleInteractiveSpacesException {
    return components.getRequiredActivityComponent(name);
  }

  /**
   * Get the activity which is running the components.
   *
   * @param <T>
   *          type of activity
   *
   * @return the activity
   */
  @SuppressWarnings("unchecked")
  public <T extends SupportedActivity> T getActivity() {
    return (T) activity;
  }

  /**
   * Get the component factory for this context.
   *
   * @return factor for creating activity components
   */
  public ActivityComponentFactory getComponentFactory() {
    return componentFactory;
  }

  /**
   * Begin the setup phase.
   */
  public void beginStartupPhase() {
    // Nothing required at the moment
  }

  /**
   * End the setup phase.
   *
   * @param success
   *          {@code true} if the setup was successful
   */
  public void endStartupPhase(boolean success) {
    handlersAllowed.set(success);
    startupLatch.countDown();
  }

  /**
   * Shutdown has begun.
   */
  public void beginShutdownPhase() {
    handlersAllowed.set(false);
  }

  /**
   * Are handler allowed to run?
   *
   * @return {@code true} if handlers can run
   */
  public boolean areHandlersAllowed() {
    return handlersAllowed.get();
  }

  /**
   * A handler has been entered.
   */
  public void enterHandler() {
    numberProcessingHandlers.incrementAndGet();
  }

  /**
   * A handler has been exited.
   */
  public void exitHandler() {
    if (numberProcessingHandlers.decrementAndGet() < 0) {
      getActivity().getLog().error("There are more handler exits than enters");
    }

  }

  /**
   * Are there still handlers which are processing data?
   *
   * @return {@code true} if there are handlers in the midst of processing
   */
  public boolean areProcessingHandlers() {
    return numberProcessingHandlers.get() > 0;
  }

  /**
   * Block until there are no longer handlers which are processing.
   *
   * @param sampleTime
   *          how often sampling should take place for whether there are
   *          processing handler, in milliseconds
   * @param maxSamplingTime
   *          how long should sampling take place before punting, in msecs
   *
   * @return {@code true} if there are no more processing handlers
   */
  public boolean waitOnNoProcessingHandlings(long sampleTime, long maxSamplingTime) {
    long start = System.currentTimeMillis();
    while (areProcessingHandlers() && (System.currentTimeMillis() - start) < maxSamplingTime) {
      InteractiveSpacesUtilities.delay(sampleTime);
    }

    return !areProcessingHandlers();
  }

  /**
   * Wait for the context to complete startup, whether successfully or
   * unsuccessfully.
   *
   * <p>
   * This method should be called before any handler runs, it will return
   * immediately if startup has completed.
   *
   * <p>
   * The await will not be for longer than a preset amount of time.
   *
   * @return {@code true} if startup was successful before timeout
   */
  public boolean awaitStartup() {
    try {
      boolean succeed = startupLatch.await(STARTUP_LATCH_TIMEOUT, TimeUnit.MILLISECONDS);

      if (!succeed) {
        getActivity().getLog().warn(
            String.format("Event handler timed out after %d msecs waiting for activity startup",
                STARTUP_LATCH_TIMEOUT));
      }
      return succeed;
    } catch (InterruptedException e) {
      return false;
    }
  }

  /**
   * Can a handler run?
   *
   * <p>
   * This call requires both {@link #areHandlersAllowed()} and
   * {@link #awaitStartup()} to both be {@code true}.
   *
   * @return {@code true} if a handle can run.
   */
  public boolean canHandlerRun() {
    return awaitStartup() && areHandlersAllowed();
  }
}
