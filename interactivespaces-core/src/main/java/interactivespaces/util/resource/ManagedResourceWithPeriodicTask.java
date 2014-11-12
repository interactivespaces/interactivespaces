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

package interactivespaces.util.resource;

import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.util.events.EventFrequency;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ManagedResource} paired with a {@link Runnable} task that will be run periodically.
 *
 * The resource is started first then the task. When shutting down, the task is shut down first, then the resource.
 *
 * @author Keith M. Hughes
 */
public class ManagedResourceWithPeriodicTask implements ManagedResource {

  /**
   * The resource being managed.
   */
  private ManagedResource resource;

  /**
   * The task to be run.
   */
  private Runnable task;

  /**
   * The period that the task should be run with.
   */
  private long period;

  /**
   * The time unit for the period.
   */
  private TimeUnit unit;

  /**
   * The space environment being run under.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The future for the task.
   */
  private Future<?> future;

  /**
   * Construct a new resource.
   *
   * @param resource
   *          the resource
   * @param task
   *          the task that uses the resource
   * @param frequency
   *          the frequency of repetition
   * @param spaceEnvironment
   *          the space environment being run under
   */
  public ManagedResourceWithPeriodicTask(ManagedResource resource, Runnable task, EventFrequency frequency,
      InteractiveSpacesEnvironment spaceEnvironment) {
    this(resource, task, frequency.getPeriod(), frequency.getUnit(), spaceEnvironment);
  }

  /**
   * Construct a new resource.
   *
   * @param resource
   *          the resource
   * @param task
   *          the task that uses the resource
   * @param period
   *          how often to repeat the task
   * @param unit
   *          time units for the period
   * @param spaceEnvironment
   *          the space environment being run under
   */
  public ManagedResourceWithPeriodicTask(ManagedResource resource, Runnable task, long period, TimeUnit unit,
      InteractiveSpacesEnvironment spaceEnvironment) {
    this.resource = resource;
    this.task = task;
    this.period = period;
    this.unit = unit;
    this.spaceEnvironment = spaceEnvironment;
  }

  @Override
  public void startup() {
    resource.startup();

    future = spaceEnvironment.getExecutorService().scheduleAtFixedRate(task, 0, period, unit);
  }

  @Override
  public void shutdown() {
    if (future != null) {
      future.cancel(true);
      future = null;
    }

    resource.shutdown();
    resource = null;
  }
}
