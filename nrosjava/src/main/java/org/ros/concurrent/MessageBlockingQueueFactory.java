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

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Class for getting a {@link MessagelockingQueue}.
 * 
 * @author Keith M. Hughes
 */
public class MessageBlockingQueueFactory {

	/**
	 * Get a new {@link MessageBlockingQueue}.
	 * 
	 * @param capacity
	 *            base capacity of the queue
	 * @param finite
	 *            {@true} if the queue should be finite and drop messages when
	 *            full
	 * 
	 * @return an implementation giving the requested features
	 */
	public static <T> MessageBlockingQueue<T> newMessageBlockingQueue(
			final int capacity, boolean finite) {
		if (finite) {
			return new MessageBlockingQueue<T>() {
				// The circular queue
				private CircularBlockingDeque<T> deque = new CircularBlockingDeque<T>(
						capacity);

				@Override
				public T take() throws InterruptedException {
					return deque.takeFirst();
				}

				@Override
				public void put(T entry) {
					deque.addLast(entry);
				}

				@Override
				public boolean isEmpty() {
					return deque.isEmpty();
				}
			};
		} else {
			return new MessageBlockingQueue<T>() {
				// The circular queue
				private ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<T>(
						capacity);

				@Override
				public T take() throws InterruptedException {
					return queue.take();
				}

				@Override
				public void put(T entry) throws InterruptedException {
					queue.put(entry);
				}

				@Override
				public boolean isEmpty() {
					return queue.isEmpty();
				}
			};
		}
	}
}
