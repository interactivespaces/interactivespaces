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

package org.ros.concurrent;

/**
 * A queue that blocks when attempting to remove.
 *
 * <p>
 * It is up to the implementation if there is blocking on put().
 *
 * @author Keith M. Hughes
 */
public interface MessageBlockingQueue<T> {

  /**
   * Take an entry from the queue.
   *
   * <p>
   * This will block if nothing is available.
   *
   * @return the item to send
   */
  T take() throws InterruptedException;

  /**
   * Take an entry from the queue if there is anything to take.
   *
   * <p>
   * This will not block if nothing is available.
   *
   * @return the head of the queue, or {@code null} if nothing in the queue
   */
  T poll();

  /**
   * Put an item into the queue.
   *
   * <p>
   * It is up to the implementation to decide what to do if the queue is full.
   *
   * @param item
   *          the item to add
   */
  void put(T item) throws InterruptedException;

  /**
   * Is the queue empty?
   *
   * @return {@code true} if empty
   */
  boolean isEmpty();
}
