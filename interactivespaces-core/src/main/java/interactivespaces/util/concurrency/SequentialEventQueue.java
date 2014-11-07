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

package interactivespaces.util.concurrency;

import interactivespaces.util.resource.ManagedResource;

/**
 * An event queue that processes events in the order they are received.
 * <p>
 * Any events which have not processed are not guaranteed to be processed or finished processing on shutdown
 *
 * @author Keith M. Hughes
 */
public interface SequentialEventQueue extends ManagedResource {

  /**
   * Add a new event to the queue.
   *
   * @param event
   *          the new event
   */
  void addEvent(Runnable event);
}
