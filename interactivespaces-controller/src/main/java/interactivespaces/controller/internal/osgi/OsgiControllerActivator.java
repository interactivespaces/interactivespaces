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

import interactivespaces.activity.binary.SimpleNativeActivityRunnerFactory;
import interactivespaces.controller.activity.configuration.PropertyFileLiveActivityConfigurationManager;
import interactivespaces.controller.activity.wrapper.internal.bridge.topic.TopicBridgeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.interactivespaces.InteractiveSpacesNativeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.osnative.NativeActivityWrapperFactory;
import interactivespaces.controller.activity.wrapper.internal.web.WebActivityWrapperFactory;
import interactivespaces.controller.client.node.ActiveControllerActivityFactory;
import interactivespaces.controller.client.node.ControllerDataBundleManager;
import interactivespaces.controller.client.node.FileSystemSpaceControllerInfoPersister;
import interactivespaces.controller.client.node.SimpleActivityInstallationManager;
import interactivespaces.controller.client.node.SimpleActivityStorageManager;
import interactivespaces.controller.client.node.SpaceControllerDataBundleManager;
import interactivespaces.controller.client.node.StandardSpaceController;
import interactivespaces.controller.client.node.internal.SimpleSpaceControllerActivityInstallationManager;
import interactivespaces.controller.client.node.internal.osgi.SimpleActiveControllerActivityFactory;
import interactivespaces.controller.client.node.ros.RosSpaceControllerCommunicator;
import interactivespaces.controller.logging.InteractiveSpacesEnvironmentActivityLogFactory;
import interactivespaces.controller.repository.internal.file.FileLocalSpaceControllerRepository;
import interactivespaces.controller.ui.internal.osgi.OsgiControllerShell;
import interactivespaces.evaluation.ExpressionEvaluatorFactory;
import interactivespaces.osgi.service.InteractiveSpacesServiceOsgiBundleActivator;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.system.InteractiveSpacesSystemControl;

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
   * The storage manager for activities.
   */
  private SimpleActivityStorageManager activityStorageManager;

  /**
   * The controller repository.
   */
  private FileLocalSpaceControllerRepository controllerRepository;

  /**
   * The activity installation manager.
   */
  private SimpleActivityInstallationManager activityInstallationManager;

  /**
   * The controller side of an activity installation manager.
   */
  private SimpleSpaceControllerActivityInstallationManager controllerActivityInstaller;

  /**
   * The actual space controller itself.
   */
  private StandardSpaceController spaceController;

  /**
   * The OSGi shell for the controller.
   */
  private OsgiControllerShell controllerShell;

  @Override
  public void onStart() {
    interactiveSpacesSystemControlTracker =
        newMyServiceTracker(InteractiveSpacesSystemControl.class.getName());

    rosEnvironmentTracker = newMyServiceTracker(RosEnvironment.class.getName());

    expressionEvaluatorFactoryTracker =
        newMyServiceTracker(ExpressionEvaluatorFactory.class.getName());
  }

  @Override
  public void onStop() {
    if (controllerShell != null) {
      controllerShell.deactivate();
      controllerShell = null;
    }

    if (spaceController != null) {
      spaceController.shutdown();
      spaceController = null;
    }

    if (activityInstallationManager != null) {
      activityInstallationManager.shutdown();
      activityInstallationManager = null;
    }

    if (controllerActivityInstaller != null) {
      controllerActivityInstaller.shutdown();
      controllerActivityInstaller = null;
    }

    if (activityStorageManager != null) {
      activityStorageManager.shutdown();
      activityStorageManager = null;
    }

    if (controllerRepository != null) {
      controllerRepository.shutdown();
      controllerRepository = null;
    }

  }

  @Override
  protected void allRequiredServicesAvailable() {
    InteractiveSpacesEnvironment spaceEnvironment =
        getInteractiveSpacesEnvironmentTracker().getMyService();
    InteractiveSpacesSystemControl spaceSystemControl =
        interactiveSpacesSystemControlTracker.getMyService();
    RosEnvironment rosEnvironment = rosEnvironmentTracker.getMyService();
    ExpressionEvaluatorFactory expressionEvaluatorFactory =
        expressionEvaluatorFactoryTracker.getMyService();

    activityStorageManager = new SimpleActivityStorageManager(spaceEnvironment);
    activityStorageManager.startup();

    controllerRepository =
        new FileLocalSpaceControllerRepository(activityStorageManager, spaceEnvironment);
    controllerRepository.startup();

    PropertyFileLiveActivityConfigurationManager activityConfigurationManager =
        new PropertyFileLiveActivityConfigurationManager(expressionEvaluatorFactory,
            spaceEnvironment);

    activityInstallationManager =
        new SimpleActivityInstallationManager(controllerRepository, activityStorageManager,
            spaceEnvironment);
    activityInstallationManager.startup();

    SimpleActiveControllerActivityFactory controllerActivityFactory =
        new SimpleActiveControllerActivityFactory();
    controllerActivityFactory.registerActivityWrapperFactory(new NativeActivityWrapperFactory());
    controllerActivityFactory.registerActivityWrapperFactory(new WebActivityWrapperFactory());
    controllerActivityFactory
        .registerActivityWrapperFactory(new TopicBridgeActivityWrapperFactory());

    controllerActivityFactory
        .registerActivityWrapperFactory(new InteractiveSpacesNativeActivityWrapperFactory(
            getBundleContext()));

    registerOsgiFrameworkService(ActiveControllerActivityFactory.class.getName(),
        controllerActivityFactory);

    controllerActivityInstaller =
        new SimpleSpaceControllerActivityInstallationManager(activityInstallationManager,
            spaceEnvironment);
    controllerActivityInstaller.startup();

    SimpleNativeActivityRunnerFactory nativeActivityRunnerFactory =
        new SimpleNativeActivityRunnerFactory(spaceEnvironment);

    InteractiveSpacesEnvironmentActivityLogFactory activityLogFactory =
        new InteractiveSpacesEnvironmentActivityLogFactory(spaceEnvironment);

    RosSpaceControllerCommunicator spaceControllerCommunicator =
        new RosSpaceControllerCommunicator(controllerActivityInstaller, rosEnvironment,
            spaceEnvironment);

    ControllerDataBundleManager dataBundleManager = new SpaceControllerDataBundleManager();

    spaceController =
        new StandardSpaceController(activityInstallationManager, controllerRepository,
            controllerActivityFactory, nativeActivityRunnerFactory, activityConfigurationManager,
            activityStorageManager, activityLogFactory, spaceControllerCommunicator,
            new FileSystemSpaceControllerInfoPersister(), spaceSystemControl, spaceEnvironment,
            dataBundleManager);
    spaceController.startup();

    controllerShell =
        new OsgiControllerShell(spaceController, spaceSystemControl, controllerRepository,
            getBundleContext());
    controllerShell.startup();
  }
}
