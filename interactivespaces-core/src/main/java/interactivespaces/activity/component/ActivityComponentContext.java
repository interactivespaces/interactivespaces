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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.SupportedActivity;
import interactivespaces.util.InteractiveSpacesUtilities;
import interactivespaces.util.graph.DependencyResolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
  private final SupportedActivity activity;

  /**
   * Factory for new components.
   */
  private final ActivityComponentFactory componentFactory;

  /**
   * The latch that threads can wait on for startup completion.
   */
  private final CountDownLatch startupLatch = new CountDownLatch(1);

  /**
   * {@code true} if handlers are allowed to run.
   */
  private final AtomicBoolean handlersAllowed = new AtomicBoolean();

  /**
   * Keeps count of the number of handlers running.
   */
  private final AtomicInteger numberProcessingHandlers = new AtomicInteger();

  /**
   * All components in the activity.
   */
  private final List<ActivityComponent> configuredComponents = Lists.newArrayList();

  /**
   * Set of component names in the activity.
   */
  private final Map<String, ActivityComponent> addedComponents = Maps.newHashMap();

  /**
   * @param activity
   *          the activity which will use this context
   * @param componentFactory
   *          the factory for any new components that will be needed
   */
  public ActivityComponentContext(SupportedActivity activity, ActivityComponentFactory componentFactory) {
    this.activity = activity;
    this.componentFactory = componentFactory;
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
   *          how often sampling should take place for whether there are processing handler, in milliseconds
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
   * Wait for the context to complete startup, whether successfully or unsuccessfully.
   *
   * <p>
   * This method should be called before any handler runs, it will return immediately if startup has completed.
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
        getActivity().getLog()
            .warn(
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
   * This call requires both {@link #areHandlersAllowed()} and {@link #awaitStartup()} to both be {@code true}.
   *
   * @return {@code true} if a handle can run.
   */
  public boolean canHandlerRun() {
    return awaitStartup() && areHandlersAllowed();
  }

  /**
   * Add a new component to the collection.
   *
   * @param component
   *          the component to add
   * @param <T>
   *          the type of the component
   *
   * @return the component added
   *
   * @throws InteractiveSpacesException
   *           component was already there
   */
  public <T extends ActivityComponent> T addComponent(T component) throws InteractiveSpacesException {
    checkIfComponentAdded(component.getName());
    addCheckedComponent(component);

    return component;
  }

  /**
   * Add a component which has been checked for whether it has been added or not.
   *
   * @param component
   *          the component to add
   */
  private void addCheckedComponent(ActivityComponent component) {
    component.setComponentContext(this);
    addedComponents.put(component.getName(), component);
  }

  /**
   * Add new components to the collection.
   *
   * @param components
   *          the components to add
   *
   * @throws InteractiveSpacesException
   *           component was already there
   */
  public void addComponents(ActivityComponent... components) throws InteractiveSpacesException {
    for (ActivityComponent component : components) {
      addComponent(component);
    }
  }

  /**
   * Add a new component to the activity.
   *
   * @param componentType
   *          the type of the component to add
   * @param <T>
   *          specific activity component type
   *
   * @return created activity component
   *
   * @throws InteractiveSpacesException
   *           component was already there
   */
  public <T extends ActivityComponent> T addComponent(String componentType) throws InteractiveSpacesException {
    checkIfComponentAdded(componentType);

    ActivityComponent component = componentFactory.newComponent(componentType);

    addCheckedComponent(component);

    @SuppressWarnings("unchecked")
    T c = (T) component;
    return c;
  }

  /**
   * Add a set of new components to the activity.
   *
   * @param componentTypes
   *          the types of the components to add
   */
  public void addComponents(String... componentTypes) {
    for (String componentType : componentTypes) {
      addComponent(componentType);
    }
  }

  /**
   * Check to see if a component has been added.
   *
   * @param componentName
   *          name of the component
   *
   * @throws InteractiveSpacesException
   *           the component was added already
   */
  private void checkIfComponentAdded(String componentName) throws InteractiveSpacesException {
    // There's a subtle behavior here in the case where two activity components
    // with the same name are dependencies of a single dependent node. In that
    // case, only one of the dependencies needs to be resolved before the
    // dependent node, which isn't the correct behavior in the strict sense of a
    // dependency. This behavior isn't really supported by the system since
    // activity components are generally considered to be singletons. Therefore,
    // help aid development by detecting the case of multiple components and
    // throwing an error.
    if (addedComponents.containsKey(componentName)) {
      throw new SimpleInteractiveSpacesException("Multiple activity components added for name " + componentName);
    }
  }

  /**
   * Do the initial startup of components.
   *
   * @throws Throwable
   *           an internal startup error
   */
  public void initialStartupComponents() throws Throwable {
    configureComponents();
    startupComponents();
  }

  /**
   * Configure all the components in the in the collection in dependency order.
   */
  private void configureComponents() {
    if (!configuredComponents.isEmpty()) {
      throw new SimpleInteractiveSpacesException("Attempt to configure already configured components");
    }

    DependencyResolver<String, ActivityComponent> resolver = new DependencyResolver<String, ActivityComponent>();

    for (ActivityComponent component : addedComponents.values()) {
      resolver.addNode(component.getName(), component);
      List<String> dependencies = component.getDependencies();
      if (dependencies != null) {
        resolver.addNodeDependencies(component.getName(), dependencies);
      }
    }

    resolver.resolve();
    for (ActivityComponent component : resolver.getOrdering()) {
      // There will be null components if addNode() was never called.
      // This means the component wasn't added but was a dependency.
      // Only the component which had it as a dependency knows if it
      // is required or not.
      if (component != null) {
        component.configureComponent(activity.getConfiguration());
        configuredComponents.add(component);
      }
    }
  }

  /**
   * Startup all components in the container.
   *
   * @throws Throwable
   *           on internal startup error
   */
  private void startupComponents() throws Throwable {
    List<ActivityComponent> startedComponents = Lists.newArrayList();
    try {
      for (ActivityComponent component : configuredComponents) {
        startupComponent(component);
        startedComponents.add(component);
      }
    } catch (Throwable e) {
      // Every component that was actually started up should be shut down.
      for (ActivityComponent component : startedComponents) {
        try {
          component.shutdownComponent();
        } catch (Throwable t) {
          handleComponentError(component, "Error shutting down after startup failure", t);
        }
      }

      throw e;
    }
  }

  /**
   * Start up a component.
   *
   * @param component
   *          the component to start
   *
   * @throws Exception
   *           something bad happened
   */
  private void startupComponent(ActivityComponent component) throws Exception {
    try {
      activity.getLog().info("Starting component " + component.getName());

      component.startupComponent();
    } catch (Exception e) {
      handleComponentError(component, "Error starting component", e);

      throw e;
    }
  }

  /**
   * Shutdown all components in the container.
   *
   * @return {@code true} if all components properly shut down.
   */
  public boolean shutdownComponents() {
    boolean properlyShutDown = true;

    for (ActivityComponent component : configuredComponents) {
      try {
        component.shutdownComponent();
      } catch (Exception e) {
        properlyShutDown = false;
        handleComponentError(component, "Error during activity component shutdown", e);
      }
    }

    return properlyShutDown;
  }

  /**
   * Clear all components from the container.
   */
  public void clear() {
    addedComponents.clear();
    configuredComponents.clear();
  }

  /**
   * Shutdown all components from the container and then clear them.
   *
   * @return {@code true} if all components properly shut down.
   */
  public boolean shutdownAndClear() {
    boolean result = shutdownComponents();
    clear();
    return result;
  }

  /**
   * Are all required components running?
   *
   * @return {@code true} if all required components are running.
   */
  public boolean areAllComponentsRunning() {
    boolean areAllRunning = true;
    for (ActivityComponent component : configuredComponents) {
      if (!component.isComponentRunning()) {
        areAllRunning = false;

        handleComponentError(component, "Activity component not running when expected", null);
      }
    }

    return areAllRunning;
  }

  /**
   * Get an activity component from the collection.
   *
   * @param componentType
   *          type of the component
   * @param <T>
   *          specific type of activity component
   *
   * @return the component with the given name or {@code null} if not present.
   */
  @SuppressWarnings("unchecked")
  public <T extends ActivityComponent> T getActivityComponent(String componentType) {
    return (T) addedComponents.get(componentType);
  }

  /**
   * Get an activity component from the collection.
   *
   * @param componentType
   *          type of the component
   * @param <T>
   *          type of activity component
   *
   * @return the component with the given name
   *
   * @throws SimpleInteractiveSpacesException
   *           if named component is not present
   */
  public <T extends ActivityComponent> T getRequiredActivityComponent(String componentType)
      throws SimpleInteractiveSpacesException {
    T component = getActivityComponent(componentType);
    if (component == null) {
      throw new SimpleInteractiveSpacesException("Could not find component " + componentType);
    }
    return component;
  }

  /**
   * Return all the configured components.
   *
   * @return all configured components
   */
  public Collection<ActivityComponent> getConfiguredComponents() {
    return Lists.newArrayList(configuredComponents);
  }

  /**
   * Handle a component error for the given throwable. Makes sure the log object from the component is valid, otherwise
   * use the global log.
   *
   * @param component
   *          component with error
   * @param message
   *          error message
   * @param t
   *          cause of the error/exception
   */
  private void handleComponentError(ActivityComponent component, String message, Throwable t) {
    activity.getLog().error(String.format("%s (%s)", message, component.getName()), t);
  }
}
