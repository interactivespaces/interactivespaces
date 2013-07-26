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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import interactivespaces.system.InteractiveSpacesEnvironment;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.ros.concurrent.DefaultScheduledExecutorService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A set of tests for the {@link AcceptingPriorityEventQueue}.
 *
 * @author Keith M. Hughes
 */
public class AcceptingPriorityEventQueueTest {

  private Log log;

  private InteractiveSpacesEnvironment spaceEnvironment;

  private ScheduledExecutorService executorService;

  private AcceptingPriorityEventQueue queue;

  @Before
  public void setup() {
    executorService = new DefaultScheduledExecutorService();

    log = Mockito.mock(Log.class);
    spaceEnvironment = Mockito.mock(InteractiveSpacesEnvironment.class);

    Mockito.when(spaceEnvironment.getExecutorService()).thenReturn(executorService);

    queue = new AcceptingPriorityEventQueue(spaceEnvironment, log);
  }

  @After
  public void cleanup() {
    queue.shutdown();
    executorService.shutdown();
  }

  /**
   * Test that the queue is accepting events and event queue was running when
   * added.
   */
  @Test
  public void testAcceptStartupBefore() throws Exception {
    queue.setAccepting(true);
    queue.startup();

    InOrderEvent event = new InOrderEvent();
    queue.addEvent(event);

    // Assume it gets answered in under a second.
    assertTrue(event.await(1, TimeUnit.SECONDS));
  }

  /**
   * Test that the queue is accepting events and event queue wasn't running when
   * added.
   */
  @Test
  public void testAcceptStartupLater() throws Exception {
    queue.setAccepting(true);

    InOrderEvent event = new InOrderEvent();
    queue.addEvent(event);

    queue.startup();

    // Assume it gets answered in under a second.
    assertTrue(event.await(1, TimeUnit.SECONDS));
  }

  /**
   * Test that the queue is accepting events but nothing gets called.
   */
  @Test
  public void testAcceptButNoProcess() throws Exception {
    queue.setAccepting(true);

    InOrderEvent event = new InOrderEvent();
    queue.addEvent(event);

    // Assume it gets answered in under a second.
    assertFalse(event.await(1, TimeUnit.SECONDS));
  }

  /**
   * Test that the queue is not accepting events and event queue wasn't running
   * when added.
   */
  @Test
  public void testNotAcceptStartupLater() throws Exception {
    queue.setAccepting(false);

    InOrderEvent event = new InOrderEvent();
    queue.addEvent(event);

    queue.startup();

    // Assume it gets answered in under a second.
    assertFalse(event.await(1, TimeUnit.SECONDS));
  }

  /**
   * Test that the queue is not accepting events and event queue wasn't running
   * when added.
   */
  @Test
  public void testNotAcceptStartupBefore() throws Exception {
    queue.setAccepting(false);
    queue.startup();

    InOrderEvent event = new InOrderEvent();
    queue.addEvent(event);

    // Assume it gets answered in under a second.
    assertFalse(event.await(1, TimeUnit.SECONDS));
  }

  /**
   * Make sure the higher priority event is handled first.
   */
  @Test
  public void testPriority() throws Exception {
    queue.setAccepting(true);

    InOrderEvent event1 = new InOrderEvent();
    InOrderEvent event2 = new InOrderEvent();
    queue.addEvent(event2, 10);
    queue.addEvent(event1, 1);

    queue.startup();

    // Assume it gets answered in under a second.
    assertTrue(event2.await(1, TimeUnit.SECONDS));
    assertTrue(event1.await(1, TimeUnit.SECONDS));

    assertTrue(event1.getOrder() < event2.getOrder());
  }

  public static class InOrderEvent implements Runnable {
    public static final AtomicInteger sequence = new AtomicInteger();

    private int order;

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void run() {
      order = sequence.incrementAndGet();
      latch.countDown();
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
      return latch.await(timeout, unit);
    }

    public int getOrder() {
      return order;
    }

  }

}
