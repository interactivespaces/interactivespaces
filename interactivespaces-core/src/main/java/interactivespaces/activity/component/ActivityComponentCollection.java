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
import com.google.common.collect.Sets;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.configuration.Configuration;
import interactivespaces.util.graph.DependencyResolver;

import org.apache.commons.logging.Log;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

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
  private final List<ActivityComponent> components = new CopyOnWriteArrayList<ActivityComponent>();

  /**
   * Set of component names in the activity.
   */
  private final Set<String> componentNames = Sets.newHashSet();

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
    if (componentNames.contains(componentName)) {
      throw new SimpleInteractiveSpacesException(
          "Multiple activity components added for name " + componentName);
    }
    componentNames.add(componentName);

    components.add(component);
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
    DependencyResolver<ActivityComponent> resolver = new DependencyResolver<ActivityComponent>();

    for (ActivityComponent component : components) {
      resolver.addNode(component.getName(), component);
      resolver.addNodeDependencies(component.getName(), component.getDependencies());
    }

    resolver.resolve();
    List<ActivityComponent> newOrder = Lists.newArrayList();
    for (ActivityComponent component : resolver.getOrdering()) {
      // There will be null components if addNode() was never called.
      // This means the component wasn't added but was a dependency.
      // Only the component which had it as a dependency knows if it
      // is required or not.
      if (component != null) {
        component.configureComponent(configuration, componentContext);
        newOrder.add(component);
      }
    }

    components.clear();
    components.addAll(newOrder);
  }

  /**
   * Startup all components in the container.
   *
   * @throws Exception on internal startup error
   */
  public void startupComponents() throws Exception {
    List<ActivityComponent> startedComponents = Lists.newArrayList();
    try {
      for (ActivityComponent component : components) {
        component.getComponentContext().getActivity().getLog()
            .info("Starting component " + component.getName());

        component.startupComponent();
        startedComponents.add(component);
      }
    } catch (Exception e) {
      // Every component that was actually started up should be shut down.
      for (ActivityComponent component : startedComponents) {
        component.shutdownComponent();
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

    for (ActivityComponent component : components) {
      try {
        component.shutdownComponent();
      } catch (Exception e) {
        properlyShutDown = false;
        component.getComponentContext().getActivity().getLog()
            .error("Error during activity component shutdown", e);
      }
    }

    return properlyShutDown;
  }

  /**
   * Clear all components from the container.
   */
  public void clear() {
    components.clear();
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
    for (ActivityComponent component : components) {
      if (!component.isComponentRunning()) {
        allAreRunning = false;

        component
            .getComponentContext()
            .getActivity()
            .getLog()
            .error(
                String.format("Activity component %s not running when expected",
                    component.getName()));
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
   * @return the component with the given name.
   */
  @SuppressWarnings("unchecked")
  public <T extends ActivityComponent> T getActivityComponent(String name) {
    // Assuming there will not be a lot of components in the collection.
    for (ActivityComponent component : components) {
      if (component.getName().equals(name)) {
        return (T) component;
      }
    }

    throw new InteractiveSpacesException(String.format("No activity component with name %s", name));
  }

  /**
   * Return a list of all the current components.
   *
   * @return list of all components
   */
  public Collection<ActivityComponent> getComponents() {
    return Lists.newArrayList(components);
  }

  /**
   * Get status messages on all components.
   *
   * @return status message for all components
   */
  public List<String> getComponentStatuses() {
    // TODO(keith): Create. Probably will need special class with all
    // components.

    return null;
  }

  /**
   * @param log
   *          the log to set
   */
  public void setLog(Log log) {
    this.log = log;
  }
}
