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

package interactivespaces.activity.component.binary;

import interactivespaces.InteractiveSpacesException;
import interactivespaces.SimpleInteractiveSpacesException;
import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityFilesystem;
import interactivespaces.activity.binary.NativeActivityRunner;
import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SystemConfiguration;
import interactivespaces.controller.SpaceController;
import interactivespaces.util.process.NativeApplicationRunner;
import interactivespaces.util.process.NativeApplicationRunnerListener;
import interactivespaces.util.process.restart.RestartStrategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * An {@link ActivityComponent} which launches native applications.
 *
 * @author Keith M. Hughes
 */
public class BasicNativeActivityComponent extends BaseActivityComponent implements NativeActivityComponent {

  /**
   * Control of the native activity.
   */
  private NativeActivityRunner nativeActivity;

  /**
   * The name of the configuration flag for getting the path to the executable.
   */
  private String executablePathProperty = CONFIGURATION_ACTIVITY_EXECUTABLE;

  /**
   * The name of the configuration flag for getting the executable flags.
   */
  private String executableFlagsProperty = CONFIGURATION_ACTIVITY_EXECUTABLE_FLAGS;

  /**
   * The restart strategy to use when the runner is finally created.
   */
  private RestartStrategy<NativeApplicationRunner> restartStrategy;

  /**
   * The listeners to add to the runner when it is created.
   */
  private List<NativeApplicationRunnerListener> listeners = Lists.newArrayList();

  /**
   * Create the component which uses the properties
   * {@link #CONFIGURATION_ACTIVITY_EXECUTABLE} and
   * {@link #CONFIGURATION_ACTIVITY_EXECUTABLE_FLAGS} for execution path and
   * flags.
   */
  public BasicNativeActivityComponent() {
  }

  /**
   * Create the component with the properties to use for execution path and
   * flags.
   *
   * @param executablePathProperty
   *          config property name for the executable path
   * @param executableFlagsProperty
   *          config property prefix for the executable flags
   */
  public BasicNativeActivityComponent(String executablePathProperty, String executableFlagsProperty) {
    this.executablePathProperty = executablePathProperty;
    this.executableFlagsProperty = executableFlagsProperty;
  }

  @Override
  public String getName() {
    return COMPONENT_NAME;
  }

  @Override
  public void configureComponent(Configuration configuration) {
    super.configureComponent(configuration);

    Activity activity = componentContext.getActivity();
    SpaceController controller = activity.getController();
    String os =
        controller.getSpaceEnvironment().getSystemConfiguration()
            .getRequiredPropertyString(SystemConfiguration.PLATFORM_OS);

    Map<String, Object> appConfig = Maps.newHashMap();
    String activityPath = configuration.getRequiredPropertyString(executablePathProperty + "." + os);

    File activityFile = new File(activityPath);
    if (activityFile.isAbsolute()) {

      if (!isAppAlowed(activityPath)) {
        throw new SimpleInteractiveSpacesException(String.format("Not allowed to run %s", activityPath));
      }
    } else {
      ActivityFilesystem activityFilesystem = activity.getActivityFilesystem();
      activityFile = new File(activityFilesystem.getInstallDirectory(), activityPath);
      if (isLocalToActivityInstallDirectory(activityFilesystem, activityFile)) {
        if (activityFile.exists()) {
          if (!activityFile.canExecute()) {
            activityFile.setExecutable(true);
          }
        } else {
          throw new SimpleInteractiveSpacesException(String.format("The native executable %s does not exist", activityPath));
        }
      } else {
        throw new InteractiveSpacesException(String.format("The native executable %s is not local to the activity",
            activityPath));
      }
    }

    appConfig.put(NativeActivityRunner.EXECUTABLE_PATHNAME, activityFile.getAbsolutePath());

    String commandFlags = configuration.getRequiredPropertyString(executableFlagsProperty + "." + os);

    appConfig.put(NativeActivityRunner.EXECUTABLE_FLAGS, commandFlags);

    nativeActivity = controller.getNativeActivityRunnerFactory().newPlatformNativeActivityRunner(activity.getLog());
    nativeActivity.configure(appConfig);

    if (restartStrategy != null) {
      nativeActivity.setRestartStrategy(restartStrategy);
    }

    for (NativeApplicationRunnerListener listener: listeners) {
      nativeActivity.addNativeApplicationRunnerListener(listener);
    }
  }

  @Override
  public void startupComponent() {
    nativeActivity.startup();
  }

  @Override
  public void shutdownComponent() {
    if (nativeActivity != null) {
      nativeActivity.shutdown();
      nativeActivity = null;
    }
  }

  @Override
  public boolean isComponentRunning() {
    if (nativeActivity != null) {
      return nativeActivity.isRunning();
    } else {
      return false;
    }
  }

  @Override
  public NativeActivityRunner getNativeActivityRunner() {
    return nativeActivity;
  }

  @Override
  public void setRestartStrategy(RestartStrategy<NativeApplicationRunner> restartStrategy) {
    this.restartStrategy = restartStrategy;
  }

  @Override
  public void addNativeApplicationRunnerListener(NativeApplicationRunnerListener listener) {
    listeners.add(listener);
  }

  /**
   * Is the application file local to the application directory?
   *
   * @param activityFilesystem
   *          file system for the activity
   * @param applicationFile
   *          the application file being checked
   *
   * @return {@code true} if the file is local to the application install
   *         directory
   */
  private boolean isLocalToActivityInstallDirectory(ActivityFilesystem activityFilesystem, File applicationFile) {

    try {
      String applicationInstallationDirectory =
          activityFilesystem.getInstallDirectory().getCanonicalPath() + File.separatorChar;
      return applicationFile.getCanonicalPath().startsWith(applicationInstallationDirectory);
    } catch (IOException e) {
      throw new InteractiveSpacesException(String.format("Could not canonical path for %s",
          applicationFile.getAbsolutePath()), e);
    }
  }

  /**
   * Is the application allowable?
   *
   * @param applicationPath
   *          path to the application
   *
   * @return {@code true} if allowed to run the app
   */
  private boolean isAppAlowed(String applicationPath) {
    // TODO(keith): Put a real check in here. May want a file containing
    // allowed applications.
    return true;
  }
}
