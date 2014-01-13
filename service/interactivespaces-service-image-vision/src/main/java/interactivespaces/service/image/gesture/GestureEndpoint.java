/*
 * Copyright (C) 2013 Google Inc.
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

package interactivespaces.service.image.gesture;

import interactivespaces.util.resource.ManagedResource;

/**
 * A connection to a gesture device.
 *
 * @author Keith M. Hughes
 */
public interface GestureEndpoint extends ManagedResource {

  /**
   * Add a new pointable listener.
   *
   * @param listener
   *          the listener to add
   */
  void addPointableListener(GesturePointableListener listener);

  /**
   * Remove a pointable listener.
   *
   * <p>
   * This does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removePointableListener(GesturePointableListener listener);

  /**
   * Add a new hand listener.
   *
   * @param listener
   *          the listener to add
   */
  void addHandListener(GestureHandListener listener);

  /**
   * Remove a hand listener.
   *
   * <p>
   * This does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeHandListener(GestureHandListener listener);

  /**
   * Add a new gesture listener.
   *
   * @param listener
   *          the listener to add
   */
  void addGestureListener(GestureListener listener);

  /**
   * Remove a gesture listener.
   *
   * <p>
   * This does nothing if the listener was never added.
   *
   * @param listener
   *          the listener to remove
   */
  void removeGestureListener(GestureListener listener);
}
