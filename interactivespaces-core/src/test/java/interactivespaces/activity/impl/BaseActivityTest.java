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

package interactivespaces.activity.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

import interactivespaces.activity.Activity;
import interactivespaces.activity.ActivityListener;
import interactivespaces.activity.ActivityState;
import interactivespaces.activity.ActivityStatus;
import interactivespaces.activity.component.ActivityComponent;
import interactivespaces.activity.component.ActivityComponentContext;
import interactivespaces.activity.component.BaseActivityComponent;
import interactivespaces.activity.execution.ActivityExecutionContext;
import interactivespaces.configuration.Configuration;
import interactivespaces.configuration.SimpleConfiguration;
import interactivespaces.controller.SpaceController;
import interactivespaces.system.InteractiveSpacesEnvironment;
import interactivespaces.time.TimeProvider;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.ros.concurrent.DefaultScheduledExecutorService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Tests for the {@link BaseActivity}.
 *
 * @author Keith M. Hughes
 */
public class BaseActivityTest {

  private BaseActivity activity;

  private ScheduledExecutorService executorService;

  private Log log;

  private SpaceController controller;

  private ActivityComponent component;

  private SimpleConfiguration configuration;

  private ActivityExecutionContext executionContext;

  private InOrder activityInOrder;
  private InOrder componentInOrder;

  private List<ActivityComponent> componentsToAdd;

  private boolean activityStarted;

  @Before
  public void setup() {
    componentsToAdd = Lists.newArrayList();

    executorService = new DefaultScheduledExecutorService();

    InteractiveSpacesEnvironment spaceEnvironment =
        Mockito.mock(InteractiveSpacesEnvironment.class);
    TimeProvider timeProvider = Mockito.mock(TimeProvider.class);
    Mockito.when(spaceEnvironment.getTimeProvider()).thenReturn(timeProvider);
    Mockito.when(timeProvider.getCurrentTime()).thenReturn(1000L);
    Mockito.when(spaceEnvironment.getExecutorService()).thenReturn(executorService);

    log = Mockito.mock(Log.class);
    controller = Mockito.mock(SpaceController.class);
    configuration = new SimpleConfiguration(null);
    executionContext = Mockito.mock(ActivityExecutionContext.class);
    component = Mockito.spy(new MyBaseActivityComponent("component1"));
    componentsToAdd.add(component);
    componentInOrder = Mockito.inOrder(component);

    Mockito.when(component.getName()).thenReturn("component");
    Mockito.when(component.getDependencies()).thenReturn(new ArrayList<String>());

    activity = Mockito.spy(new MyBaseActivity());
    activity.setController(controller);
    activity.setSpaceEnvironment(spaceEnvironment);
    activity.setConfiguration(configuration);
    activity.setExecutionContext(executionContext);
    activity.setLog(log);

    activityInOrder = Mockito.inOrder(activity);

    // In general th activity will be started
    activityStarted = true;
  }

  @After
  public void cleanup() {
    executorService.shutdown();
  }

  /**
   * Test that a clean startup works.
   */
  @Test
  public void testCleanStartup() {
    activity.startup();

    activityInOrder.verify(activity).onActivitySetup();
    activityInOrder.verify(activity).onActivityStartup();
    activityInOrder.verify(activity).onActivityPostStartup();

    componentInOrder.verify(component).setComponentContext(activity.getActivityComponentContext());
    componentInOrder.verify(component).configureComponent(configuration);
    componentInOrder.verify(component).startupComponent();

    assertEquals(ActivityState.RUNNING, activity.getActivityStatus().getState());

    assertTrue(activity.getActivityComponentContext().areHandlersAllowed());
  }

  /**
   * Test that a clean activate works.
   */
  @Test
  public void testCleanActivate() {
    activity.startup();
    activity.activate();

    activityInOrder.verify(activity).onActivityActivate();
    assertEquals(ActivityState.ACTIVE, activity.getActivityStatus().getState());
  }

  /**
   * Test that a clean activate works.
   *
   */
  @Test
  public void testCleanDeactivate() {
    activity.startup();
    activity.activate();
    activity.deactivate();

    activityInOrder.verify(activity).onActivityDeactivate();
    assertEquals(ActivityState.RUNNING, activity.getActivityStatus().getState());
  }

  /**
   * Test that a clean shutdown works.
   */
  @Test
  public void testCleanShutdown() {
    activity.startup();
    activity.shutdown();

    activityInOrder.verify(activity).onActivityPreShutdown();
    activityInOrder.verify(activity).onActivityShutdown();
    activityInOrder.verify(activity).onActivityCleanup();

    componentInOrder.verify(component).shutdownComponent();

    assertEquals(ActivityState.READY, activity.getActivityStatus().getState());

    ActivityComponentContext activityComponentContext = activity.getActivityComponentContext();
    assertFalse(activityComponentContext.areHandlersAllowed());
    assertTrue(activityComponentContext.waitOnNoProcessingHandlings(500, 1000));
  }

  /**
   * Test that a clean shutdown works even if a handler hasn't returned.
   */
  @Test
  public void testShutdownWithRunningHandler() {
    activity.startup();

    activity.getActivityComponentContext().enterHandler();

    activity.shutdown();

    activityInOrder.verify(activity).onActivityPreShutdown();
    activityInOrder.verify(activity).onActivityShutdown();
    activityInOrder.verify(activity).onActivityCleanup();

    componentInOrder.verify(component).shutdownComponent();

    assertEquals(ActivityState.READY, activity.getActivityStatus().getState());

    ActivityComponentContext activityComponentContext = activity.getActivityComponentContext();
    assertFalse(activityComponentContext.areHandlersAllowed());
    assertTrue(activityComponentContext.areProcessingHandlers());

    Mockito.verify(log, Mockito.times(1)).warn(Mockito.anyString());
  }

  /**
   * Test that a broken setup works.
   */
  @Test
  public void testBrokenSetup() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).onActivitySetup();

    activity.startup();

    activityInOrder.verify(activity).onActivitySetup();
    Mockito.verify(activity, Mockito.never()).onActivityStartup();
    Mockito.verify(activity, Mockito.never()).onActivityPostStartup();

    assertEquals(ActivityState.STARTUP_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));

    assertFalse(activity.getActivityComponentContext().areHandlersAllowed());
  }

  /**
   * Test that a broken component configuring works.
   */
  @Test
  public void testBrokenComponentConfigure() {
    Exception e = new RuntimeException();
    Mockito
        .doThrow(e)
        .when(component)
        .configureComponent((Configuration) Mockito.anyObject()
        );

    activity.startup();

    activityInOrder.verify(activity).onActivitySetup();
    Mockito.verify(activity, Mockito.never()).onActivityStartup();
    Mockito.verify(activity, Mockito.never()).onActivityPostStartup();

    assertEquals(ActivityState.STARTUP_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));

    componentInOrder.verify(component).setComponentContext(activity.getActivityComponentContext());
    componentInOrder.verify(component).configureComponent(configuration);

    assertFalse(activity.getActivityComponentContext().areHandlersAllowed());
  }

  /**
   * Test that a broken component startup works.
   */
  @Test
  public void testBrokenComponentStartup() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(component).startupComponent();

    activity.startup();

    activityInOrder.verify(activity).onActivitySetup();
    Mockito.verify(activity, Mockito.never()).onActivityStartup();
    Mockito.verify(activity, Mockito.never()).onActivityPostStartup();

    assertEquals(ActivityState.STARTUP_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));

    componentInOrder.verify(component).setComponentContext(activity.getActivityComponentContext());
    componentInOrder.verify(component).configureComponent(configuration);
    componentInOrder.verify(component).startupComponent();

    assertFalse(activity.getActivityComponentContext().areHandlersAllowed());
  }

  /**
   * Test that a broken component startup works when there are two components
   * and the second one craps out. The first one should then shut down.
   */
  @Test
  public void testBrokenComponentStartupWithComponentShutdown() {
    ActivityComponent component2 = Mockito.spy(new MyBaseActivityComponent("component2"));
    componentInOrder = Mockito.inOrder(component, component2);
    componentsToAdd.add(component2);

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(component).startupComponent();

    activity.startup();

    activityInOrder.verify(activity).onActivitySetup();
    Mockito.verify(activity, Mockito.never()).onActivityStartup();
    Mockito.verify(activity, Mockito.never()).onActivityPostStartup();

    assertEquals(ActivityState.STARTUP_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));

    componentInOrder.verify(component).setComponentContext(activity.getActivityComponentContext());
    componentInOrder.verify(component2).setComponentContext(activity.getActivityComponentContext());

    componentInOrder.verify(component2).configureComponent(configuration);
    componentInOrder.verify(component).configureComponent(configuration);

    componentInOrder.verify(component2).startupComponent();
    componentInOrder.verify(component).startupComponent();

    componentInOrder.verify(component2).shutdownComponent();

    assertFalse(activity.getActivityComponentContext().areHandlersAllowed());
  }

  /**
   * Test that a broken onStartup works.
   */
  @Test
  public void testBrokenStartup() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).onActivityStartup();

    activity.startup();

    activityInOrder.verify(activity).onActivitySetup();
    activityInOrder.verify(activity).onActivityStartup();
    Mockito.verify(activity, Mockito.never()).onActivityPostStartup();

    assertEquals(ActivityState.STARTUP_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));

    componentInOrder.verify(component).setComponentContext(activity.getActivityComponentContext());
    componentInOrder.verify(component).configureComponent(configuration);
    componentInOrder.verify(component).startupComponent();

    assertFalse(activity.getActivityComponentContext().areHandlersAllowed());
  }

  /**
   * Test that a clean startup with broken Post Startup will actually end up
   * with a running activity.
   */
  @Test
  public void testBrokenPostStartup() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).onActivityPostStartup();

    activity.startup();

    activityInOrder.verify(activity).onActivitySetup();
    activityInOrder.verify(activity).onActivityStartup();
    activityInOrder.verify(activity).onActivityPostStartup();

    componentInOrder.verify(component).setComponentContext(activity.getActivityComponentContext());
    componentInOrder.verify(component).configureComponent(configuration);
    componentInOrder.verify(component).startupComponent();

    assertEquals(ActivityState.RUNNING, activity.getActivityStatus().getState());

    assertTrue(activity.getActivityComponentContext().areHandlersAllowed());

    // Make sure the failure is logged.
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));
  }

  /**
   * Test that a broken onActivate works.
   */
  @Test
  public void testBrokenActivate() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).onActivityActivate();

    activity.startup();
    activity.activate();

    activityInOrder.verify(activity).onActivityActivate();
    assertEquals(ActivityState.ACTIVATE_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));
  }

  /**
   * Test that a broken onDeactivate works.
   */
  @Test
  public void testBrokenDeactivate() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).onActivityDeactivate();

    activity.startup();
    activity.activate();
    activity.deactivate();

    activityInOrder.verify(activity).onActivityDeactivate();
    assertEquals(ActivityState.DEACTIVATE_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));
  }

  /**
   * Test that a broken onPreShutdown works.
   */
  @Test
  public void testBrokenPreShutdown() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).onActivityPreShutdown();

    activity.startup();
    activity.shutdown();

    activityInOrder.verify(activity).onActivityPreShutdown();
    Mockito.verify(activity, Mockito.never()).onActivityShutdown();

    // Cleanup must always be called.
    activityInOrder.verify(activity).onActivityCleanup();

    assertEquals(ActivityState.SHUTDOWN_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));

    // Everything is shut down
    componentInOrder.verify(component).shutdownComponent();

    assertFalse(activity.getActivityComponentContext().areHandlersAllowed());
  }

  /**
   * Test that a broken onShutdown works.
   */
  @Test
  public void testBrokenShutdown() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(activity).onActivityShutdown();

    activity.startup();
    activity.shutdown();

    activityInOrder.verify(activity).onActivityShutdown();

    // Cleanup must always be called.
    activityInOrder.verify(activity).onActivityCleanup();

    assertEquals(ActivityState.SHUTDOWN_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));

    // Everything is shut down
    componentInOrder.verify(component).shutdownComponent();

    assertFalse(activity.getActivityComponentContext().areHandlersAllowed());
  }

  /**
   * Test that a broken onShutdown works.
   */
  @Test
  public void testBrokenComponentShutdown() {
    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(component).shutdownComponent();

    activity.startup();
    activity.shutdown();

    activityInOrder.verify(activity).onActivityShutdown();

    // Cleanup must always be called.
    activityInOrder.verify(activity).onActivityCleanup();

    assertEquals(ActivityState.SHUTDOWN_FAILURE, activity.getActivityStatus().getState());
    Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.eq(e));

    // Everything is shut down
    componentInOrder.verify(component).shutdownComponent();

    assertFalse(activity.getActivityComponentContext().areHandlersAllowed());
  }

  /**
   * Test that status listeners work.
   */
  @Test
  public void testStatusListener() {
    activityStarted = false;
    ActivityListener listener = Mockito.mock(ActivityListener.class);

    ActivityStatus oldStatus = activity.getActivityStatus();
    ActivityStatus newStatus = Mockito.mock(ActivityStatus.class);

    activity.addActivityListener(listener);

    activity.setActivityStatus(newStatus);

    // Need the any() because we are spying on the BaseActivity so the
    // object for the listener isn't the same as the spy makes a wrapper
    // class
    Mockito.verify(listener, Mockito.times(1)).onActivityStatusChange(Mockito.any(Activity.class),
        Mockito.eq(oldStatus), Mockito.eq(newStatus));
  }

  private class MyBaseActivity extends BaseActivity {
    public void onActivitySetup() {
      for (ActivityComponent c : componentsToAdd) {
        addActivityComponent(c);
      }
    }
  }

  private class MyBaseActivityComponent extends BaseActivityComponent {
    private String name;
    private boolean running;

    MyBaseActivityComponent(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void configureComponent(Configuration configuration) {
      super.configureComponent(configuration);
    }

    @Override
    public void startupComponent() {
      running = true;
    }

    @Override
    public void shutdownComponent() {
      running = false;
    }

    @Override
    public boolean isComponentRunning() {
      return running;
    }
  }
}
