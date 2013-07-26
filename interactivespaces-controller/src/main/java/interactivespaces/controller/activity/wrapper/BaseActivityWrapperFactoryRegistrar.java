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

package interactivespaces.controller.activity.wrapper;

import interactivespaces.controller.client.node.ActiveControllerActivityFactory;

/**
 * A helper class for registering {@link ActivityWrapperFactory} instances.
 *
 * @author Keith M. Hughes
 */
public abstract class BaseActivityWrapperFactoryRegistrar {

  /**
   * The wrapper factory to be handed out.
   */
  private ActivityWrapperFactory wrapperFactory;

  /**
   * Activity factory we are registered with.
   */
  protected ActiveControllerActivityFactory liveControllerActivityFactory;

  /**
   * Start up the registrar.
   */
  public void startup() {
    wrapperFactory = newWrapperFactory();
  }

  /**
   * Create a new instance of the wrapper factory.
   *
   * @return a new instance of the wrapper factory
   */
  public abstract ActivityWrapperFactory newWrapperFactory();

  /**
   * Shut the registrar down.
   */
  public void shutdown() {
    unregisterWrapperFactory();
    wrapperFactory = null;
  }

  /**
   * Set the activity factory which will use the wrapper factory.
   *
   * @param liveControllerActivityFactory
   */
  public void setActiveControllerActivityFactory(
      ActiveControllerActivityFactory liveControllerActivityFactory) {
    this.liveControllerActivityFactory = liveControllerActivityFactory;

    registerWrapperFactory();
  }

  /**
   * Remove the activity factory which used the wrapper factory.
   *
   * @param liveControllerActivityFactory
   */
  public void unsetActiveControllerActivityFactory(
      ActiveControllerActivityFactory liveControllerActivityFactory) {
    this.liveControllerActivityFactory = null;

    unregisterWrapperFactory();
  }

  /**
   * Register the wrapper factory instance.
   */
  protected void registerWrapperFactory() {
    if (wrapperFactory != null && liveControllerActivityFactory != null) {
      liveControllerActivityFactory.registerActivityWrapperFactory(wrapperFactory);
    }
  }

  /**
   * Unregister the wrapper factory instance.
   */
  protected void unregisterWrapperFactory() {
    if (wrapperFactory != null && liveControllerActivityFactory != null) {
      liveControllerActivityFactory.unregisterActivityWrapperFactory(wrapperFactory);
    }
  }

}