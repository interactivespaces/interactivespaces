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

import interactivespaces.activity.SupportedActivity;
import interactivespaces.configuration.Configuration;
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

  private SupportedActivity activity;

  private ActivityComponentFactory componentFactory;

  private ActivityComponentContext context;

  private ExecutorService executor;

  private Log log;

  private Configuration activityConfiguration;

  @Before
  public void setup() {
    executor = Executors.newFixedThreadPool(10);

    activity = Mockito.mock(SupportedActivity.class);

    activityConfiguration = Mockito.mock(Configuration.class);
    Mockito.when(activity.getConfiguration()).thenReturn(activityConfiguration);

    log = Mockito.mock(Log.class);
    Mockito.when(activity.getLog()).thenReturn(log);

    componentFactory = Mockito.mock(ActivityComponentFactory.class);

    context = new ActivityComponentContext(activity, componentFactory);
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

  /**
   * Add in a single component and have it start successfully.
   */
  @Test
  public void addSingleComponentSuccess() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("foo");

    context.addComponent(component1);

    context.initialStartupComponents();

    Mockito.verify(component1).configureComponent(activityConfiguration);
    Mockito.verify(component1).startupComponent();
  }

  /**
   * Add in a single component and have it fail on startup.
   */
  @Test
  public void addSingleComponentFailStartup() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("foo");

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(component1).startupComponent();

    context.addComponent(component1);

    try {
      context.initialStartupComponents();

      Assert.fail();
    } catch (Exception e1) {
      // Where we want to be
    }

    Mockito.verify(component1).configureComponent(activityConfiguration);
    Mockito.verify(component1).startupComponent();
  }

  /**
   * Add in a single component and have it fail on configuration.
   */
  @Test
  public void addSingleComponentFailConfigure() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("foo");

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(component1).configureComponent(activityConfiguration);

    context.addComponent(component1);

    try {
      context.initialStartupComponents();

      Assert.fail();
    } catch (Exception e1) {
      // Where we want to be
    }

    Mockito.verify(component1).configureComponent(activityConfiguration);
    Mockito.verify(component1, Mockito.never()).startupComponent();
  }

  /**
   * Add in multiple components and have them start successfully.
   */
  @Test
  public void addMultipleComponentsSuccess() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("foo");

    ActivityComponent component2 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component2.getName()).thenReturn("bar");

    context.addComponent(component1);
    context.addComponent(component2);

    context.initialStartupComponents();

    Mockito.verify(component1).configureComponent(activityConfiguration);
    Mockito.verify(component1).startupComponent();

    Mockito.verify(component2).configureComponent(activityConfiguration);
    Mockito.verify(component2).startupComponent();
  }

  /**
   * Add in multiple components and have the first fail on startup.
   */
  @Test
  public void addMultipleComponentFailStartup1() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("foo");

    ActivityComponent component2 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component2.getName()).thenReturn("banana");

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(component1).startupComponent();

    context.addComponent(component1);
    context.addComponent(component2);

    try {
      context.initialStartupComponents();

      Assert.fail();
    } catch (Exception e1) {
      // Where we want to be
    }

    Mockito.verify(component1).configureComponent(activityConfiguration);
    Mockito.verify(component1).startupComponent();

    Mockito.verify(component2).configureComponent(activityConfiguration);
    Mockito.verify(component2, Mockito.never()).startupComponent();
  }

  /**
   * Add in multiple components and have the second fail on startup.
   */
  @Test
  public void addMultipleComponentFailStartup2() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("foo");

    ActivityComponent component2 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component2.getName()).thenReturn("banana");

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(component2).startupComponent();

    context.addComponent(component1);
    context.addComponent(component2);

    try {
      context.initialStartupComponents();

      Assert.fail();
    } catch (Exception e1) {
      // Where we want to be
    }

    Mockito.verify(component1).configureComponent(activityConfiguration);
    Mockito.verify(component1).startupComponent();
    Mockito.verify(component1).shutdownComponent();

    Mockito.verify(component2).configureComponent(activityConfiguration);
    Mockito.verify(component2).startupComponent();
    Mockito.verify(component2, Mockito.never()).shutdownComponent();
  }

  /**
   * Add in multiple components and have the first one fail on configuration.
   */
  @Test
  public void addMultipleComponentFailConfigure1() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("blerg");

    ActivityComponent component2 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component2.getName()).thenReturn("garg");

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(component1).configureComponent(activityConfiguration);

    context.addComponent(component1);
    context.addComponent(component2);

    try {
      context.initialStartupComponents();

      Assert.fail();
    } catch (Exception e1) {
      // Where we want to be
    }

    Mockito.verify(component1).configureComponent(activityConfiguration);
    Mockito.verify(component1, Mockito.never()).startupComponent();
    Mockito.verify(component1, Mockito.never()).shutdownComponent();

    Mockito.verify(component2, Mockito.never()).configureComponent(activityConfiguration);
    Mockito.verify(component2, Mockito.never()).startupComponent();
    Mockito.verify(component2, Mockito.never()).shutdownComponent();
  }

  /**
   * Add in multiple components and have the second one fail on configuration.
   */
  @Test
  public void addMultipleComponentFailConfigure2() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("blerg");

    ActivityComponent component2 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component2.getName()).thenReturn("garg");

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(component2).configureComponent(activityConfiguration);

    context.addComponent(component1);
    context.addComponent(component2);

    try {
      context.initialStartupComponents();

      Assert.fail();
    } catch (Exception e1) {
      // Where we want to be
    }

    Mockito.verify(component1).configureComponent(activityConfiguration);
    Mockito.verify(component1, Mockito.never()).startupComponent();
    Mockito.verify(component1, Mockito.never()).shutdownComponent();

    Mockito.verify(component2).configureComponent(activityConfiguration);
    Mockito.verify(component2, Mockito.never()).startupComponent();
    Mockito.verify(component2, Mockito.never()).shutdownComponent();
  }

  /**
   * Add in multiple components and have all succeed on shutdown.
   */
  @Test
  public void shutdownAllComponentsSuccess() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("blerg");

    ActivityComponent component2 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component2.getName()).thenReturn("garg");

    context.addComponent(component1);
    context.addComponent(component2);

    context.initialStartupComponents();

    Assert.assertTrue(context.shutdownAndClear());

    Mockito.verify(component1).shutdownComponent();
    Mockito.verify(component2).shutdownComponent();
  }

  /**
   * Add in multiple components and have all fail on shutdown.
   */
  @Test
  public void shutdownAllComponentsFailure() throws Exception {
    ActivityComponent component1 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component1.getName()).thenReturn("blerg");

    ActivityComponent component2 = Mockito.mock(ActivityComponent.class);
    Mockito.when(component2.getName()).thenReturn("garg");

    Exception e1 = new RuntimeException();
    Mockito.doThrow(e1).when(component1).shutdownComponent();

    Exception e2 = new RuntimeException();
    Mockito.doThrow(e2).when(component2).shutdownComponent();

    context.addComponent(component1);
    context.addComponent(component2);

    context.initialStartupComponents();

    Assert.assertFalse(context.shutdownAndClear());

    Mockito.verify(component1).shutdownComponent();
    Mockito.verify(component2).shutdownComponent();
  }
}
