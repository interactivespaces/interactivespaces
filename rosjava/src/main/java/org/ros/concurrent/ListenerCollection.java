package org.ros.concurrent;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages a collection of listeners and makes it easy to execute a listener
 * callback on the entire collection.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class ListenerCollection<T> {

	private final ScheduledExecutorService executorService;
	private final Collection<T> listeners;

	/**
	 * The list of runnables to process if serialized.
	 */
	private final BlockingQueue<Runnable> serializedRunnables = new LinkedBlockingQueue<Runnable>();

	/**
	 * {@code true} if the event listeners should be called in a synchronized
	 * manner. This means that any signal event will be queued to happen in
	 * order.
	 */
	private final boolean serialize;

	/**
	 * The future if the collection is serialized.
	 */
	private Future<?> serializeFuture;

	public interface SignalRunnable<U> {
		void run(U listener);
	}

	/**
	 * @param executorService
	 *            the {@link ScheduledExecutorService} to use when executing
	 *            listener callbacks
	 */
	public ListenerCollection(ScheduledExecutorService executorService) {
		this(null, executorService);
	}

	/**
	 * @param executorService
	 *            the {@link ScheduledExecutorService} to use when executing
	 *            listener callbacks
	 * @param serialize
	 *            {@code true} if the event listeners should be called in a
	 *            synchronized manner. This means that any signal event will be
	 *            queued to happen in order.
	 */
	public ListenerCollection(ScheduledExecutorService executorService,
			boolean serialize) {
		this(null, executorService, serialize);
	}

	/**
	 * This will not serialize listener notifications.
	 * 
	 * @param listeners
	 *            an initial {@link Collection} of listeners to add
	 * @param executorService
	 *            the {@link ScheduledExecutorService} to use when executing
	 *            listener callbacks
	 */
	public ListenerCollection(Collection<T> listeners,
			ScheduledExecutorService executorService) {
		this(listeners, executorService, false);
	}

	/**
	 * @param listeners
	 *            an initial {@link Collection} of listeners to add
	 * @param executorService
	 *            the {@link ScheduledExecutorService} to use when executing
	 *            listener callbacks
	 * @param serialize
	 *            {@code true} if the event listeners should be called in a
	 *            synchronized manner. This means that any signal event will be
	 *            queued to happen in order.
	 */
	public ListenerCollection(Collection<T> listeners,
			ScheduledExecutorService executorService, boolean serialize) {
		this.executorService = executorService;
		this.listeners = new CopyOnWriteArrayList<T>();
		if (listeners != null) {
			addAll(listeners);
		}
		this.serialize = serialize;

		if (serialize) {
			serializeFuture = executorService.submit(new Runnable() {
				@Override
				public void run() {
					handleSerializedSignals();
				}
			});
		}
	}

	/**
	 * @param listener
	 *            the listener to add
	 */
	public void add(T listener) {
		listeners.add(listener);
	}

	/**
	 * @param listeners
	 *            a {@link Collection} of listeners to add
	 */
	public void addAll(Collection<T> listeners) {
		this.listeners.addAll(listeners);
	}

	/**
	 * @param listener
	 *            the listener to remove
	 */
	public void remove(T listener) {
		listeners.remove(listener);
	}

	/**
	 * Removes all listeners.
	 */
	public void clear() {
		listeners.clear();
	}

	/**
	 * Shut down the collection.
	 * 
	 * <p>
	 * This is only needed if the collection is serialized. But it is harmless
	 * to call if the collection isn't serialized.
	 */
	public void shutdown() {
		if (serialize) {
			serializeFuture.cancel(true);
		}
	}

	/**
	 * Signal all listeners.
	 * 
	 * <p>
	 * Each {@link SignalRunnable} is executed in a separate thread.
	 */
	public void signal(SignalRunnable<T> signalRunnable) {
		try {
			signal(signalRunnable, 0, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// We don't wait for completion so we can ignore the unlikely
			// InterruptedException thrown by CountDownLatch.await().
		}
	}

	/**
	 * Signal all listeners and wait for the all {@link SignalRunnable}s to
	 * return.
	 * 
	 * <p>
	 * Each {@link SignalRunnable} is executed in a separate thread.
	 */
	public void signal(final SignalRunnable<T> signalRunnable, long timeout,
			TimeUnit unit) throws InterruptedException {
		CountDownLatch latch;
		if (serialize) {
			latch = signalSerialized(signalRunnable);
		} else {
			latch = signalConcurrent(signalRunnable);
		}
		latch.await(timeout, unit);
	}

	/**
	 * Signal all listeners concurrently.
	 * 
	 * @param signalRunnable
	 *            the runnable to run
	 * 
	 * @return the latch to wait for all to finish.
	 */
	private CountDownLatch signalConcurrent(
			final SignalRunnable<T> signalRunnable) {
		final CountDownLatch latch = new CountDownLatch(listeners.size());
		for (final T listener : listeners) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						signalRunnable.run(listener);
					} catch (Exception e) {
						// TODO(keith): Should log
					}
					latch.countDown();
				}
			});
		}
		return latch;
	}

	/**
	 * Signal all listeners concurrently.
	 * 
	 * @param signalRunnable
	 *            the runnable to run
	 * 
	 * @return the latch to wait for all to finish.
	 */
	private CountDownLatch signalSerialized(
			final SignalRunnable<T> signalRunnable) {
		final CountDownLatch latch = new CountDownLatch(1);
		try {
			serializedRunnables.put(new Runnable() {
				@Override
				public void run() {
					for (final T listener : listeners) {
						try {
							signalRunnable.run(listener);
						} catch (Exception e) {
							// TODO(keith): Should log
						}
					}
					
					latch.countDown();
				}
			});
		} catch (InterruptedException e) {
			// Don't care
		}

		return latch;
	}

	/**
	 * Handle all serialized signals.
	 */
	private void handleSerializedSignals() {
		try {
			while (!Thread.interrupted()) {
				processNextSignal();
			}
		} catch (InterruptedException e) {
			// Don't care
		}
	}

	private void processNextSignal() throws InterruptedException {
		try {
			Runnable signal = serializedRunnables.take();

			signal.run();
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			// TODO(keith): Show be logged.
		}
	}
}
