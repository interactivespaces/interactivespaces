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

package interactivespaces.activity.component.web;

import interactivespaces.activity.component.ActivityComponentContext;
import interactivespaces.activity.impl.SupportedActivity;
import interactivespaces.service.web.server.WebServerWebSocketHandler;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Tests for the {@link BasicWebServerActivityComponent}.
 *
 * @author Keith M. Hughes
 */
public class BasicWebServerActivityComponentTest {

  private BasicWebServerActivityComponent activityComponent;
  private WebServerWebSocketHandler delegate;
  private ActivityComponentContext activityComponentContext;
  private BasicWebServerActivityComponent.MyWebServerWebSocketHandler handler;
  private InOrder activityComponentContextInOrder;
  private SupportedActivity activity;
  private Log log;

  @Before
  public void setup() {
    delegate = Mockito.mock(WebServerWebSocketHandler.class);
    activityComponentContext = Mockito.mock(ActivityComponentContext.class);

    activityComponent = Mockito.mock(BasicWebServerActivityComponent.class);
    Mockito.when(activityComponent.getComponentContext()).thenReturn(activityComponentContext);

    activity = Mockito.mock(SupportedActivity.class);
    Mockito.when(activityComponentContext.getActivity()).thenReturn(activity);

    log = Mockito.mock(Log.class);
    Mockito.when(activity.getLog()).thenReturn(log);

    handler = new BasicWebServerActivityComponent.MyWebServerWebSocketHandler(delegate, activityComponent);

    activityComponentContextInOrder = Mockito.inOrder(activityComponentContext);
  }

  /**
   * Test the onConnect of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnConnectSuccess() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    handler.onConnect();

    Mockito.verify(delegate).onConnect();

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * Test the onConnect failure of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnConnectFailure() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(delegate).onConnect();

    handler.onConnect();

    Mockito.verify(delegate).onConnect();

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();

    Mockito.verify(activityComponent, Mockito.times(1)).handleError(Mockito.anyString(), Mockito.eq(e));
  }

  /**
   * Test the onConnect of the web socket handler when context won't allow it to
   * run.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnConnectNoRun() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(false);

    handler.onConnect();

    Mockito.verify(delegate, Mockito.never()).onConnect();

    Mockito.verify(activityComponentContext, Mockito.never()).enterHandler();
    Mockito.verify(activityComponentContext, Mockito.never()).exitHandler();
  }

  /**
   * Test the onClose of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnCloseSuccess() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    handler.onClose();

    Mockito.verify(delegate).onClose();

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * Test the onClose failure of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnCloseFailure() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(delegate).onClose();

    handler.onClose();

    Mockito.verify(delegate).onClose();

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();

    Mockito.verify(activityComponent, Mockito.times(1)).handleError(Mockito.anyString(), Mockito.eq(e));
  }

  /**
   * Test the onClose of the web socket handler when context won't allow it to
   * run.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnCloseNoRun() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(false);

    handler.onClose();

    Mockito.verify(delegate, Mockito.never()).onClose();

    Mockito.verify(activityComponentContext, Mockito.never()).enterHandler();
    Mockito.verify(activityComponentContext, Mockito.never()).exitHandler();
  }

  /**
   * Test the onReceive of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnReceiveSuccess() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Object data = new Object();

    handler.onReceive(data);

    Mockito.verify(delegate).onReceive(data);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * Test the onReceive failure of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnReceiveFailure() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Object data = new Object();

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(delegate).onReceive(data);

    handler.onReceive(data);

    Mockito.verify(delegate).onReceive(data);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();

    Mockito.verify(activityComponent, Mockito.times(1)).handleError(Mockito.anyString(), Mockito.eq(e));
  }

  /**
   * Test the onReceive of the web socket handler when context won't allow it to
   * run.
   */
  @Test
  public void testMyWebServerWebSocketHandlerOnReceiveNoRun() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(false);

    Object data = new Object();

    handler.onReceive(data);

    Mockito.verify(delegate, Mockito.never()).onReceive(data);

    Mockito.verify(activityComponentContext, Mockito.never()).enterHandler();
    Mockito.verify(activityComponentContext, Mockito.never()).exitHandler();
  }

  /**
   * Test the sendJson of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerSendJsonSuccess() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Object data = new Object();

    handler.sendJson(data);

    Mockito.verify(delegate).sendJson(data);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * Test the sendJson failure of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerSendJsonFailure() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    Object data = new Object();

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(delegate).sendJson(data);

    handler.sendJson(data);

    Mockito.verify(delegate).sendJson(data);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();

    Mockito.verify(activityComponent, Mockito.times(1)).handleError(Mockito.anyString(), Mockito.eq(e));
  }

  /**
   * Test the sendString of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerSendStringSuccess() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    String data = "Yowza";

    handler.sendString(data);

    Mockito.verify(delegate).sendString(data);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();
  }

  /**
   * Test the sendJson failure of the web socket handler.
   */
  @Test
  public void testMyWebServerWebSocketHandlerSendStringFailure() {
    Mockito.when(activityComponentContext.canHandlerRun()).thenReturn(true);

    String data = "Yowza";

    Exception e = new RuntimeException();
    Mockito.doThrow(e).when(delegate).sendString(data);

    handler.sendString(data);

    Mockito.verify(delegate).sendString(data);

    activityComponentContextInOrder.verify(activityComponentContext).enterHandler();
    activityComponentContextInOrder.verify(activityComponentContext).exitHandler();

    Mockito.verify(activityComponent, Mockito.times(1)).handleError(Mockito.anyString(), Mockito.eq(e));
  }
}
