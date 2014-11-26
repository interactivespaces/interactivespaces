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

package interactivespaces.util.events;

import java.util.concurrent.TimeUnit;

/**
 * A delay after which an event should take place.
 *
 * @author Keith M. Hughes
 */
public class EventDelay {

  /**
   * One second in milliseconds.
   */
  public static final double ONE_SECOND_IN_MILLISECONDS = 1000.0;

  /**
   * One minute in milliseconds.
   */
  public static final double ONE_MINUTE_IN_MILLISECONDS = 60000.0;

  /**
   * Get a delay in seconds.
   *
   * @param seconds
   *          the delay in seconds
   *
   * @return the delay
   */
  public static EventDelay seconds(double seconds) {
    return new EventDelay((long) (ONE_SECOND_IN_MILLISECONDS * seconds), TimeUnit.MILLISECONDS);
  }

  /**
   * Get a delay in minutes.
   *
   * @param minutes
   *          the delay in minutes
   *
   * @return the delay
   */
  public static EventDelay minutes(double minutes) {
    return new EventDelay((long) (ONE_MINUTE_IN_MILLISECONDS * minutes), TimeUnit.MILLISECONDS);
  }

  /**
   * The delay for the event.
   */
  private final long delay;

  /**
   * The time unit for the delay.
   */
  private final TimeUnit unit;

  /**
   * Construct a new frequency.
   *
   * @param delay
   *          how often things will be repeated
   * @param unit
   *          the time units for the delay
   */
  public EventDelay(long delay, TimeUnit unit) {
    this.delay = delay;
    this.unit = unit;
  }

  /**
   * Get the delay.
   *
   * @return the delay
   */
  public long getDelay() {
    return delay;
  }

  /**
   * Get the time unit of the delay.
   *
   * @return the time unit
   */
  public TimeUnit getUnit() {
    return unit;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (delay ^ (delay >>> 32));
    result = prime * result + unit.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EventDelay other = (EventDelay) obj;
    if (delay != other.delay) {
      return false;
    }
    if (unit != other.unit) {
      return false;
    }

    return true;
  }
}
