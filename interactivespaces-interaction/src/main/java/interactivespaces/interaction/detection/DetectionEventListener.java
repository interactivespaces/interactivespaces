/*
 * Copyright (C) 2014 Google Inc.
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

package interactivespaces.interaction.detection;

import java.util.Collection;

/**
 * A listener for events which are detected.
 *
 * @param <EventSource>
 *          the type for the event source
 * @param <EventData>
 *          the type for the detection event data
 *
 * @author Keith M. Hughes
 */
public interface DetectionEventListener<EventSource, EventData> {

  /**
   * A new detection event has happened.
   *
   * @param source
   *          source of the event detection
   * @param events
   *          the list of event data
   */
  void onNewDetectionEvent(EventSource source, Collection<EventData> events);
}
