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

import interactivespaces.InteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.SupportedActivity;
import interactivespaces.activity.annotation.ConfigurationPropertyAnnotationProcessor;
import interactivespaces.activity.annotation.StandardConfigurationPropertyAnnotationProcessor;
import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.activity.component.ActivityComponentContext;
import interactivespaces.activity.execution.ActivityMethodInvocation;
import interactivespaces.configuration.Configuration;
import interactivespaces.hardware.driver.Driver;
import interactivespaces.util.concurrency.ManagedCommands;
import interactivespaces.util.concurrency.SimpleManagedCommands;
import interactivespaces.util.io.FileSupport;
import interactivespaces.util.io.FileSupportImpl;
import interactivespaces.util.resource.ManagedResource;
import interactivespaces.util.resource.ManagedResources;

import com.google.common.collect.Maps;

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
   * How long to wait between samples when checking for event handlers to complete, in msecs.
   */
  private static final int SHUTDOWN_EVENT_HANDLER_COMPLETION_SAMPLE_TIME = 500;

  /**
   * Filename for activity startup config log.
   */
  private static final String ACTIVITY_STARTUP_CONFIG_LOG = "startup.conf";

  /**
   * Display header to use for the activity status.
   */
  public static final String ACTIVITY_STATUS_HEADER = "Activity Status";

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
  private SimpleManagedCommands managedCommands;

  /**
   * File support for use with the activity.
   */
  private final FileSupport fileSupport = FileSupportImpl.INSTANCE;

  /**
   * The annotation processor for configuration parameters.
   */
  private ConfigurationPropertyAnnotationProcessor configurationAnnotationProcessor;

  @Override
  public <T extends ActivityComponent> T getActivityComponent(String componentType) {
    return componentContext.getActivityComponent(componentType);
  }

  /**
   * Get the activity component with the specified type.
   *
   * @param componentType
   *          the component type
   * @param <T>
   *          type of the activity component
   *
   * @return the component with the specified type, or {@code null} if none registered
   *
   * @deprecated Use {@link #getActivityComponent(String)}.
   */
  @Deprecated
  public <T extends ActivityComponent> T getComponent(String componentType) {
    return getActivityComponent(componentType);
  }

  @Override
  public <T extends ActivityComponent> T getRequiredActivityComponent(String componentType)
      throws InteractiveSpacesException {
    return componentContext.getRequiredActivityComponent(componentType);
  }

  @Override
  public <T extends ActivityComponent> T addActivityComponent(T component) {
    return componentContext.addComponent(component);
  }

  @Override
  public void addActivityComponents(ActivityComponent... components) {
    componentContext.addComponents(components);
  }

  @Override
  public <T extends ActivityComponent> T addActivityComponent(String componentType) {
    return componentContext.addComponent(componentType);
  }

  @Override
  public void addActivityComponents(String... componentTypes) {
    componentContext.addComponents(componentTypes);
  }

  @Override
  public ConfigurationPropertyAnnotationProcessor getActivityConfigurationPropertyAnnotationProcessor() {
    return configurationAnnotationProcessor;
  }

  /**
   * Get the component context.
   *
   * @return the component context
   */
  public ActivityComponentContext getActivityComponentContext() {
    return componentContext;
  }

  @Override
  public void addManagedResource(ManagedResource resource) {
    managedResources.addResource(resource);
  }

  @Override
  public ManagedCommands getManagedCommands() {
    return managedCommands;
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
  public void updateConfiguration(Map<String, String> update) {
    try {
      handleUpdateConfigurationUnprotected(update);
    } catch (Throwable e) {
      logException("Failure when calling onActivityConfiguration", e);
    }
  }

  /**
   * Do a configuration update unprotected by an exception handler.
   *
   * @param update
   *          the update, can be {@code null}
   */
  private void handleUpdateConfigurationUnprotected(Map<String, String> update) {
    commonActivityConfigurationUpdate(update);

    callOnActivityConfigurationUpdate(update);
  }

  /**
   * Properly call {@link #onActivityConfiguration(Map)}.
   *
   * @param update
   *          the update map
   */
  private void callOnActivityConfigurationUpdate(Map<String, String> update) {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityConfigurationUpdate(update);
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
  }

  @Override
  public void startup() {
    long beginStartTime = getSpaceEnvironment().getTimeProvider().getCurrentTime();

    setActivityStatus(ActivityState.STARTUP_ATTEMPT, null);

    componentContext = new ActivityComponentContext(this, getActivityRuntime().getActivityComponentFactory());

    managedResources = new ManagedResources(getLog());

    managedCommands = new SimpleManagedCommands(getSpaceEnvironment().getExecutorService(), getLog());

    componentContext.beginStartupPhase();
    try {
      getConfiguration().setValue(Activity.CONFIGURATION_PROPERTY_ACTIVITY_UUID, getUuid().replace("-", "_"));

      callOnActivityPreSetup();

      logConfiguration(ACTIVITY_STARTUP_CONFIG_LOG);

      configurationAnnotationProcessor =
          new StandardConfigurationPropertyAnnotationProcessor(getConfiguration(), getLog());
      configurationAnnotationProcessor.process(this);

      handleUpdateConfigurationUnprotected(null);

      commonActivitySetup();

      callOnActivitySetup();

      managedResources.startupResources();

      componentContext.initialStartupComponents();

      commonActivityStartup();

      callOnActivityStartup();

      setCompositeActivityState(ActivityState.RUNNING);

      if (getLog().isInfoEnabled()) {
        getLog().info(
            String.format("Live activity %s running in %d milliseconds", getUuid(), (getSpaceEnvironment()
                .getTimeProvider().getCurrentTime() - beginStartTime)));
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
      componentContext.shutdownAndClearRunningComponents();
      componentContext.endStartupPhase(false);
      logException("Could not startup activity", e);

      setActivityStatus(ActivityState.STARTUP_FAILURE, null, e);
    }
  }

  /**
   * Properly call {@link #onActivityPreSetup()}.
   */
  private void callOnActivityPreSetup() {
    ActivityMethodInvocation invocation = getExecutionContext().enterMethod();

    try {
      onActivityPreSetup();
    } finally {
      getExecutionContext().exitMethod(invocation);
    }
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
   * Get the active status detail for this activity. By default this is simply an 'active' indicator along with status
   * details from any managed components.
   *
   * @param baseDetail
   *          basic detail for this activity, sans components
   *
   * @return status detail for this activity including components
   */
  protected String getActivityStatusDetailComposite(String baseDetail) {
    StringBuilder detailString = new StringBuilder();
    detailString.append(String.format(StatusDetail.HEADER_FORMAT, "status-detail"))
        .append(String.format(StatusDetail.PREFIX_FORMAT, "activity-status")).append(ACTIVITY_STATUS_HEADER)
        .append(StatusDetail.SEPARATOR).append(baseDetail).append(StatusDetail.POSTFIX);
    for (ActivityComponent component : componentContext.getConfiguredComponents()) {
      String detail = component.getComponentStatusDetail();
      if (detail != null) {
        detailString.append(String.format(StatusDetail.PREFIX_FORMAT, "component-status"))
            .append(component.getDescription()).append(StatusDetail.SEPARATOR).append(detail)
            .append(StatusDetail.POSTFIX);
      }
    }

    detailString.append(String.format(StatusDetail.PREFIX_FORMAT, "managed-resources")).append("Managed Resources")
        .append(StatusDetail.SEPARATOR);
    for (ManagedResource managedResource : managedResources.getResources()) {
      detailString.append(managedResource.toString()).append(StatusDetail.BREAK);
    }
    detailString.append(StatusDetail.POSTFIX);

    detailString.append(StatusDetail.FOOTER);
    return detailString.toString();
  }

  @Override
  public void activate() {
    try {
      commonActivityActivate();

      callOnActivityActivate();

      setCompositeActivityState(ActivityState.ACTIVE);
    } catch (Throwable e) {
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
  @Override
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
      commonActivityDeactivate();

      callOnActivityDeactivate();

      setCompositeActivityState(ActivityState.RUNNING);
    } catch (Throwable e) {
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
  @Override
  public void onActivityDeactivate() {
    // Default is to do nothing.
  }

  @Override
  public void shutdown() {
    Throwable t = null;
    boolean cleanShutdown = true;

    try {
      callOnActivityPreShutdown();
    } catch (Throwable e) {
      t = e;
      logException("Error while calling onActivityPreShutdown", e);
      cleanShutdown = false;
    }

    try {
      commonActivityPreShutdown();
    } catch (Throwable e) {
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
    } catch (Throwable e) {
      t = e;
      logException("Error while calling onActivityShutdown", e);
      cleanShutdown = false;
    }

    try {
      if (cleanShutdown) {
        commonActivityShutdown();
      }
    } catch (Throwable e) {
      t = e;
      logException("Error while calling commonActivityShutdown", e);
      cleanShutdown = false;
    }

    try {
      callOnActivityCleanup();
    } catch (Throwable e) {
      t = e;
      logException("Error while calling onActivityCleanup", e);
      cleanShutdown = false;
    }
    try {
      commonActivityCleanup();
    } catch (Throwable e) {
      t = e;
      logException("Error while cleaning up common activity", e);
      cleanShutdown = false;
    }

    try {
      if (!componentContext.shutdownAndClearRunningComponents()) {
        cleanShutdown = false;
      }
    } catch (Throwable e) {
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

    boolean areAllComponentsRunning = componentContext.areAllComponentsRunning();
    boolean callOnCheckActivityState = callOnCheckActivityState();
    if (!areAllComponentsRunning || !callOnCheckActivityState) {
      // TODO(keith): Figure out if we can get an exception in here
      setActivityStatus(ActivityState.CRASHED, "Activity no longer running");
      getLog().error(
          String.format("Activity marked as CRASHED, components stat %s, onCheckActivityState() %s",
              areAllComponentsRunning, callOnCheckActivityState));

      try {
        callOnActivityFailure();
      } catch (Throwable e) {
        logException("Error while calling onActivityFailure", e);
      }
      try {
        callOnActivityCleanup();
      } catch (Throwable e) {
        logException("Error while calling onActivityCleanup", e);
      }
      try {
        commonActivityCleanup();
      } catch (Throwable e) {
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
   * Call {@link #onActivityCheckState()} properly.
   *
   * @return {@code true} if the activity is running correctly
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
   * Perform any common tasks for activity configuration.
   *
   * <p>
   * This method is not normally used by activity developers. This should only be touched if you know what you are
   * doing.
   *
   * @param update
   *          the update map
   */
  public void commonActivityConfigurationUpdate(Map<String, String> update) {
    // Default is to do nothing.
  }

  /**
   * Setup any needed activity components and other startup.
   *
   * <p>
   * This method is not normally used by activity developers, they should install components in
   * {@link #addActivityComponent(ActivityComponent)}. This allows a support base class to add in things unknown to the
   * casual user.
   */
  public void commonActivitySetup() {
    // Default is do nothing.
  }

  /**
   * Any common startup tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only be touched if you know what you are
   * doing.
   */
  public void commonActivityStartup() {
    // Default is do nothing.
  }

  /**
   * Any common post startup tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only be touched if you know what you are
   * doing.
   */
  public void commonActivityPostStartup() {
    // Default is do nothing.
  }

  /**
   * Any common activate tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only be touched if you know what you are
   * doing.
   */
  public void commonActivityActivate() {
    // Default is do nothing.
  }

  /**
   * Any common deactivate tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only be touched if you know what you are
   * doing.
   */
  public void commonActivityDeactivate() {
    // Default is do nothing.
  }

  /**
   * Any common pre-shutdown tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only be touched if you know what you are
   * doing.
   */
  public void commonActivityPreShutdown() {
    // Default is do nothing.
  }

  /**
   * Any common shutdown tasks.
   *
   * <p>
   * This method is not normally used by activity developers. This should only be touched if you know what you are
   * doing.
   */
  public void commonActivityShutdown() {
    // Default is do nothing.
  }

  /**
   * Cleanup any activity in support implementations.
   *
   * <p>
   * This method is not normally used by activity developers, they should clean up their activity in
   * {@link #onActivityCleanup()}. This allows a support base class to add in things unknown to the casual user.
   */
  public void commonActivityCleanup() {
    // Default is do nothing.
  }

  @Override
  public void onActivityConfigurationUpdate(Map<String, String> update) {
    // Default is to call the old method.
    Map<String, Object> remap = null;

    if (update != null) {
      remap = Maps.newHashMap();
      remap.putAll(update);
    }

    onActivityConfiguration(remap);
  }

  @Override
  public void onActivityConfiguration(Map<String, Object> update) {
    // Default is to do nothing.
  }

  @Override
  public void onActivityPreSetup() {
    // Default is to do nothing.
  }

  @Override
  public void onActivitySetup() {
    // Default is to do nothing.
  }

  @Override
  public void onActivityStartup() {
    // Default is to do nothing.
  }

  @Override
  public void onActivityPostStartup() {
    // Default is to do nothing.
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
   *
   * @param logFile
   *          file to write the log into
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
        } catch (Throwable e) {
          value = e.toString();
        }
        logBuilder.append(String.format("%s=%s\n", entry.getKey(), value));
      }
      File configLog = new File(getActivityFilesystem().getLogDirectory(), logFile);
      fileSupport.writeFile(configLog, logBuilder.toString());
    } catch (Throwable e) {
      logException("While logging activity configuration", e);
    }
  }
}
