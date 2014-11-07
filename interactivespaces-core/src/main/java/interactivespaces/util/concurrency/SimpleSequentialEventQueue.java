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

import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An event queue which will run its event handlers in First in, First Out order.
 *
 * @author Keith M. Hughes
 */
public class SimpleSequentialEventQueue implements SequentialEventQueue {

  /**
   * The list of events to process.
   */
  private BlockingQueue<Runnable> events = new LinkedBlockingQueue<Runnable>();

  /**
   * The queue future, used for shutting the queue down.
   */
  private Future<?> queueFuture;

  /**
   * The space environment to run under.
   */
  private InteractiveSpacesEnvironment spaceEnvironment;

  /**
   * The logger for errors.
   */
  private Log log;

  /**
   * Construct a new event queue.
   *
   * @param spaceEnvironment
   *          the space environment the queue will use
   * @param log
   *          the logger to use
   */
  public SimpleSequentialEventQueue(InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;
  }

  @Override
  public void startup() {
    queueFuture = spaceEnvironment.getExecutorService().submit(new Runnable() {
      @Override
      public void run() {
        processEvents();
      }
    });
  }

  @Override
  public void shutdown() {
    if (queueFuture != null) {
      queueFuture.cancel(true);
      queueFuture = null;

      events.clear();
    }
  }

  @Override
  public void addEvent(Runnable event) {
    try {
      events.put(event);
    } catch (InterruptedException e) {
      // Don't care
    }
  }

  /**
   * Process events until the event processing thread is interrupted.
   */
  private void processEvents() {
    try {
      while (!Thread.interrupted()) {
        processNextEvent();
      }
    } catch (InterruptedException e) {
      // Don't care
    }
  }

  /**
   * Process the next event.
   *
   * @throws InterruptedException
   *           the thread got interrupted
   */
  private void processNextEvent() throws InterruptedException {
    try {
      Runnable event = events.take();

      event.run();
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error during event processing", e);
    }
  }
}
