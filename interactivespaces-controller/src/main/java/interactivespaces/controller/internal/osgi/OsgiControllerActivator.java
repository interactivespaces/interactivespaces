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

package interactivespaces.controller.internal.osgi;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.binary.SimpleNativeActivityRunnerFactory;
import interactivespaces.controller.SpaceController;
import interactivespaces.controller.activity.configuration.PropertyFileLiveActivityConfigurationManager;
import interactivespaces.controller.activity.installation.ActivityInstallationManager;
import interactivespaces.controller.activity.wrapper.internal.bridge.topic.TopicBridgeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.interactivespaces.InteractiveSpacesNativeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.osnative.NativeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.web.WebActivityWrapperFactory;
import interactivespaces.controller.client.node.ActiveControllerActivityFactory;
import interactivespaces.controller.client.node.ActivityStorageManager;
import interactivespaces.controller.client.node.ControllerDataBundleManager;
import interactivespaces.controller.client.node.FileSystemSpaceControllerInfoPersister;
import interactivespaces.controller.client.node.SimpleActivityInstallationManager;
import interactivespaces.controller.client.node.SimpleActivityStorageManager;
import interactivespaces.controller.client.node.SpaceControllerActivityInstallationManager;
import interactivespaces.controller.client.node.SpaceControllerDataBundleManager;
import interactivespaces.controller.client.node.StandardSpaceController;
import interactivespaces.controller.client.node.internal.SimpleActiveControllerActivityFactory;
import interactivespaces.controller.client.node.internal.SimpleSpaceControllerActivityInstallationManager;
import interactivespaces.controller.client.node.ros.RosSpaceControllerCommunicator;
import interactivespaces.controller.logging.InteractiveSpacesEnvironmentActivityLogFactory;
import interactivespaces.controller.repository.LocalSpaceControllerRepository;
import interactivespaces.controller.repository.internal.file.FileLocalSpaceControllerRepository;
import interactivespaces.controller.resource.deployment.ContainerResourceDeploymentManager;
import interactivespaces.controller.resource.deployment.ControllerContainerResourceDeploymentManager;
import interactivespaces.controller.ui.internal.osgi.OsgiControllerShell;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;
import interactivespaces.system.resources.ContainerResourceManager;

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
  private ActiveControllerActivityFactory controllerActivityFactory;

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

    String controllerMode = spaceEnvironment.getSystemConfiguration().getPropertyString(
        SpaceController.CONFIGURATION_INTERACTIVESPACES_CONTROLLER_MODE,
        SpaceController.CONFIGURATION_VALUE_STANDARD_CONTROLLER_MODE);
    if (SpaceController.CONFIGURATION_VALUE_STANDARD_CONTROLLER_MODE.equals(controllerMode)) {
      activateStandardSpaceController();
    } else {
      spaceEnvironment.getLog().info("Not activating standard space controller, mode is " + controllerMode);
    }
  }

  /**
   * Initialize all the base components for this controller, which are then available to any controller
   * that may be instantiated.
   */
  private void initializeBaseSpaceControllerComponents() {
    spaceEnvironment = getInteractiveSpacesEnvironmentTracker().getMyService();

    controllerActivityFactory = new SimpleActiveControllerActivityFactory();
    controllerActivityFactory.registerActivityWrapperFactory(new NativeActivityWrapperFactory());
    controllerActivityFactory.registerActivityWrapperFactory(new WebActivityWrapperFactory());
    controllerActivityFactory.registerActivityWrapperFactory(new TopicBridgeActivityWrapperFactory());
    controllerActivityFactory.registerActivityWrapperFactory(new InteractiveSpacesNativeActivityWrapperFactory(
        getBundleContext()));
    registerOsgiFrameworkService(ActiveControllerActivityFactory.class.getName(), controllerActivityFactory);

    nativeActivityRunnerFactory = new SimpleNativeActivityRunnerFactory(spaceEnvironment);
    registerOsgiFrameworkService(NativeActivityRunnerFactory.class.getName(), nativeActivityRunnerFactory);
  }

  /**
   * Initialize components that are necessary only to the {@link StandardSpaceController}, and then instantiate
   * and register the space controller itself..
   */
  private void activateStandardSpaceController() {
    InteractiveSpacesSystemControl spaceSystemControl = interactiveSpacesSystemControlTracker.getMyService();
    RosEnvironment rosEnvironment = rosEnvironmentTracker.getMyService();
    ExpressionEvaluatorFactory expressionEvaluatorFactory = expressionEvaluatorFactoryTracker.getMyService();

    ContainerResourceManager containerResourceManager = containerResourceManagerTracker.getMyService();

    ContainerResourceDeploymentManager containerResourceDeploymentManager =
        new ControllerContainerResourceDeploymentManager(containerResourceManager, spaceEnvironment);
    addManagedResource(containerResourceDeploymentManager);

    ActivityStorageManager activityStorageManager = new SimpleActivityStorageManager(spaceEnvironment);
    addManagedResource(activityStorageManager);

    LocalSpaceControllerRepository controllerRepository =
        new FileLocalSpaceControllerRepository(activityStorageManager, spaceEnvironment);
    addManagedResource(controllerRepository);

    PropertyFileLiveActivityConfigurationManager activityConfigurationManager =
        new PropertyFileLiveActivityConfigurationManager(expressionEvaluatorFactory, spaceEnvironment);

    ActivityInstallationManager activityInstallationManager =
        new SimpleActivityInstallationManager(controllerRepository, activityStorageManager, spaceEnvironment);
    addManagedResource(activityInstallationManager);

    SpaceControllerActivityInstallationManager controllerActivityInstaller =
        new SimpleSpaceControllerActivityInstallationManager(activityInstallationManager, spaceEnvironment);
    addManagedResource(controllerActivityInstaller);

    InteractiveSpacesEnvironmentActivityLogFactory activityLogFactory =
        new InteractiveSpacesEnvironmentActivityLogFactory(spaceEnvironment);

    RosSpaceControllerCommunicator spaceControllerCommunicator =
        new RosSpaceControllerCommunicator(controllerActivityInstaller, containerResourceDeploymentManager,
            rosEnvironment, spaceEnvironment);

    ControllerDataBundleManager dataBundleManager = new SpaceControllerDataBundleManager();

    SpaceController spaceController =
        new StandardSpaceController(activityInstallationManager, controllerRepository, controllerActivityFactory,
            nativeActivityRunnerFactory, activityConfigurationManager, activityStorageManager, activityLogFactory,
            spaceControllerCommunicator, new FileSystemSpaceControllerInfoPersister(), spaceSystemControl,
            dataBundleManager, spaceEnvironment);
    addManagedResource(spaceController);

    OsgiControllerShell controllerShell =
        new OsgiControllerShell(spaceController, spaceSystemControl, controllerRepository, getBundleContext());
    addManagedResource(controllerShell);
  }
}
