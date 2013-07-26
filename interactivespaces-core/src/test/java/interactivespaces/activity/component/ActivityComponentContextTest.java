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

package interactivespaces.activity.component;

import interactivespaces.activity.Activity;
import interactivespaces.util.InteractiveSpacesUtilities;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for {@link ActivityComponentContext}
 *
 * @author Keith M. Hughes
 */
public class ActivityComponentContextTest {

  private Activity activity;

  private ActivityComponentCollection components;

  private ActivityComponentFactory componentFactory;

  private ActivityComponentContext context;

  private ExecutorService executor;

  private Log log;

  @Before
  public void setup() {
    executor = Executors.newFixedThreadPool(10);

    activity = Mockito.mock(Activity.class);

    log = Mockito.mock(Log.class);
    Mockito.when(activity.getLog()).thenReturn(log);

    components = Mockito.mock(ActivityComponentCollection.class);
    componentFactory = Mockito.mock(ActivityComponentFactory.class);

    context = new ActivityComponentContext(activity, components, componentFactory);
  }

  @After
  public void cleanup() {
    executor.shutdown();
  }

  /**
   * Test that handler enters and exits are noted properly.
   */
  @Test
  public void testHandlerEnterExit() {
    // Should be nothing yet as is a fresh context
    Assert.assertFalse(context.areProcessingHandlers());

    context.enterHandler();
    Assert.assertTrue(context.areProcessingHandlers());
    context.enterHandler();
    Assert.assertTrue(context.areProcessingHandlers());
    context.enterHandler();
    Assert.assertTrue(context.areProcessingHandlers());

    context.exitHandler();
    Assert.assertTrue(context.areProcessingHandlers());
    context.exitHandler();
    Assert.assertTrue(context.areProcessingHandlers());
    context.exitHandler();
    Assert.assertFalse(context.areProcessingHandlers());
  }

  /**
   * Test that there were never any processing handlers and Wait return
   * immediately.
   */
  @Test
  public void testWaitNoProcessingHandlersEver() {
    Assert.assertTrue(context.waitOnNoProcessingHandlings(500, 2000));
  }

  /**
   * Test that there is a processing handler that never exits and the wait has
   * to exit.
   */
  @Test
  public void testWaitOneProcessingHandlers() {
    context.enterHandler();
    Assert.assertFalse(context.waitOnNoProcessingHandlings(500, 2000));
  }

  /**
   * Test that there is a processing handler that never exits and the wait has
   * to exit.
   */
  @Test
  public void testWaitMultithreadedProcessingHandlers() throws Exception {
    final CountDownLatch latch = new CountDownLatch(2);
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        context.enterHandler();
        latch.countDown();
        InteractiveSpacesUtilities.delay(1000);
        context.exitHandler();
      }
    };

    executor.execute(runnable);
    executor.execute(runnable);

    // Make sure they have both entered before starting the wait.
    Assert.assertTrue(latch.await(500, TimeUnit.MILLISECONDS));

    Assert.assertTrue(context.waitOnNoProcessingHandlings(500, 4000));
  }

  /**
   * Tests the context failing to startup in time when waiting for the context
   * to signal startup complete.
   */
  @Test
  public void testFailedStartupWait() {
    Assert.assertFalse(context.awaitStartup());
  }

  /**
   * Make a couple of threads start running and see if they properly stop
   * running when the context signals startup successful.
   */
  @Test
  public void testStartupWaitWithTwoThreadsSuccess() throws Exception {
    final CountDownLatch startLatch = new CountDownLatch(2);
    final CountDownLatch stopLatch = new CountDownLatch(2);
    final AtomicInteger countAllowedHandlers = new AtomicInteger(0);
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        startLatch.countDown();

        if (context.canHandlerRun()) {
          countAllowedHandlers.incrementAndGet();
        }

        stopLatch.countDown();
      }
    };

    executor.execute(runnable);
    executor.execute(runnable);

    // Make sure they have both entered before starting the wait.
    Assert.assertTrue(startLatch.await(500, TimeUnit.MILLISECONDS));

    context.endStartupPhase(true);

    // Make sure they have both entered before starting the wait.
    Assert.assertTrue(stopLatch.await(500, TimeUnit.MILLISECONDS));

    // All handlers should have been allowed.
    Assert.assertEquals(2, countAllowedHandlers.get());
  }

  /**
   * Make a couple of threads start running and see if they properly stop
   * running when the context signals startup failure.
   */
  @Test
  public void testStartupWaitWithTwoThreadsFailure() throws Exception {
    final CountDownLatch startLatch = new CountDownLatch(2);
    final CountDownLatch stopLatch = new CountDownLatch(2);
    final AtomicInteger countAllowedHandlers = new AtomicInteger(0);

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        startLatch.countDown();

        if (context.canHandlerRun()) {
          countAllowedHandlers.incrementAndGet();
        }

        stopLatch.countDown();
      }
    };

    executor.execute(runnable);
    executor.execute(runnable);

    // Make sure they have both entered before starting the wait.
    Assert.assertTrue(startLatch.await(500, TimeUnit.MILLISECONDS));

    context.endStartupPhase(false);

    // Make sure they have both entered before starting the wait.
    Assert.assertTrue(stopLatch.await(500, TimeUnit.MILLISECONDS));

    // No handlers should have been allowed.
    Assert.assertEquals(0, countAllowedHandlers.get());
  }
}
