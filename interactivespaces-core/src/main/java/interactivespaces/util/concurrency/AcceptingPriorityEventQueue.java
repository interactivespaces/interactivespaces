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

package interactivespaces.util.concurrency;

import com.google.common.collect.Queues;

import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;

import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An event queue which will run its event handlers in First in, First Out
 * order. Events can be given a priority. Lower priority values are handled
 * before higher priority values.
 *
 * <p>
 * The event queue can also be set to accepting new events or not.
 *
 * <p>
 * Integrates into Interactive Spaces thread pools.
 *
 * @author Keith M. Hughes
 */
public class AcceptingPriorityEventQueue {

  /**
   * The base priority for the event.
   */
  public static final int DEFAULT_PRIORITY = 10;

  /**
   * {@code true} if the queue is accepting new events.
   */
  private boolean accepting;

  /**
   * The event queue.
   */
  private PriorityBlockingQueue<Event> events = Queues.newPriorityBlockingQueue();

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
   * Lock for adding items to the queue.
   */
  private Object acceptingMutex = new Object();

  /**
   * Lock for starting and stopping the queue.
   */
  private Object runningMutex = new Object();

  public AcceptingPriorityEventQueue(InteractiveSpacesEnvironment spaceEnvironment, Log log) {
    this.spaceEnvironment = spaceEnvironment;
    this.log = log;
  }

  /**
   * Start the event queue processing events which come in.
   *
   * <p>
   * This is independent from accepting new events.
   */
  public void startup() {
    synchronized (runningMutex) {
      if (queueFuture == null) {
        queueFuture = spaceEnvironment.getExecutorService().submit(new Runnable() {
          @Override
          public void run() {
            processEvents();
          }
        });
      }
    }
  }

  /**
   * Stop the event queue from processing events which come in.
   *
   * <p>
   * This is independent from accepting new events.
   */
  public void shutdown() {
    synchronized (runningMutex) {
      if (queueFuture != null) {
        queueFuture.cancel(true);
        queueFuture = null;
      }
    }
  }

  /**
   * Stop accepting new events and shut down the queue.
   */
  public void stopAcceptingAndShutdown() {
    synchronized (runningMutex) {
      setAccepting(false);
      shutdown();
    }
  }

  /**
   * Is the event queue running?
   *
   * @return {@code true} if it is running.
   */
  public boolean isRunning() {
    synchronized (runningMutex) {
      return queueFuture != null;
    }
  }

  /**
   * Set whether the queue is accepting new events or not.
   *
   * @param accepting
   *          {@code true} if accepting new events
   */
  public void setAccepting(boolean accepting) {
    synchronized (acceptingMutex) {
      this.accepting = accepting;
    }
  }

  /**
   * Add a new event to the queue with the default priority
   * {@link #DEFAULT_PRIORITY}.
   *
   * @param event
   *          the new event
   */
  public void addEvent(Runnable event) {
    addEvent(event, DEFAULT_PRIORITY);
  }

  /**
   * Add a new event to the queue.
   *
   * @param event
   *          the new event
   * @param priority
   *          priority of the event, lower values run first
   */
  public void addEvent(Runnable event, int priority) {
    synchronized (acceptingMutex) {
      if (accepting) {
        events.put(new Event(event, priority));
      }
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
   */
  private void processNextEvent() throws InterruptedException {
    try {
      Event event = events.take();

      event.run();
    } catch (InterruptedException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error during event processing", e);
    }
  }

  /**
   * An event in the queue.
   *
   * @author Keith M. Hughes
   */
  private static class Event implements Comparable<Event> {

    /**
     * The sequence of events to support FIFO ordering.
     */
    private static final AtomicLong sequence = new AtomicLong();

    /**
     * The runnable for the event.
     */
    private Runnable runnable;

    /**
     * The priority of the event.
     */
    private int priority;

    /**
     * The sequence number to force FIFO.
     */
    private long sequenceNumber;

    public Event(Runnable runnable, int priority) {
      this.runnable = runnable;
      this.priority = priority;
      this.sequenceNumber = sequence.getAndIncrement();
    }

    /**
     * Run the wrapped runnable.
     */
    public void run() {
      runnable.run();
    }

    @Override
    public int compareTo(Event o) {
      int res = priority - o.priority;
      if (res == 0 && this != o) {
        res = (priority < o.priority) ? -1 : 1;
      }

      return res;
    }
  }
}
