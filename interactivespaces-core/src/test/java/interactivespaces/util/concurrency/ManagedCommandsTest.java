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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ros.concurrent.DefaultScheduledExecutorService;

/**
 * Tests for {@link ManagedCommands}
 * 
 * @author Keith M. Hughes
 */
public class ManagedCommandsTest {

	private ScheduledExecutorService executorService;

	private ManagedCommands commands;

	private Log log;

	@Before
	public void setup() {
		executorService = new DefaultScheduledExecutorService();

		log = Mockito.mock(Log.class);

		commands = new ManagedCommands(executorService, log);
	}

	@After
	public void cleanup() {
		commands.shutdownAll();
		executorService.shutdown();
	}

	/**
	 * Test a submit that runs for only a short time.
	 */
	@Test
	public void testShortSubmit() throws Exception {
		final AtomicBoolean started = new AtomicBoolean();
		final CountDownLatch startLatch = new CountDownLatch(1);
		final CountDownLatch endLatch = new CountDownLatch(1);

		ManagedCommand command = commands.submit(new Runnable() {
			@Override
			public void run() {
				try {
					started.set(startLatch.await(500, TimeUnit.MILLISECONDS));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				endLatch.countDown();
			}
		});

		assertTrue(commands.contains(command));
		startLatch.countDown();
		assertTrue(endLatch.await(4, TimeUnit.SECONDS));
		assertTrue(started.get());
		Thread.sleep(2000);
		assertFalse(commands.contains(command));
		assertTrue(command.isDone());
		assertFalse(command.isCancelled());
	}

	/**
	 * Test a submit that loops forever.
	 */
	@Test
	public void testLoopingSubmit() throws Exception {
		final AtomicBoolean started = new AtomicBoolean();
		final CountDownLatch startLatch = new CountDownLatch(1);

		ManagedCommand command = commands.submit(new Runnable() {
			@Override
			public void run() {
				try {
					started.set(startLatch.await(500, TimeUnit.MILLISECONDS));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (!Thread.interrupted())
					;
			}
		});

		assertTrue(commands.contains(command));
		startLatch.countDown();

		Thread.sleep(1000);
		assertTrue(commands.contains(command));
		assertFalse(command.isDone());
		assertFalse(command.isCancelled());

		// Will be shut down when everything shut down on test cleanup
	}

	/**
	 * Test a submit that loops forever but is cancelled.
	 */
	@Test
	public void testLoopingCancelSubmit() throws Exception {
		final AtomicBoolean started = new AtomicBoolean();
		final CountDownLatch startLatch = new CountDownLatch(1);

		ManagedCommand command = commands.submit(new Runnable() {
			@Override
			public void run() {
				try {
					started.set(startLatch.await(500, TimeUnit.MILLISECONDS));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (!Thread.interrupted())
					;
			}
		});

		assertTrue(commands.contains(command));
		startLatch.countDown();
		command.cancel();
		Thread.sleep(2000);
		assertFalse(commands.contains(command));
		assertTrue(command.isDone());
		assertTrue(command.isCancelled());
	}

	/**
	 * Test a submit that loops forever but is shut down.
	 */
	@Test
	public void testLoopingShutdownSubmit() throws Exception {
		final AtomicBoolean started = new AtomicBoolean();
		final CountDownLatch startLatch = new CountDownLatch(1);

		ManagedCommand command = commands.submit(new Runnable() {
			@Override
			public void run() {
				try {
					started.set(startLatch.await(500, TimeUnit.MILLISECONDS));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (!Thread.interrupted())
					;
			}
		});

		assertTrue(commands.contains(command));
		startLatch.countDown();

		commands.shutdownAll();

		Thread.sleep(1000);
		assertFalse(commands.contains(command));
		assertTrue(command.isDone());
		assertTrue(command.isCancelled());
	}

	/**
	 * Test a fixed rate that loops forever.
	 */
	@Test
	public void testNormalFixedRate() throws Exception {
		final AtomicInteger count = new AtomicInteger(0);
		final CountDownLatch startLatch = new CountDownLatch(1);

		ManagedCommand command = commands.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				startLatch.countDown();
				count.incrementAndGet();
			}
		}, 0, 500, TimeUnit.MILLISECONDS);

		assertTrue(commands.contains(command));
		assertTrue(startLatch.await(1, TimeUnit.SECONDS));
		Thread.sleep(2000);
		assertTrue(commands.contains(command));
		assertTrue(count.get() > 1);
		assertFalse(command.isDone());
		assertFalse(command.isCancelled());

		// This will be shut off when the commands are shut down.
	}

	/**
	 * Test a fixed rate that is cancelled.
	 */
	@Test
	public void testCancelFixedRate() throws Exception {
		final CountDownLatch startLatch = new CountDownLatch(1);

		ManagedCommand command = commands.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				startLatch.countDown();
			}
		}, 0, 500, TimeUnit.MILLISECONDS, true);

		assertTrue(commands.contains(command));
		assertTrue(startLatch.await(1, TimeUnit.SECONDS));

		command.cancel();

		Thread.sleep(1000);
		assertFalse(commands.contains(command));
		assertTrue(command.isDone());
		assertTrue(command.isCancelled());

		// This will be shut off when the commands are shut down.
	}

	/**
	 * Test a fixed rate where the commands are shut down.
	 */
	@Test
	public void testShutdownFixedRate() throws Exception {
		final CountDownLatch startLatch = new CountDownLatch(1);

		ManagedCommand command = commands.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				startLatch.countDown();
			}
		}, 0, 500, TimeUnit.MILLISECONDS, true);

		assertTrue(commands.contains(command));
		assertTrue(startLatch.await(1, TimeUnit.SECONDS));

		commands.shutdownAll();

		Thread.sleep(1000);
		assertFalse(commands.contains(command));
		assertTrue(command.isDone());
		assertTrue(command.isCancelled());
	}

	/**
	 * Test a fixed rate that loops forever, but throws an exception.
	 */
	@Test
	public void testExceptionTerminateFixedRate() throws Exception {
		final AtomicInteger count = new AtomicInteger(0);
		final CountDownLatch startLatch = new CountDownLatch(1);

		ManagedCommand command = commands.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				startLatch.countDown();
				count.incrementAndGet();

				throw new RuntimeException();
			}
		}, 0, 500, TimeUnit.MILLISECONDS, true);

		assertTrue(commands.contains(command));
		assertTrue(startLatch.await(1, TimeUnit.SECONDS));
		Thread.sleep(2000);
		assertFalse(commands.contains(command));
		assertEquals(1, count.get());
		assertTrue(command.isDone());
		assertFalse(command.isCancelled());

		// This will be shut off when the commands are shut down.
	}

	/**
	 * Test a fixed rate that loops forever but throws an exception allowing to
	 * repeat.
	 */
	@Test
	public void testExceptionNonTerminateFixedRate() throws Exception {
		final AtomicInteger count = new AtomicInteger(0);
		final CountDownLatch startLatch = new CountDownLatch(1);

		ManagedCommand command = commands.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				startLatch.countDown();
				count.incrementAndGet();

				throw new RuntimeException();
			}
		}, 0, 500, TimeUnit.MILLISECONDS, false);

		assertTrue(commands.contains(command));
		assertTrue(startLatch.await(1, TimeUnit.SECONDS));
		Thread.sleep(1000);
		assertTrue(commands.contains(command));
		assertTrue(count.get() > 1);
		assertFalse(command.isDone());
		assertFalse(command.isCancelled());

		command.cancel();
		Thread.sleep(1000);
		assertFalse(commands.contains(command));
		assertTrue(command.isDone());
		assertTrue(command.isCancelled());
	}
}
