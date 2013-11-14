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

package interactivespaces.activity;

import interactivespaces.activity.component.ActivityComponent;

import java.util.Map;

/**
 * Some extra methods added onto {@link Activity} implementations which are
 * using the support classes supplied.
 *
 * <p>
 * Call back methods are called in the following sequence during startup.
 *
 * <ul>
 * <li>{@link #onActivitySetup()} is called before any components are
 * configured. This is a good time to call
 * {@link #addActivityComponent(ActivityComponent)} or
 * {@link #addActivityComponent(String...)}</li>
 * <li>{@link #onActivityStartup()} is called after all components have been
 * configured.</li>
 * </ul>
 *
 * @author Keith M. Hughes
 */
public interface SupportedActivity extends Activity {

  /**
   * Called during the setup of the activity.
   *
   * <p>
   * This method should throw an exception if it can't set up. Any exceptions
   * thrown will be caught.
   */
  void onActivitySetup();

  /**
   * Called during the startup of the activity.
   *
   * <p>
   * This method should throw an exception if it can't start. Any exceptions
   * thrown will be caught.
   */
  void onActivityStartup();

  /**
   * Called after the startup of the activity.
   *
   * <p>
   * Once this is called, the activity is assumed to be fully configured,
   * initialized, and running. There can be some race conditions with native
   * activity startup (they may not be fully started, so waiting until you
   * receive websocket or other connections is wise when using native
   * compoenents), so do be aware of this.
   *
   * <p>
   * The activity will be considered running even if this method throws an
   * exception. The exception will be properly logged.
   */
  void onActivityPostStartup();

  /**
   * Called before the shutdown of the activity.
   *
   * <p>
   * This method should throw an exception if it can't shutdown. Any exceptions
   * thrown will be caught.
   */
  void onActivityPreShutdown();

  /**
   * Called during the shutdown of the activity.
   *
   * <p>
   * This method should throw an exception if it can't shutdown. Any exceptions
   * thrown will be caught.
   */
  void onActivityShutdown();

  /**
   * Called during the activation of the activity.
   *
   * <p>
   * This method should throw an exception if it can't activate. Any exceptions
   * thrown will be caught.
   */
  void onActivityActivate();

  /**
   * Called during the deactivation of the activity.
   *
   * <p>
   * This method should throw an exception if it can't deactivate. Any
   * exceptions thrown will be caught.
   */
  void onActivityDeactivate();

  /**
   * Something in the activity has failed. This can be any installed components
   * or something the user has set up.
   */
  void onActivityFailure();

  /**
   * The activity has shut down either due to a shutdown or by activity failure.
   * It should clean up all resources.
   */
  void onActivityCleanup();

  /**
   * This method will be called when the activity state is being checked by the
   * controller.
   *
   * <p>
   * This method should not change the activity state, it should just return
   * whether or not the activity is doing what it is supposed to in its current
   * state.
   *
   * @return {@code true} if the activity is fine, false otherwise.
   */
  boolean onActivityCheckState();

  /**
   * A configuration update is coming in.
   *
   * @param update
   *          the full update
   */
  void onActivityConfigurationUpdate(Map<String, Object> update);

  /**
   * Add a new component to the activity.
   *
   * @param component
   *          the component to add
   * @param <T>
   *          specific activity component type
   *
   * @return the component just added
   */
  <T extends ActivityComponent> T addActivityComponent(T component);

  /**
   * Add a new component to the activity.
   *
   * @param componentType
   *          the type of the component to add
   * @param <T>
   *          specific activity component type
   *
   * @return created activity component
   */
  <T extends ActivityComponent> T addActivityComponent(String componentType);

  /**
   * Add a set of new components to the activity.
   *
   * @param componentTypes
   *          the types of the components to add
   */
  void addActivityComponents(String... componentTypes);

  /**
   * Handle an error from an activity component.
   *
   * @param component
   *          the source component
   * @param message
   *          error message
   * @param t
   *          triggering source of error or {@code null}
   */
  void onActivityComponentError(ActivityComponent component, String message, Throwable t);
}
