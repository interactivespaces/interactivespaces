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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.graph.DependencyResolver;

import org.apache.commons.logging.Log;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A collection of {@link ActivityComponent} instances.
 *
 * <p>
 * This collection is thread-safe.
 *
 * @author Keith M. Hughes
 */
public class ActivityComponentCollection {

  /**
   * All components in the activity.
   */
  private final List<ActivityComponent> configuredComponents = Lists.newArrayList();

  /**
   * Set of component names in the activity.
   */
  private final Map<String, ActivityComponent> addedComponents = Maps.newHashMap();

  /**
   * The log for the collection.
   */
  private Log log;

  /**
   * Add a new component to the collection.
   *
   * @param component
   *          the component to add.
   */
  public void addComponent(ActivityComponent component) {
    // There's a subtle behavior here in the case where two activity components with
    // the same name are dependencies of a single dependent node. In that case, only one of the
    // dependencies needs to be resolved before ethe dependent node, which isn't the correct
    // behavior in the strict sense of a dependency. This behavior isn't really supported by
    // the system since activity components are generally considered to be singletons. Therefore,
    // help aid development by detecting the case of multiple components and throwing an error.
    String componentName = component.getName();
    if (addedComponents.put(componentName, component) != null) {
      throw new SimpleInteractiveSpacesException("Multiple activity components added for name " + componentName);
    }
  }

  /**
   * Configure all the components in the in the collection in dependency order.
   *
   * @param configuration
   *          configuration to use for configuring components
   * @param componentContext
   *          the context for the activity components
   */
  public void configureComponents(Configuration configuration,
      ActivityComponentContext componentContext) {
    if (!configuredComponents.isEmpty()) {
      throw new InteractiveSpacesException("Attempt to configure already configured components");
    }

    DependencyResolver<ActivityComponent> resolver = new DependencyResolver<ActivityComponent>();

    for (ActivityComponent component : addedComponents.values()) {
      component.setComponentContext(componentContext);
      resolver.addNode(component.getName(), component);
      resolver.addNodeDependencies(component.getName(), component.getDependencies());
    }

    resolver.resolve();
    for (ActivityComponent component : resolver.getOrdering()) {
      // There will be null components if addNode() was never called.
      // This means the component wasn't added but was a dependency.
      // Only the component which had it as a dependency knows if it
      // is required or not.
      if (component != null) {
        component.configureComponent(configuration);
        configuredComponents.add(component);
      }
    }
  }

  /**
   * Startup all components in the container.
   *
   * @throws Exception on internal startup error
   */
  public void startupComponents() throws Exception {
    List<ActivityComponent> startedComponents = Lists.newArrayList();
    try {
      for (ActivityComponent component : configuredComponents) {
        component.getComponentContext().getActivity().getLog()
            .info("Starting component " + component.getName());

        component.startupComponent();
        startedComponents.add(component);
      }
    } catch (Exception e) {
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
    // Will stop if any of the components have stopped.
    boolean allAreRunning = true;
    for (ActivityComponent component : configuredComponents) {
      if (!component.isComponentRunning()) {
        allAreRunning = false;

        handleComponentError(component, "Activity component not running when expected", null);
      }
    }

    return allAreRunning;
  }

  /**
   * Get an activity component from the collection.
   *
   * @param name
   *          name of the component
   * @param <T>
   *          specific type of activity component
   *
   * @return the component with the given name or {@code null} if not present.
   */
  @SuppressWarnings("unchecked")
  public <T extends ActivityComponent> T getActivityComponent(String name) {
    return (T) addedComponents.get(name);
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
    T component = getActivityComponent(name);
    if (component == null) {
      throw new SimpleInteractiveSpacesException("Could not find component " + name);
    }
    return component;
  }

  /**
   * Return a list of all the configured components.
   *
   * @return list of all configured components
   */
  public Collection<ActivityComponent> getConfiguredComponents() {
    return Lists.newArrayList(configuredComponents);
  }

  /**
   * @param log
   *          the log to set
   */
  public void setLog(Log log) {
    this.log = log;
  }

  /**
   * Handle a component error for the given throwable. Makes sure the log object from the component
   * is valid, otherwise use the global log.
   *
   * @param component
   *          component with error
   * @param message
   *          error message
   * @param t
   *          cause of the error/exception
   */
  private void handleComponentError(ActivityComponent component, String message, Throwable t) {
    Log targetLog = component.getComponentContext().getActivity().getLog();
    String componentName = component.getName();
    targetLog.error(String.format("%s (%s)", message, componentName), t);
  }
}
