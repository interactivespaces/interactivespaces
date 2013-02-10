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

package org.ros.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author damonkohler@google.com (Damon Kohler)
 * 
 * @param <T>
 *            the listener type
 */
public class EventDispatcher<T> extends CancellableLoop {

	private final T listener;
	private final MessageBlockingQueue<SignalRunnable<T>> events;

	private final CountDownLatch fullyShutdownLatch = new CountDownLatch(1);
	
	private AtomicBoolean isShuttingDown = new AtomicBoolean(false);

	public EventDispatcher(T listener, int queueCapacity) {
		this.listener = listener;
		events = MessageBlockingQueueFactory.newMessageBlockingQueue(queueCapacity, false);
	}

	public void signal(final SignalRunnable<T> signalRunnable) {
		if (isShuttingDown.get()) {
			try {
				fullyShutdownLatch.await();
			} catch (InterruptedException e) {
				// Don't care
			}
			
			// Just in case something came in while we were waiting.
			// Done this way so always in order it was supposed to happen.
			try {
				events.put(signalRunnable);
			} catch (InterruptedException e) {
				// Don't care.
			}
			flush();
		} else {
			try {
				events.put(signalRunnable);
			} catch (InterruptedException e) {
				// Don't care.
			}
		}
	}

	@Override
	public void loop() throws InterruptedException {
		SignalRunnable<T> signalRunnable = events.take();
		signalRunnable.run(listener);
	}

	@Override
	protected void cleanup() {
		isShuttingDown.set(true);
		flush();
		fullyShutdownLatch.countDown();
	}

	/**
	 * Flush all events out of the event dispatcher.
	 * 
	 * <p>
	 * This is run after the dispatcher is shut down and empties out any events
	 * which have not been written.
	 */
	private void flush() {
		while (!events.isEmpty()) {
			try {
				SignalRunnable<T> signalRunnable = events.take();
				signalRunnable.run(listener);
			} catch (InterruptedException e) {
				// Don't care
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}