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

import java.util.Map;

/**
 * A listener for pointable events from the gesture camera.
 *
 * @author Keith M. Hughes
 */
public interface GesturePointableListener {

  /**
   * A new set of pointables have come in.
   *
   * <p>
   * The map is not modifiable.
   *
   * @param pointables
   *        the pointables obtained indexed by their ID.
   */
  void onGesturePointables(Map<String, GesturePointable> pointables);
}
