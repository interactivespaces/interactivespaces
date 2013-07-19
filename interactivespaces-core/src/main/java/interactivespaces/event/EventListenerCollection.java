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

package interactivespaces.event;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * A collection of {@link EventListener} instances.
 *
 * <p>
 * Provides useful methods for wotking with the collection.
 *
 * @author Keith M. Hughes
 */
public class EventListenerCollection implements EventSubscriber {

  /**
   * A map of listeners keyed by the event type.
   */
  private SetMultimap<String, EventListener> listeners;

  public EventListenerCollection() {
    listeners = LinkedHashMultimap.create();
    listeners = Multimaps.synchronizedSetMultimap(listeners);
  }

  @Override
  public void addEventListener(String type, EventListener listener) {
    listeners.put(type, listener);
  }

  @Override
  public void removeEventListener(String type, EventListener listener) {
    listeners.remove(type, listener);
  }

  /**
   * Send the event to all listeners in the collection for the event type.
   *
   * @param event
   *          the event to broadcast
   */
  public void broadcastEvent(Event event) {
    for (EventListener listener : listeners.get(event.getType())) {
      listener.onEvent(event);
    }
  }
}
