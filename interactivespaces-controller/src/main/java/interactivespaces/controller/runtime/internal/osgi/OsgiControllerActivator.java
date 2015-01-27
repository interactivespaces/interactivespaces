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

package interactivespaces.controller.runtime.internal.osgi;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.binary.SimpleNativeActivityRunnerFactory;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.wrapper.internal.bridge.topic.TopicBridgeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.interactivespaces.InteractiveSpacesNativeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.osnative.NativeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.web.WebActivityWrapperFactory;
import interactivespaces.controller.client.node.ActiveControllerActivityFactory;
import interactivespaces.controller.resource.deployment.ContainerResourceDeploymentManager;
import interactivespaces.controller.resource.deployment.ControllerContainerResourceDeploymentManager;
import interactivespaces.controller.runtime.FileSystemSpaceControllerInfoPersister;
import interactivespaces.controller.runtime.SpaceControllerActivityInstallationManager;
import interactivespaces.controller.runtime.SpaceControllerDataBundleManager;
import interactivespaces.controller.runtime.StandardSpaceController;
import interactivespaces.controller.runtime.StandardSpaceControllerDataBundleManager;
import interactivespaces.controller.runtime.configuration.StandardSpaceControllerConfigurationManager;
import interactivespaces.controller.runtime.internal.SimpleSpaceControllerActivityInstallationManager;
import interactivespaces.controller.runtime.logging.LoggingAlertStatusManager;
import interactivespaces.controller.runtime.ros.RosSpaceControllerCommunicator;
import interactivespaces.controller.ui.internal.osgi.OsgiControllerShell;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.liveactivity.runtime.LiveActivityRunnerFactory;
import interactivespaces.liveactivity.runtime.LiveActivityStorageManager;
import interactivespaces.liveactivity.runtime.SimpleActivityInstallationManager;
import interactivespaces.liveactivity.runtime.SimpleLiveActivityStorageManager;
import interactivespaces.liveactivity.runtime.StandardLiveActivityRunnerFactory;
import interactivespaces.liveactivity.runtime.StandardLiveActivityRuntime;
import interactivespaces.liveactivity.runtime.configuration.PropertyFileLiveActivityConfigurationManager;
import interactivespaces.liveactivity.runtime.installation.ActivityInstallationManager;
import interactivespaces.liveactivity.runtime.logging.InteractiveSpacesEnvironmentLiveActivityLogFactory;
import interactivespaces.liveactivity.runtime.repository.LocalLiveActivityRepository;
import interactivespaces.liveactivity.runtime.repository.internal.file.FileLocalLiveActivityRepository;
import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.system.resources.ContainerResourceManager;
import interactivespaces.util.concurrency.SequentialEventQueue;
import interactivespaces.util.concurrency.SimpleSequentialEventQueue;

import org.ros.osgi.common.RosEnvironment;

/**
 * An OSGi activator for an Interactive Spaces space controller.
 *
 * @author Keith M. Hughes
 */
public class OsgiControllerActivator extends InteractiveSpacesServiceOsgiBundleActivator {

  /**
   * OSGi service tracker for the interactive spaces control.
   */
  private MyServiceTracker<InteractiveSpacesSystemControl> interactiveSpacesSystemControlTracker;

  /**
   * OSGi service tracker for the ROS environment.
   */
  private MyServiceTracker<RosEnvironment> rosEnvironmentTracker;

  /**
   * OSGi service tracker for the expression evaluator factory.
   */
  private MyServiceTracker<ExpressionEvaluatorFactory> expressionEvaluatorFactoryTracker;

  /**
   * OSGi service tracker for the container resource manager.
   */
  private MyServiceTracker<ContainerResourceManager> containerResourceManagerTracker;

  /**
   * The space environment for this controller.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * Activity factory for the controller.
   */
  private LiveActivityRunnerFactory liveActivityRunnerFactory;

  /**
   * Native activity runner factory.
   */
  private SimpleNativeActivityRunnerFactory nativeActivityRunnerFactory;

  @Override
  public void onStart() {
    interactiveSpacesSystemControlTracker = newMyServiceTracker(InteractiveSpacesSystemControl.class.getName());

    rosEnvironmentTracker = newMyServiceTracker(RosEnvironment.class.getName());

    expressionEvaluatorFactoryTracker = newMyServiceTracker(ExpressionEvaluatorFactory.class.getName());

    containerResourceManagerTracker = newMyServiceTracker(ContainerResourceManager.class.getName());
  }

  @Override
  protected void allRequiredServicesAvailable() {
    initializeBaseSpaceControllerComponents();

    String controllerMode =
        spaceEnvironment.getSystemConfiguration().getPropertyString(
            SpaceController.CONFIGURATION_INTERACTIVESPACES_CONTROLLER_MODE,
            SpaceController.CONFIGURATION_VALUE_STANDARD_CONTROLLER_MODE);
    if (SpaceController.CONFIGURATION_VALUE_STANDARD_CONTROLLER_MODE.equals(controllerMode)) {
      activateStandardSpaceController();
    } else {
      spaceEnvironment.getLog().info("Not activating standard space controller, mode is " + controllerMode);
    }
  }

  /**
   * Initialize all the base components for this controller, which are then available to any controller that may be
   * instantiated.
   */
  private void initializeBaseSpaceControllerComponents() {
    spaceEnvironment = getInteractiveSpacesEnvironmentTracker().getMyService();

    liveActivityRunnerFactory = new StandardLiveActivityRunnerFactory(spaceEnvironment.getLog());
    liveActivityRunnerFactory.registerActivityWrapperFactory(new NativeActivityWrapperFactory());
    liveActivityRunnerFactory.registerActivityWrapperFactory(new WebActivityWrapperFactory());
    liveActivityRunnerFactory.registerActivityWrapperFactory(new TopicBridgeActivityWrapperFactory());
    liveActivityRunnerFactory.registerActivityWrapperFactory(new InteractiveSpacesNativeActivityWrapperFactory(
        getBundleContext()));
    registerOsgiFrameworkService(LiveActivityRunnerFactory.class.getName(), liveActivityRunnerFactory);
    registerOsgiFrameworkService(ActiveControllerActivityFactory.class.getName(), liveActivityRunnerFactory);

    nativeActivityRunnerFactory = new SimpleNativeActivityRunnerFactory(spaceEnvironment);
    registerOsgiFrameworkService(NativeActivityRunnerFactory.class.getName(), nativeActivityRunnerFactory);
  }

  /**
   * Initialize components that are necessary only to the {@link StandardSpaceController}, and then instantiate and
   * register the space controller itself..
   */
  private void activateStandardSpaceController() {
    InteractiveSpacesSystemControl spaceSystemControl = interactiveSpacesSystemControlTracker.getMyService();
    RosEnvironment rosEnvironment = rosEnvironmentTracker.getMyService();
    ExpressionEvaluatorFactory expressionEvaluatorFactory = expressionEvaluatorFactoryTracker.getMyService();

    ContainerResourceManager containerResourceManager = containerResourceManagerTracker.getMyService();

    ContainerResourceDeploymentManager containerResourceDeploymentManager =
        new ControllerContainerResourceDeploymentManager(containerResourceManager, spaceEnvironment);
    addManagedResource(containerResourceDeploymentManager);

    LiveActivityStorageManager liveActivityStorageManager = new SimpleLiveActivityStorageManager(spaceEnvironment);
    addManagedResource(liveActivityStorageManager);

    LocalLiveActivityRepository liveActivityRepository =
        new FileLocalLiveActivityRepository(liveActivityStorageManager, spaceEnvironment);
    addManagedResource(liveActivityRepository);

    PropertyFileLiveActivityConfigurationManager liveActivityConfigurationManager =
        new PropertyFileLiveActivityConfigurationManager(expressionEvaluatorFactory, spaceEnvironment);

    ActivityInstallationManager activityInstallationManager =
        new SimpleActivityInstallationManager(liveActivityRepository, liveActivityStorageManager, spaceEnvironment);
    addManagedResource(activityInstallationManager);

    SpaceControllerActivityInstallationManager controllerActivityInstaller =
        new SimpleSpaceControllerActivityInstallationManager(activityInstallationManager, spaceEnvironment);
    addManagedResource(controllerActivityInstaller);

    InteractiveSpacesEnvironmentLiveActivityLogFactory activityLogFactory =
        new InteractiveSpacesEnvironmentLiveActivityLogFactory(spaceEnvironment);

    RosSpaceControllerCommunicator spaceControllerCommunicator =
        new RosSpaceControllerCommunicator(controllerActivityInstaller, containerResourceDeploymentManager,
            rosEnvironment, spaceEnvironment);

    SpaceControllerDataBundleManager dataBundleManager = new StandardSpaceControllerDataBundleManager();
    dataBundleManager.setActivityStorageManager(liveActivityStorageManager);

    StandardSpaceControllerConfigurationManager spaceControllerConfigurationManager =
        new StandardSpaceControllerConfigurationManager(spaceEnvironment);
    addManagedResource(spaceControllerConfigurationManager);

    SequentialEventQueue eventQueue = new SimpleSequentialEventQueue(spaceEnvironment, spaceEnvironment.getLog());
    addManagedResource(eventQueue);
    StandardLiveActivityRuntime liveActivityRuntime =
        new StandardLiveActivityRuntime(liveActivityRunnerFactory, nativeActivityRunnerFactory, liveActivityRepository,
            activityInstallationManager, activityLogFactory, liveActivityConfigurationManager,
            liveActivityStorageManager, new LoggingAlertStatusManager(spaceEnvironment.getLog()), eventQueue,
            spaceEnvironment);
    addManagedResource(liveActivityRuntime);

    StandardSpaceController spaceController =
        new StandardSpaceController(spaceControllerCommunicator, new FileSystemSpaceControllerInfoPersister(),
            spaceSystemControl, dataBundleManager, spaceControllerConfigurationManager, liveActivityRuntime,
            eventQueue, spaceEnvironment);
    addManagedResource(spaceController);

    OsgiControllerShell controllerShell =
        new OsgiControllerShell(spaceController, spaceSystemControl, liveActivityRepository, getBundleContext());
    addManagedResource(controllerShell);
  }
}
