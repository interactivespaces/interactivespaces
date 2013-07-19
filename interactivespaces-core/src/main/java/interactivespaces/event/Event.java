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

/**
 * An Interactive Spaces event.
 *
 * @author Keith M. Hughes
 */
public class Event {

  /**
   * The event type.
   */
  private String type;

  /**
   * The source of the event.
   */
  private String source;

  /**
   * The data of the event.
   */
  private Object data;

  /**
   *
   * @param type
   *          the type of the event
   * @param source
   *          the source of the event
   * @param data
   *          the data of the event (can be {@link null}
   */
  public Event(String type, String source, Object data) {
    this.type = type;
    this.source = source;
    this.data = data;
  }

  /**
   * Get the type of the event.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Get the source of the event.
   *
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * Get the data for the event.
   *
   * @return the event data packet (can be {@code null})
   */
  @SuppressWarnings("unchecked")
  public <T> T getData() {
    return (T) data;
  }
}
