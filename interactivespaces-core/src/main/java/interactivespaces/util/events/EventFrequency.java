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
 * A frequency with which things should be repeated.
 *
 * @author Keith M. Hughes
 */
public class EventFrequency {

  /**
   * One second in milliseconds.
   */
  public static final double ONE_SECOND_IN_MILLISECONDS = 1000.0;

  /**
   * One minute in milliseconds.
   */
  public static final double ONE_MINUTE_IN_MILLISECONDS = 60000.0;

  /**
   * Get a frequency for events per second.
   *
   * @param eventsPerSecond
   *          the events per second desired
   *
   * @return the frequency in events per second
   */
  public static EventFrequency eventsPerSecond(double eventsPerSecond) {
    return new EventFrequency((long) (ONE_SECOND_IN_MILLISECONDS / eventsPerSecond), TimeUnit.MILLISECONDS);
  }

  /**
   * Get a frequency for events per minute.
   *
   * @param eventsPerMinute
   *          the events per second desired
   *
   * @return the frequency in events per second
   */
  public static EventFrequency eventsPerMinute(double eventsPerMinute) {
    return new EventFrequency((long) (ONE_MINUTE_IN_MILLISECONDS / eventsPerMinute), TimeUnit.MILLISECONDS);
  }

  /**
   * How frequently things should be repeated.
   */
  private final long period;

  /**
   * The time unit for the period.
   */
  private final TimeUnit unit;

  /**
   * Construct a new frequency.
   *
   * @param period
   *          how often things will be repeated
   * @param unit
   *          the time units for the period
   */
  public EventFrequency(long period, TimeUnit unit) {
    this.period = period;
    this.unit = unit;
  }

  /**
   * Get the period.
   *
   * @return the period
   */
  public long getPeriod() {
    return period;
  }

  /**
   * Get the time unit of the period.
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
    result = prime * result + (int) (period ^ (period >>> 32));
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
    EventFrequency other = (EventFrequency) obj;
    if (period != other.period) {
      return false;
    }
    if (unit != other.unit) {
      return false;
    }

    return true;
  }
}
