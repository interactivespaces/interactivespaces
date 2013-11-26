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

package interactivespaces.activity.impl;

import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.SupportedActivity;
import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.activity.component.ActivityComponentCollection;
import interactivespaces.activity.component.ActivityComponentContext;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.activity.execution.ActivityMethodInvocation;
import interactivespaces.configuration.Configuration;
import interactivespaces.hardware.driver.Driver;
import interactivespaces.util.concurrency.ManagedCommands;
import interactivespaces.util.io.Files;
import interactivespaces.util.resource.ManagedResource;
import interactivespaces.util.resource.ManagedResources;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 * Support for building an Interactive Spaces activity.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseActivity extends ActivitySupport implements SupportedActivity {

  /**
   * The maximum amount of time to wait for all handlers to complete, in msecs.
   */
  private static final int SHUTDOWN_EVENT_HANDLER_COMPLETION_MAX_SAMPLE_TIME = 3000;

  /**
   * How long to wait between samples when checking for event handlers to
   * complete, in msecs.
   */
  private static final int SHUTDOWN_EVENT_HANDLER_COMPLETION_SAMPLE_TIME = 500;

  /**
   * Filename for activity startup config log.
   */
  private static final String ACTIVITY_STARTUP_CONFIG_LOG = "startup.conf";

  /**
   * All components in the activity.
   */
  private ActivityComponentCollection components = new ActivityComponentCollection();

  /**
   * Context all activity components will run under.
   */
  private ActivityComponentContext componentContext;

  /**
   * The collection of managed resources for the activity.
   */
  private ManagedResources managedResources;

  /**
   * The commands which are being managed.
   */
  private ManagedCommands managedCommands;

  /**
   * Get one of the components for the activity.
   *
   * @param componentName
   *          the name of the component
   * @param <T>
   *            type of activity component retrieved
   *
   * @return the component with the given name, or {@code null} if none
   */
  public <T extends ActivityComponent> T getComponent(String componentName) {
    return components.getActivityComponent(componentName);
  }

  /**
   * Get one of the components for the activity.
   *
   * @param componentName
   *          the name of the component
   * @param <T>
   *            type of activity component retrieved
   *
   * @return the component with the given name
   *
   * @throws SimpleInteractiveSpacesException
   *           if named component is not present
   */
  public <T extends ActivityComponent> T getRequiredComponent(String componentName)
      throws SimpleInteractiveSpacesException {
    return components.getRequiredActivityComponent(componentName);
  }

  /**
   * Add a new managed resource to the activity.
   *
   * @param resource
   *          the resource to add
   */
  public void addManagedResource(ManagedResource resource) {
    managedResources.addResource(resource);
  }

  /**
   * Add a driver to the activity as a {@link ManagedResource}.
   *
   * <p>
   * The drivers
   * {@link Driver#prepare(interactivespaces.system.InteractiveSpacesEnvironment, org.apache.commons.logging.Log)}
   * method will be called with the activity's space environment and log.
   *
   * @param driver
   *          the driver to add to the activity
   */
  public void addDriver(Driver driver) {
    driver.prepare(getSpaceEnvironment(), getConfiguration(), getLog());

    addManagedResource(driver);
  }

  @Override
  public void updateConfiguration(Map<String, Object> update) {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityConfigurationUpdate(update);
    } catch (Exception e) {
      logException("Failure when calling onActivityConfigurationUpdate", e);
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  @Override
  public void startup() {
    long beginStartTime = getSpaceEnvironment().getTimeProvider().getCurrentTime();

    if (getLog().isInfoEnabled()) {
      logConfiguration(ACTIVITY_STARTUP_CONFIG_LOG);
    }

    setActivityStatus(ActivityState.STARTUP_ATTEMPT, null);

    components.setLog(getLog());

    componentContext =
        new ActivityComponentContext(this, components, getController()
            .getActivityComponentFactory());

    managedResources = new ManagedResources(getLog());

    managedCommands = new ManagedCommands(getSpaceEnvironment().getExecutorService(), getLog());

    componentContext.beginStartupPhase();
    try {
      commonActivitySetup();

      callOnActivitySetup();

      managedResources.startupResources();

      components.configureComponents(getConfiguration(), componentContext);
      components.startupComponents();

      commonActivityStartup();

      callOnActivityStartup();

      setCompositeActivityState(ActivityState.RUNNING);

      if (getLog().isInfoEnabled()) {
        getLog().info(
            String.format("Live activity %s running in %d milliseconds", getUuid(),
                (getSpaceEnvironment().getTimeProvider().getCurrentTime() - beginStartTime)));
      }

      // Let everything start running before Post Startup
      componentContext.endStartupPhase(true);

      try {
        commonActivityPostStartup();

        callOnActivityPostStartup();
      } catch (Throwable e) {
        logException("Exception while running Post Startup", e);
      }
    } catch (Throwable e) {
      componentContext.endStartupPhase(false);

      logException("Could not start activity up", e);

      setActivityStatus(ActivityState.STARTUP_FAILURE, null, e);
    }
  }

  /**
   * Get the collection of managed commands.
   *
   * @return the managed commands (will be {@code null} if the activity has not
   *         been started, though will be available for any startup callbacks.
   */
  public ManagedCommands getManagedCommands() {
    return managedCommands;
  }

  /**
   * Properly call {@link #onActivityStartup()}.
   */
  private void callOnActivitySetup() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivitySetup();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Properly call {@link #onActivityStartup()}.
   */
  private void callOnActivityStartup() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityStartup();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Properly call {@link #onActivityPostStartup()}.
   */
  private void callOnActivityPostStartup() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityPostStartup();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  @Override
  public void handleStartupFailure() {
    try {
      callOnActivityStartupFailure();
    } finally {
      shutdown();
    }
  }

  /**
   * Properly call {@link #onActivityStartupFailure}.
   */
  private void callOnActivityStartupFailure() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityStartupFailure();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Any activity specific handling of startup failures.
   */
  public void onActivityStartupFailure() {
    // Default is to do nothing.
  }

  /**
   * Get the active status detail for this activity. By default this is simply
   * an 'active' indicator along with status details from any managed components.
   *
   * @param baseDetail
   *            basic detail for this activity, sans components
   *
   * @return status detail for this activity including components
   */
  protected String getActivityStatusDetailComposite(String baseDetail) {
    StringBuilder detailString = new StringBuilder(baseDetail);
    for (ActivityComponent component : components.getConfiguredComponents()) {
      String detail = component.getComponentStatusDetail();
      if (detail != null) {
        detailString.append(" ").append(detail);
      }
    }
    return detailString.toString();
  }

  @Override
  public void activate() {
    try {
      callOnActivityActivate();

      setCompositeActivityState(ActivityState.ACTIVE);
    } catch (Exception e) {
      logException("Cannot activate activity", e);

      setActivityStatus(ActivityState.ACTIVATE_FAILURE, null, e);
    }
  }

  /**
   * Properly call {@link #onActivityActivate()}.
   */
  private void callOnActivityActivate() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();
    try {
      onActivityActivate();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Activate the activity.
   */
  public void onActivityActivate() {
    // Default is to do nothing.
  }

  @Override
  public boolean isActivated() {
    return ActivityState.ACTIVE.equals(getActivityStatus().getState());
  }

  @Override
  public void deactivate() {
    try {
      callOnActivityDeactivate();

      setCompositeActivityState(ActivityState.RUNNING);
    } catch (Exception e) {
      logException("Cannot deactivate activity", e);

      setActivityStatus(ActivityState.DEACTIVATE_FAILURE, null, e);
    }
  }

  /**
   * Properly call {@link #onActivityDeactivate()}.
   */
  private void callOnActivityDeactivate() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();
    try {
      onActivityDeactivate();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Deactivate the activity.
   */
  public void onActivityDeactivate() {
    // Default is to do nothing.
  }

  @Override
  public void shutdown() {
    Throwable t = null;
    boolean cleanShutdown = true;

    try {
      callOnActivityPreShutdown();
    } catch (Exception e) {
      t = e;
      logException("Error while calling onActivityPreShutdown", e);
      cleanShutdown = false;
    }

    try {
      commonActivityPreShutdown();
    } catch (Exception e) {
      t = e;
      logException("Error while calling commonActivityPreShutdown", e);
      cleanShutdown = false;
    }

    componentContext.beginShutdownPhase();
    boolean handlersAllComplete =
        componentContext.waitOnNoProcessingHandlings(SHUTDOWN_EVENT_HANDLER_COMPLETION_SAMPLE_TIME,
            SHUTDOWN_EVENT_HANDLER_COMPLETION_MAX_SAMPLE_TIME);
    if (!handlersAllComplete) {
      getLog().warn(
          String.format("Handlers still running after %d msecs of shutdown",
              SHUTDOWN_EVENT_HANDLER_COMPLETION_MAX_SAMPLE_TIME));
    }

    if (managedCommands != null) {
      managedCommands.shutdownAll();
      managedCommands = null;
    }

    try {
      if (cleanShutdown) {
        callOnActivityShutdown();
      }
    } catch (Exception e) {
      t = e;
      logException("Error while calling onActivityShutdown", e);
      cleanShutdown = false;
    }

    try {
      if (cleanShutdown) {
        commonActivityShutdown();
      }
    } catch (Exception e) {
      t = e;
      logException("Error while calling commonActivityShutdown", e);
      cleanShutdown = false;
    }

    try {
      callOnActivityCleanup();
    } catch (Exception e) {
      t = e;
      logException("Error while calling onActivityCleanup", e);
      cleanShutdown = false;
    }
    try {
      commonActivityCleanup();
    } catch (Exception e) {
      t = e;
      logException("Error while cleaning up common activity", e);
      cleanShutdown = false;
    }

    try {
      if (!components.shutdownAndClear()) {
        cleanShutdown = false;
      }
    } catch (Exception e) {
      t = e;
      logException("Error while shutting down activity components", e);
      cleanShutdown = false;
    }

    managedResources.shutdownResources();
    managedResources.clear();

    if (cleanShutdown) {
      setActivityStatus(ActivityState.READY, "Post clean shutdown");
    } else {
      setActivityStatus(ActivityState.SHUTDOWN_FAILURE, "Failures during shutdown", t);
    }
  }

  /**
   * Properly call {@link #onActivityPreShutdown()}.
   */
  private void callOnActivityPreShutdown() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityPreShutdown();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Properly call {@link #onActivityShutdown()}.
   */
  private void callOnActivityShutdown() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityShutdown();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Properly call {@link #onActivityCleanup()}.
   */
  private void callOnActivityCleanup() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityCleanup();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  @Override
  public void checkActivityState() {
    // If in startup, don't do a scan
    ActivityState state = getActivityStatus().getState();
    if (state == ActivityState.STARTUP_ATTEMPT || !state.isRunning()) {
      return;
    }

    boolean areAllComponentsRunning = components.areAllComponentsRunning();
    boolean callOnCheckActivityState = callOnCheckActivityState();
    if (!areAllComponentsRunning || !callOnCheckActivityState) {
      // TODO(keith): Figure out if we can get an exception in here
      setActivityStatus(ActivityState.CRASHED, "Activity no longer running");
      getLog().error(
          String.format(
              "Activity marked as CRASHED, components stat %s, onCheckActivityState() %s",
              areAllComponentsRunning, callOnCheckActivityState));

      try {
        callOnActivityFailure();
      } catch (Exception e) {
        logException("Error while calling onActivityFailure", e);
      }
      try {
        callOnActivityCleanup();
      } catch (Exception e) {
        logException("Error while calling onActivityCleanup", e);
      }
      try {
        commonActivityCleanup();
      } catch (Exception e) {
        logException("Error while cleaning up activity", e);
      }
    }
  }

  /**
   * Properly call {@link #onActivityShutdown()}.
   */
  private void callOnActivityFailure() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityFailure();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Call {@link #onActivityCheckState()} properly
   *
   * @return
   */
  private boolean callOnCheckActivityState() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      return onActivityCheckState();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  /**
   * Setup any needed activity components and other startup.
   *
   * <p>
   * This method is not normally used by activity developers, they should
   * install components in {@link #addActivityComponent(ActivityComponent)}.
   * This allows a support base class to add in things unknown to the casual
   * user.
   */
  public void commonActivitySetup() {
    // Default is do nothing.
  }

  /**
   * Any common startup tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  public void commonActivityStartup() {
    // Default is do nothing.
  }

  /**
   * Any common post startup tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  public void commonActivityPostStartup() {
    // Default is do nothing.
  }

  /**
   * Any common activate tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  public void commonActivityActivate() {
    // Default is do nothing.
  }

  /**
   * Any common deactivate tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  public void commonActivityDeactivate() {
    // Default is do nothing.
  }

  /**
   * Any common pre-shutdown tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  public void commonActivityPreShutdown() {
    // Default is do nothing.
  }

  /**
   * Any common shutdown tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only
   * be touched if you know what you are doing.
   */
  public void commonActivityShutdown() {
    // Default is do nothing.
  }

  /**
   * Cleanup any activity in support implementations.
   *
   * <p>
   * This method is not normally used by activity developers, they should clean
   * up their activity in {@link #onActivityCleanup()}. This allows a support
   * base class to add in things unknown to the casual user.
   */
  public void commonActivityCleanup() {
    // Default is do nothing.
  }

  @Override
  public void onActivityConfigurationUpdate(Map<String, Object> update) {
    // Default is nothing on startup.
  }

  @Override
  public void onActivitySetup() {
    // Default is nothing on startup.
  }

  @Override
  public void onActivityStartup() {
    // Default is nothing on startup.
  }

  @Override
  public void onActivityPostStartup() {
    // Default is nothing on post startup.
  }

  @Override
  public void onActivityPreShutdown() {
    // Default is nothing on pre shutdown.
  }

  @Override
  public void onActivityShutdown() {
    // Default is nothing on shutdown.
  }

  @Override
  public <T extends ActivityComponent> T addActivityComponent(T component) {
    components.addComponent(component);

    return component;
  }

  @Override
  public <T extends ActivityComponent> T addActivityComponent(String componentType) {
    ActivityComponentFactory componentFactory = componentContext.getComponentFactory();

    ActivityComponent component = componentFactory.newComponent(componentType);
    components.addComponent(component);

    @SuppressWarnings("unchecked")
    T c = (T) component;
    return c;
  }

  @Override
  public void addActivityComponents(String... componentTypes) {
    ActivityComponentFactory componentFactory = componentContext.getComponentFactory();

    for (String componentType : componentTypes) {
      components.addComponent(componentFactory.newComponent(componentType));
    }
  }

  @Override
  public void onActivityComponentError(ActivityComponent component, String message, Throwable t) {
    // Default is to do nothing. Basic logging is handled elsewhere.
  }

  @Override
  public boolean onActivityCheckState() {
    // Default is all is OK.
    return true;
  }

  @Override
  public void onActivityFailure() {
    // Default is to do nothing.
  }

  @Override
  public void onActivityCleanup() {
    // Default is to do nothing.
  }

  /**
   * Get the component context.
   *
   * @return the component context
   */
  public ActivityComponentContext getActivityComponentContext() {
    return componentContext;
  }

  /**
   * Set the activity status and include detail of internal components.
   *
   * @param status
   *          new status of the activity.
   */
  private void setCompositeActivityState(ActivityState status) {
    setActivityStatus(status, getActivityStatusDetailComposite(status.toString()), null);
  }

  /**
   * Log the activity configuration information to an activity config log file.
   */
  private void logConfiguration(String logFile) {
    try {
      StringBuilder logBuilder = new StringBuilder();
      getLog().info("Logging activity configuration to " + logFile);
      Configuration configuration = getConfiguration();
      Map<String, String> configMap = configuration.getCollapsedMap();
      TreeMap<String, String> sortedMap = new TreeMap<String, String>(configMap);
      for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
        String value;
        try {
          value = configuration.evaluate(entry.getValue());
        } catch (Exception e) {
          value = e.toString();
        }
        logBuilder.append(String.format("%s=%s\n", entry.getKey(), value));
      }
      File configLog = new File(getActivityFilesystem().getLogDirectory(), logFile);
      Files.writeFile(configLog, logBuilder.toString());
    } catch (Exception e) {
      logException("While logging activity configuration", e);
    }
  }
}
