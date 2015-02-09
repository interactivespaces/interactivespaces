/*
 * Copyright (C) 2015 Google Inc.
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

package interactivespaces.liveactivity.runtime;

import interactivespaces.activity.binary.NativeActivityRunnerFactory;
import interactivespaces.activity.binary.SimpleNativeActivityRunnerFactory;
import interactivespaces.activity.component.ActivityComponentFactory;
import interactivespaces.activity.component.SimpleActivityComponentFactory;
import interactivespaces.activity.component.binary.BasicNativeActivityComponent;
import interactivespaces.activity.component.binary.NativeActivityComponent;
import interactivespaces.activity.component.ros.BasicRosActivityComponent;
import interactivespaces.activity.component.ros.RosActivityComponent;
import interactivespaces.activity.component.route.ros.RosMessageRouterActivityComponent;
import interactivespaces.activity.component.web.BasicWebBrowserActivityComponent;
import interactivespaces.activity.component.web.BasicWebServerActivityComponent;
import interactivespaces.activity.component.web.WebBrowserActivityComponent;
import interactivespaces.activity.component.web.WebServerActivityComponent;
import interactivespaces.liveactivity.runtime.activity.wrapper.internal.bridge.topic.TopicBridgeActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.activity.wrapper.internal.interactivespaces.InteractiveSpacesNativeActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.activity.wrapper.internal.osnative.NativeActivityWrapperFactory;
import interactivespaces.liveactivity.runtime.activity.wrapper.internal.web.WebActivityWrapperFactory;
import interactivespaces.system.InteractiveSpacesEnvironment;

import org.osgi.framework.BundleContext;

/**
 * A factory for creating various components for a live activity runtime.
 *
 * <p>
 * This is to ensure consistency between the various types of runtimes and controllers.
 *
 * @author Keith M. Hughes
 */
public class StandardLiveActivityRuntimeComponentFactory implements LiveActivityRuntimeComponentFactory {

  /**
   * Run under a space environment.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The bundle context.
   */
  private BundleContext bundleContext;

  /**
   * Construct a new factory.
   *
   * @param spaceEnvironment
   *          the space environment to use
   * @param bundleContext
   *          the bundle context to use
   */
  public StandardLiveActivityRuntimeComponentFactory(InteractiveSpacesEnvironment spaceEnvironment,
      BundleContext bundleContext) {
    this.spaceEnvironment = spaceEnvironment;
    this.bundleContext = bundleContext;
  }

  /**
   * Create a new live activity runner factory.
   *
   * @return a new live activity runner factory
   */
  @Override
  public LiveActivityRunnerFactory newLiveActivityRunnerFactory() {
    LiveActivityRunnerFactory liveActivityRunnerFactory =
        new StandardLiveActivityRunnerFactory(spaceEnvironment.getLog());
    liveActivityRunnerFactory.registerActivityWrapperFactory(new NativeActivityWrapperFactory());
    liveActivityRunnerFactory.registerActivityWrapperFactory(new WebActivityWrapperFactory());
    liveActivityRunnerFactory.registerActivityWrapperFactory(new TopicBridgeActivityWrapperFactory());
    liveActivityRunnerFactory.registerActivityWrapperFactory(new InteractiveSpacesNativeActivityWrapperFactory(
        bundleContext));

    return liveActivityRunnerFactory;
  }

  /**
   * Create a new native activity runner factory.
   *
   * @return a new native activity runner factory
   */
  @Override
  public NativeActivityRunnerFactory newNativeActivityRunnerFactory() {
    return new SimpleNativeActivityRunnerFactory(spaceEnvironment);
  }

  /**
   * Create a new activity component factory.
   *
   * @return a new activity component factory
   */
  @Override
  public ActivityComponentFactory newActivityComponentFactory() {
    SimpleActivityComponentFactory factory = new SimpleActivityComponentFactory();

    factory.register(NativeActivityComponent.COMPONENT_NAME, BasicNativeActivityComponent.class);
    factory.register(RosActivityComponent.COMPONENT_NAME, BasicRosActivityComponent.class);
    factory.register(RosMessageRouterActivityComponent.COMPONENT_NAME, RosMessageRouterActivityComponent.class);
    factory.register(WebBrowserActivityComponent.COMPONENT_NAME, BasicWebBrowserActivityComponent.class);
    factory.register(WebServerActivityComponent.COMPONENT_NAME, BasicWebServerActivityComponent.class);

    return factory;
  }
}
